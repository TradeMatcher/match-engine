package com.tradematcher.framework.command;

import com.tradematcher.framework.command.vo.CommandInfo;
import com.tradematcher.framework.command.vo.ResultInfo;
import com.tradematcher.usecase.MatchRequest;
import com.tradematcher.util.Constants;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 撤单命令结果
 * @program: match-engine
 * @author: TradeMatcher
 * @create: 2021-07-20 14:13
 **/
@Data
@NoArgsConstructor
public class CancelOrderCommandResult extends CommandResult {
	/**
	 * 撤单原始命令
	 */
	private CancelOrderCommand origin;


	public CancelOrderCommandResult(ResultInfo resultInfo) {
		super(resultInfo);
		setCommand(CommandInfo.CANCEL_ORDER.getCode() + Constants.Match.RESULT_SUFFIX);
	}

	@Override
	public void convertToCommand(MatchRequest matchRequest) {
		String orderInfo = matchRequest.getOrderID();
		String[] orderInfoArray = orderInfo.split("_");

		origin = new CancelOrderCommand();
		origin.setCommand(CommandInfo.CANCEL_ORDER.getCode());
		origin.setOrderId(Long.parseLong(orderInfoArray[1]));
		origin.setCompanyId(Integer.parseInt(orderInfoArray[0]));
		origin.setUid(Long.parseLong(orderInfoArray[2]));
		origin.setSymbolId(matchRequest.getSymbolID());
		origin.setTradeType(matchRequest.getTradeType());

	}
}
