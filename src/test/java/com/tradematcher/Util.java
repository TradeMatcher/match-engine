package com.tradematcher;

import com.tradematcher.entity.Event;
import com.tradematcher.usecase.Match;
import com.tradematcher.usecase.MatchRequest;
import com.tradematcher.util.Constants;

import java.math.BigInteger;

import static com.tradematcher.util.Constants.Code.MATCHING_REQUEST_SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @program: new-match-engine
 * @author: TradeMatcher
 * @create: 2023-04-23 11:27
 **/
public class Util {
	public static final int SYMBOL_ID = Integer.MAX_VALUE;

	public static BigInteger powPlus(BigInteger value) {
		return value.multiply(BigInteger.TEN.pow(18));
	}

	public static BigInteger powMinus(BigInteger value) {
		return value.divide(BigInteger.TEN.pow(18));
	}

	public static void checkDepth(Match match, int askDepth, int bidDepth) {
		assertEquals(askDepth, match.getCopyOrderBookMap().get(SYMBOL_ID).getCopyAskBuckets().size());
		assertEquals(bidDepth, match.getCopyOrderBookMap().get(SYMBOL_ID).getCopyBidBuckets().size());
	}

	public static void mainCheck(Match match, MatchRequest matchRequest, int askDepth, int bidDepth,
			BigInteger totalFilledPrice, BigInteger filledSize, BigInteger totalRejectedPrice, int makerEventNum,
			int takerEventNum, int rejectEventNum, boolean takerCompleted, boolean hasMarket) {
		mainCheck(match, matchRequest, askDepth, bidDepth, totalFilledPrice, filledSize, totalRejectedPrice, null,
				makerEventNum, takerEventNum, rejectEventNum, takerCompleted, hasMarket, 1, SYMBOL_ID,
				MATCHING_REQUEST_SUCCESS);
	}

	public static void mainCheck(Match match, MatchRequest matchRequest, int askDepth, int bidDepth,
			BigInteger totalFilledPrice, BigInteger filledSize, BigInteger totalRejectedPrice, BigInteger rejectedSize,
			int makerEventNum, int takerEventNum, int rejectEventNum, boolean takerCompleted, boolean hasMarket,
			int orderBookSize, int symbolID, short code) {

		if (code != MATCHING_REQUEST_SUCCESS) {
			Event event = matchRequest.getMatchResult().getEvent();
			assertEquals(Constants.EventType.REJECT, event.getType());
			assertEquals(code, matchRequest.getCode());
			assertEquals(1, rejectEventNum);
			assertEquals(totalRejectedPrice, event.getTotalPrice());
			assertEquals(matchRequest.getTotalPrice(), event.getTotalPrice());
			assertTrue(takerCompleted);
		} else {
			assertEquals(orderBookSize, match.getCopyOrderBookMap().size());
			assertEquals(askDepth, match.getCopyOrderBookMap().get(symbolID).getCopyAskBuckets().size());
			if (hasMarket) {
				assertEquals(askDepth, matchRequest.getMatchResult().getMarket().getAskDepth());
				assertEquals(askDepth, matchRequest.getMatchResult().getMarket().getAskSizes().length);
				assertEquals(askDepth, matchRequest.getMatchResult().getMarket().getAskOrderNumbers().length);
				assertEquals(askDepth, matchRequest.getMatchResult().getMarket().getAskUnitPrices().length);
			}

			assertEquals(bidDepth, match.getCopyOrderBookMap().get(symbolID).getCopyBidBuckets().size());
			if (hasMarket) {
				assertEquals(bidDepth, matchRequest.getMatchResult().getMarket().getBidDepth());
				assertEquals(bidDepth, matchRequest.getMatchResult().getMarket().getBidSizes().length);
				assertEquals(bidDepth, matchRequest.getMatchResult().getMarket().getBidOrderNumbers().length);
				assertEquals(bidDepth, matchRequest.getMatchResult().getMarket().getBidUnitPrices().length);
			}

			assertEquals(code, matchRequest.getCode());
			if (takerCompleted) {
				assertEquals(totalFilledPrice, matchRequest.getMatchResult().getTotalFilledPrice());
				assertEquals(filledSize, matchRequest.getMatchResult().getFilledSize());
			}

			BigInteger eventRejectTotalPrice = new BigInteger("0");
			BigInteger eventRejectSize = null;
			int eventMakerEventNum = 0;
			int eventRejectEventNum = 0;
			Event event = matchRequest.getMatchResult().getEvent();
			while (event != null) {
				if (event.getType() == Constants.EventType.MAKER) {
					eventMakerEventNum++;
				} else if (event.getType() == Constants.EventType.REJECT) {
					eventRejectEventNum++;
					eventRejectTotalPrice = event.getTotalPrice();
					eventRejectSize = event.getSize();
				}
				event = event.getNext();
			}

			assertEquals(makerEventNum, eventMakerEventNum);
			assertEquals(rejectEventNum, eventRejectEventNum);
			assertEquals(totalRejectedPrice, eventRejectTotalPrice);
			assertEquals(rejectedSize, eventRejectSize);
			assertEquals(takerCompleted, matchRequest.getMatchResult().isTakerCompleted());
		}

	}
}
