package com.tradematcher.usecase.instruction;

import com.tradematcher.entity.MatchResult;
import com.tradematcher.entity.OrderBook;
import com.tradematcher.usecase.MatchRequest;

/**
 * 撮合市场深度指令
 * @program: new-match-engine
 * @author: TradeMatcher
 * @create: 2023-04-26 15:42
 **/
public final class MarketOrderBook extends Instruction {
	MarketOrderBook(MatchRequest matchRequest, OrderBook orderBook) {
		super(matchRequest, orderBook);
	}

	@Override
	public void action() {
		MatchResult matchResult = orderBook.getMarketOrderBook(matchRequest.getMarketDepth());
		matchRequest.setMatchResult(matchResult);
	}
}
