package com.tradematcher.entity;

import com.tradematcher.usecase.MatchRequest;
import com.tradematcher.util.Constants;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * 撮合订单簿基类
 * @program: new-match-engine
 * @author: TradeMatcher
 * @create: 2023-04-17 11:51
 **/
public abstract sealed class OrderBook implements Serializable permits OrderBookImpl {
	protected final HashMap<String, Order> orderIDIndex = new HashMap<>();
	protected final Symbol symbol;
	private final Buckets askBuckets = new Buckets();
	private final Buckets bidBuckets = new Buckets(Comparator.reverseOrder());
	protected Order bestBidOrder = null;
	protected Order bestAskOrder = null;

	protected OrderBook(Symbol symbol) {
		this.symbol = symbol;
	}

	/**
	 * 按总价进行撮合买卖
	 * @param takerOrderID 撮合请求订单号
	 * @param totalPrice 带撮合总价
	 * @param action 买卖
	 * @param reservePrice 价格距离
	 * @param minSize 最小数量
	 * @return 撮合结果
	 */
	public abstract MatchResult matchByTotalPrice(String takerOrderID, final BigInteger totalPrice, final byte action,
			final long reservePrice, final long minSize);

	/**
	 * 按总价进行预撮合买卖
	 * @param totalPrice 待撮合总价
	 * @param action 买卖
	 * @param reservePrice 价格距离
	 * @param minSize 最小数量
	 * @return 撮合结果
	 */
	public abstract boolean tryMatchByTotalPrice(final BigInteger totalPrice, final byte action,
			final long reservePrice, final long minSize);

	/**
	 * 按数量进行撮合卖
	 * @param takerOrderID 撮合请求订单号
	 * @param size 待撮合数量
	 * @return 撮合结果
	 */
	public abstract MatchResult matchAskBySize(String takerOrderID, final BigInteger size);

	/**
	 * 按单价和数量进行撮合买卖
	 * @param takerOrderID 撮合请求订单号
	 * @param unitPrice 待撮合单价
	 * @param size 待撮合数量
	 * @param action 买卖
	 * @return 撮合结果
	 */
	public abstract MatchResult matchByUnitPriceAndSize(String takerOrderID, final BigInteger unitPrice,
			final BigInteger size, final byte action);

	/**
	 * 按单价进行无限深度撮合买卖
	 * @param takerOrderID 撮合请求订单号
	 * @param unitPrice 待撮合单价
	 * @param action 买卖
	 * @return 撮合结果
	 */
	public abstract MatchResult matchByUnitPrice(String takerOrderID, final BigInteger unitPrice, final byte action);

	/**
	 * 创建新撮合订单
	 * @param unitPrice 撮合单价
	 * @param size 撮合数量
	 * @param action 买卖
	 * @param orderID 订单号
	 * @return 创建订单后的盘口信息
	 */
	public final Market createOrder(final BigInteger unitPrice, final BigInteger size, final byte action,
			final String orderID, final int volumeDigits, final int tradeType, final int symbolDecimal) {
		Map<BigInteger, Bucket> buckets = getBuckets(action);
		Bucket bucket = buckets.get(unitPrice);
		Order newOrder;
		if (bucket != null) {
			newOrder = bucket.addOrder(orderID, unitPrice, size, action, System.currentTimeMillis(), volumeDigits,
					tradeType, symbolDecimal);

			newOrder.chain(bucket);

			bucket.setTail(newOrder);
		} else {
			bucket = new Bucket();

			newOrder = bucket.addOrder(orderID, unitPrice, size, action, System.currentTimeMillis(), volumeDigits,
					tradeType, symbolDecimal);

			Bucket nearbyBucket = Bucket.getNearbyBucket(action, unitPrice, askBuckets, bidBuckets);

			if (nearbyBucket != null) {
				newOrder.chain(nearbyBucket);
			} else {
				Order bestPriceOrder = getBestPriceOrder(action);
				if (bestPriceOrder != null) {
					bestPriceOrder.setNext(newOrder);
				}

				updateBestOrder(action, newOrder);

				newOrder.setNext(null);
				newOrder.setPrevious(bestPriceOrder);
			}

			bucket.setTail(newOrder);
			buckets.put(unitPrice, bucket);
		}

		orderIDIndex.put(newOrder.getOrderID(), newOrder);

		return getMarkets();
	}

