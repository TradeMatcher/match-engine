package com.tradematcher.framework.command;

import com.tradematcher.framework.command.vo.CommandInfo;
import com.tradematcher.framework.command.vo.ResultInfo;
import com.tradematcher.usecase.MatchRequest;
import com.tradematcher.util.Constants;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

/**
 * 下单命令结果
 * @program: match-engine
 * @author: TradeMatcher
 * @create: 2021-07-20 11:01
 **/
@Data
@NoArgsConstructor
public class PlaceOrderCommandResult extends CommandResult {

	private PlaceOrderCommand origin;

	public PlaceOrderCommandResult(ResultInfo resultInfo) {
		super(resultInfo);
		setCommand(CommandInfo.PLACE_ORDER.getCode() + Constants.Match.RESULT_SUFFIX);
	}

	@Override
	public void convertToCommand(MatchRequest matchRequest) {
		origin = new PlaceOrderCommand();
		origin.setCommand(CommandInfo.PLACE_ORDER.getCode());
		String orderInfo = matchRequest.getOrderID();
		String[] orderInfoArray = orderInfo.split("_");
		origin.setOrderId(Long.parseLong(orderInfoArray[1]));
		origin.setAction(matchRequest.getAction());
		origin.setCompanyId(Integer.parseInt(orderInfoArray[0]));
		if (matchRequest.getUnitPrice() == null || matchRequest.getUnitPrice().compareTo(BigInteger.ZERO) == 0) {
			origin.setPrice(matchRequest.getTotalPrice().toString());
		} else {
			origin.setPrice(matchRequest.getUnitPrice().toString());
		}
		origin.setReservePrice(matchRequest.getReservePrice());
		if (matchRequest.getSize() != null) {
			origin.setSize(matchRequest.getSize().toString());
		}
		origin.setUid(Long.parseLong(orderInfoArray[2]));
		origin.setSymbolId(matchRequest.getSymbolID());
		origin.setMinSize(matchRequest.getMinSize());

		origin.setVolumeDigits(matchRequest.getVolumeDigits());
		origin.setTradeType(matchRequest.getTradeType());
		origin.setOrderType(matchRequest.getOrderType());
	}
}
