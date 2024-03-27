package com.tradematcher.framework.queue;

import com.tradematcher.entity.OrderBook;
import com.tradematcher.framework.command.*;
import com.tradematcher.framework.ws.MEEventsHandler;
import com.tradematcher.usecase.MatchRequest;
import com.tradematcher.util.Constants;
import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;
import journal.io.api.Journal;
import journal.io.api.JournalBuilder;
import journal.io.api.Location;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

/**
 * 撮合请求队列
 * @program: new-match-engine
 * @author: TradeMatcher
 * @create: 2023-04-24 10:11
 **/
@Slf4j
public class Dispatcher {
	private final Disruptor<MatchRequest> disruptor;
	private final EventTranslatorOneArg<MatchRequest, PlaceOrderCommand> placeOrderTranslator = (event, seq, msg) -> {
		event.setOrderID(msg.getCompanyId() + "_" + msg.getOrderId() + "_" + msg.getUid());
		event.setSymbolID(msg.getSymbolId());
		if (msg.getOrderType() == Constants.OrderType.MARKET_FOK) {
			event.setInstructionType(Constants.Instruction.PLACE_FOK_ORDER);
			event.setTotalPrice(new BigInteger(msg.getPrice()));
			event.setReservePrice(msg.getReservePrice());
		} else if (msg.getOrderType() == Constants.OrderType.MARKET_FAK) {
			event.setInstructionType(Constants.Instruction.PLACE_FAK_ORDER);
			event.setTotalPrice(new BigInteger(msg.getPrice()));
			event.setReservePrice(msg.getReservePrice());
			event.setMinSize(msg.getMinSize());
		} else if (msg.getOrderType() == Constants.OrderType.MARKET_MTL) {
			event.setInstructionType(Constants.Instruction.PLACE_MTL_ORDER);
			event.setTotalPrice(new BigInteger(msg.getPrice()));
			event.setReservePrice(msg.getReservePrice());
		} else if (msg.getOrderType() == Constants.OrderType.LIMIT_GTC) {
			event.setInstructionType(Constants.Instruction.PLACE_GTC_ORDER);
			event.setUnitPrice(new BigInteger(msg.getPrice()));
			event.setSize(new BigInteger(msg.getSize()));
		} else if (msg.getOrderType() == Constants.OrderType.LIMIT_FOK) {
			event.setInstructionType(Constants.Instruction.PLACE_LIMIT_FOK_ORDER);
			event.setUnitPrice(new BigInteger(msg.getPrice()));
		}

		event.setOrderType(msg.getOrderType());
		event.setSymbolDecimal(msg.getSymbolDecimal());
		event.setVolumeDigits(msg.getVolumeDigits());
		event.setTradeType(msg.getTradeType());

		event.setAction(msg.getAction());
		event.setCode(Constants.Code.MATCHING_NEW);
	};

	private final EventTranslatorOneArg<MatchRequest, CancelOrderCommand> cancelOrderTranslator = (event, seq, msg) -> {
		event.setOrderID(msg.getCompanyId() + "_" + msg.getOrderId() + "_" + msg.getUid());
		event.setSymbolID(msg.getSymbolId());
		event.setInstructionType(Constants.Instruction.CANCEL_ORDER);
		event.setCode(Constants.Code.MATCHING_NEW);
	};

	private final EventTranslatorOneArg<MatchRequest, MarketCommand> marketTranslator = (event, seq, msg) -> {
		event.setSymbolID(msg.getSymbolId());
		event.setMarketDepth(msg.getDepth());
		event.setInstructionType(Constants.Instruction.MARKET_ORDER_BOOK);
		event.setCode(Constants.Code.MATCHING_NEW);
	};

	private final EventTranslatorOneArg<MatchRequest, QueryOrderCommand> queryOrderTranslator = (event, seq, msg) -> {
		event.setOrderID(msg.getCompanyId() + "_" + msg.getOrderId() + "_" + msg.getUid());
		event.setInstructionType(Constants.Instruction.QUERY_ORDER);
		event.setCode(Constants.Code.MATCHING_NEW);
	};

	private final EventTranslatorOneArg<MatchRequest, ListSymbolCommand> symbolListTranslator = (event, seq, msg) -> {
		event.setInstructionType(Constants.Instruction.LIST_SYMBOL);
		event.setCode(Constants.Code.MATCHING_NEW);
	};

	private final EventTranslatorOneArg<MatchRequest, MatchRequest> restoreTranslator = (event, seq, msg) -> {
		event.setOrderID(msg.getOrderID());
		event.setSymbolID(msg.getSymbolID());
		event.setInstructionType(msg.getInstructionType());
		event.setUnitPrice(msg.getUnitPrice());
		event.setTotalPrice(msg.getTotalPrice());
		event.setReservePrice(msg.getReservePrice());
		event.setMinSize(msg.getMinSize());
		event.setSize(msg.getSize());
		event.setAction(msg.getAction());

		event.setCode(Constants.Code.MATCHING_RESTORE);
	};

