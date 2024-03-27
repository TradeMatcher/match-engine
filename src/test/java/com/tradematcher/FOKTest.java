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
import static com.tradematcher.util.Constants.Instruction.PLACE_FOK_ORDER;
import static com.tradematcher.util.Constants.Instruction.PLACE_GTC_ORDER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FOK订单测试
 * @program: new-match-engine
 * @author: TradeMatcher
 * @create: 2023-04-21 18:21
 **/
class FOKTest {

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

		matchRequest = new MatchRequest("A1", SYMBOL_ID, PLACE_GTC_ORDER, Constants.Action.ASK, new BigInteger("5"), 0,
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
	void FOK_BID_1() {
		int symbolID = SYMBOL_ID - 10;
		MatchRequest matchRequest = new MatchRequest("FOK_B_1", symbolID, PLACE_FOK_ORDER, Constants.Action.BID,
				new BigInteger("0"), 0, new BigInteger("0"), 0, new BigInteger("1"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);
		assertEquals(MATCHING_COUNTERPARTY_NOT_FOUND, matchRequest.getCode());
		assertEquals(2, match.getCopyOrderBookMap().size());
		assertTrue(match.getCopyOrderBookMap().containsKey(SYMBOL_ID));
		assertTrue(match.getCopyOrderBookMap().containsKey(symbolID));
	}

	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_BID_2() {
		MatchRequest matchRequest = new MatchRequest("FOK_B_2", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.BID,
				new BigInteger("0"), 0, new BigInteger("0"), 0, new BigInteger("30"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("30");
		BigInteger filledSize = new BigInteger("6");
		BigInteger totalRejectedPrice = new BigInteger("0");
		mainCheck(match, matchRequest, 2, 3, totalFilledPrice, filledSize, totalRejectedPrice, 1, 1, 0, true, true);
	}

	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_BID_3() {
		MatchRequest matchRequest = new MatchRequest("FOK_B_3", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.BID,
				new BigInteger("0"), 0, new BigInteger("0"), 0, new BigInteger("4"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("0");
		BigInteger filledSize = new BigInteger("0");
		BigInteger totalRejectedPrice = new BigInteger("4");
		mainCheck(match, matchRequest, 3, 3, totalFilledPrice, filledSize, totalRejectedPrice, 0, 0, 1, true, false);
	}

	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_BID_4() {
		MatchRequest matchRequest = new MatchRequest("FOK_B_4", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.BID,
				new BigInteger("0"), 0, new BigInteger("0"), 0, new BigInteger("6"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("5");
		BigInteger filledSize = new BigInteger("1");
		BigInteger totalRejectedPrice = new BigInteger("1");
		mainCheck(match, matchRequest, 3, 3, totalFilledPrice, filledSize, totalRejectedPrice, 1, 1, 1, true, true);
	}

	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_BID_5() {
		MatchRequest matchRequest = new MatchRequest("FOK_B_5", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.BID,
				new BigInteger("0"), 0, new BigInteger("0"), 0, new BigInteger("9"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("5");
		BigInteger filledSize = new BigInteger("1");
		BigInteger totalRejectedPrice = new BigInteger("4");
		mainCheck(match, matchRequest, 3, 3, totalFilledPrice, filledSize, totalRejectedPrice, 1, 1, 1, true, true);
	}

	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_BID_6() {
		MatchRequest matchRequest = new MatchRequest("FOK_B_6", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.BID,
				new BigInteger("0"), 0, new BigInteger("0"), 0, new BigInteger("15"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("15");
		BigInteger filledSize = new BigInteger("3");
		BigInteger totalRejectedPrice = new BigInteger("0");
		mainCheck(match, matchRequest, 3, 3, totalFilledPrice, filledSize, totalRejectedPrice, 1, 1, 0, true, true);
	}

	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_BID_7() {
		MatchRequest matchRequest = new MatchRequest("FOK_B_7", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.BID,
				new BigInteger("0"), 0, new BigInteger("0"), 0, new BigInteger("89"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("0");
		BigInteger filledSize = new BigInteger("0");
		BigInteger totalRejectedPrice = new BigInteger("89");
		mainCheck(match, matchRequest, 3, 3, totalFilledPrice, filledSize, totalRejectedPrice, 0, 0, 1, true, false);
	}

	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_BID_8() {
		MatchRequest matchRequest = new MatchRequest("FOK_B_8", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.BID,
				new BigInteger("0"), 0, new BigInteger("0"), 0, new BigInteger("88"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("0");
		BigInteger filledSize = new BigInteger("0");
		BigInteger totalRejectedPrice = new BigInteger("88");
		mainCheck(match, matchRequest, 3, 3, totalFilledPrice, filledSize, totalRejectedPrice, 0, 0, 1, true, false);
	}

	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_BID_9() {
		MatchRequest matchRequest = new MatchRequest("FOK_B_9", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.BID,
				new BigInteger("0"), 1, new BigInteger("0"), 0, new BigInteger("72"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("72");
		BigInteger filledSize = new BigInteger("13");
		BigInteger totalRejectedPrice = new BigInteger("0");
		mainCheck(match, matchRequest, 1, 3, totalFilledPrice, filledSize, totalRejectedPrice, 3, 1, 0, true, true);
	}


	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_BID_10() {
		MatchRequest matchRequest = new MatchRequest("FOK_B_10", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.BID,
				new BigInteger("0"), 3, new BigInteger("0"), 0, new BigInteger("88"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("88");
		BigInteger filledSize = new BigInteger("15");
		BigInteger totalRejectedPrice = new BigInteger("0");
		mainCheck(match, matchRequest, 0, 3, totalFilledPrice, filledSize, totalRejectedPrice, 4, 1, 0, true, true);
	}

	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_BID_11() {
		MatchRequest matchRequest = new MatchRequest("FOK_B_11", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.BID,
				new BigInteger("0"), 1, new BigInteger("0"), 0, new BigInteger("6"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("5");
		BigInteger filledSize = new BigInteger("1");
		BigInteger totalRejectedPrice = new BigInteger("1");
		mainCheck(match, matchRequest, 3, 3, totalFilledPrice, filledSize, totalRejectedPrice, 1, 1, 1, true, true);
	}

	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_BID_12() {
		MatchRequest matchRequest = new MatchRequest("FOK_B_12", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.BID,
				new BigInteger("0"), 1, new BigInteger("0"), 0, new BigInteger("9"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("5");
		BigInteger filledSize = new BigInteger("1");
		BigInteger totalRejectedPrice = new BigInteger("4");
		mainCheck(match, matchRequest, 3, 3, totalFilledPrice, filledSize, totalRejectedPrice, 1, 1, 1, true, true);
	}

	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_BID_13() {
		MatchRequest matchRequest = new MatchRequest("FOK_B_13", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.BID,
				new BigInteger("0"), 1, new BigInteger("0"), 0, new BigInteger("30"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("30");
		BigInteger filledSize = new BigInteger("6");
		BigInteger totalRejectedPrice = new BigInteger("0");
		mainCheck(match, matchRequest, 2, 3, totalFilledPrice, filledSize, totalRejectedPrice, 1, 1, 0, true, true);
	}

	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_BID_14() {
		MatchRequest matchRequest = new MatchRequest("FOK_B_14", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.BID,
				new BigInteger("0"), 1, new BigInteger("0"), 0, new BigInteger("35"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("30");
		BigInteger filledSize = new BigInteger("6");
		BigInteger totalRejectedPrice = new BigInteger("5");
		mainCheck(match, matchRequest, 2, 3, totalFilledPrice, filledSize, totalRejectedPrice, 1, 1, 1, true, true);
	}

	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_BID_15() {
		MatchRequest matchRequest = new MatchRequest("FOK_B_15", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.BID,
				new BigInteger("0"), 1, new BigInteger("0"), 0, new BigInteger("36"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("36");
		BigInteger filledSize = new BigInteger("7");
		BigInteger totalRejectedPrice = new BigInteger("0");
		mainCheck(match, matchRequest, 2, 3, totalFilledPrice, filledSize, totalRejectedPrice, 2, 1, 0, true, true);
	}

	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_BID_16() {
		MatchRequest matchRequest = new MatchRequest("FOK_B_16", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.BID,
				new BigInteger("0"), 1, new BigInteger("0"), 0, new BigInteger("37"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("36");
		BigInteger filledSize = new BigInteger("7");
		BigInteger totalRejectedPrice = new BigInteger("1");
		mainCheck(match, matchRequest, 2, 3, totalFilledPrice, filledSize, totalRejectedPrice, 2, 1, 1, true, true);
	}

	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_BID_17() {
		MatchRequest matchRequest = new MatchRequest("FOK_B_17", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.BID,
				new BigInteger("0"), 3, new BigInteger("0"), 0, new BigInteger("81"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("80");
		BigInteger filledSize = new BigInteger("14");
		BigInteger totalRejectedPrice = new BigInteger("1");
		mainCheck(match, matchRequest, 1, 3, totalFilledPrice, filledSize, totalRejectedPrice, 4, 1, 1, true, true);
	}

	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_BID_18() {
		MatchRequest matchRequest = new MatchRequest("FOK_B_18", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.BID,
				new BigInteger("0"), 10, new BigInteger("0"), 0, new BigInteger("73"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("72");
		BigInteger filledSize = new BigInteger("13");
		BigInteger totalRejectedPrice = new BigInteger("1");
		mainCheck(match, matchRequest, 1, 3, totalFilledPrice, filledSize, totalRejectedPrice, 3, 1, 1, true, true);
	}

	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_BID_20() {
		MatchRequest matchRequest = new MatchRequest("FOK_B_20", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.BID,
				new BigInteger("0"), 1, new BigInteger("0"), 0, new BigInteger("4"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("0");
		BigInteger filledSize = new BigInteger("0");
		BigInteger totalRejectedPrice = new BigInteger("4");
		mainCheck(match, matchRequest, 3, 3, totalFilledPrice, filledSize, totalRejectedPrice, 0, 0, 1, true, true);
	}

	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_BID_21() {
		MatchRequest matchRequest = new MatchRequest("FOK_B_21", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.BID,
				new BigInteger("0"), 2, new BigInteger("0"), 0, new BigInteger("80"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("0");
		BigInteger filledSize = new BigInteger("0");
		BigInteger totalRejectedPrice = new BigInteger("80");
		mainCheck(match, matchRequest, 3, 3, totalFilledPrice, filledSize, totalRejectedPrice, 0, 0, 1, true, false);
	}

	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_BID_22() {
		MatchRequest matchRequest = new MatchRequest("FOK_B_22", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.BID,
				new BigInteger("0"), 2, new BigInteger("0"), 0, new BigInteger("36"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("36");
		BigInteger filledSize = new BigInteger("7");
		BigInteger totalRejectedPrice = new BigInteger("0");
		mainCheck(match, matchRequest, 2, 3, totalFilledPrice, filledSize, totalRejectedPrice, 2, 1, 0, true, true);
	}

	@Test
	void FOK_BID_23() {
		MatchRequest matchRequest = new MatchRequest("FOK_B_23", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.BID,
				new BigInteger("0"), 2, new BigInteger("0"), 0, new BigInteger("88"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("0");
		BigInteger filledSize = new BigInteger("0");
		BigInteger totalRejectedPrice = new BigInteger("88");
		mainCheck(match, matchRequest, 3, 3, totalFilledPrice, filledSize, totalRejectedPrice, 0, 0, 1, true, false);
	}

	@Test
	void FOK_BID_24() {
		MatchRequest matchRequest = new MatchRequest("FOK_B_24", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.BID,
				new BigInteger("0"), 2, new BigInteger("0"), 0, new BigInteger("100"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("0");
		BigInteger filledSize = new BigInteger("0");
		BigInteger totalRejectedPrice = new BigInteger("100");
		mainCheck(match, matchRequest, 3, 3, totalFilledPrice, filledSize, totalRejectedPrice, 0, 0, 1, true, false);
	}

	@Test
	void FOK_BID_25() {
		MatchRequest matchRequest = new MatchRequest("FOK_B_25", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.BID,
				new BigInteger("0"), 10, new BigInteger("0"), 0, new BigInteger("1000"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("0");
		BigInteger filledSize = new BigInteger("0");
		BigInteger totalRejectedPrice = new BigInteger("1000");
		mainCheck(match, matchRequest, 3, 3, totalFilledPrice, filledSize, totalRejectedPrice, 0, 0, 1, true, false);
	}

	@Test
	void FOK_BID_26() {
		int symbolID = SYMBOL_ID - 10;
		MatchRequest matchRequest = new MatchRequest("FOK_B_26", symbolID, PLACE_FOK_ORDER, Constants.Action.BID,
				new BigInteger("0"), 0, new BigInteger("0"), 0, new BigInteger("500"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("0");
		BigInteger filledSize = new BigInteger("0");
		BigInteger totalRejectedPrice = new BigInteger("500");
		mainCheck(match, matchRequest, 0, 0, totalFilledPrice, filledSize, totalRejectedPrice, null, 0, 0, 1, true,
				false, 2, symbolID, MATCHING_COUNTERPARTY_NOT_FOUND);
	}

	@Test
	void FOK_ASK_1() {
		int symbolID = SYMBOL_ID - 10;
		MatchRequest matchRequest = new MatchRequest("FOK_A_1", symbolID, PLACE_FOK_ORDER, Constants.Action.ASK,
				new BigInteger("0"), 0, new BigInteger("1"), 0, new BigInteger("1"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);
		assertEquals(MATCHING_COUNTERPARTY_NOT_FOUND, matchRequest.getCode());
		assertEquals(2, match.getCopyOrderBookMap().size());
		assertTrue(match.getCopyOrderBookMap().containsKey(SYMBOL_ID));
		assertTrue(match.getCopyOrderBookMap().containsKey(symbolID));
	}


	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_ASK_2() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_2", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.ASK,
				new BigInteger("0"), 0, new BigInteger("0"), 0, new BigInteger("24"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("24");
		BigInteger filledSize = new BigInteger("6");
		BigInteger totalRejectedPrice = new BigInteger("0");
		mainCheck(match, matchRequest, 3, 2, totalFilledPrice, filledSize, totalRejectedPrice, 1, 1, 0, true, true);
	}

	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_ASK_3() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_3", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.ASK,
				new BigInteger("0"), 0, new BigInteger("0"), 0, new BigInteger("3"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("0");
		BigInteger filledSize = new BigInteger("0");
		BigInteger totalRejectedPrice = new BigInteger("3");
		mainCheck(match, matchRequest, 3, 3, totalFilledPrice, filledSize, totalRejectedPrice, 0, 0, 1, true, false);
	}

	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_ASK_4() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_4", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.ASK,
				new BigInteger("0"), 0, new BigInteger("0"), 0, new BigInteger("5"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("4");
		BigInteger filledSize = new BigInteger("1");
		BigInteger totalRejectedPrice = new BigInteger("1");
		mainCheck(match, matchRequest, 3, 3, totalFilledPrice, filledSize, totalRejectedPrice, 1, 1, 1, true, true);
	}

	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_ASK_5() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_5", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.ASK,
				new BigInteger("0"), 0, new BigInteger("0"), 0, new BigInteger("7"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("4");
		BigInteger filledSize = new BigInteger("1");
		BigInteger totalRejectedPrice = new BigInteger("3");
		mainCheck(match, matchRequest, 3, 3, totalFilledPrice, filledSize, totalRejectedPrice, 1, 1, 1, true, true);
	}

	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_ASK_6() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_6", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.ASK,
				new BigInteger("0"), 0, new BigInteger("0"), 0, new BigInteger("12"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("12");
		BigInteger filledSize = new BigInteger("3");
		BigInteger totalRejectedPrice = new BigInteger("0");
		mainCheck(match, matchRequest, 3, 3, totalFilledPrice, filledSize, totalRejectedPrice, 1, 1, 0, true, true);
	}

	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_ASK_7() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_7", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.ASK,
				new BigInteger("0"), 0, new BigInteger("0"), 0, new BigInteger("48"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("0");
		BigInteger filledSize = new BigInteger("0");
		BigInteger totalRejectedPrice = new BigInteger("48");
		mainCheck(match, matchRequest, 3, 3, totalFilledPrice, filledSize, totalRejectedPrice, 0, 0, 1, true, false);
	}

	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_ASK_8() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_8", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.ASK,
				new BigInteger("0"), 0, new BigInteger("0"), 0, new BigInteger("47"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("0");
		BigInteger filledSize = new BigInteger("0");
		BigInteger totalRejectedPrice = new BigInteger("47");
		mainCheck(match, matchRequest, 3, 3, totalFilledPrice, filledSize, totalRejectedPrice, 0, 0, 1, true, false);
	}

	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_ASK_9() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_9", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.ASK,
				new BigInteger("0"), 1, new BigInteger("0"), 0, new BigInteger("45"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("45");
		BigInteger filledSize = new BigInteger("13");
		BigInteger totalRejectedPrice = new BigInteger("0");
		mainCheck(match, matchRequest, 3, 1, totalFilledPrice, filledSize, totalRejectedPrice, 3, 1, 0, true, true);
	}


	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_ASK_10() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_10", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.ASK,
				new BigInteger("0"), 3, new BigInteger("0"), 0, new BigInteger("47"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("47");
		BigInteger filledSize = new BigInteger("15");
		BigInteger totalRejectedPrice = new BigInteger("0");
		mainCheck(match, matchRequest, 3, 0, totalFilledPrice, filledSize, totalRejectedPrice, 4, 1, 0, true, true);
	}

	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_ASK_11() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_11", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.ASK,
				new BigInteger("0"), 1, new BigInteger("0"), 0, new BigInteger("5"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("4");
		BigInteger filledSize = new BigInteger("1");
		BigInteger totalRejectedPrice = new BigInteger("1");
		mainCheck(match, matchRequest, 3, 3, totalFilledPrice, filledSize, totalRejectedPrice, 1, 1, 1, true, true);
	}

	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_ASK_12() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_12", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.ASK,
				new BigInteger("0"), 1, new BigInteger("0"), 0, new BigInteger("7"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("4");
		BigInteger filledSize = new BigInteger("1");
		BigInteger totalRejectedPrice = new BigInteger("3");
		mainCheck(match, matchRequest, 3, 3, totalFilledPrice, filledSize, totalRejectedPrice, 1, 1, 1, true, true);
	}

	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_ASK_13() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_13", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.ASK,
				new BigInteger("0"), 1, new BigInteger("0"), 0, new BigInteger("24"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("24");
		BigInteger filledSize = new BigInteger("6");
		BigInteger totalRejectedPrice = new BigInteger("0");
		mainCheck(match, matchRequest, 3, 2, totalFilledPrice, filledSize, totalRejectedPrice, 1, 1, 0, true, true);
	}

	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_ASK_14() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_14", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.ASK,
				new BigInteger("0"), 1, new BigInteger("0"), 0, new BigInteger("26"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("24");
		BigInteger filledSize = new BigInteger("6");
		BigInteger totalRejectedPrice = new BigInteger("2");
		mainCheck(match, matchRequest, 3, 2, totalFilledPrice, filledSize, totalRejectedPrice, 1, 1, 1, true, true);
	}

	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_ASK_15() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_15", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.ASK,
				new BigInteger("0"), 1, new BigInteger("0"), 0, new BigInteger("27"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("27");
		BigInteger filledSize = new BigInteger("7");
		BigInteger totalRejectedPrice = new BigInteger("0");
		mainCheck(match, matchRequest, 3, 2, totalFilledPrice, filledSize, totalRejectedPrice, 2, 1, 0, true, true);
	}

	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_ASK_16() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_16", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.ASK,
				new BigInteger("0"), 1, new BigInteger("0"), 0, new BigInteger("28"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("27");
		BigInteger filledSize = new BigInteger("7");
		BigInteger totalRejectedPrice = new BigInteger("1");
		mainCheck(match, matchRequest, 3, 2, totalFilledPrice, filledSize, totalRejectedPrice, 2, 1, 1, true, true);
	}

	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_ASK_17() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_17", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.ASK,
				new BigInteger("0"), 3, new BigInteger("0"), 0, new BigInteger("44"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("42");
		BigInteger filledSize = new BigInteger("12");
		BigInteger totalRejectedPrice = new BigInteger("2");
		mainCheck(match, matchRequest, 3, 2, totalFilledPrice, filledSize, totalRejectedPrice, 3, 1, 1, true, true);
	}

	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_ASK_18() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_18", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.ASK,
				new BigInteger("0"), 10, new BigInteger("0"), 0, new BigInteger("46"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("46");
		BigInteger filledSize = new BigInteger("14");
		BigInteger totalRejectedPrice = new BigInteger("0");
		mainCheck(match, matchRequest, 3, 1, totalFilledPrice, filledSize, totalRejectedPrice, 4, 1, 0, true, true);
	}

	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_ASK_20() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_20", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.ASK,
				new BigInteger("0"), 1, new BigInteger("0"), 0, new BigInteger("3"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("0");
		BigInteger filledSize = new BigInteger("0");
		BigInteger totalRejectedPrice = new BigInteger("3");
		mainCheck(match, matchRequest, 3, 3, totalFilledPrice, filledSize, totalRejectedPrice, 0, 0, 1, true, true);
	}

	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_ASK_21() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_21", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.ASK,
				new BigInteger("0"), 2, new BigInteger("0"), 0, new BigInteger("80"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("0");
		BigInteger filledSize = new BigInteger("0");
		BigInteger totalRejectedPrice = new BigInteger("80");
		mainCheck(match, matchRequest, 3, 3, totalFilledPrice, filledSize, totalRejectedPrice, 0, 0, 1, true, false);
	}

	/**
	 * ask3 8(2)  <br>
	 * ask2 6(3+4)  <br>
	 * ask1 5(6)  <br>
	 * ----------  <br>
	 * bid1 4(6)  <br>
	 * bid2 3(4+3)  <br>
	 * bid3 1(2)  <br>
	 * <br>
	 *
	 */
	@Test
	void FOK_ASK_22() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_22", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.ASK,
				new BigInteger("0"), 2, new BigInteger("0"), 0, new BigInteger("27"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("27");
		BigInteger filledSize = new BigInteger("7");
		BigInteger totalRejectedPrice = new BigInteger("0");
		mainCheck(match, matchRequest, 3, 2, totalFilledPrice, filledSize, totalRejectedPrice, 2, 1, 0, true, true);
	}

	@Test
	void FOK_ASK_23() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_23", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.ASK,
				new BigInteger("0"), 2, new BigInteger("0"), 0, new BigInteger("47"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("0");
		BigInteger filledSize = new BigInteger("0");
		BigInteger totalRejectedPrice = new BigInteger("47");
		mainCheck(match, matchRequest, 3, 3, totalFilledPrice, filledSize, totalRejectedPrice, 0, 0, 1, true, false);
	}

	@Test
	void FOK_ASK_24() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_24", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.ASK,
				new BigInteger("0"), 2, new BigInteger("0"), 0, new BigInteger("100"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("0");
		BigInteger filledSize = new BigInteger("0");
		BigInteger totalRejectedPrice = new BigInteger("100");
		mainCheck(match, matchRequest, 3, 3, totalFilledPrice, filledSize, totalRejectedPrice, 0, 0, 1, true, false);
	}

	@Test
	void FOK_ASK_25() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_25", SYMBOL_ID, PLACE_FOK_ORDER, Constants.Action.ASK,
				new BigInteger("0"), 10, new BigInteger("0"), 0, new BigInteger("1000"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("0");
		BigInteger filledSize = new BigInteger("0");
		BigInteger totalRejectedPrice = new BigInteger("1000");
		mainCheck(match, matchRequest, 3, 3, totalFilledPrice, filledSize, totalRejectedPrice, 0, 0, 1, true, false);
	}

	@Test
	void FOK_ASK_26() {
		int symbolID = SYMBOL_ID - 10;
		MatchRequest matchRequest = new MatchRequest("FOK_A_26", symbolID, PLACE_FOK_ORDER, Constants.Action.ASK,
				new BigInteger("0"), 0, new BigInteger("0"), 0, new BigInteger("500"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("0");
		BigInteger filledSize = new BigInteger("0");
		BigInteger totalRejectedPrice = new BigInteger("500");
		mainCheck(match, matchRequest, 0, 0, totalFilledPrice, filledSize, totalRejectedPrice, null, 0, 0, 1, true,
				false, 2, symbolID, MATCHING_COUNTERPARTY_NOT_FOUND);
	}
}
