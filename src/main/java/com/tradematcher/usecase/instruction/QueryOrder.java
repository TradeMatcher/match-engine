package com.tradematcher.usecase.instruction;

import com.tradematcher.entity.Order;
import com.tradematcher.entity.OrderBook;
import com.tradematcher.usecase.MatchRequest;

/**
 * 撮合订单查询指令
 * @program: new-match-engine
 * @author: TradeMatcher
 * @create: 2023-04-26 15:45
 **/
public final class QueryOrder extends Instruction {
	QueryOrder(MatchRequest matchRequest, OrderBook orderBook) {
		super(matchRequest, orderBook);
	}

	@Override
	public void action() {
		String orderID = matchRequest.getOrderID();
		Order order = orderBook.getOrderByID(orderID);
		matchRequest.setAction(order.getAction());
		matchRequest.setUnitPrice(order.getUnitPrice());
		matchRequest.setSize(order.getSize());
		matchRequest.setFilledSize(order.getFilledSize());
		matchRequest.setOrderTimestamp(order.getTimestamp());
	}
}
