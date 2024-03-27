package com.tradematcher.entity;

import com.tradematcher.util.Constants;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Map;

/**
 * 撮合订单簿
 * @program: new-match-engine
 * @author: TradeMatcher
 * @create: 2023-04-17 12:01
 **/
@Slf4j
public final class OrderBookImpl extends OrderBook implements Serializable {
	public OrderBookImpl(Symbol symbol) {
		super(symbol);
	}

	@Override
	public MatchResult matchByTotalPrice(String takerOrderID, final BigInteger totalPrice, final byte action,
			final long reservePrice, final long minSize) {
		byte counterpartAction = Constants.Action.getCounterpartAction(action);
		Order makerOrder = super.getBestPriceOrder(counterpartAction);
		Event firstEvent = null;
		Event tailEvent = null;
		BigInteger latestPrice = null;
		BigInteger totalFilledPrice = BigInteger.ZERO;
		BigInteger filledSize = BigInteger.ZERO;

		boolean matchCompleted = false;

		Order priceBucketTail = makerOrder.getParent().getTail();

		BigInteger worstPrice = super.getWorstPrice(action, reservePrice);

		while (makerOrder != null && super.isPriceValid(makerOrder.getUnitPrice(), worstPrice, action)
			   && makerOrder.getSize().compareTo(makerOrder.getFilledSize()) > 0
			   && (totalPrice.subtract(totalFilledPrice)).compareTo(makerOrder.getUnitPrice()) >= 0
			   && !matchCompleted) {
			//totalAmount.subtract(filledTotalPrice) = 剩余待匹配金额
			BigInteger remainedSize = makerOrder.getSize().subtract(makerOrder.getFilledSize());
			BigInteger makerOrderPrice = makerOrder.getUnitPrice();
			BigInteger filledTotalPriceTemp = totalFilledPrice.add(remainedSize.multiply(makerOrderPrice));
			BigInteger makerFilledSize = BigInteger.ZERO;
			// 最后一个makeOrder 部分成交
			if (filledTotalPriceTemp.compareTo(totalPrice) > 0) {
				BigInteger tempTotalSize = totalPrice.subtract(totalFilledPrice).divide(makerOrder.getUnitPrice());
				BigInteger minSizeBI = BigInteger.valueOf(minSize);

				if (tempTotalSize.compareTo(minSizeBI) >= 0) {
					if (tempTotalSize.compareTo(remainedSize) > 0) {
						//take剩余金额除以单价所得手数大于当前make剩余手数
						tempTotalSize = remainedSize;
					} else {
						//todo 最后一个make需要判断成交数量必须是最小手数的倍数, fok min size = 0
						if (minSizeBI.compareTo(BigInteger.ZERO) == 0) {
							minSizeBI = BigInteger.ONE;
						}
						BigInteger mod = tempTotalSize.mod(minSizeBI);
						tempTotalSize = tempTotalSize.subtract(mod);
					}

					makerOrder.setFilledSize(makerOrder.getFilledSize().add(tempTotalSize));
					makerOrder.getParent().decreaseTotalSize(tempTotalSize);
					makerFilledSize = makerFilledSize.add(tempTotalSize);
					BigInteger tempTotalPrice = makerOrder.getUnitPrice().multiply(tempTotalSize);
					totalFilledPrice = totalFilledPrice.add(tempTotalPrice);
				} else {
					//不够最小成交手数
					log.info("matchByTotalPrice compare order:{} minSize:{} totalSize:{}", takerOrderID, minSizeBI,
							tempTotalSize);
				}

				matchCompleted = true;
			} else {
				// maker 全都成交
				if (filledTotalPriceTemp.compareTo(totalPrice) == 0) {
					matchCompleted = true;
				}
				totalFilledPrice = filledTotalPriceTemp;
				makerOrder.setFilledSize(makerOrder.getFilledSize().add(remainedSize));

				makerOrder.getParent().decrease(remainedSize);

				makerFilledSize = makerFilledSize.add(remainedSize);
			}

			boolean makerCompleted = makerOrder.getSize().compareTo(makerOrder.getFilledSize()) == 0;

			Order makerOrderTmp = makerOrder.getPrevious();
			if (!matchCompleted && makerOrderTmp != null) {
				boolean priceMatchTemp = super.isPriceValid(makerOrderTmp.getUnitPrice(), worstPrice, action);
				if (!priceMatchTemp)
					matchCompleted = true;
			}

			Event currentEvent = Event.createMakerEvent(makerOrder.getOrderID(), makerOrder.getUnitPrice(),
					makerFilledSize, makerCompleted, makerOrder.getAction());

			filledSize = filledSize.add(makerFilledSize);

			if (tailEvent == null) {
				firstEvent = currentEvent;
			} else {
				tailEvent.setNext(currentEvent);
			}
			tailEvent = currentEvent;

			latestPrice = makerOrder.getUnitPrice();

			if (makerCompleted) {
				if (makerOrder == priceBucketTail) {
					final Map<BigInteger, Bucket> buckets = getBuckets(makerOrder.getAction());
					buckets.remove(makerOrder.getUnitPrice());
					if (makerOrder.getPrevious() != null) {
						priceBucketTail = makerOrder.getPrevious().getParent().getTail();
					}
				}
				super.orderIDIndex.remove(makerOrder.getOrderID());

				makerOrder = makerOrder.getPrevious();

				if (makerOrder != null) {
					makerOrder.setNext(null);
				}
			}
		}

		super.updateBestOrder(counterpartAction, makerOrder);

		return new MatchResult(latestPrice, totalFilledPrice, filledSize, totalPrice.compareTo(totalFilledPrice) == 0,
				firstEvent, getMarkets());
	}

