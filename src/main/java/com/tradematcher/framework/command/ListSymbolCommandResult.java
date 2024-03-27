package com.tradematcher.framework.command;

import com.tradematcher.framework.command.vo.CommandInfo;
import com.tradematcher.framework.command.vo.ResultInfo;
import com.tradematcher.usecase.MatchRequest;
import com.tradematcher.util.Constants;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 产品列表命令结果
 * @program: match-engine
 * @author: TradeMatcher
 * @create: 2021-07-26 14:14
 **/
@Data
@NoArgsConstructor
public class ListSymbolCommandResult extends CommandResult {
	private ListSymbolCommand origin;
	private int[] info;

	public ListSymbolCommandResult(ResultInfo resultInfo) {
		super(resultInfo);
		setCommand(CommandInfo.SYMBOL_LIST.getCode() + Constants.Match.RESULT_SUFFIX);
	}

	@Override
	public void convertToCommand(MatchRequest matchRequest) {
		info = matchRequest.getSymbolIDs();
	}
}
