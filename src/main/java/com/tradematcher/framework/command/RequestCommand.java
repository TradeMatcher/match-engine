package com.tradematcher.framework.command;

import com.tradematcher.framework.command.vo.CommandInfo;
import com.tradematcher.framework.command.vo.ResultInfo;
import com.tradematcher.framework.exception.MEException;
import com.tradematcher.framework.queue.Dispatcher;
import com.tradematcher.util.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Data;

/**
 * 基础命令
 * @program: match-engine
 * @author: TradeMatcher
 * @create: 2021-07-20 11:05
 **/
@Data
public abstract class RequestCommand extends BaseCommand {
	private static final Gson GSON = new Gson();
	private String accessKey;

	public static RequestCommand getCommandByJson(JsonObject jsonObject, String type) {
		String command = jsonObject.get(Constants.Match.COMMAND_CONST).getAsString();
		CommandInfo commandInfo = CommandInfo.getCommandInfo(command);
		if (commandInfo == null) {
			throw new MEException(ResultInfo.NO_COMMAND);
		}

		if (!commandInfo.getFlag().contains(type)) {
			throw new MEException(ResultInfo.INVALID_REQUEST);
		}

		return (RequestCommand) GSON.fromJson(jsonObject, commandInfo.getClazz());
	}

	public abstract void execute(Dispatcher dispatcher);

	public abstract CommandResult check();
}
