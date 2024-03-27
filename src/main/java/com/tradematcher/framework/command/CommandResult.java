package com.tradematcher.framework.command;

import com.tradematcher.framework.command.vo.ResultInfo;
import com.tradematcher.usecase.MatchRequest;
import lombok.Data;

/**
 * 通用命令结果
 * @program: match-engine
 * @author: TradeMatcher
 * @create: 2021-07-20 11:06
 **/
@Data
public abstract class CommandResult extends BaseCommand {
	private int code;
	private String msg;

	protected CommandResult() {
	}

	protected CommandResult(ResultInfo resultInfo) {
		this.code = resultInfo.getCode();
		this.msg = resultInfo.getMsg();
	}

	public abstract void convertToCommand(MatchRequest matchRequest);
}
