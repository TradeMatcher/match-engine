package com.tradematcher.usecase.instruction;

import com.tradematcher.entity.Event;
import com.tradematcher.entity.Market;
import com.tradematcher.entity.MatchResult;
import com.tradematcher.entity.OrderBook;
import com.tradematcher.usecase.MatchRequest;
import com.tradematcher.util.Constants;

import java.math.BigInteger;

/**
 * MTL撮合订单指令
 * @program: new-match-engine
 * @author: TradeMatcher
 * @create: 2023-04-19 10:40
 **/
public final class PlaceMTLOrder extends Instruction {
	PlaceMTLOrder(MatchRequest matchRequest, OrderBook orderBook) {
		super(matchRequest, orderBook);
	}

	@Override
	public void action() {
		orderBook.checkExist(matchRequest);
		short code = matchRequest.getCode();
		if (code == Constants.Code.VALID_FOR_MATCHING_ENGINE) {
			BigInteger totalRejectedPrice = matchRequest.getTotalPrice();
			MatchResult matchResult;
			orderBook.checkCounterparty(matchRequest);
			code = matchRequest.getCode();
			if (code == Constants.Code.VALID_FOR_MATCHING_ENGINE) {
				matchRequest.setCode(Constants.Code.MATCHING_REQUEST_SUCCESS);

				matchResult = orderBook.matchByTotalPrice(matchRequest.getOrderID(), matchRequest.getTotalPrice(),
						matchRequest.getAction(), matchRequest.getReservePrice(), matchRequest.getMinSize());

				BigInteger totalFilledPrice = matchResult.getTotalFilledPrice();

				BigInteger latestPrice = matchResult.getLatestPrice();
				totalRejectedPrice = totalRejectedPrice.subtract(totalFilledPrice);
				if (latestPrice != null && latestPrice.compareTo(BigInteger.ZERO) > 0
					&& totalRejectedPrice.compareTo(BigInteger.ZERO) > 0) {
					BigInteger remainedSize = totalRejectedPrice.divide(latestPrice);
					if (remainedSize.compareTo(BigInteger.ZERO) > 0) {
						Market market = orderBook.createOrder(latestPrice, remainedSize, matchRequest.getAction(),
								matchRequest.getOrderID(), matchRequest.getVolumeDigits(), matchRequest.getTradeType(),
								matchRequest.getSymbolDecimal());
						matchResult.setMarket(market);
					}

					totalRejectedPrice = totalRejectedPrice.subtract(remainedSize.multiply(latestPrice));

				}

				if (totalRejectedPrice.compareTo(BigInteger.ZERO) > 0) {
					Event rejectEvent = Event.createRejectEvent(matchRequest.getOrderID(), totalRejectedPrice, null,
							matchRequest.getAction());
					matchResult.attachEvent(rejectEvent);
				}

				if (matchRequest.getTotalPrice().compareTo(totalRejectedPrice.add(totalFilledPrice)) == 0) {
					matchResult.setTakerCompleted(true);
				}
			} else {
				Event rejectEvent = Event.createRejectEvent(matchRequest.getOrderID(), totalRejectedPrice, null,
						matchRequest.getAction());
				matchResult = new MatchResult(null, BigInteger.ZERO, BigInteger.ZERO, true, rejectEvent, null);
			}

			matchRequest.setMatchResult(matchResult);
		}
	}
}
