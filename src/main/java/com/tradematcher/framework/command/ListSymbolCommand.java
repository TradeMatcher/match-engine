package com.tradematcher.framework.command;

import com.tradematcher.framework.command.vo.ResultInfo;
import com.tradematcher.framework.queue.Dispatcher;
import lombok.extern.slf4j.Slf4j;

/**
 * 产品查询命令
 * @program: match-engine
 * @author: TradeMatcher
 * @create: 2021-07-26 14:06
 **/
@Slf4j
public class ListSymbolCommand extends RequestCommand {
	@Override
	public void execute(Dispatcher dispatcher) {
		dispatcher.dispatch(this);
	}

	@Override
	public CommandResult check() {
		return new ListSymbolCommandResult(ResultInfo.SUCCESS);
	}
}
