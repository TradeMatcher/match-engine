package com.tradematcher.usecase.instruction;

import com.tradematcher.entity.OrderBook;
import com.tradematcher.usecase.MatchRequest;
import com.tradematcher.util.Constants;

/**
 * 指令工厂
 * @program: new-match-engine
 * @author: TradeMatcher
 * @create: 2023-04-19 10:06
 **/
public class InstructionFactory {
	private InstructionFactory() {
	}

	public static Instruction getInstruction(MatchRequest matchRequest, OrderBook orderBook) {
		String instruction = matchRequest.getInstructionType();
		return switch (instruction) {
			case Constants.Instruction.PLACE_FOK_ORDER -> new PlaceFOKOrder(matchRequest, orderBook);
			case Constants.Instruction.PLACE_FAK_ORDER -> new PlaceFAKOrder(matchRequest, orderBook);
			case Constants.Instruction.PLACE_MTL_ORDER -> new PlaceMTLOrder(matchRequest, orderBook);
			case Constants.Instruction.PLACE_GTC_ORDER -> new PlaceGTCOrder(matchRequest, orderBook);
			case Constants.Instruction.PLACE_LIMIT_FOK_ORDER -> new PlaceLimitFOKOrder(matchRequest, orderBook);
			case Constants.Instruction.CANCEL_ORDER -> new CancelOrder(matchRequest, orderBook);
			case Constants.Instruction.MARKET_ORDER_BOOK -> new MarketOrderBook(matchRequest, orderBook);
			default -> null;
		};
	}
}
