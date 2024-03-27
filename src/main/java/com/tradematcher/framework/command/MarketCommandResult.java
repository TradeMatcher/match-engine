package com.tradematcher.framework.command;

import com.tradematcher.framework.command.vo.CommandInfo;
import com.tradematcher.framework.command.vo.ResultInfo;
import com.tradematcher.usecase.MatchRequest;
import com.tradematcher.util.Constants;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 撮合行情命令结果
 * @program: match-engine
 * @author: TradeMatcher
 * @create: 2021-07-20 15:45
 **/
@Data
@NoArgsConstructor
public class MarketCommandResult extends CommandResult {
	private MarketCommand origin;

	public MarketCommandResult(ResultInfo resultInfo) {
		super(resultInfo);
		setCommand(CommandInfo.MARKET.getCode() + Constants.Match.RESULT_SUFFIX);
	}

	@Override
	public void convertToCommand(MatchRequest matchRequest) {
		origin = new MarketCommand();
		origin.setCommand(CommandInfo.MARKET.getCode());
		origin.setSymbolId(matchRequest.getSymbolID());
		origin.setDepth(matchRequest.getMarketDepth());
	}
}
