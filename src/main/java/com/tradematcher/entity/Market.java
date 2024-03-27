package com.tradematcher.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigInteger;
import java.util.SortedMap;


/**
 * 盘口信息
 * @program: new-match-engine
 * @author: TradeMatcher
 * @create: 2023-04-19 11:49
 **/
@Setter
@Getter
@ToString
public class Market {
	private int askDepth;
	private BigInteger[] askUnitPrices;
	private BigInteger[] askSizes;
	private Integer[] askOrderNumbers;
	private int bidDepth;
	private BigInteger[] bidUnitPrices;
	private BigInteger[] bidSizes;
	private Integer[] bidOrderNumbers;
	private int volumeDigits;
	private int symbolDecimal;

	void fillAskMarket(final SortedMap<BigInteger, Bucket> askBuckets, int depth) {
		int size = Integer.min(askBuckets.size(), depth);
		askUnitPrices = new BigInteger[size];
		askSizes = new BigInteger[size];
		askOrderNumbers = new Integer[size];
		askDepth = 0;
		try {
			askBuckets.forEach((unitPrice, bucket) -> {
				if (askDepth < size) {
					askUnitPrices[askDepth] = unitPrice;
					askSizes[askDepth] = bucket.getTotalSize();
					askOrderNumbers[askDepth] = bucket.getOrderNumber();
					askDepth++;
				} else {
					throw new RuntimeException("market data done.");
				}
			});
		} catch (Exception e) {
			// 循环赋值结束
		}
	}

	void fillBidMarket(final Buckets bidBuckets, int depth) {
		int size = Integer.min(bidBuckets.size(), depth);
		bidUnitPrices = new BigInteger[size];
		bidSizes = new BigInteger[size];
		bidOrderNumbers = new Integer[size];
		bidDepth = 0;
		try {
			bidBuckets.forEach((unitPrice, bucket) -> {
				if (bidDepth < size) {
					bidUnitPrices[bidDepth] = unitPrice;
					bidSizes[bidDepth] = bucket.getTotalSize();
					bidOrderNumbers[bidDepth] = bucket.getOrderNumber();
					bidDepth++;
				} else {
					throw new RuntimeException("market data done.");
				}
			});
		} catch (Exception e) {
			// 循环赋值结束
		}
	}
}
