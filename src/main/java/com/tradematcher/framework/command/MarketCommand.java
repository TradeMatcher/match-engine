package com.tradematcher.framework.command;

import com.tradematcher.framework.command.vo.ResultInfo;
import com.tradematcher.framework.queue.Dispatcher;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 撮合行情命令
 * 无需主动调用此命令，撮合成功后，会自动发送行情信息
 * @program: match-engine
 * @author: TradeMatcher
 * @create: 2021-07-20 14:39
 **/
@Data
@Slf4j
public class MarketCommand extends RequestCommand {
	private int symbolId;
	private int depth;

	@Override
	public MarketCommandResult check() {
		MarketCommandResult marketCommandResult = new MarketCommandResult(ResultInfo.SUCCESS);
		if (getSymbolId() <= 0 || getDepth() <= 0) {
			log.warn("MARKET_COMMAND_PARAMETERS_FORMAT_ERROR_1 " + getSymbolId() + " " + getDepth());
			marketCommandResult = new MarketCommandResult(ResultInfo.COMMAND_PARAMETERS_FORMAT_ERROR);
			marketCommandResult.setOrigin(this);
		}
		return marketCommandResult;
	}

	@Override
	public void execute(Dispatcher dispatcher) {
		dispatcher.dispatch(this);
	}
}
