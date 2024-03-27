package com.tradematcher.framework.command;

import com.tradematcher.framework.command.vo.CommandInfo;
import com.tradematcher.framework.command.vo.ResultInfo;
import com.tradematcher.usecase.MatchRequest;
import com.tradematcher.util.Constants;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单查询命令结果
 * @program: match-engine
 * @author: TradeMatcher
 * @create: 2021-07-26 14:14
 **/
@Data
@NoArgsConstructor
public class QueryOrderCommandResult extends CommandResult {

	/**
	 * 市价单 = 成交总额、限价单 = 单价
	 */
	private String price;
	/**
	 * 市价单 = null、限价单 = 数量
	 */
	private String size;
	/**
	 * 委托单编号
	 */
	private long orderId;
	/**
	 * 买卖方向 ASK = 0, BID = 1
	 */
	private byte action;
	/**
	 * 市价单价格距离 （+/- price)
	 */
	private long reservePrice;

	private long timestamp;
	private String filled;

	private QueryOrderCommand origin;

	public QueryOrderCommandResult(ResultInfo resultInfo) {
		super(resultInfo);
		setCommand(CommandInfo.QUERY_ORDER.getCode() + Constants.Match.RESULT_SUFFIX);
	}

	@Override
	public void convertToCommand(MatchRequest matchRequest) {
		String orderInfo = matchRequest.getOrderID();
		String[] orderInfoArray = orderInfo.split("_");

		origin = new QueryOrderCommand();
		origin.setCompanyId(Integer.parseInt(orderInfoArray[0]));
		origin.setOrderId(Long.parseLong(orderInfoArray[1]));
		origin.setUid(Long.parseLong(orderInfoArray[2]));
		origin.setSymbolId(matchRequest.getSymbolID());

		setPrice(matchRequest.getUnitPrice().toString());
		setSize(matchRequest.getSize().toString());
		setOrderId(Long.parseLong(orderInfoArray[1]));
		setAction(matchRequest.getAction());
		setFilled(matchRequest.getFilledSize().toString());
		setTimestamp(matchRequest.getOrderTimestamp());
	}
}
