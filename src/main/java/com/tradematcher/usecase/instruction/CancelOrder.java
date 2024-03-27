package com.tradematcher.usecase.instruction;

import com.tradematcher.entity.Order;
import com.tradematcher.entity.OrderBook;
import com.tradematcher.usecase.MatchRequest;
import com.tradematcher.util.Constants;

/**
 * 撤单指令
 * @program: new-match-engine
 * @author: TradeMatcher
 * @create: 2023-04-18 23:51
 **/
public final class CancelOrder extends Instruction {
	CancelOrder(MatchRequest matchRequest, OrderBook orderBook) {
		super(matchRequest, orderBook);
	}

	@Override
	public void action() {
		Order order = orderBook.getOrderByID(matchRequest.getOrderID());
		if (order == null) {
			matchRequest.setCode(Constants.Code.MATCHING_UNKNOWN_ORDER_ID);
		} else {
			matchRequest.setCode(Constants.Code.MATCHING_REQUEST_SUCCESS);
			orderBook.cancelOrder(order);
		}
	}
}
