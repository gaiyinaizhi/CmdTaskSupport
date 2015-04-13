package org.mz.command.handler;

import java.util.HashMap;
import java.util.Map;

import org.mz.common.util.Assert;
import org.mz.task.Task;

public class DefaultCommandHandler extends AbstractCommandHandler {

	private Map<String, String> savedParams;

	public DefaultCommandHandler(Task task) {
		super(task);
		savedParams = new HashMap<String, String>();
	}

	/** 退出系统。 */
	public void exit() {
		System.exit(0);
	}

	/**
	 * 休眠
	 * 
	 * @param millSec
	 */
	public void sleep(String millSec) {
		try {
			Thread.sleep(Long.parseLong(millSec));
		} catch (NumberFormatException e) {
		} catch (InterruptedException e) {
		}
	}

	/**
	 * 设置参数
	 * 
	 * @param params
	 */
	public void setParam(String[] params) {
		Assert.lengthEqual(params, 2);
		this.savedParams.put(params[0], params[1]);
	}

	/**
	 * 获取参数
	 * 
	 * @param paramName
	 * @return
	 */
	public String getParam(String paramName) {
		return this.savedParams.get(paramName);
	}

}
