package com.tradematcher.usecase.instruction;

import com.tradematcher.entity.OrderBook;
import com.tradematcher.usecase.MatchRequest;

/**
 * 撮合产品集指令
 * @program: new-match-engine
 * @author: TradeMatcher
 * @create: 2023-04-26 16:06
 **/
public final class ListSymbol extends Instruction {
	ListSymbol(MatchRequest matchRequest, OrderBook orderBook) {
		super(matchRequest, orderBook);
	}

	@Override
	public void action() {
		throw new UnsupportedOperationException();
	}
}
