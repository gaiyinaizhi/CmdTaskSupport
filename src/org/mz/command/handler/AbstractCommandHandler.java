package org.mz.command.handler;

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

}
