package com.tradematcher;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 *
 * @program: new-match-engine
 * @author: TradeMatcher
 * @create: 2023-04-27 21:57
 **/
@Suite
@SelectClasses({FAKTest.class, FOKTest.class, MTLTest.class, GTCTest.class, LimitFAKTest.class})
class MatchTest {
}
