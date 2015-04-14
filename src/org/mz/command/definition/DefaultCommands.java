package org.mz.command.definition;

import org.mz.command.bean.Command;
import org.mz.command.handler.DefaultCommandHandler;

public interface DefaultCommands extends CommandDefinition {

	/** 休眠， 接受参数， 毫秒数 */
	Command SLEEP = Command.createStrParam(DefaultCommandHandler.class, "sleep");

	/** 结束整个任务 */
	Command EXIT = Command.createNoParam(DefaultCommandHandler.class, "exit");

	/** 直接调用方法，
	 * 接受参数不限，第一个参数为类名.函数名，之后为方法参数，函数参数接受无参，String，String[]，如：EXE_JAVA_METHOD
	 * "org.mz.AAClass.bbMethod" "param1" "param2" */
	Command EXE_JAVA_METHOD = Command.createDynParam(DefaultCommandHandler.class, "executeJavaMethod");

	/** 设置参数， 参数名，值 */
	Command SET_PARAM = Command.createStrArrParam(DefaultCommandHandler.class, "setParam", 2);

	/** 获取元素属性值， 属性名 */
	Command GET_PARAM = Command.createStrParam(DefaultCommandHandler.class, "getParam");
	
	/** 显示信息 */
	Command SHOW_MESSAGE = Command.createDynParam(DefaultCommandHandler.class, "showMessage");
}
