package com.tradematcher.usecase;

import com.tradematcher.entity.MatchResult;
import lombok.*;

import java.math.BigInteger;

/**
 * 撮合请求
 * @program: new-match-engine
 * @author: TradeMatcher
 * @create: 2023-04-18 23:37
 **/
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class MatchRequest {
	private String orderID;
	private int symbolID;
	private String instructionType;
	private byte action;
	private BigInteger unitPrice;
	private long reservePrice;
	private BigInteger size;
	private long minSize;
	private BigInteger totalPrice;
	private byte orderType;
	private short code;

	private long orderTimestamp;

	private BigInteger filledSize;

	private int marketDepth;

	private long snapshot;

	private int[] symbolIDs;

	private MatchResult matchResult;

	/**
	 * 资产小数位（仅转发）
	 */
	private int volumeDigits;
	/**
	 * 成交模式（仅转发）
	 */
	private int tradeType;
	/**
	 * 资产小数位（仅转发）
	 */
	private int symbolDecimal;

	public MatchRequest(String orderID, int symbolID, String instructionType, byte action, BigInteger unitPrice,
			long reservePrice, BigInteger size, long minSize, BigInteger totalPrice, short code) {
		this.orderID = orderID;
		this.symbolID = symbolID;
		this.instructionType = instructionType;
		this.action = action;
		this.unitPrice = unitPrice;
		this.reservePrice = reservePrice;
		this.size = size;
		this.minSize = minSize;
		this.totalPrice = totalPrice;
		this.code = code;
	}

	public String serialize() {
		return orderID + "," + symbolID + "," + instructionType + "," + action + "," + unitPrice + "," + reservePrice
			   + "," + size + "," + minSize + "," + totalPrice + "," + volumeDigits + "," + tradeType + ","
			   + symbolDecimal + "," + orderType;
	}
}
