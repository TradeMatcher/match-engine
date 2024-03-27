package com.tradematcher.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigInteger;

/**
 * 撮合结果
 * @program: new-match-engine
 * @author: TradeMatcher
 * @create: 2023-04-19 11:45
 **/
@AllArgsConstructor
@ToString
@Setter
public class MatchResult {
	private BigInteger latestPrice;
	private BigInteger totalFilledPrice;
	private BigInteger filledSize;
	@Getter
	private boolean takerCompleted;
	@Getter
	private Event event;
	@Getter
	private Market market;

	public void attachEvent(Event event) {
		Event tail = this.event;
		event.setNext(tail);
		this.event = event;
	}

	public BigInteger getLatestPrice() {
		return latestPrice == null ? null : new BigInteger(latestPrice.toByteArray());
	}

	public BigInteger getTotalFilledPrice() {
		return new BigInteger(totalFilledPrice.toByteArray());
	}

	public BigInteger getFilledSize() {
		return filledSize == null ? null : new BigInteger(filledSize.toByteArray());
	}
}
