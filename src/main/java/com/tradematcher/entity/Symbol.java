package com.tradematcher.entity;

import java.io.Serializable;

/**
 * 撮合产品信息
 * @program: new-match-engine
 * @author: TradeMatcher
 * @create: 2023-04-17 12:03
 **/

public record Symbol(int symbolID, int volumeDigits, int symbolDecimal) implements Serializable {
}
