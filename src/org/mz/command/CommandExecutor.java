package org.mz.command;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.mz.command.bean.Command;
import org.mz.command.factory.CommandFactory;
import org.mz.common.util.Assert;
import org.mz.common.util.Common;
import org.mz.common.util.StringUtil;
import org.mz.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandExecutor {

	private static final char PARAM_TOKEN = '"';
	private static final char COMMAND_TOKEN = ' ';

	private static final char LEFT_TOKEN = '(';
	private static final char RIGHT_TOKEN = ')';

	private static final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);

	private static Map<String, Map<Object, Object>> pooledInstances = new HashMap<String, Map<Object, Object>>();

	private static final Object LOCK = new Object();

	private int line;

	private Object task;

	public CommandExecutor(Object task) {
		this.task = task;
	}

	/**
	 * 执行任务文件
	 * 
	 * @param taskFile
	 */
	public void execute(String taskFile) {
		List<String> lines = Common.readTaskFileList(taskFile);
		this.line = 1;
		try {
			for (String lineContent : lines) {
				if (StringUtil.isNotEmpty(lineContent) && StringUtil.isNotNote(lineContent)) {
					executeLine(lineContent);
				}
				this.line++;
			}

		} catch (Exception e) {
			Common.error(new StringBuilder("第").append(this.line).append("行执行出错：").append(e.getMessage())
					.toString(), e);
		}
	}

	/**
	 * 执行一行命令
	 * 
	 * @param line
	 * @return
	 */
	public String executeLine(String line) {
		String tokenString = line.trim();
		if (tokenString.indexOf(LEFT_TOKEN) != -1) {
			return bracketsMatcherExecutor(tokenString, 0);
		} else {
			return executeOneCommand(tokenString);
		}
	}

	/**
	 * 执行一条命令
	 * 
	 * @param cmdString
	 * @return
	 */
	public String executeOneCommand(String cmdString) {
		logger.debug("start execute command line: {}", cmdString);
		String pureCommand = cmdString.trim();
		int spliterIndex = pureCommand.indexOf(COMMAND_TOKEN);
		try {
			if (spliterIndex == -1) {
				Command cmd = CommandFactory.getCommand(pureCommand);
				return cmd.execute(getCmdInstance(cmd), null);
			}

			Command cmd = CommandFactory.getCommand(pureCommand.substring(0, spliterIndex));
			return cmd.execute(getCmdInstance(cmd), loadParams(cmd, pureCommand.substring(spliterIndex + 1)));
		} catch (IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("无效命令: " + cmdString);
		} catch (NullPointerException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("无效命令: " + cmdString);
		}
	}

	/**
	 * 括号匹配执行器
	 * 
	 * @param tokenString
	 * @param startIndex
	 * @return
	 */
	private String bracketsMatcherExecutor(String tokenString, int startIndex) {
		int len = tokenString.length();

		Stack<Integer> leftPosStack = new Stack<Integer>();

		String singleCommand = null;
		char symbol;
		for (int index = 0; index < len; index++) {
			symbol = tokenString.charAt(index);
			switch (symbol) {
			case LEFT_TOKEN:
				leftPosStack.push(index);
				break;
			case RIGHT_TOKEN:
				singleCommand = tokenString.substring(leftPosStack.peek() + 1, index);
				String newTokenString = new StringBuilder(tokenString.substring(0, leftPosStack.peek()))
						.append(PARAM_TOKEN).append(executeOneCommand(singleCommand)).append(PARAM_TOKEN)
						.append(tokenString.substring(index + 1)).toString();
				return bracketsMatcherExecutor(newTokenString, leftPosStack.peek());
			case '"':
				index = skipParamSplitter(tokenString, index);
				break;
			default:
				break;
			}
		}

		return executeOneCommand(tokenString);
	}

	/**
	 * 略过参数扫描， 直接扫描下一个括号
	 * 
	 * @param tokenString
	 * @param index
	 * @return
	 */
	private int skipParamSplitter(String tokenString, int index) {
		index++;
		int len = tokenString.length();
		char symbol;
		for (; index < len; index++) {
			symbol = tokenString.charAt(index);
			if (symbol == '"') {
				return ++index;
			}
		}

		logger.error("格式不匹配！！！");
		throw new RuntimeException("双引号格式不匹配！！！");
	}

	/**
	 * 加载命令的参数
	 * 
	 * @param cmd
	 * @param leftLineString
	 * @return
	 */
	private String[] loadParams(Command cmd, String leftLineString) {
		if (!cmd.hasParam()) {
			return null;
		}
		List<String> params = new ArrayList<String>();
		Stack<Integer> startPosStack = new Stack<Integer>();
		int len = leftLineString.length();
		char symbol;
		for (int index = 0; index < len; index++) {
			symbol = leftLineString.charAt(index);
			if (symbol == '"') {
				if (startPosStack.isEmpty()) {
					startPosStack.push(index);
				} else {
					int start = startPosStack.pop();
					params.add(leftLineString.substring(start + 1, index));
					if (!cmd.isDynamicParam()
							&& (!cmd.isParamArray() || params.size() == cmd.getAcceptArrayLength())) {
						break;
					}
				}
			}
		}

		if (!cmd.isDynamicParam() && cmd.isParamArray() && cmd.getAcceptArrayLength() != -1) {
			Assert.lengthEqual(params, cmd.getAcceptArrayLength());
		} else if (!cmd.isDynamicParam() && !cmd.isParamArray()) {
			Assert.lengthEqual(params, 1);
		}

		return params.toArray(new String[params.size()]);
	}

	/**
	 * 获取命令参数
	 * 
	 * @param cmd
	 * @return
	 */
	public Object getCmdInstance(final Command cmd) {
		String className = cmd.getClazz().getName();
		Object instance = null;
		if (!(pooledInstances.containsKey(className) && pooledInstances.get(className).containsKey(
				task))) {
			synchronized (LOCK) {
				try {
					if (!pooledInstances.containsKey(className)) {
						pooledInstances.put(className, new HashMap<Object, Object>() {
							/** */
							private static final long serialVersionUID = 1L;

							{
								put(task, getInstance(cmd.getClazz()));
							}
						});
					}
					if (!pooledInstances.get(className).containsKey(task)) {
						pooledInstances.get(className).put(task, getInstance(cmd.getClazz()));
					}
				} catch (Exception e) {
					Common.error(new StringBuilder(cmd.getCommandName()).append("执行失败，获取Handler失败！")
							.toString(), e);
				}

			}
		}

		instance = pooledInstances.get(className).get(task);
		Assert.notNull(instance, cmd.getCommandName() + "执行失败,获取Handler失败！命令处理对象");

		return instance;
	}
	
	private Object getInstance(Class<?> cls) throws Exception {
		Constructor<?> construct = cls.getConstructor(Task.class);
		return construct.newInstance(this.task);
	}
	
}
