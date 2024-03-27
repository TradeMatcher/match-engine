package com.tradematcher.util;

/**
 * Match Constants
 * @program: new-match-engine
 * @author: TradeMatcher
 * @create: 2023-04-18 23:41
 **/
public final class Constants {
	public static final int MAX_MARKET_DEPTH = 200;

	private Constants() {
	}

	public static class Action {
		public static final byte ASK = 0;
		public static final byte BID = 1;

		private Action() {
		}

		public static byte getCounterpartAction(byte action) {
			return action == ASK ? BID : ASK;
		}
	}

	public static class OrderType {
		public static final byte MARKET_FOK = 5;
		public static final byte MARKET_FAK = 6;
		public static final byte MARKET_MTL = 7;
		public static final byte LIMIT_GTC = 8;
		public static final byte LIMIT_HISTORY = 9;
		public static final byte LIMIT_FOK = 10;

		private OrderType() {
		}
	}

	public static class Instruction {
		public static final String PLACE_FOK_ORDER = "placeFOKOrder";
		public static final String PLACE_FAK_ORDER = "placeFAKOrder";
		public static final String PLACE_MTL_ORDER = "placeMTLOrder";
		public static final String PLACE_GTC_ORDER = "placeGTCOrder";
		public static final String PLACE_LIMIT_FOK_ORDER = "placeLimitFOKOrder";
		public static final String CANCEL_ORDER = "cancelOrder";
		public static final String MARKET_ORDER_BOOK = "market_orderBook";
		public static final String QUERY_ORDER = "queryOrder";
		public static final String LIST_SYMBOL = "listSymbol";

		private Instruction() {
		}
	}

	public static class Code {
		public static final short MATCHING_NEW = 0;
		public static final short VALID_FOR_MATCHING_ENGINE = 1;
		public static final short MATCHING_RESTORE = 2;
		public static final short MATCHING_DUPLICATE_ORDER_ID = -3003;
		public static final short MATCHING_COUNTERPARTY_NOT_FOUND = -3001;
		public static final short MATCHING_UNKNOWN_ORDER_ID = -3002;
		public static final short MATCHING_REQUEST_SUCCESS = 100;

		private Code() {

		}
	}

	public static class EventType {
		public static final byte MAKER = 0;
		public static final byte TAKER = 1;
		public static final byte REJECT = 2;

		private EventType() {
		}
	}

	public static class Match {
		public static final String COMMAND_CONST = "command";
		public static final String COMMON_COMMAND = "commonCommand";
		public static final String ACCESS_KEY = "accessKey";
		public static final String TRADE_KEY = "trade";
		public static final String MARKET_KEY = "market";
		public static final String TRADE_MARKET_KEY = "trade,market";
		public static final String EVENT_ID_M_PREFIX = "M";
		public static final String EVENT_ID_R_PREFIX = "R";
		public static final String EVENT_ID_T_PREFIX = "T";
		/**
		 * 结果命令后缀
		 */
		public static final String RESULT_SUFFIX = "_result";

		private Match() {
		}
	}
}
