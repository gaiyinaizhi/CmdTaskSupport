package org.mz.command.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.mz.command.CommandExecutor;
import org.mz.command.definition.DefaultCommands;
import org.mz.task.Task;

/**
 * 所有命令处理类的父类。
 * 
 * @author mengqk
 */
public abstract class AbstractCommandHandler {

	private Task taskInfo;

	public AbstractCommandHandler(Task task) {
		this.taskInfo = task;
	}

	public Task getTaskInfo() {
		return taskInfo;
	}

	public void setTaskInfo(Task taskInfo) {
		this.taskInfo = taskInfo;
	}
	
	protected Map<String, String> handlePramsInData(Map<String, String> data) {
		if (null == data || data.isEmpty()) {
			return data;
		}
		
		Map<String, String> newData = new HashMap<String, String>(data.size());
		for (Entry<String, String> entry : data.entrySet()) {
			newData.put(getParam(entry.getKey()), getParam(entry.getValue()));
		}
		
		return newData;
	}
	
	protected String getParam(String param) {
		if (StringUtils.isNotEmpty(param) && param.length() > 1 && param.startsWith("$")) {
			return DefaultCommands.GET_PARAM.execute(CommandExecutor.getCmdInstance(DefaultCommands.GET_PARAM, getTaskInfo()), new String[]{param.substring(1)});
		}
		
		return param;
	}

}
