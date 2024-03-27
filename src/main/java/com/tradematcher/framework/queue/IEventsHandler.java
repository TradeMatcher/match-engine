package com.tradematcher.framework.queue;

import com.tradematcher.entity.Event;
import com.tradematcher.entity.MatchResult;
import com.tradematcher.framework.command.CommandResult;
import com.tradematcher.framework.command.vo.OrderAction;
import com.tradematcher.usecase.MatchRequest;
import lombok.Data;

import java.math.BigInteger;
import java.util.List;

/**
 * 撮合结果处理器接口
 * @program: new-match-engine
 * @author: TradeMatcher
 * @create: 2023-04-24 19:18
 **/
public interface IEventsHandler {
	static Maker convertToMaker(Event event, MatchRequest matchRequest) {
		String makerOrderID = event.getOrderID();
		String[] makerOrderIDInfoArray = makerOrderID.split("_");

		String takerOrderID = matchRequest.getOrderID();
		String[] takerOrderIDInfoArray = takerOrderID.split("_");

		Maker maker = new Maker();
		maker.setOrderId(Long.parseLong(makerOrderIDInfoArray[1]));
		maker.setCompleted(event.getCompleted());
		maker.setPrice(event.getUnitPrice().toString());
		maker.setSymbolId(matchRequest.getSymbolID());
		maker.setUid(Long.parseLong(makerOrderIDInfoArray[2]));
		maker.setVolume(event.getSize().toString());
		maker.setCompanyId(Integer.parseInt(makerOrderIDInfoArray[0]));
		maker.setAction(event.getAction());
		maker.setTakerCompanyId(Integer.parseInt(takerOrderIDInfoArray[0]));
		maker.setTakerOrderId(Long.parseLong(takerOrderIDInfoArray[1]));
		maker.setTimestamp(System.currentTimeMillis());

		maker.setSymbolDecimal(matchRequest.getSymbolDecimal());
		maker.setVolumeDigits(matchRequest.getVolumeDigits());
		maker.setTradeType(matchRequest.getTradeType());
		return maker;
	}

	static Taker convertToTaker(MatchResult matchResult, MatchRequest matchRequest) {
		String takerOrderID = matchRequest.getOrderID();
		String[] takerOrderIDInfoArray = takerOrderID.split("_");

		Taker taker = new Taker();
		taker.setOrderId(Long.parseLong(takerOrderIDInfoArray[1]));
		taker.setCompleted(matchResult.isTakerCompleted());
		taker.setSymbolId(matchRequest.getSymbolID());
		taker.setUid(Long.parseLong(takerOrderIDInfoArray[2]));
		taker.setAction(matchRequest.getAction());
		taker.setTotalVolume(matchResult.getFilledSize().toString());
		taker.setTotalPrice(matchResult.getTotalFilledPrice().toString());
		taker.setCompanyId(Integer.parseInt(takerOrderIDInfoArray[0]));
		taker.setTimestamp(System.currentTimeMillis());
		taker.setSymbolDecimal(matchRequest.getSymbolDecimal());
		taker.setVolumeDigits(matchRequest.getVolumeDigits());
		taker.setTradeType(matchRequest.getTradeType());
		return taker;
	}

	static Reject convertToReject(Event event, MatchRequest matchRequest) {
		String takerOrderID = matchRequest.getOrderID();
		String[] takerOrderIDInfoArray = takerOrderID.split("_");

		BigInteger totalPrice = event.getTotalPrice();
		BigInteger size = event.getSize();
		Reject reject = new Reject();
		reject.setOrderId(Long.parseLong(takerOrderIDInfoArray[1]));
		if (totalPrice != null) {
			reject.setRejectedAmount(totalPrice.toString());
		}
		reject.setSymbolId(matchRequest.getSymbolID());
		reject.setUid(Long.parseLong(takerOrderIDInfoArray[2]));
		reject.setCompanyId(Integer.parseInt(takerOrderIDInfoArray[0]));
		reject.setAction(matchRequest.getAction());
		if (size != null) {
			reject.setRejectedSize(size.toString());
		}
		reject.setTimestamp(System.currentTimeMillis());
		reject.setSymbolDecimal(matchRequest.getSymbolDecimal());
		reject.setVolumeDigits(matchRequest.getVolumeDigits());
		reject.setTradeType(matchRequest.getTradeType());
		return reject;
	}

	void commandResult(CommandResult commandResult);

	void marketEvent(OrderBookInfo orderBookInfo);

	void takerEvent(IEventsHandler.Taker taker);

	void makerEvent(IEventsHandler.Maker maker);

