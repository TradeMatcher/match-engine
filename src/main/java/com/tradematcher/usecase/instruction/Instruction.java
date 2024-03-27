package com.tradematcher.usecase.instruction;

import com.tradematcher.entity.OrderBook;
import com.tradematcher.usecase.MatchRequest;

/**
 * 撮合指令
 * @program: new-match-engine
 * @author: TradeMatcher
 * @create: 2023-04-18 22:11
 **/
public abstract sealed class Instruction
		permits PlaceFOKOrder, PlaceFAKOrder, PlaceGTCOrder, PlaceMTLOrder, PlaceLimitFOKOrder, CancelOrder,
		MarketOrderBook, QueryOrder, ListSymbol {
	protected final OrderBook orderBook;
	protected final MatchRequest matchRequest;

	protected Instruction(MatchRequest matchRequest, OrderBook orderBook) {
		this.matchRequest = matchRequest;
		this.orderBook = orderBook;
	}

	public abstract void action();
}
