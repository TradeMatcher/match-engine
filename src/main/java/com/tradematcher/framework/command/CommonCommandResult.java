package com.tradematcher.framework.command;

import com.tradematcher.framework.command.vo.ResultInfo;
import com.tradematcher.usecase.MatchRequest;
import com.tradematcher.util.Constants;

/**
 * 默认命令结果
 * @program: match-engine
 * @author: TradeMatcher
 * @create: 2021-07-20 14:58
 **/
public class CommonCommandResult extends CommandResult {
	public CommonCommandResult(ResultInfo resultInfo) {
		super(resultInfo);
		setCommand(Constants.Match.COMMON_COMMAND + Constants.Match.RESULT_SUFFIX);
	}

	@Override
	public void convertToCommand(MatchRequest matchRequest) {
		throw new UnsupportedOperationException();
	}
}
