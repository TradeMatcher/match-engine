package com.tradematcher.framework.command;

import com.tradematcher.framework.command.vo.ResultInfo;
import com.tradematcher.framework.queue.Dispatcher;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 订单查询命令
 * @program: match-engine
 * @author: TradeMatcher
 * @create: 2021-07-26 14:09
 **/
@Data
@Slf4j
public class QueryOrderCommand extends RequestCommand {

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

	@Override
	public void execute(Dispatcher dispatcher) {
		dispatcher.dispatch(this);
	}

	@Override
	public CommandResult check() {
		QueryOrderCommandResult queryOrderCommandResult = new QueryOrderCommandResult(ResultInfo.SUCCESS);

		if (getCompanyId() <= 0 || getOrderId() <= 0 || getSymbolId() <= 0 || getUid() <= 0) {
			log.warn("QUERYORDER_COMMAND_PARAMETERS_FORMAT_ERROR_1 " + getCompanyId() + " " + getOrderId() + " "
					 + getSymbolId() + " " + getUid());
			queryOrderCommandResult = new QueryOrderCommandResult(ResultInfo.COMMAND_PARAMETERS_FORMAT_ERROR);
			queryOrderCommandResult.setOrigin(this);
		}
		return queryOrderCommandResult;
	}
}
