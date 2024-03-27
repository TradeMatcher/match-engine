package com.tradematcher.framework.ws;

import com.tradematcher.framework.command.CommandResult;
import com.tradematcher.framework.queue.IEventsHandler;
import com.tradematcher.framework.queue.MarketInfo;
import com.tradematcher.util.Constants;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.WebSocket;

import java.util.*;

/**
 * 撮合结果处理器
 * @program: new-match-engine
 * @author: TradeMatcher
 * @create: 2023-04-24 19:37
 **/
@Slf4j
public class MEEventsHandler implements IEventsHandler {
	private final Gson gson = new Gson();

	@Override
	public void commandResult(CommandResult commandResult) {

		Optional<WebSocket> any = MatchWebSocket.TRADE_CHANNEL_MAP.values().stream().filter(WebSocket::isOpen)
				.findAny();
		if (any.isPresent()) {
			WebSocket webSocket = any.get();
			webSocket.send(gson.toJson(commandResult));
		}
	}

	@Override
	public void marketEvent(OrderBookInfo orderBookInfo) {
		MarketInfo market = new MarketInfo();
		market.setInfo(orderBookInfo);
		String encode = gson.toJson(market);

		Set<Map.Entry<String, WebSocket>> marketEntries = MatchWebSocket.MARKET_CHANNEL_MAP.entrySet();
		if (!marketEntries.isEmpty()) {
			for (Map.Entry<String, WebSocket> next : marketEntries) {
				String accessKey = next.getKey();
				WebSocket ws = next.getValue();
				if (ws.isClosed()) {
					MatchWebSocket.MARKET_CHANNEL_MAP.remove(accessKey);
				} else {
					log.info("market ws orderBook event({}) {}", accessKey, encode);
					ws.send(encode);
				}
			}
		}
	}

	@Override
	public void takerEvent(IEventsHandler.Taker taker) {
		long takerParse = Objects.hash(
				taker.getCompanyId() + taker.getUid() + taker.getOrderId() + taker.getTotalPrice());
		taker.setEventId(Constants.Match.EVENT_ID_T_PREFIX + takerParse);

		String takerEncode = gson.toJson(taker);
		Optional<Map.Entry<String, WebSocket>> takerAny = MatchWebSocket.TRADE_CHANNEL_MAP.entrySet().stream()
				.filter(e -> !e.getValue().isClosed()).findAny();
		if (takerAny.isPresent()) {
			WebSocket serverWebSocket = takerAny.get().getValue();

			log.info("trade ws taker event({}) {}", takerAny.get().getKey(), takerEncode);
			serverWebSocket.send(takerEncode);
		}

		//删除已关闭trade ws连接
		List<String> closedTradeWS = MatchWebSocket.TRADE_CHANNEL_MAP.entrySet().stream()
				.filter(e -> e.getValue().isClosed()).map(Map.Entry::getKey).toList();
		closedTradeWS.forEach(MatchWebSocket.TRADE_CHANNEL_MAP::remove);
	}

	@Override
	public void makerEvent(IEventsHandler.Maker maker) {
		long makerParse = Objects.hash(
				maker.getCompanyId() + maker.getUid() + maker.getOrderId() + maker.getTakerOrderId());
		maker.setEventId(Constants.Match.EVENT_ID_M_PREFIX + makerParse);
		Optional<Map.Entry<String, WebSocket>> makerAny = MatchWebSocket.TRADE_CHANNEL_MAP.entrySet().stream()
				.filter(e -> !e.getValue().isClosed()).findAny();
		if (makerAny.isPresent()) {
			WebSocket webSocket = makerAny.get().getValue();
			String encode = gson.toJson(maker);
			log.info("trade ws maker event({}) {}", makerAny.get().getKey(), encode);
			webSocket.send(encode);
		}

		Set<Map.Entry<String, WebSocket>> marketEntries = MatchWebSocket.MARKET_CHANNEL_MAP.entrySet();
		for (Map.Entry<String, WebSocket> next : marketEntries) {
			String accessKey = next.getKey();
			WebSocket ws = next.getValue();
			if (ws.isClosed()) {
				MatchWebSocket.MARKET_CHANNEL_MAP.remove(accessKey);
			} else {
				String encode = gson.toJson(maker);
				log.info("market ws maker event({}) {}", accessKey, encode);
				ws.send(encode);
			}
		}
	}

	@Override
	public void rejectEvent(IEventsHandler.Reject reject) {
		long parse = Objects.hash(reject.getCompanyId() + reject.getUid() + reject.getOrderId());
		reject.setEventId(Constants.Match.EVENT_ID_R_PREFIX + parse);
		Optional<Map.Entry<String, WebSocket>> rejectAny = MatchWebSocket.TRADE_CHANNEL_MAP.entrySet().stream()
				.filter(e -> !e.getValue().isClosed()).findAny();
		if (rejectAny.isPresent()) {
			WebSocket serverWebSocket = rejectAny.get().getValue();

			String encode = gson.toJson(reject);
			log.info("trade ws reject event({}) {}", rejectAny.get().getKey(), encode);
			serverWebSocket.send(encode);
		}
	}
}
