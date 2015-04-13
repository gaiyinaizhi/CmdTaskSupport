package org.mz.command.factory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mz.command.bean.Command;
import org.mz.command.definition.CommandDefinition;
import org.mz.common.util.Assert;
import org.mz.common.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandFactory {

	protected static final Logger logger = LoggerFactory.getLogger(CommandFactory.class);

	private static Map<String, Command> registry = new HashMap<String, Command>();;

	private static Set<String> pooledCommandNames = new HashSet<String>();

	public static void addRegistry(Command cmd) {
		Assert.notNull(cmd, "命令对象");
		if (pooledCommandNames.add(cmd.getCommandName())) {
			registry.put(cmd.getCommandName(), cmd);
		} else {
			Common.error(cmd + "命令已存在！");
		}
	}

	public static void importRegistry(Class<?> cls) {
		try {
			Field[] fields = cls.getDeclaredFields();
			int counter = 0;
			Class<Command> importingClass = Command.class;
			Command cmd = null;
			for (Field field : fields) {
				if (field.getType() != importingClass) {
					continue;
				}
				cmd = (Command) field.get(null);
				cmd.setCommandName(field.getName());
				if (pooledCommandNames.contains(cmd.getCommandName())) {
					logger.warn("Command name exists: {}, existed command: {}.", cmd, registry.get(cmd.getCommandName()));
					continue;
				}
				addRegistry(cmd);
				counter++;
			}

			logger.debug("Import commands from class {} finished, {} commands loaded.", cls.getName(), counter);

		} catch (Exception e) {
			Common.error("导入命令失败！ class: " + cls.getName(), e);
		}
	}

	/**
	 * 根据类名导入
	 * 
	 * @param className
	 */
	public static void importRegistry(String className) {

		try {
			Class<?> cls = Class.forName(className);
			if (CommandDefinition.class.isAssignableFrom(cls)) {
				importRegistry(cls);
			} else {
				Common.error("导入命令失败！ 非命令接口");
			}
		} catch (ClassNotFoundException e) {
			Common.error("导入命令失败！" + className + " 类未找到！");
		}
	}

	public static Command getCommand(String commandName) {
		return registry.get(commandName);
	}

	/**
	 * 判断命令是否已经存在
	 * 
	 * @param commandStr
	 * @return
	 */
	public static boolean commandExists(String commandStr) {
		return null != commandStr && pooledCommandNames.contains(commandStr);
	}

	/**
	 * 判断命令枚举对象是否存在
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean commandExists(Object obj) {
		return null != obj && pooledCommandNames.contains(obj.toString());
	}

	/**
	 * 执行命令行
	 * 
	 * @param instance
	 * @param cmd
	 * @param params
	 * @return
	 */
	public static String executeCommand(Object instance, Command cmd, String[] params) {
		if (logger.isDebugEnabled()) {
			logger.debug("Start execute command {} with params {}.", cmd.getCommandName(),
					Arrays.toString(params));
		}
		Object result = null;
		try {
			if (cmd.isDynamicParam()) {
				if (null == params || params.length == 0) {
					result = invokeNoParam(instance, cmd);
				} else if (params.length == 1) {
					result = invokeStringParam(instance, cmd, params);
				} else {
					result = invokeStringArrayParam(instance, cmd, params);
				}
			} else if (cmd.hasParam()) {
				if (cmd.isParamArray()) {
					result = invokeStringArrayParam(instance, cmd, params);
				} else {
					result = invokeStringParam(instance, cmd, params);
				}
			} else {
				result = invokeNoParam(instance, cmd);
			}

		} catch (Exception e) {
			Common.error("execute command " + cmd.getCommandName() + " failed!", e);
		}

		logger.debug("End execute command {} with result {}.", cmd.getCommandName(), result);
		return null == result ? null : result.toString();
	}
	
	public static Object getHandlerInstance(Command cmd) {
		return null;
	}

	/**
	 * 调用无参方法
	 * 
	 * @param instance
	 * @param cmd
	 * @return
	 * @throws Exception
	 */
	private static Object invokeNoParam(Object instance, Command cmd) throws Exception {
		Method invokedMethod = cmd.getClazz().getMethod(cmd.getMethodName());
		return invokedMethod.invoke(instance);
	}

	/**
	 * 调用字符串参数方法
	 * 
	 * @param instance
	 * @param cmd
	 * @param params
	 * @return
	 * @throws Exception
	 */
	private static Object invokeStringParam(Object instance, Command cmd, String[] params) throws Exception {
		Method invokedMethod = cmd.getClazz().getMethod(cmd.getMethodName(), String.class);
		return invokedMethod.invoke(instance, null == params || params.length == 0 ? null : params[0]);
	}

	/**
	 * 调用字符串数组参数方法
	 * 
	 * @param instance
	 * @param cmd
	 * @param params
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("all")
	private static Object invokeStringArrayParam(Object instance, Command cmd, String[] params)
			throws Exception {
		Method invokedMethod = cmd.getClazz().getMethod(cmd.getMethodName(), String[].class);
		return invokedMethod.invoke(instance, params);
	}

}
