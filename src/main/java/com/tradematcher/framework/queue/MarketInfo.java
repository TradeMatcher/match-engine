package com.tradematcher.framework.queue;

import lombok.Data;

/**
 * 撮合盘口事件
 * @program: new-match-engine
 * @author: TradeMatcher
 * @create: 2023-04-24 19:36
 **/
@Data
public class MarketInfo {
	private String event = "market";
	private IEventsHandler.OrderBookInfo info;
}
