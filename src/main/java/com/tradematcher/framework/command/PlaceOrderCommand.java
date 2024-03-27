package com.tradematcher.framework.command;


import com.tradematcher.framework.command.vo.OrderAction;
import com.tradematcher.framework.command.vo.ResultInfo;
import com.tradematcher.framework.queue.Dispatcher;
import com.tradematcher.util.Constants;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;

/**
 * 下单命令
 * @program: match-engine
 * @author: TradeMatcher
 * @create: 2021-07-20 10:41
 **/
@Data
@Slf4j
public class PlaceOrderCommand extends RequestCommand {
	/**
	 * 公司编号
	 */
	private int companyId;
	/**
	 * 市价单 = 成交总额、限价单 = 单价
	 */
	private String price;
	/**
	 * 市价单 = 数量、限价单 = 数量
	 */
	private String size;
	/**
	 * 委托单编号
	 */
	private long orderId;
	/**
	 * 买卖方向
	 * <p>ASK = 0 市价单size
	 * <p>BID = 1 市价单price
	 */
	private byte action;
	/**
	 * <p> 市价 (限定总成交额,价格距离,全都成交,全都不成交 FOK)
	 * <p> 市价 (限定总成交额,价格距离,部分成交,剩余部分全取消 FAK)
	 * <p> 市价 (限定总成交额,价格距离,部分成交,剩余部分转挂单 MTL)
	 * <p> 限价 (单价+数量=限定总成交额,部分成交,剩余部分转挂单 GTC)
	 */
	private byte orderType;
	/**
	 * 交易产品编号
	 */
	private int symbolId;
	/**
	 * 交易账户编号
	 */
	private long uid;
	/**
	 * 市价单价格距离（自然数)
	 */
	private long reservePrice;
	/**
	 * 单笔最小数量
	 */
	private long minSize;
	/**
	 * 数量小数位（仅转发）
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

	@Override
	public PlaceOrderCommandResult check() {
		PlaceOrderCommandResult placeOrderCommandResult = new PlaceOrderCommandResult(ResultInfo.SUCCESS);

		if (getCompanyId() <= 0 || getOrderId() <= 0 || getOrderType() <= 0 || getSymbolId() <= 0 || getUid() <= 0
			|| getVolumeDigits() < 0 || getTradeType() <= 0) {
			log.warn("PLACEORDER_COMMAND_PARAMETERS_FORMAT_ERROR_1 " + getCompanyId() + " " + getOrderId() + " "
					 + getOrderType() + " " + getSymbolId() + " " + getUid() + " " + getVolumeDigits() + " "
					 + getTradeType());
			placeOrderCommandResult = new PlaceOrderCommandResult(ResultInfo.COMMAND_PARAMETERS_FORMAT_ERROR);
			placeOrderCommandResult.setOrigin(this);
		}

		if (getAction() != OrderAction.ASK.getCode() && getAction() != OrderAction.BID.getCode()) {
			log.warn("PLACEORDER_COMMAND_PARAMETERS_FORMAT_ERROR_2 " + getAction());
			placeOrderCommandResult = new PlaceOrderCommandResult(ResultInfo.COMMAND_PARAMETERS_FORMAT_ERROR);
			placeOrderCommandResult.setOrigin(this);
		}
		if (getOrderType() != Constants.OrderType.MARKET_FOK && getOrderType() != Constants.OrderType.MARKET_FAK
			&& getOrderType() != Constants.OrderType.MARKET_MTL && getOrderType() != Constants.OrderType.LIMIT_GTC) {
			log.warn("PLACEORDER_COMMAND_PARAMETERS_FORMAT_ERROR_3 " + getOrderType());
			placeOrderCommandResult = new PlaceOrderCommandResult(ResultInfo.COMMAND_PARAMETERS_FORMAT_ERROR);
			placeOrderCommandResult.setOrigin(this);
		}

		if ((getOrderType() == Constants.OrderType.MARKET_FOK || getOrderType() == Constants.OrderType.MARKET_FAK
			 || getOrderType() == Constants.OrderType.LIMIT_GTC) && getReservePrice() < 0) {
			log.warn("PLACEORDER_COMMAND_PARAMETERS_FORMAT_ERROR_4 " + getOrderType());
			placeOrderCommandResult = new PlaceOrderCommandResult(ResultInfo.COMMAND_PARAMETERS_FORMAT_ERROR);
			placeOrderCommandResult.setOrigin(this);
		}

		//fak市价单卖，使用size
		BigInteger sizeBI = new BigInteger(size);
		if (getOrderType() == Constants.OrderType.MARKET_FAK && getAction() == OrderAction.ASK.getCode()
			&& sizeBI.compareTo(BigInteger.ZERO) <= 0) {
			log.warn("PLACEORDER_COMMAND_PARAMETERS_FORMAT_ERROR_5 " + getOrderType() + " " + getAction() + " "
					 + getSize());
			placeOrderCommandResult = new PlaceOrderCommandResult(ResultInfo.COMMAND_PARAMETERS_FORMAT_ERROR);
			placeOrderCommandResult.setOrigin(this);
		}

		//fak市价单买，使用price
		BigInteger priceBI = new BigInteger(price);
		if (getOrderType() == Constants.OrderType.MARKET_FAK && getAction() == OrderAction.BID.getCode()
			&& priceBI.compareTo(BigInteger.ZERO) <= 0 && getMinSize() < 0) {
			log.warn("PLACEORDER_COMMAND_PARAMETERS_FORMAT_ERROR_6 " + getOrderType() + " " + getAction() + " "
					 + getPrice() + " " + getMinSize());
			placeOrderCommandResult = new PlaceOrderCommandResult(ResultInfo.COMMAND_PARAMETERS_FORMAT_ERROR);
			placeOrderCommandResult.setOrigin(this);
		}

		if (getOrderType() == Constants.OrderType.LIMIT_GTC && (priceBI.compareTo(BigInteger.ZERO) <= 0
																|| sizeBI.compareTo(BigInteger.ZERO) <= 0)) {
			log.warn("PLACEORDER_COMMAND_PARAMETERS_FORMAT_ERROR_7 " + getOrderType() + " " + getPrice() + " "
					 + getSize());
			placeOrderCommandResult = new PlaceOrderCommandResult(ResultInfo.COMMAND_PARAMETERS_FORMAT_ERROR);
			placeOrderCommandResult.setOrigin(this);
		}

		if ((getOrderType() == Constants.OrderType.MARKET_FOK || getOrderType() == Constants.OrderType.MARKET_MTL)
			&& priceBI.compareTo(BigInteger.ZERO) <= 0) {
			log.warn("PLACEORDER_COMMAND_PARAMETERS_FORMAT_ERROR_8 " + getOrderType() + " " + getPrice());
			placeOrderCommandResult = new PlaceOrderCommandResult(ResultInfo.COMMAND_PARAMETERS_FORMAT_ERROR);
			placeOrderCommandResult.setOrigin(this);
		}

		if ((getOrderType() == Constants.OrderType.MARKET_FOK || getOrderType() == Constants.OrderType.MARKET_FAK
			 || getOrderType() == Constants.OrderType.MARKET_MTL || getOrderType() == Constants.OrderType.LIMIT_GTC
			 || getOrderType() == Constants.OrderType.LIMIT_FOK) && (getVolumeDigits() == 0 || getTradeType() == 0
																	 || getSymbolDecimal() == 0)) {
			log.warn("PLACEORDER_COMMAND_PARAMETERS_FORMAT_ERROR_9 " + getOrderType() + " " + getVolumeDigits() + " "
					 + getTradeType() + " " + getSymbolDecimal());
			placeOrderCommandResult = new PlaceOrderCommandResult(ResultInfo.COMMAND_PARAMETERS_FORMAT_ERROR);
			placeOrderCommandResult.setOrigin(this);
		}

		return placeOrderCommandResult;
	}

	@Override
	public void execute(Dispatcher dispatcher) {
		dispatcher.dispatch(this);
	}
}
