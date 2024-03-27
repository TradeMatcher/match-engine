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
import static com.tradematcher.util.Constants.Instruction.PLACE_MTL_ORDER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MTL订单测试
 * @program: new-match-engine
 * @author: TradeMatcher
 * @create: 2023-04-26 21:28
 **/
class MTLTest {

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
	void MTL_BID_1() {
		int symbolID = SYMBOL_ID - 10;
		MatchRequest matchRequest = new MatchRequest("MTL_B_1", symbolID, PLACE_MTL_ORDER, Constants.Action.BID,
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
	void MTL_BID_2() {
		MatchRequest matchRequest = new MatchRequest("MTL_B_2", SYMBOL_ID, PLACE_MTL_ORDER, Constants.Action.BID,
				new BigInteger("0"), 0, new BigInteger("0"), 0, new BigInteger("5"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("5");
		BigInteger filledSize = new BigInteger("1");
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
	void MTL_BID_3() {
		MatchRequest matchRequest = new MatchRequest("MTL_B_2", SYMBOL_ID, PLACE_MTL_ORDER, Constants.Action.BID,
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
	void MTL_BID_4() {
		MatchRequest matchRequest = new MatchRequest("MTL_B_2", SYMBOL_ID, PLACE_MTL_ORDER, Constants.Action.BID,
				new BigInteger("0"), 0, new BigInteger("0"), 0, new BigInteger("10"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("10");
		BigInteger filledSize = new BigInteger("2");
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
	void MTL_BID_5() {
		MatchRequest matchRequest = new MatchRequest("MTL_B_2", SYMBOL_ID, PLACE_MTL_ORDER, Constants.Action.BID,
				new BigInteger("0"), 0, new BigInteger("0"), 0, new BigInteger("11"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("10");
		BigInteger filledSize = new BigInteger("2");
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
	void MTL_BID_6() {
		MatchRequest matchRequest = new MatchRequest("MTL_B_2", SYMBOL_ID, PLACE_MTL_ORDER, Constants.Action.BID,
				new BigInteger("0"), 0, new BigInteger("0"), 0, new BigInteger("14"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("10");
		BigInteger filledSize = new BigInteger("2");
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
	void MTL_BID_7() {
		MatchRequest matchRequest = new MatchRequest("MTL_B_2", SYMBOL_ID, PLACE_MTL_ORDER, Constants.Action.BID,
				new BigInteger("0"), 3, new BigInteger("0"), 0, new BigInteger("95"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("88");
		BigInteger filledSize = new BigInteger("15");
		BigInteger totalRejectedPrice = new BigInteger("7");
		mainCheck(match, matchRequest, 0, 3, totalFilledPrice, filledSize, totalRejectedPrice, 4, 1, 1, true, true);
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
	void MTL_BID_8() {
		MatchRequest matchRequest = new MatchRequest("MTL_B_2", SYMBOL_ID, PLACE_MTL_ORDER, Constants.Action.BID,
				new BigInteger("0"), 3, new BigInteger("0"), 0, new BigInteger("96"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("88");
		BigInteger filledSize = new BigInteger("0");
		BigInteger totalRejectedPrice = new BigInteger("0");
		mainCheck(match, matchRequest, 0, 4, totalFilledPrice, filledSize, totalRejectedPrice, 4, 1, 0, false, true);
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
	void MTL_BID_9() {
		MatchRequest matchRequest = new MatchRequest("MTL_B_2", SYMBOL_ID, PLACE_MTL_ORDER, Constants.Action.BID,
				new BigInteger("0"), 3, new BigInteger("0"), 0, new BigInteger("103"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("88");
		BigInteger filledSize = new BigInteger("0");
		BigInteger totalRejectedPrice = new BigInteger("7");
		mainCheck(match, matchRequest, 0, 4, totalFilledPrice, filledSize, totalRejectedPrice, 4, 1, 1, false, true);
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
	void MTL_BID_10() {
		MatchRequest matchRequest = new MatchRequest("MTL_B_2", SYMBOL_ID, PLACE_MTL_ORDER, Constants.Action.BID,
				new BigInteger("0"), 3, new BigInteger("0"), 0, new BigInteger("121"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("88");
		BigInteger filledSize = new BigInteger("0");
		BigInteger totalRejectedPrice = new BigInteger("1");
		mainCheck(match, matchRequest, 0, 4, totalFilledPrice, filledSize, totalRejectedPrice, 4, 1, 1, false, true);
	}

	@Test
	void MTL_BID_11() {
		int symbolID = SYMBOL_ID - 10;
		MatchRequest matchRequest = new MatchRequest("MTL_B_26", symbolID, PLACE_MTL_ORDER, Constants.Action.BID,
				new BigInteger("0"), 0, new BigInteger("0"), 0, new BigInteger("500"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("0");
		BigInteger filledSize = new BigInteger("0");
		BigInteger totalRejectedPrice = new BigInteger("500");
		mainCheck(match, matchRequest, 0, 0, totalFilledPrice, filledSize, totalRejectedPrice, null, 0, 0, 1, true,
				false, 2, symbolID, MATCHING_COUNTERPARTY_NOT_FOUND);
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
	void MTL_BID_12() {
		MatchRequest matchRequest = new MatchRequest("MTL_B_2", SYMBOL_ID, PLACE_MTL_ORDER, Constants.Action.BID,
				new BigInteger("0"), 0, new BigInteger("0"), 0, new BigInteger("3"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("0");
		BigInteger filledSize = new BigInteger("0");
		BigInteger totalRejectedPrice = new BigInteger("3");
		mainCheck(match, matchRequest, 3, 3, totalFilledPrice, filledSize, totalRejectedPrice, 0, 0, 1, true, true);
	}

	@Test
	void MTL_ASK_1() {
		int symbolID = SYMBOL_ID - 10;
		MatchRequest matchRequest = new MatchRequest("MTL_A_1", symbolID, PLACE_MTL_ORDER, Constants.Action.ASK,
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
	void MTL_ASK_2() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_2", SYMBOL_ID, PLACE_MTL_ORDER, Constants.Action.ASK,
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
	void MTL_ASK_3() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_3", SYMBOL_ID, PLACE_MTL_ORDER, Constants.Action.ASK,
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
	void MTL_ASK_4() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_4", SYMBOL_ID, PLACE_MTL_ORDER, Constants.Action.ASK,
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
	void MTL_ASK_5() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_5", SYMBOL_ID, PLACE_MTL_ORDER, Constants.Action.ASK,
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
	void MTL_ASK_6() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_6", SYMBOL_ID, PLACE_MTL_ORDER, Constants.Action.ASK,
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
	void MTL_ASK_7() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_7", SYMBOL_ID, PLACE_MTL_ORDER, Constants.Action.ASK,
				new BigInteger("0"), 0, new BigInteger("0"), 0, new BigInteger("48"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("24");
		BigInteger filledSize = new BigInteger("6");
		BigInteger totalRejectedPrice = new BigInteger("0");
		mainCheck(match, matchRequest, 4, 2, totalFilledPrice, filledSize, totalRejectedPrice, 1, 1, 0, false, true);
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
	void MTL_ASK_8() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_8", SYMBOL_ID, PLACE_MTL_ORDER, Constants.Action.ASK,
				new BigInteger("0"), 0, new BigInteger("0"), 0, new BigInteger("47"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("24");
		BigInteger filledSize = new BigInteger("0");
		BigInteger totalRejectedPrice = new BigInteger("3");
		mainCheck(match, matchRequest, 4, 2, totalFilledPrice, filledSize, totalRejectedPrice, 1, 1, 1, false, true);
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
	void MTL_ASK_9() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_9", SYMBOL_ID, PLACE_MTL_ORDER, Constants.Action.ASK,
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
	void MTL_ASK_10() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_10", SYMBOL_ID, PLACE_MTL_ORDER, Constants.Action.ASK,
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
	void MTL_ASK_11() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_11", SYMBOL_ID, PLACE_MTL_ORDER, Constants.Action.ASK,
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
	void MTL_ASK_12() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_12", SYMBOL_ID, PLACE_MTL_ORDER, Constants.Action.ASK,
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
	void MTL_ASK_13() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_13", SYMBOL_ID, PLACE_MTL_ORDER, Constants.Action.ASK,
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
	void MTL_ASK_14() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_14", SYMBOL_ID, PLACE_MTL_ORDER, Constants.Action.ASK,
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
	void MTL_ASK_15() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_15", SYMBOL_ID, PLACE_MTL_ORDER, Constants.Action.ASK,
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
	void MTL_ASK_16() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_16", SYMBOL_ID, PLACE_MTL_ORDER, Constants.Action.ASK,
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
	void MTL_ASK_17() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_17", SYMBOL_ID, PLACE_MTL_ORDER, Constants.Action.ASK,
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
	 * 0
	 */
	@Test
	void MTL_ASK_18() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_18", SYMBOL_ID, PLACE_MTL_ORDER, Constants.Action.ASK,
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
	void MTL_ASK_20() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_20", SYMBOL_ID, PLACE_MTL_ORDER, Constants.Action.ASK,
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
	void MTL_ASK_21() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_21", SYMBOL_ID, PLACE_MTL_ORDER, Constants.Action.ASK,
				new BigInteger("0"), 2, new BigInteger("0"), 0, new BigInteger("80"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("45");
		BigInteger filledSize = new BigInteger("0");
		BigInteger totalRejectedPrice = new BigInteger("2");
		mainCheck(match, matchRequest, 4, 1, totalFilledPrice, filledSize, totalRejectedPrice, 3, 1, 1, false, true);
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
	void MTL_ASK_22() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_22", SYMBOL_ID, PLACE_MTL_ORDER, Constants.Action.ASK,
				new BigInteger("0"), 2, new BigInteger("0"), 0, new BigInteger("27"), VALID_FOR_MATCHING_ENGINE);
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
	void MTL_ASK_23() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_23", SYMBOL_ID, PLACE_MTL_ORDER, Constants.Action.ASK,
				new BigInteger("0"), 2, new BigInteger("0"), 0, new BigInteger("47"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("45");
		BigInteger filledSize = new BigInteger("13");
		BigInteger totalRejectedPrice = new BigInteger("2");
		mainCheck(match, matchRequest, 3, 1, totalFilledPrice, filledSize, totalRejectedPrice, 3, 1, 1, true, true);
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
	void MTL_ASK_24() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_24", SYMBOL_ID, PLACE_MTL_ORDER, Constants.Action.ASK,
				new BigInteger("0"), 2, new BigInteger("0"), 0, new BigInteger("100"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("45");
		BigInteger filledSize = new BigInteger("0");
		BigInteger totalRejectedPrice = new BigInteger("1");
		mainCheck(match, matchRequest, 4, 1, totalFilledPrice, filledSize, totalRejectedPrice, 3, 1, 1, false, false);
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
	void MTL_ASK_25() {
		MatchRequest matchRequest = new MatchRequest("FOK_A_25", SYMBOL_ID, PLACE_MTL_ORDER, Constants.Action.ASK,
				new BigInteger("0"), 10, new BigInteger("0"), 0, new BigInteger("1000"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("47");
		BigInteger filledSize = new BigInteger("0");
		BigInteger totalRejectedPrice = new BigInteger("0");
		mainCheck(match, matchRequest, 4, 0, totalFilledPrice, filledSize, totalRejectedPrice, 4, 1, 0, false, true);
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
	void MTL_ASK_26() {
		int symbolID = SYMBOL_ID - 10;
		MatchRequest matchRequest = new MatchRequest("FOK_A_26", symbolID, PLACE_MTL_ORDER, Constants.Action.ASK,
				new BigInteger("0"), 0, new BigInteger("0"), 0, new BigInteger("500"), VALID_FOR_MATCHING_ENGINE);
		match.doAction(matchRequest);

		BigInteger totalFilledPrice = new BigInteger("0");
		BigInteger filledSize = new BigInteger("0");
		BigInteger totalRejectedPrice = new BigInteger("500");
		mainCheck(match, matchRequest, 0, 0, totalFilledPrice, filledSize, totalRejectedPrice, null, 0, 0, 1, true,
				false, 2, symbolID, MATCHING_COUNTERPARTY_NOT_FOUND);
	}
}
