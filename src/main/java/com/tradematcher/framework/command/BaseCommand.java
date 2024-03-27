package com.tradematcher.framework.command;

import lombok.Data;

/**
 * 撮合命令基础信息
 * @program: match-engine
 * @author: TradeMatcher
 * @create: 2021-07-20 14:39
 **/
@Data
public abstract class BaseCommand {
	/**
	 * 命令名称
	 */
	private String command;
}
