package com.tradematcher.entity;

import com.tradematcher.util.Constants;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigInteger;

/**
 * 撮合事件
 * @program: new-match-engine
 * @author: TradeMatcher
 * @create: 2023-04-19 11:49
 **/
@AllArgsConstructor
@Getter
@ToString
public class Event {
	/**
	 * {@link Constants.EventType}
	 */
	private byte type;
	private String orderID;
	private BigInteger unitPrice;
	private BigInteger totalPrice;
	private BigInteger size;
	private Byte action;
	private Boolean completed;
	@Setter
	private Event next;

	public static Event createRejectEvent(String takeOrderID, BigInteger totalRejectedPrice, BigInteger rejectSize,
			byte action) {
		return new Event(Constants.EventType.REJECT, takeOrderID, null, totalRejectedPrice, rejectSize, action, null,
				null);
	}

	public static Event createMakerEvent(String orderID, BigInteger unitPrice, BigInteger filledSize, boolean completed,
			byte action) {
		return new Event(Constants.EventType.MAKER, orderID, unitPrice, null, filledSize, action, completed, null);
	}
}