	void rejectEvent(IEventsHandler.Reject reject);

	@Data
	class OrderBookInfo {
		public final int symbol;
		public final int symbolDecimal;
		public final int volumeDigits;
		public final List<RecordInfo> asks;
		public final List<RecordInfo> bids;
		public final long timestamp;
	}

	@Data
	class RecordInfo {
		public final String price;
		public final String volume;
		public final int orders;
	}

	/**
	 * Maker撮合结果信息
	 */
	@Data
	class Maker {
		private String event = "match_maker";
		/**
		 * 事件编号
		 */
		private String eventId;
		/**
		 * 产品编号
		 */
		private int symbolId;
		/**
		 * 订单编号
		 */
		private long orderId;
		/**
		 * 公司编号
		 */
		private int companyId;
		/**
		 * 用户编号
		 */
		private long uid;
		/**
		 * 订单是否完成
		 */
		private boolean completed;
		/**
		 * 单价
		 */
		private String price;
		/**
		 * 数量
		 */
		private String volume;

		/**
		 * 买卖方向
		 */
		private byte action;
		/**
		 * taker订单编号
		 */
		private long takerOrderId;
		/**
		 * taker公司编号
		 */
		private long takerCompanyId;
		/**
		 * 资产小数位
		 */
		private int symbolDecimal;
		/**
		 * 时间戳
		 */
		private long timestamp;
		/**
		 * 数量小数位
		 */
		private int volumeDigits;
		/**
		 * 成交模式
		 */
		private int tradeType;
	}

	/**
	 * Taker撮合结果信息　
	 */
	@Data
	class Taker {
		private String event = "match_taker";
		/**
		 * 事件编号
		 */
		private String eventId;
		/**
		 * 产品编号
		 */
		private int symbolId;
		/**
		 * 总撮合量
		 */
		private String totalVolume;
		/**
		 * 总撮合价
		 */
		private String totalPrice;
		/**
		 * 订单编号
		 */
		private long orderId;
		/**
		 * 用户编号
		 */
		private long uid;
		/**
		 * 公司编号
		 */
		private int companyId;
		/**
		 * 买卖方向
		 */
		private byte action;
		/**
		 * 是否完成
		 */
		private boolean completed;
		/**
		 * 资产小数位
		 */
		private int symbolDecimal;
		/**
		 * 时间戳
		 */
		private long timestamp;
		/**
		 * 数量小数位
		 */
		private int volumeDigits;
		/**
		 * 成交模式
		 */
		private int tradeType;
	}

	/**
	 * 撮合拒绝的金额结果信息
	 */
	@Data
	class Reject {
		private String event = "match_reject";
		/**
		 * 事件编号
		 */
		private String eventId;
		/**
		 * 产品编号
		 */
		private int symbolId;
		/**
		 * 订单编号
		 */
		private long orderId;
		/**
		 * 公司编号
		 */
		private int companyId;
		/**
		 * 用户编号
		 */
		private long uid;
		/**
		 * 拒绝金额
		 */
		private String rejectedAmount;
		/**
		 * 拒绝数量
		 */
		private String rejectedSize;

		/**
		 * 买卖方向
		 */
		private byte action;
		/**
		 * 资产小数位
		 */
		private int symbolDecimal;
		/**
		 * 时间戳
		 */
		private long timestamp;
		/**
		 * 数量小数位
		 */
		private int volumeDigits;
		/**
		 * 成交模式
		 */
		private int tradeType;
	}

	@Data
	class RejectEvent {
		public final int symbol;
		public final long rejectedVolume;
		public final long price;
		public final long orderId;
		public final long uid;
		public final long timestamp;
		public final long rejectedAmount;
		public final long rejectedSize;
		public final String accessKey;
		public final int companyId;
		public final byte orderAction;
		public final int symbolDecimal;
		public final int volumeDigits;
		public final int tradeType;
	}

	@Data
	class Trade {
		public final long makerOrderId;
		public final long makerUid;
		public final boolean makerOrderCompleted;
		public final long price;
		public final long volume;
		public final String accessKey;
		public final int companyId;
		public final byte orderAction;
		public final int symbolDecimal;
		public final int volumeDigits;
		public final int tradeType;
	}

	@Data
	class TradeEvent {
		public final int symbol;
		public final long totalVolume;
		public final long takerOrderId;
		public final long takerUid;
		public final OrderAction takerAction;
		public final boolean takeOrderCompleted;
		public final long timestamp;
		public final List<Trade> trades;
		public final String accessKey;
		public final int companyId;
		public final int volumeDigits;
		public final int tradeType;
	}
}
