package com.tradematcher.usecase.instruction;

import com.tradematcher.entity.Event;
import com.tradematcher.entity.MatchResult;
import com.tradematcher.entity.OrderBook;
import com.tradematcher.usecase.MatchRequest;
import com.tradematcher.util.Constants;

import java.math.BigInteger;

/**
 * FOK撮合订单指令
 * @program: new-match-engine
 * @author: TradeMatcher
 * @create: 2023-04-18 23:49
 **/
public final class PlaceFOKOrder extends Instruction {
	PlaceFOKOrder(MatchRequest matchRequest, OrderBook orderBook) {
		super(matchRequest, orderBook);
	}

	@Override
	public void action() {
		MatchResult matchResult = null;
		orderBook.checkExist(matchRequest);
		short code = matchRequest.getCode();
		if (code == Constants.Code.VALID_FOR_MATCHING_ENGINE) {
			orderBook.checkCounterparty(matchRequest);
			code = matchRequest.getCode();
			if (code == Constants.Code.VALID_FOR_MATCHING_ENGINE) {
				matchRequest.setCode(Constants.Code.MATCHING_REQUEST_SUCCESS);
				boolean enough = orderBook.tryMatchByTotalPrice(matchRequest.getTotalPrice(), matchRequest.getAction(),
						matchRequest.getReservePrice(), matchRequest.getMinSize());

				if (enough) {
					matchResult = orderBook.matchByTotalPrice(matchRequest.getOrderID(), matchRequest.getTotalPrice(),
							matchRequest.getAction(), matchRequest.getReservePrice(), matchRequest.getMinSize());

					BigInteger totalFilledPrice = matchResult.getTotalFilledPrice();

					BigInteger totalRejectedPrice = matchRequest.getTotalPrice().subtract(totalFilledPrice);

					if (totalRejectedPrice.compareTo(BigInteger.ZERO) > 0) {
						Event rejectEvent = Event.createRejectEvent(matchRequest.getOrderID(), totalRejectedPrice, null,
								matchRequest.getAction());
						matchResult.attachEvent(rejectEvent);

						if (totalRejectedPrice.compareTo(matchRequest.getTotalPrice()) == 0) {
							matchResult.setTakerCompleted(true);
						}
					}

					if (totalFilledPrice.compareTo(BigInteger.ZERO) > 0) {
						matchResult.setTakerCompleted(true);
					}
				} else {
					Event rejectEvent = Event.createRejectEvent(matchRequest.getOrderID(), matchRequest.getTotalPrice(),
							null, matchRequest.getAction());
					matchResult = new MatchResult(null, BigInteger.ZERO, BigInteger.ZERO, true, rejectEvent, null);
				}
			} else {
				Event rejectEvent = Event.createRejectEvent(matchRequest.getOrderID(), matchRequest.getTotalPrice(),
						null, matchRequest.getAction());
				matchResult = new MatchResult(null, BigInteger.ZERO, BigInteger.ZERO, true, rejectEvent, null);
			}
		}

		matchRequest.setMatchResult(matchResult);
	}
}
