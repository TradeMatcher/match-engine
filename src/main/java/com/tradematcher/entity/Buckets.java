package com.tradematcher.entity;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.TreeMap;

/**
 * 档位集合
 * @program: new-match-engine
 * @author: TradeMatcher
 * @create: 2023-04-21 15:45
 **/
public class Buckets extends TreeMap<BigInteger, Bucket> implements Serializable {
	Buckets() {
		super();
	}

	Buckets(Comparator<? super BigInteger> comparator) {
		super(comparator);
	}

	private Buckets(Buckets buckets) {
		super(buckets);
	}

	Buckets getCopyBuckets() {
		return new Buckets(this);
	}
}