	/**
	 * 取消撮合挂单
	 * @param order 挂单信息
	 */
	public abstract void cancelOrder(final Order order);

	/**
	 * 根据买卖方向获取最优价格订单
	 * @param action 买卖
	 * @return 最优订单
	 */
	protected final Order getBestPriceOrder(byte action) {
		if (action == Constants.Action.ASK) {
			return bestAskOrder;
		} else {
			return bestBidOrder;
		}
	}

	/**
	 * 根据价格距离获取最差撮合价格
	 * @param action 买卖
	 * @param reservePrice 价格距离
	 * @return 最差撮合价格
	 */
	protected final BigInteger getWorstPrice(byte action, long reservePrice) {
		byte counterparty = Constants.Action.getCounterpartAction(action);
		Order bestDirectOrder = getBestPriceOrder(counterparty);
		if (Constants.Action.BID == action) {
			return bestDirectOrder.getUnitPrice().add(BigInteger.valueOf(reservePrice));
		} else {
			return bestDirectOrder.getUnitPrice().subtract(BigInteger.valueOf(reservePrice));
		}
	}

	/**
	 * 判断撮合请求订单号是否存在
	 */
	public void checkExist(final MatchRequest matchRequest) {
		String orderID = matchRequest.getOrderID();
		Order order = getOrderByID(orderID);
		if (order == null) {
			matchRequest.setCode(Constants.Code.VALID_FOR_MATCHING_ENGINE);
		} else {
			matchRequest.setCode(Constants.Code.MATCHING_DUPLICATE_ORDER_ID);
		}
	}

	public Order getOrderByID(String orderID) {
		return orderIDIndex.get(orderID);
	}

	/**
	 * 判断撮合请求是否有对手盘
	 */
	public void checkCounterparty(final MatchRequest matchRequest) {
		byte action = matchRequest.getAction();
		byte counterpartAction = Constants.Action.getCounterpartAction(action);
		Order bestPriceOrder = getBestPriceOrder(counterpartAction);
		if (bestPriceOrder != null) {
			matchRequest.setCode(Constants.Code.VALID_FOR_MATCHING_ENGINE);
		} else {
			matchRequest.setCode(Constants.Code.MATCHING_COUNTERPARTY_NOT_FOUND);
		}
	}

	/**
	 * 根据买卖方向判断盘口是否满足最差撮合价格
	 * @param makerPrice 盘口最优价格
	 * @param worsePrice 最差撮合价格
	 * @param action 买卖
	 * @return true: 满足最差撮合价格
	 */
	protected final boolean isPriceValid(final BigInteger makerPrice, final BigInteger worsePrice, final byte action) {
		return action == Constants.Action.BID ?
				(makerPrice.compareTo(worsePrice) <= 0) :
				(makerPrice.compareTo(worsePrice) >= 0);
	}

	/**
	 * 根据买卖方向获取档位集合
	 * @param action 买卖
	 * @return 档位集合
	 */
	protected final Map<BigInteger, Bucket> getBuckets(final byte action) {
		return action == Constants.Action.BID ? bidBuckets : askBuckets;
	}

	/**
	 * 更新买卖方向最优订单引用
	 * @param action 买卖
	 * @param makerOrder 最优订单
	 */
	protected final void updateBestOrder(final byte action, final Order makerOrder) {
		if (action == Constants.Action.BID) {
			bestBidOrder = makerOrder;
		} else {
			bestAskOrder = makerOrder;
		}
	}

	/**
	 * 返回当前盘口信息
	 * @return 盘口信息
	 */
	public final Market getMarkets() {
		return getMarkets(Constants.MAX_MARKET_DEPTH);
	}

	protected final Market getMarkets(int depth) {
		Market market = new Market();

		market.fillBidMarket(bidBuckets, depth);
		market.fillAskMarket(askBuckets, depth);

		market.setVolumeDigits(this.symbol.volumeDigits());
		market.setSymbolDecimal(this.symbol.symbolDecimal());
		return market;
	}

	public Buckets getCopyAskBuckets() {
		return askBuckets.getCopyBuckets();
	}

	public Buckets getCopyBidBuckets() {
		return bidBuckets.getCopyBuckets();
	}

	public MatchResult getMarketOrderBook(int depth) {
		return new MatchResult(null, BigInteger.ZERO, BigInteger.ZERO, false, null, getMarkets(depth));
	}
}
