package com.tradematcher.usecase.instruction;

import com.tradematcher.entity.Event;
import com.tradematcher.entity.Market;
import com.tradematcher.entity.MatchResult;
import com.tradematcher.entity.OrderBook;
import com.tradematcher.usecase.MatchRequest;
import com.tradematcher.util.Constants;

import java.math.BigInteger;

/**
 * GTC撮合订单指令
 * @program: new-match-engine
 * @author: TradeMatcher
 * @create: 2023-04-19 10:13
 **/
public final class PlaceGTCOrder extends Instruction {
	PlaceGTCOrder(MatchRequest matchRequest, OrderBook orderBook) {
		super(matchRequest, orderBook);
	}

	@Override
	public void action() {
		orderBook.checkExist(matchRequest);
		short code = matchRequest.getCode();
		if (code == Constants.Code.VALID_FOR_MATCHING_ENGINE) {
			matchRequest.setCode(Constants.Code.MATCHING_REQUEST_SUCCESS);

			MatchResult matchResult = orderBook.matchByUnitPriceAndSize(matchRequest.getOrderID(),
					matchRequest.getUnitPrice(), matchRequest.getSize(), matchRequest.getAction());

			BigInteger totalFilledSize = matchResult.getFilledSize();

			if (totalFilledSize.compareTo(BigInteger.ZERO) >= 0) {
				BigInteger remainedSize = matchRequest.getSize().subtract(totalFilledSize);
				if (remainedSize.compareTo(BigInteger.ZERO) > 0) {
					Market market = orderBook.createOrder(matchRequest.getUnitPrice(), remainedSize,
							matchRequest.getAction(), matchRequest.getOrderID(), matchRequest.getVolumeDigits(),
							matchRequest.getTradeType(), matchRequest.getSymbolDecimal());

					matchResult.setMarket(market);
				}

				if (totalFilledSize.compareTo(BigInteger.ZERO) > 0) {
					Event event = matchResult.getEvent();
					BigInteger totalFilledPrice = BigInteger.ZERO;
					while (event != null && event.getType() == Constants.EventType.MAKER) {
						totalFilledPrice = totalFilledPrice.add(event.getUnitPrice().multiply(event.getSize()));
						event = event.getNext();
					}

					matchResult.setTotalFilledPrice(totalFilledPrice);
				}
			}
			matchRequest.setMatchResult(matchResult);
		}
	}
}
