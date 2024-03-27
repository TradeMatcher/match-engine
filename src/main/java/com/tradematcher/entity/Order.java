package com.tradematcher.entity;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * 撮合订单
 * @program: new-match-engine
 * @author: TradeMatcher
 * @create: 2023-04-17 12:19
 **/
@Getter
@RequiredArgsConstructor
@ToString
public final class Order implements Serializable {
	/**
	 * 关联档位桶
	 */
	private final Bucket parent;
	/**
	 * 订单ID
	 */
	private final String orderID;
	/**
	 * 单价
	 */
	private final BigInteger unitPrice;
	/**
	 * 数量
	 */
	private final BigInteger size;
	/**
	 * 买卖
	 */
	private final byte action;
	/**
	 * 下单时间戳
	 */
	private final long timestamp;
	/**
	 * 已成交数量
	 */
	@Setter
	private BigInteger filledSize = BigInteger.ZERO;
	/**
	 * 订单链表前一个
	 */
	@Setter
	private Order previous;
	/**
	 * 订单链表后一个
	 */
	@Setter
	private Order next;

	/**
	 * 数量小数位（仅转发）
	 */
	private final int volumeDigits;
	/**
	 * 成交模式（仅转发）
	 */
	private final int tradeType;
	/**
	 * 资产小数位（仅转发）
	 */
	private final int symbolDecimal;

	void chain(Bucket bucket) {
		Order oldTail = bucket.getTail();
		Order oldTailPrevious = oldTail.getPrevious();

		oldTail.setPrevious(this);
		if (oldTailPrevious != null) {
			oldTailPrevious.setNext(this);
		}

		this.setNext(oldTail);
		this.setPrevious(oldTailPrevious);
	}
}
