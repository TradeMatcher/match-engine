package com.tradematcher;

import com.tradematcher.entity.OrderBook;
import com.tradematcher.usecase.Match;
import com.tradematcher.usecase.MatchRequest;
import com.tradematcher.util.Constants;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static com.tradematcher.Util.*;
import static com.tradematcher.util.Constants.Code.*;
import static com.tradematcher.util.Constants.Instruction.PLACE_GTC_ORDER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @program: new-match-engine
 * @author: TradeMatcher
 * @create: 2023-04-28 07:27
 **/
class GTCTest {
	Match match;

	@org.junit.jupiter.api.BeforeEach
	void setUp() {
		Map<Integer, OrderBook> orderBookMap = new HashMap<>(0);
		match = new Match(orderBookMap, "./.test_backup/");
		MatchRequest matchRequest = new MatchRequest("A4", SYMBOL_ID, PLACE_GTC_ORDER, Constants.Action.ASK,
				new BigInteger("8"), 0, new BigInteger("2"), 0, new BigInteger("0"), MATCHING_NEW);
		match.doAction(matchRequest);
		assertEquals(MATCHING_REQUEST_SUCCESS, matchRequest.getCode());
		assertEquals(1, match.getCopyOrderBookMap().size());
		checkDepth(match, 1, 0);

		matchRequest = new MatchRequest("A3", SYMBOL_ID, PLACE_GTC_ORDER, Constants.Action.ASK, new BigInteger("6"), 0,
				new BigInteger("3"), 0, new BigInteger("0"), MATCHING_NEW);
		match.doAction(matchRequest);
		assertEquals(MATCHING_REQUEST_SUCCESS, matchRequest.getCode());
		assertEquals(1, match.getCopyOrderBookMap().size());
		checkDepth(match, 2, 0);

		matchRequest = new MatchRequest("A2", SYMBOL_ID, PLACE_GTC_ORDER, Constants.Action.ASK, new BigInteger("6"), 0,
				new BigInteger("4"), 0, new BigInteger("0"), MATCHING_NEW);
		match.doAction(matchRequest);
		assertEquals(MATCHING_REQUEST_SUCCESS, matchRequest.getCode());
		assertEquals(1, match.getCopyOrderBookMap().size());
		checkDepth(match, 2, 0);

		matchRequest = new MatchRequest("A1", SYMBOL_ID, PLACE_GTC_ORDER, Constants.Action.ASK, new BigInteger("9"), 0,
				new BigInteger("6"), 0, new BigInteger("0"), MATCHING_NEW);
		match.doAction(matchRequest);
		assertEquals(MATCHING_REQUEST_SUCCESS, matchRequest.getCode());
		assertEquals(1, match.getCopyOrderBookMap().size());
		checkDepth(match, 3, 0);

		matchRequest = new MatchRequest("B1", SYMBOL_ID, PLACE_GTC_ORDER, Constants.Action.BID, new BigInteger("4"), 0,
				new BigInteger("6"), 0, new BigInteger("0"), MATCHING_NEW);
		match.doAction(matchRequest);
		assertEquals(MATCHING_REQUEST_SUCCESS, matchRequest.getCode());
		assertEquals(1, match.getCopyOrderBookMap().size());
		checkDepth(match, 3, 1);

		matchRequest = new MatchRequest("B2", SYMBOL_ID, PLACE_GTC_ORDER, Constants.Action.BID, new BigInteger("3"), 0,
				new BigInteger("4"), 0, new BigInteger("0"), MATCHING_NEW);
		match.doAction(matchRequest);
		assertEquals(MATCHING_REQUEST_SUCCESS, matchRequest.getCode());
		assertEquals(1, match.getCopyOrderBookMap().size());
		checkDepth(match, 3, 2);

		matchRequest = new MatchRequest("B3", SYMBOL_ID, PLACE_GTC_ORDER, Constants.Action.BID, new BigInteger("3"), 0,
				new BigInteger("3"), 0, new BigInteger("0"), MATCHING_NEW);
		match.doAction(matchRequest);
		assertEquals(MATCHING_REQUEST_SUCCESS, matchRequest.getCode());
		assertEquals(1, match.getCopyOrderBookMap().size());
		checkDepth(match, 3, 2);

		matchRequest = new MatchRequest("B4", SYMBOL_ID, PLACE_GTC_ORDER, Constants.Action.BID, new BigInteger("1"), 0,
				new BigInteger("2"), 0, new BigInteger("0"), MATCHING_NEW);
		match.doAction(matchRequest);
		assertEquals(MATCHING_REQUEST_SUCCESS, matchRequest.getCode());
		assertEquals(1, match.getCopyOrderBookMap().size());
		checkDepth(match, 3, 3);
	}