	@Override
	public boolean tryMatchByTotalPrice(final BigInteger totalPrice, final byte action, final long reservePrice,
			final long minSize) {
		byte counterpartAction = Constants.Action.getCounterpartAction(action);
		Order bestPriceOrder = getBestPriceOrder(counterpartAction);
		// 订单预计最差单价
		BigInteger worstPrice = getWorstPrice(action, reservePrice);
		// 订单预计购买总金额
		BigInteger bestTotalAmount = BigInteger.ZERO;
		// 统计限定最差单价总共能够买金额
		while (bestPriceOrder != null && super.isPriceValid(bestPriceOrder.getUnitPrice(), worstPrice, action)
			   && bestTotalAmount.compareTo(totalPrice) < 0) {
			Bucket bucket = bestPriceOrder.getParent();
			bestTotalAmount = bestTotalAmount.add(bucket.getTotalSize().multiply(bestPriceOrder.getUnitPrice()));
			bestPriceOrder = bucket.getTail().getPrevious();
		}
		return totalPrice.compareTo(bestTotalAmount) <= 0;
	}

	@Override
	public MatchResult matchAskBySize(String takerOrderID, final BigInteger wantedSize) {
		Event firstEvent = null;
		Event tailEvent = null;
		BigInteger latestPrice = null;
		BigInteger totalFilledPrice = BigInteger.ZERO;
		BigInteger filledSize = BigInteger.ZERO;

		Order priceBucketTail = bestBidOrder.getParent().getTail();
		while (bestBidOrder != null && wantedSize.compareTo(filledSize) != 0) {
			BigInteger makerFilledSize;
			BigInteger remainedSize = bestBidOrder.getSize().subtract(bestBidOrder.getFilledSize());

			if (remainedSize.compareTo(wantedSize.subtract(filledSize)) > 0) {
				BigInteger diff = wantedSize.subtract(filledSize);
				filledSize = filledSize.add(diff);
				bestBidOrder.setFilledSize(bestBidOrder.getFilledSize().add(diff));

				bestBidOrder.getParent().decreaseTotalSize(diff);

				makerFilledSize = diff;
			} else {
				filledSize = filledSize.add(remainedSize);

				bestBidOrder.setFilledSize(bestBidOrder.getFilledSize().add(remainedSize));

				bestBidOrder.getParent().decrease(remainedSize);

				makerFilledSize = remainedSize;
			}

			boolean makerCompleted = bestBidOrder.getSize().compareTo(bestBidOrder.getFilledSize()) == 0;

			latestPrice = bestBidOrder.getUnitPrice();

			Event currentEvent = Event.createMakerEvent(bestBidOrder.getOrderID(), bestBidOrder.getUnitPrice(),
					makerFilledSize, makerCompleted, bestBidOrder.getAction());
			totalFilledPrice = totalFilledPrice.add(bestBidOrder.getUnitPrice().multiply(makerFilledSize));
			if (tailEvent == null) {
				firstEvent = currentEvent;
			} else {
				tailEvent.setNext(currentEvent);
			}
			tailEvent = currentEvent;

			if (makerCompleted) {
				if (bestBidOrder == priceBucketTail) {
					final Map<BigInteger, Bucket> buckets = getBuckets(bestBidOrder.getAction());
					buckets.remove(bestBidOrder.getUnitPrice());

					if (bestBidOrder.getPrevious() != null) {
						priceBucketTail = bestBidOrder.getPrevious().getParent().getTail();
					}
				}
				super.orderIDIndex.remove(bestBidOrder.getOrderID());

				bestBidOrder = bestBidOrder.getPrevious();

				// 断开最后一个订单链接
				if (bestBidOrder != null) {
					bestBidOrder.setNext(null);
				}
			}
		}

		return new MatchResult(latestPrice, totalFilledPrice, filledSize, wantedSize.compareTo(filledSize) == 0,
				firstEvent, getMarkets());
	}

