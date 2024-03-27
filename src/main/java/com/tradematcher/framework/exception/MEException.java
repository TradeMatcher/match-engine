package com.tradematcher.framework.exception;

import com.tradematcher.framework.command.vo.ResultInfo;

/**
 * 撮合异常
 * @program: match-engine
 * @author: TradeMatcher
 * @create: 2021-07-15 16:46
 **/
public class MEException extends RuntimeException {
	private ResultInfo resultInfo;

	public MEException(ResultInfo resultInfo) {
		this.resultInfo = resultInfo;
	}

	public MEException(String message) {
		super(message);
	}

	public MEException(Throwable cause) {
		super(cause);
	}

	public ResultInfo getResultInfo() {
		return resultInfo;
	}
}
