package com.tradematcher.usecase;

import com.tradematcher.entity.OrderBook;
import com.tradematcher.entity.OrderBookImpl;
import com.tradematcher.entity.Symbol;
import com.tradematcher.usecase.instruction.Instruction;
import com.tradematcher.usecase.instruction.InstructionFactory;
import com.tradematcher.usecase.instruction.ListSymbol;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * 撮合控制器
 * @program: new-match-engine
 * @author: TradeMatcher
 * @create: 2023-04-18 23:41
 **/
public class Match {
	private final Map<Integer, OrderBook> orderBookMap;

	private final String directory;

	public Match(Map<Integer, OrderBook> orderBookMap, String directory) {
		this.orderBookMap = new HashMap<>(orderBookMap);
		this.directory = directory;
	}

	public void doAction(MatchRequest matchRequest) {

		doSnapshot(matchRequest.getSnapshot());

		int symbolID = matchRequest.getSymbolID();
		OrderBook orderBook = orderBookMap.computeIfAbsent(symbolID, key -> new OrderBookImpl(
				new Symbol(symbolID, matchRequest.getVolumeDigits(), matchRequest.getSymbolDecimal())));

		Instruction instruction = InstructionFactory.getInstruction(matchRequest, orderBook);
		if (instruction instanceof ListSymbol) {
			matchRequest.setSymbolIDs(orderBookMap.keySet().stream().mapToInt(Integer::intValue).toArray());
		} else {
			instruction.action();
		}
	}

	public Map<Integer, OrderBook> getCopyOrderBookMap() {
		return new HashMap<>(orderBookMap);
	}


	private void doSnapshot(long snapshotTime) {
		if (snapshotTime != 0) {
			try (FileOutputStream fileOutputStream = new FileOutputStream(directory + snapshotTime + ".orderbook");
					GZIPOutputStream gzipOutputStream = new GZIPOutputStream(fileOutputStream);
					ObjectOutputStream objectOutputStream = new ObjectOutputStream(gzipOutputStream)) {
				objectOutputStream.writeObject(orderBookMap);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