	@Test
	void GTC_BID_1() {
		int symbolID = SYMBOL_ID - 10;
		MatchRequest matchRequest = new MatchRequest("GTC_B_1", symbolID, PLACE_GTC_ORDER, Constants.Action.BID,
				new BigInteger("10"), 0, new BigInteger("3"), 0, new BigInteger("0"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);
		assertEquals(MATCHING_REQUEST_SUCCESS, matchRequest.getCode());
		assertEquals(2, match.getCopyOrderBookMap().size());
		assertTrue(match.getCopyOrderBookMap().containsKey(SYMBOL_ID));
		assertTrue(match.getCopyOrderBookMap().containsKey(symbolID));
	}

	/**
	 * ask3 9(6) <br>
	 * ask2 8(2)   <br>
	 * ask1 6(3+4)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void GTC_BID_2() {
		MatchRequest matchRequest = new MatchRequest("GTC_B_2", SYMBOL_ID, PLACE_GTC_ORDER, Constants.Action.BID,
				new BigInteger("6"), 0, new BigInteger("1"), 0, new BigInteger("0"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("6");
		BigInteger filledSize = new BigInteger("1");
		BigInteger totalRejectedPrice = new BigInteger("0");
		mainCheck(match, matchRequest, 3, 3, totalFilledPrice, filledSize, totalRejectedPrice, 1, 1, 0, true, true);
	}


	/**
	 * ask3 9(6) <br>
	 * ask2 8(2)   <br>
	 * ask1 6(3+4)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void GTC_BID_3() {
		MatchRequest matchRequest = new MatchRequest("GTC_B_2", SYMBOL_ID, PLACE_GTC_ORDER, Constants.Action.BID,
				new BigInteger("7"), 0, new BigInteger("1"), 0, new BigInteger("0"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("6");
		BigInteger filledSize = new BigInteger("1");
		BigInteger totalRejectedPrice = new BigInteger("0");
		mainCheck(match, matchRequest, 3, 3, totalFilledPrice, filledSize, totalRejectedPrice, 1, 1, 0, true, true);
	}

	/**
	 * ask3 9(6) <br>
	 * ask2 8(2)   <br>
	 * ask1 6(3+4)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void GTC_BID_4() {
		MatchRequest matchRequest = new MatchRequest("GTC_B_2", SYMBOL_ID, PLACE_GTC_ORDER, Constants.Action.BID,
				new BigInteger("6"), 0, new BigInteger("2"), 0, new BigInteger("0"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("12");
		BigInteger filledSize = new BigInteger("2");
		BigInteger totalRejectedPrice = new BigInteger("0");
		mainCheck(match, matchRequest, 3, 3, totalFilledPrice, filledSize, totalRejectedPrice, 1, 1, 0, true, true);
	}

	/**
	 * ask3 9(6) <br>
	 * ask2 8(2)   <br>
	 * ask1 6(3+4)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void GTC_BID_5() {
		MatchRequest matchRequest = new MatchRequest("GTC_B_2", SYMBOL_ID, PLACE_GTC_ORDER, Constants.Action.BID,
				new BigInteger("6"), 0, new BigInteger("7"), 0, new BigInteger("0"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("42");
		BigInteger filledSize = new BigInteger("7");
		BigInteger totalRejectedPrice = new BigInteger("0");
		mainCheck(match, matchRequest, 2, 3, totalFilledPrice, filledSize, totalRejectedPrice, 2, 1, 0, true, true);
	}

	/**
	 * ask3 9(6) <br>
	 * ask2 8(2)   <br>
	 * ask1 6(3+4)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void GTC_BID_6() {
		MatchRequest matchRequest = new MatchRequest("GTC_B_2", SYMBOL_ID, PLACE_GTC_ORDER, Constants.Action.BID,
				new BigInteger("9"), 0, new BigInteger("9"), 0, new BigInteger("0"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("58");
		BigInteger filledSize = new BigInteger("9");
		BigInteger totalRejectedPrice = new BigInteger("0");
		mainCheck(match, matchRequest, 1, 3, totalFilledPrice, filledSize, totalRejectedPrice, 3, 1, 0, true, true);
	}

	/**
	 * ask3 9(6) <br>
	 * ask2 8(2)   <br>
	 * ask1 6(3+4)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void GTC_BID_7() {
		MatchRequest matchRequest = new MatchRequest("GTC_B_2", SYMBOL_ID, PLACE_GTC_ORDER, Constants.Action.BID,
				new BigInteger("9"), 0, new BigInteger("15"), 0, new BigInteger("0"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("112");
		BigInteger filledSize = new BigInteger("15");
		BigInteger totalRejectedPrice = new BigInteger("0");
		mainCheck(match, matchRequest, 0, 3, totalFilledPrice, filledSize, totalRejectedPrice, 4, 1, 0, true, true);
	}

	/**
	 * ask3 9(6) <br>
	 * ask2 8(2)   <br>
	 * ask1 6(3+4)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void GTC_BID_8() {
		MatchRequest matchRequest = new MatchRequest("GTC_B_2", SYMBOL_ID, PLACE_GTC_ORDER, Constants.Action.BID,
				new BigInteger("9"), 0, new BigInteger("16"), 0, new BigInteger("0"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("88");
		BigInteger filledSize = new BigInteger("15");
		BigInteger totalRejectedPrice = new BigInteger("0");
		mainCheck(match, matchRequest, 0, 4, totalFilledPrice, filledSize, totalRejectedPrice, 4, 1, 0, false, true);
	}

	/**
	 * ask3 9(6) <br>
	 * ask2 8(2)   <br>
	 * ask1 6(3+4)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void GTC_BID_9() {
		MatchRequest matchRequest = new MatchRequest("GTC_B_2", SYMBOL_ID, PLACE_GTC_ORDER, Constants.Action.BID,
				new BigInteger("20"), 0, new BigInteger("20"), 0, new BigInteger("0"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("88");
		BigInteger filledSize = new BigInteger("15");
		BigInteger totalRejectedPrice = new BigInteger("0");
		mainCheck(match, matchRequest, 0, 4, totalFilledPrice, filledSize, totalRejectedPrice, 4, 1, 0, false, true);
	}

	/**
	 * ask3 9(6) <br>
	 * ask2 8(2)   <br>
	 * ask1 6(3+4)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void GTC_BID_10() {
		MatchRequest matchRequest = new MatchRequest("GTC_B_2", SYMBOL_ID, PLACE_GTC_ORDER, Constants.Action.BID,
				new BigInteger("5"), 0, new BigInteger("7"), 0, new BigInteger("0"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("0");
		BigInteger filledSize = new BigInteger("0");
		BigInteger totalRejectedPrice = new BigInteger("0");
		mainCheck(match, matchRequest, 3, 4, totalFilledPrice, filledSize, totalRejectedPrice, 0, 0, 0, false, true);
	}

	@Test
	void FOK_BID_11() {
		int symbolID = SYMBOL_ID - 10;
		MatchRequest matchRequest = new MatchRequest("GTC_B_2", symbolID, PLACE_GTC_ORDER, Constants.Action.BID,
				new BigInteger("10"), 0, new BigInteger("1"), 0, new BigInteger("0"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("0");
		BigInteger filledSize = new BigInteger("0");
		BigInteger totalRejectedPrice = new BigInteger("0");
		mainCheck(match, matchRequest, 0, 1, totalFilledPrice, filledSize, totalRejectedPrice, null, 0, 0, 0, false,
				true, 2, symbolID, MATCHING_REQUEST_SUCCESS);
	}

	/**
	 * ask3 9(6) <br>
	 * ask2 8(2)   <br>
	 * ask1 6(3+4)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void GTC_ASK_1() {
		int symbolID = SYMBOL_ID - 10;
		MatchRequest matchRequest = new MatchRequest("GTC_B_1", symbolID, PLACE_GTC_ORDER, Constants.Action.ASK,
				new BigInteger("10"), 0, new BigInteger("3"), 0, new BigInteger("0"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);
		assertEquals(MATCHING_REQUEST_SUCCESS, matchRequest.getCode());
		assertEquals(2, match.getCopyOrderBookMap().size());
		assertTrue(match.getCopyOrderBookMap().containsKey(SYMBOL_ID));
		assertTrue(match.getCopyOrderBookMap().containsKey(symbolID));
	}

	/**
	 * ask3 9(6) <br>
	 * ask2 8(2)   <br>
	 * ask1 6(3+4)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void GTC_ASK_2() {
		MatchRequest matchRequest = new MatchRequest("GTC_B_2", SYMBOL_ID, PLACE_GTC_ORDER, Constants.Action.ASK,
				new BigInteger("4"), 0, new BigInteger("1"), 0, new BigInteger("0"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("4");
		BigInteger filledSize = new BigInteger("1");
		BigInteger totalRejectedPrice = new BigInteger("0");
		mainCheck(match, matchRequest, 3, 3, totalFilledPrice, filledSize, totalRejectedPrice, 1, 1, 0, true, true);
	}


	/**
	 * ask3 9(6) <br>
	 * ask2 8(2)   <br>
	 * ask1 6(3+4)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void GTC_ASK_3() {
		MatchRequest matchRequest = new MatchRequest("GTC_B_2", SYMBOL_ID, PLACE_GTC_ORDER, Constants.Action.ASK,
				new BigInteger("3"), 0, new BigInteger("1"), 0, new BigInteger("0"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("4");
		BigInteger filledSize = new BigInteger("1");
		BigInteger totalRejectedPrice = new BigInteger("0");
		mainCheck(match, matchRequest, 3, 3, totalFilledPrice, filledSize, totalRejectedPrice, 1, 1, 0, true, true);
	}

	/**
	 * ask3 9(6) <br>
	 * ask2 8(2)   <br>
	 * ask1 6(3+4)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void GTC_ASK_4() {
		MatchRequest matchRequest = new MatchRequest("GTC_B_2", SYMBOL_ID, PLACE_GTC_ORDER, Constants.Action.ASK,
				new BigInteger("4"), 0, new BigInteger("2"), 0, new BigInteger("0"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("8");
		BigInteger filledSize = new BigInteger("2");
		BigInteger totalRejectedPrice = new BigInteger("0");
		mainCheck(match, matchRequest, 3, 3, totalFilledPrice, filledSize, totalRejectedPrice, 1, 1, 0, true, true);
	}

	/**
	 * ask3 9(6) <br>
	 * ask2 8(2)   <br>
	 * ask1 6(3+4)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void GTC_ASK_5() {
		MatchRequest matchRequest = new MatchRequest("GTC_B_2", SYMBOL_ID, PLACE_GTC_ORDER, Constants.Action.ASK,
				new BigInteger("4"), 0, new BigInteger("6"), 0, new BigInteger("0"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("24");
		BigInteger filledSize = new BigInteger("6");
		BigInteger totalRejectedPrice = new BigInteger("0");
		mainCheck(match, matchRequest, 3, 2, totalFilledPrice, filledSize, totalRejectedPrice, 1, 1, 0, true, true);
	}

	/**
	 * ask3 9(6) <br>
	 * ask2 8(2)   <br>
	 * ask1 6(3+4)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void GTC_ASK_6() {
		MatchRequest matchRequest = new MatchRequest("GTC_B_2", SYMBOL_ID, PLACE_GTC_ORDER, Constants.Action.ASK,
				new BigInteger("1"), 0, new BigInteger("13"), 0, new BigInteger("0"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("45");
		BigInteger filledSize = new BigInteger("13");
		BigInteger totalRejectedPrice = new BigInteger("0");
		mainCheck(match, matchRequest, 3, 1, totalFilledPrice, filledSize, totalRejectedPrice, 3, 1, 0, true, true);
	}

	/**
	 * ask3 9(6) <br>
	 * ask2 8(2)   <br>
	 * ask1 6(3+4)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void GTC_ASK_7() {
		MatchRequest matchRequest = new MatchRequest("GTC_B_2", SYMBOL_ID, PLACE_GTC_ORDER, Constants.Action.ASK,
				new BigInteger("1"), 0, new BigInteger("15"), 0, new BigInteger("0"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("47");
		BigInteger filledSize = new BigInteger("15");
		BigInteger totalRejectedPrice = new BigInteger("0");
		mainCheck(match, matchRequest, 3, 0, totalFilledPrice, filledSize, totalRejectedPrice, 4, 1, 0, true, true);
	}

	/**
	 * ask3 9(6) <br>
	 * ask2 8(2)   <br>
	 * ask1 6(3+4)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void GTC_ASK_8() {
		MatchRequest matchRequest = new MatchRequest("GTC_B_2", SYMBOL_ID, PLACE_GTC_ORDER, Constants.Action.ASK,
				new BigInteger("1"), 0, new BigInteger("16"), 0, new BigInteger("0"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("47");
		BigInteger filledSize = new BigInteger("15");
		BigInteger totalRejectedPrice = new BigInteger("0");
		mainCheck(match, matchRequest, 4, 0, totalFilledPrice, filledSize, totalRejectedPrice, 4, 1, 0, false, true);
	}

	/**
	 * ask3 9(6) <br>
	 * ask2 8(2)   <br>
	 * ask1 6(3+4)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void GTC_ASK_9() {
		MatchRequest matchRequest = new MatchRequest("GTC_B_2", SYMBOL_ID, PLACE_GTC_ORDER, Constants.Action.ASK,
				new BigInteger("1"), 0, new BigInteger("20"), 0, new BigInteger("0"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("47");
		BigInteger filledSize = new BigInteger("15");
		BigInteger totalRejectedPrice = new BigInteger("0");
		mainCheck(match, matchRequest, 4, 0, totalFilledPrice, filledSize, totalRejectedPrice, 4, 1, 0, false, true);
	}

	/**
	 * ask3 9(6) <br>
	 * ask2 8(2)   <br>
	 * ask1 6(3+4)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void GTC_ASK_10() {
		MatchRequest matchRequest = new MatchRequest("GTC_B_2", SYMBOL_ID, PLACE_GTC_ORDER, Constants.Action.ASK,
				new BigInteger("5"), 0, new BigInteger("7"), 0, new BigInteger("0"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("0");
		BigInteger filledSize = new BigInteger("0");
		BigInteger totalRejectedPrice = new BigInteger("0");
		mainCheck(match, matchRequest, 4, 3, totalFilledPrice, filledSize, totalRejectedPrice, 0, 0, 0, false, true);
	}

	@Test
	void GTC_ASK_11() {
		int symbolID = SYMBOL_ID - 10;
		MatchRequest matchRequest = new MatchRequest("GTC_B_2", symbolID, PLACE_GTC_ORDER, Constants.Action.ASK,
				new BigInteger("1"), 0, new BigInteger("1"), 0, new BigInteger("0"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("0");
		BigInteger filledSize = new BigInteger("0");
		BigInteger totalRejectedPrice = new BigInteger("0");
		mainCheck(match, matchRequest, 1, 0, totalFilledPrice, filledSize, totalRejectedPrice, null, 0, 0, 0, false,
				true, 2, symbolID, MATCHING_REQUEST_SUCCESS);
	}
}
