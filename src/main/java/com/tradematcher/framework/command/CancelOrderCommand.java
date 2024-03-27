package com.tradematcher.framework.command;

import com.tradematcher.framework.command.vo.ResultInfo;
import com.tradematcher.framework.queue.Dispatcher;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 撤单命令
 * @program: match-engine
 * @author: TradeMatcher
 * @create: 2021-07-20 14:13
 **/
@Data
@Slf4j
public class CancelOrderCommand extends RequestCommand {
	/**
	 * 公司编号
	 */
	private int companyId;
	/**
	 * 订单编号
	 */
	private long orderId;
	/**
	 * 客户编号
	 */
	private long uid;
	/**
	 * 产品编号
	 */
	private int symbolId;
	/**
	 * 成交模式（仅转发）
	 */
	private int tradeType;

	@Override
	public CancelOrderCommandResult check() {
		CancelOrderCommandResult cancelOrderCommandResult = new CancelOrderCommandResult(ResultInfo.SUCCESS);
		if (getCompanyId() <= 0 || getOrderId() <= 0 || getUid() <= 0 || getSymbolId() <= 0 || getTradeType() <= 0) {
			log.warn("CANCELORDER_COMMAND_PARAMETERS_FORMAT_ERROR_1 " + getCompanyId() + " " + getOrderId() + " "
					 + getUid() + " " + getSymbolId() + " " + getTradeType());
			cancelOrderCommandResult = new CancelOrderCommandResult(ResultInfo.COMMAND_PARAMETERS_FORMAT_ERROR);
			cancelOrderCommandResult.setOrigin(this);
		}
		return cancelOrderCommandResult;
	}

	@Override
	public void execute(Dispatcher dispatcher) {
		dispatcher.dispatch(this);
	}
}