	@Override
	public MatchResult matchByUnitPriceAndSize(String takerOrderID, final BigInteger unitPrice, final BigInteger size,
			final byte action) {
		Event firstEvent = null;
		Event tailEvent = null;
		BigInteger latestPrice = null;

		byte counterpartAction = Constants.Action.getCounterpartAction(action);
		Order makerOrder = getBestPriceOrder(counterpartAction);

		boolean valid = unitPriceValid(action, unitPrice);
		if (!valid) {
			return new MatchResult(latestPrice, BigInteger.ZERO, BigInteger.ZERO, false, null, null);
		}

		BigInteger totalFilledPrice = BigInteger.ZERO;
		BigInteger filledSize = BigInteger.ZERO;

		Order priceBucketTail = makerOrder.getParent().getTail();

		while (makerOrder != null && super.isPriceValid(makerOrder.getUnitPrice(), unitPrice, action)
			   && makerOrder.getSize().compareTo(makerOrder.getFilledSize()) > 0 && (size.compareTo(filledSize)) > 0) {
			BigInteger takerRemainedSize = size.subtract(filledSize);
			BigInteger makerRemainedSize = makerOrder.getSize().subtract(makerOrder.getFilledSize());

			BigInteger makerFilledSize;

			if (takerRemainedSize.compareTo(makerRemainedSize) > 0) {
				makerOrder.setFilledSize(makerOrder.getFilledSize().add(makerRemainedSize));
				filledSize = filledSize.add(makerRemainedSize);

				makerFilledSize = makerRemainedSize;
			} else {
				makerOrder.setFilledSize(makerOrder.getFilledSize().add(takerRemainedSize));
				filledSize = filledSize.add(takerRemainedSize);
				makerFilledSize = takerRemainedSize;

			}

			makerOrder.getParent().decreaseTotalSize(makerFilledSize);
			boolean makerCompleted =
					makerOrder.getSize().subtract(makerOrder.getFilledSize()).compareTo(BigInteger.ZERO) == 0;

			Event currentEvent = Event.createMakerEvent(makerOrder.getOrderID(), makerOrder.getUnitPrice(),
					makerFilledSize, makerCompleted, makerOrder.getAction());

			totalFilledPrice = totalFilledPrice.add(makerOrder.getUnitPrice().multiply(makerFilledSize));

			if (tailEvent == null) {
				firstEvent = currentEvent;
			} else {
				tailEvent.setNext(currentEvent);
			}
			tailEvent = currentEvent;

			latestPrice = makerOrder.getUnitPrice();

			if (makerCompleted) {
				makerOrder.getParent().decreaseOrderNumber();

				if (makerOrder == priceBucketTail) {
					Map<BigInteger, Bucket> buckets = getBuckets(makerOrder.getAction());
					buckets.remove(makerOrder.getUnitPrice());

					if (makerOrder.getPrevious() != null) {
						priceBucketTail = makerOrder.getPrevious().getParent().getTail();
					}
				}
				super.orderIDIndex.remove(makerOrder.getOrderID());

				makerOrder = makerOrder.getPrevious();

				if (makerOrder != null) {
					makerOrder.setNext(null);
				}
			}
		}

		updateBestOrder(counterpartAction, makerOrder);

		return new MatchResult(latestPrice, totalFilledPrice, filledSize, size.compareTo(filledSize) == 0, firstEvent,
				getMarkets());
	}

