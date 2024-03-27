package com.tradematcher.usecase.instruction;

import com.tradematcher.entity.Event;
import com.tradematcher.entity.MatchResult;
import com.tradematcher.entity.OrderBook;
import com.tradematcher.usecase.MatchRequest;
import com.tradematcher.util.Constants;

import java.math.BigInteger;

/**
 * FAK撮合订单指令
 * @program: new-match-engine
 * @author: TradeMatcher
 * @create: 2023-04-19 10:12
 **/
public final class PlaceFAKOrder extends Instruction {
	PlaceFAKOrder(MatchRequest matchRequest, OrderBook orderBook) {
		super(matchRequest, orderBook);
	}

	@Override
	public void action() {
		orderBook.checkExist(matchRequest);
		short code = matchRequest.getCode();
		if (code == Constants.Code.VALID_FOR_MATCHING_ENGINE) {
			orderBook.checkCounterparty(matchRequest);
			code = matchRequest.getCode();
			if (code == Constants.Code.VALID_FOR_MATCHING_ENGINE) {
				matchRequest.setCode(Constants.Code.MATCHING_REQUEST_SUCCESS);
				if (matchRequest.getAction() == Constants.Action.ASK) {ask();} else {bid();}
			} else {
				Event rejectEvent = Event.createRejectEvent(matchRequest.getOrderID(), matchRequest.getTotalPrice(),
						matchRequest.getSize(), matchRequest.getAction());
				MatchResult matchResult = new MatchResult(null, BigInteger.ZERO, BigInteger.ZERO, true, rejectEvent,
						null);

				matchRequest.setMatchResult(matchResult);
			}
		}
	}

	private void bid() {
		MatchResult matchResult = orderBook.matchByTotalPrice(matchRequest.getOrderID(), matchRequest.getTotalPrice(),
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

		matchRequest.setMatchResult(matchResult);
	}

	private void ask() {
		MatchResult matchResult = orderBook.matchAskBySize(matchRequest.getOrderID(), matchRequest.getSize());

		BigInteger totalFilledSize = matchResult.getFilledSize();

		BigInteger rejectedSize = matchRequest.getSize().subtract(totalFilledSize);
		if (rejectedSize.compareTo(BigInteger.ZERO) > 0) {
			Event rejectEvent = Event.createRejectEvent(matchRequest.getOrderID(), null, rejectedSize,
					matchRequest.getAction());

			matchResult.attachEvent(rejectEvent);
		}

		if (totalFilledSize.compareTo(BigInteger.ZERO) > 0) {
			matchResult.setTakerCompleted(true);
		}

		matchRequest.setMatchResult(matchResult);
	}
}
