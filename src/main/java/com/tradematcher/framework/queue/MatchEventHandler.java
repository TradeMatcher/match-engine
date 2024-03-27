package com.tradematcher.framework.queue;

import com.tradematcher.entity.OrderBook;
import com.tradematcher.usecase.Match;
import com.tradematcher.usecase.MatchRequest;
import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 撮合请求处理器
 * @program: new-match-engine
 * @author: TradeMatcher
 * @create: 2023-04-24 10:08
 **/
@Slf4j
public class MatchEventHandler implements EventHandler<MatchRequest> {
	private final Match match;

	public MatchEventHandler(Map<Integer, OrderBook> orderBookMap, String directory) {
		match = new Match(orderBookMap, directory);
	}

	@Override
	public void onEvent(MatchRequest request, long sequence, boolean endOfBatch) throws Exception {
		try {
			match.doAction(request);
		} catch (Exception e) {
			log.error("MatchEventHandler error", e);
		}
	}
}