	private boolean unitPriceValid(byte action, BigInteger unitPrice) {
		final boolean isBidAction = action == Constants.Action.BID;
		byte counterpartAction = Constants.Action.getCounterpartAction(action);
		Order makerOrder = getBestPriceOrder(counterpartAction);

		if (makerOrder == null) {
			return false;
		}

		if (isBidAction && makerOrder.getUnitPrice().compareTo(unitPrice) > 0) {
			return false;
		}

		if (!isBidAction && makerOrder.getUnitPrice().compareTo(unitPrice) < 0) {
			return false;
		}

		return true;
	}

	@Override
	public MatchResult matchByUnitPrice(String takerOrderID, BigInteger unitPrice, byte action) {
		BigInteger latestPrice = null;
		Event firstEvent = null;
		Event tailEvent = null;

		byte counterpartAction = Constants.Action.getCounterpartAction(action);
		Order makerOrder = getBestPriceOrder(counterpartAction);

		boolean valid = unitPriceValid(action, unitPrice);
		if (!valid) {
			return new MatchResult(latestPrice, BigInteger.ZERO, BigInteger.ZERO, false, null, null);
		}

		BigInteger totalFilledPrice = BigInteger.ZERO;
		BigInteger filledSize = BigInteger.ZERO;

		Order priceBucketTail = makerOrder.getParent().getTail();
		//无限深度，只匹配价格
		while (makerOrder != null && super.isPriceValid(makerOrder.getUnitPrice(), unitPrice, action)) {
			BigInteger makerRemainedSize = makerOrder.getSize().subtract(makerOrder.getFilledSize());

			makerOrder.setFilledSize(makerOrder.getFilledSize().add(makerRemainedSize));
			filledSize = filledSize.add(makerRemainedSize);

			makerOrder.getParent().decreaseTotalSize(makerRemainedSize);

			boolean makerCompleted =
					makerOrder.getSize().subtract(makerOrder.getFilledSize()).compareTo(BigInteger.ZERO) == 0;

			Event currentEvent = Event.createMakerEvent(makerOrder.getOrderID(), makerOrder.getUnitPrice(),
					makerRemainedSize, makerCompleted, makerOrder.getAction());

			totalFilledPrice = totalFilledPrice.add(makerOrder.getUnitPrice().multiply(makerRemainedSize));

			if (tailEvent == null) {
				firstEvent = currentEvent;
			} else {
				tailEvent.setNext(currentEvent);
			}
			tailEvent = currentEvent;

			latestPrice = makerOrder.getUnitPrice();

			if (makerCompleted) {
				makerOrder.getParent().decreaseOrderNumber();

				if (makerOrder == priceBucketTail) {
					Map<BigInteger, Bucket> buckets = getBuckets(makerOrder.getAction());
					buckets.remove(makerOrder.getUnitPrice());

					if (makerOrder.getPrevious() != null) {
						priceBucketTail = makerOrder.getPrevious().getParent().getTail();
					}
				}
				super.orderIDIndex.remove(makerOrder.getOrderID());

				makerOrder = makerOrder.getPrevious();

				if (makerOrder != null) {
					makerOrder.setNext(null);
				}
			}
		}

		updateBestOrder(action, makerOrder);

		return new MatchResult(latestPrice, totalFilledPrice, filledSize, true, firstEvent, getMarkets());
	}

	@Override
	public void cancelOrder(Order order) {
		super.orderIDIndex.remove(order.getOrderID());

		final Bucket bucket = order.getParent();

		bucket.decrease(order.getSize().subtract(order.getFilledSize()));

		if (bucket.getTail() == order) {
			if (order.getNext() == null || order.getNext().getParent() != bucket) {
				Map<BigInteger, Bucket> buckets = getBuckets(order.getAction());
				buckets.remove(order.getUnitPrice());
			} else {
				bucket.setTail(order.getNext());
			}
		}

		if (order.getNext() != null) {
			order.getNext().setPrevious(order.getPrevious());
		}
		if (order.getPrevious() != null) {
			order.getPrevious().setNext(order.getNext());
		}

		if (order == bestAskOrder) {
			bestAskOrder = order.getPrevious();
		} else if (order == bestBidOrder) {
			bestBidOrder = order.getPrevious();
		}
	}
}
