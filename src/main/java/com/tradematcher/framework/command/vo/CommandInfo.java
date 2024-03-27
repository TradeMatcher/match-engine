package com.tradematcher.framework.command.vo;

import com.tradematcher.framework.command.*;
import com.tradematcher.util.Constants;

import java.util.Arrays;
import java.util.Optional;

/**
 * 撮合命令信息
 * @program: new-match-engine
 * @author: TradeMatcher
 * @create: 2023-04-19 19:29
 **/
public enum CommandInfo {
	// @formatter:off
	PLACE_ORDER("placeOrder",PlaceOrderCommand.class,Constants.Match.TRADE_KEY),
	CANCEL_ORDER("cancelOrder",CancelOrderCommand.class,Constants.Match.TRADE_KEY),
	MARKET("market",MarketCommand.class,Constants.Match.MARKET_KEY),
	SYMBOL_LIST("symbolList",ListSymbolCommand.class,Constants.Match.TRADE_MARKET_KEY),
	QUERY_ORDER("queryOrder",QueryOrderCommand.class,Constants.Match.TRADE_KEY),
	;
	// @formatter:on
	private final String code;
	private final Class<? extends RequestCommand> clazz;
	private final String flag;

	CommandInfo(String code, Class<? extends RequestCommand> clazz, String flag) {
		this.code = code;
		this.clazz = clazz;
		this.flag = flag;
	}

	public static CommandInfo getCommandInfo(String code) {
		Optional<CommandInfo> any = Arrays.stream(CommandInfo.values()).filter(info -> info.getCode().equals(code))
				.findAny();
		return any.orElse(null);
	}

	public Class<? extends BaseCommand> getClazz() {
		return clazz;
	}

	public String getCode() {
		return code;
	}

	public String getFlag() {
		return flag;
	}
}
