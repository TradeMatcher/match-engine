package com.tradematcher.entity;

import com.tradematcher.util.Constants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Map;

/**
 * 档位桶
 * @program: new-match-engine
 * @author: TradeMatcher
 * @create: 2023-04-17 12:18
 **/
@ToString
@NoArgsConstructor
public class Bucket implements Serializable {
	private BigInteger totalSize = BigInteger.ZERO;
	@Getter
	private int orderNumber = 0;
	@Setter
	@Getter
	private Order tail;

	/**
	 * 获取临近的档位
	 * @param action 买卖
	 * @param unitPrice 单价
	 * @param askBuckets 卖档
	 * @param bidBuckets 买档
	 * @return 临近的档位
	 */
	static Bucket getNearbyBucket(final byte action, final BigInteger unitPrice, final Buckets askBuckets,
			final Buckets bidBuckets) {
		if (action == Constants.Action.ASK) {
			return getAskOrBidBucket(unitPrice, askBuckets);
		} else {
			return getAskOrBidBucket(unitPrice, bidBuckets);
		}
	}

	private static Bucket getAskOrBidBucket(final BigInteger unitPrice, final Buckets buckets) {
		Map.Entry<BigInteger, Bucket> lowerEntry = buckets.lowerEntry(unitPrice);
		Bucket lowerBucket = lowerEntry == null ? null : lowerEntry.getValue();
		Bucket defaultBucket = buckets.isEmpty() ? null : lowerBucket;
		return buckets.getOrDefault(unitPrice, defaultBucket);
	}

	private void increase(final BigInteger size) {
		totalSize = totalSize.add(size);
		orderNumber++;
	}

	/**
	 * 添加订单
	 * @param orderID 订单ID
	 * @param unitPrice 单价
	 * @param size 数量
	 * @param action 买卖
	 * @param timestamp 时间戳
	 * @return 新订单
	 */
	Order addOrder(final String orderID, final BigInteger unitPrice, final BigInteger size, final byte action,
			final long timestamp, int volumeDigits, int tradeType, int symbolDecimal) {
		Order order = new Order(this, orderID, unitPrice, size, action, timestamp, volumeDigits, tradeType,
				symbolDecimal);

		increase(size);

		return order;
	}

	void decrease(final BigInteger size) {
		decreaseTotalSize(size);
		decreaseOrderNumber();
	}

	void decreaseTotalSize(final BigInteger size) {
		this.totalSize = this.totalSize.subtract(size);
	}

	void decreaseOrderNumber() {
		orderNumber--;
	}

	BigInteger getTotalSize() {
		return new BigInteger(totalSize.toByteArray());
	}
}