	public Dispatcher(String directory, int snapshotCount) {
		// ringBufferSize大小影响请求延迟，越大延迟越大。
		this.disruptor = new Disruptor<>(MatchRequest::new, 2 * 1024, DaemonThreadFactory.INSTANCE);
		Map<Integer, OrderBook> orderBookMap = new HashMap<>(0);

		// 撮合订单簿恢复
		String journalDirectory = null;
		File snapshotFile = getRecentSnapshotFileByDesc(directory);
		if (snapshotFile != null) {
			String snapshotFileName = snapshotFile.getName();
			log.info("Found snapshot file: {}", snapshotFileName);
			try (FileInputStream fileOutputStream = new FileInputStream(snapshotFile);
					GZIPInputStream gzipInputStream = new GZIPInputStream(fileOutputStream);
					ObjectInputStream objectOutputStream = new ObjectInputStream(gzipInputStream)) {
				orderBookMap = (Map<Integer, OrderBook>) objectOutputStream.readObject();
			} catch (Exception e) {
				log.error("Failed to read snapshot file: {}", snapshotFileName, e);
				return;
			}

			journalDirectory = snapshotFileName.replace(".orderbook", "");
		}

		disruptor.handleEventsWith(new JournalEventHandler(directory, snapshotCount))
				.then(new MatchEventHandler(orderBookMap, directory))
				.then(new NotificationEventHandler(new MEEventsHandler()));

		disruptor.start();

		//根据撮合订单簿时间，增量执行撮合命令
		if (journalDirectory != null) {
			try {
				restore(directory + journalDirectory);
			} catch (IOException e) {
				log.error("Failed to restore journal: {}", journalDirectory, e);
			}
		}
	}

	public void dispatch(PlaceOrderCommand command) {
		RingBuffer<MatchRequest> ringBuffer = disruptor.getRingBuffer();
		ringBuffer.publishEvent(placeOrderTranslator, command);
	}

	public void dispatch(CancelOrderCommand command) {
		RingBuffer<MatchRequest> ringBuffer = disruptor.getRingBuffer();
		ringBuffer.publishEvent(cancelOrderTranslator, command);
	}

	public void dispatch(ListSymbolCommand command) {
		RingBuffer<MatchRequest> ringBuffer = disruptor.getRingBuffer();
		ringBuffer.publishEvent(symbolListTranslator, command);
	}

	public void dispatch(MarketCommand command) {
		RingBuffer<MatchRequest> ringBuffer = disruptor.getRingBuffer();
		ringBuffer.publishEvent(marketTranslator, command);
	}

	public void dispatch(QueryOrderCommand command) {
		RingBuffer<MatchRequest> ringBuffer = disruptor.getRingBuffer();
		ringBuffer.publishEvent(queryOrderTranslator, command);
	}

	private void restore(String directory) throws IOException {

		RingBuffer<MatchRequest> ringBuffer = disruptor.getRingBuffer();
		Journal journal = JournalBuilder.of(Path.of(directory).toFile()).setFileSuffix(".journal").open();
		log.info("Restore from journal: {}", journal);
		for (Location location : journal.redo()) {
			byte[] journalByteArray = journal.read(location, Journal.ReadType.SYNC);
			String journalString = new String(journalByteArray, StandardCharsets.UTF_8);
			log.info("journal string : {}", journalString);
			String[] journalInfo = journalString.split(",");
			String instruction = journalInfo[2];
			MatchRequest matchRequest = new MatchRequest();
			matchRequest.setOrderID(journalInfo[0]);
			matchRequest.setSymbolID(Integer.parseInt(journalInfo[1]));
			matchRequest.setAction(Byte.parseByte(journalInfo[3]));
			matchRequest.setInstructionType(instruction);
			if (!journalInfo[4].equals("null"))
				matchRequest.setUnitPrice(new BigInteger(journalInfo[4]));
			if (!journalInfo[5].equals("null"))
				matchRequest.setReservePrice(Long.parseLong(journalInfo[5]));
			if (!journalInfo[6].equals("null"))
				matchRequest.setSize(new BigInteger(journalInfo[6]));
			if (!journalInfo[7].equals("null"))
				matchRequest.setMinSize(Long.parseLong(journalInfo[7]));
			if (!journalInfo[8].equals("null"))
				matchRequest.setTotalPrice(new BigInteger(journalInfo[8]));

			if (!instruction.equals(Constants.Instruction.CANCEL_ORDER)) {
				matchRequest.setVolumeDigits(Integer.parseInt(journalInfo[9]));
				matchRequest.setTradeType(Integer.parseInt(journalInfo[10]));
				matchRequest.setSymbolDecimal(Integer.parseInt(journalInfo[11]));
				matchRequest.setOrderType(Byte.parseByte(journalInfo[12]));
			}

			ringBuffer.publishEvent(restoreTranslator, matchRequest);
		}
	}

	/**
	 * 获取最新撮合订单簿快照文件
	 * @param directory 快照文件目录
	 * @return 最新撮合订单簿快照文件
	 */
	private File getRecentSnapshotFileByDesc(String directory) {
		FileFilter fileFilter = file -> file.getName().endsWith(".orderbook");
		File dir = new File(directory);
		File[] files = Optional.ofNullable(dir.listFiles(fileFilter)).orElse(new File[0]);
		Arrays.sort(files, (o1, o2) -> {
			boolean equals = o1.lastModified() == o2.lastModified();
			if (equals)
				return 0;
			boolean great = o1.lastModified() > o2.lastModified();
			if (great)
				return -1;
			else
				return 1;
		});

		return files.length == 0 ? null : files[0];
	}
}

