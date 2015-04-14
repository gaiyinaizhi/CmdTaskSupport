package org.mz.command.bean;

import java.lang.reflect.Field;

import org.mz.command.factory.CommandFactory;
import org.mz.command.handler.AbstractCommandHandler;
import org.mz.common.annotation.NotNull;
import org.mz.common.util.Assert;

/**
 * 命令汇总， 参数以双引号为分隔符
 * 
 * @author mengqk
 *
 */
public class Command {

	private String commandName;

	@NotNull
	private Class<? extends AbstractCommandHandler> clazz;

	@NotNull
	private String methodName;

	/** 暂时最大只支持一个, 0或1 */
	private boolean hasParam;

	/** 参数是否为String数组，不是数组就是String字符串 */
	private boolean paramArray;

	/** 数组长度， -1为不校验数组长度 */
	private int acceptArrayLength = -1;

	/** 参数类型， 可无，可有，可String， 可String[]，根据传入参数判断 */
	private boolean dynamicParam = false;

	public Command() {
	}

	public Command(Class<? extends AbstractCommandHandler> cls, String methodName, boolean hasParam,
			boolean paramArray, int acceptArrayLength, boolean returnVoid) {
		super();
		this.clazz = cls;
		this.methodName = methodName;
		this.hasParam = hasParam;
		this.paramArray = paramArray;
		this.acceptArrayLength = acceptArrayLength;
	}

	public static Command createNoParam(Class<? extends AbstractCommandHandler> cls, String methodName) {
		Command desc = new Command();
		desc.clazz = cls;
		desc.methodName = methodName;
		desc.hasParam = false;
		desc.paramArray = false;

		checkNecessary(desc);
		return desc;
	}

	public static Command createStrParam(Class<? extends AbstractCommandHandler> cls, String methodName) {
		Command desc = new Command();
		desc.clazz = cls;
		desc.methodName = methodName;
		desc.hasParam = true;
		desc.paramArray = false;

		checkNecessary(desc);
		return desc;
	}

	public static Command createStrArrParam(Class<? extends AbstractCommandHandler> cls, String methodName,
			int acceptArrLength) {
		Command desc = new Command();
		desc.clazz = cls;
		desc.methodName = methodName;
		desc.hasParam = true;
		desc.paramArray = true;
		desc.acceptArrayLength = acceptArrLength;

		checkNecessary(desc);
		return desc;
	}

	public static Command createStrArrParam(Class<? extends AbstractCommandHandler> cls, String methodName) {
		Command desc = new Command();
		desc.clazz = cls;
		desc.methodName = methodName;
		desc.hasParam = true;
		desc.paramArray = true;
		checkNecessary(desc);
		return desc;
	}

	/**
	 * 动态参数，可有，可String， 可String[]，根据传入参数判断
	 * 
	 * @param cls
	 * @param methodName
	 * @return
	 */
	public static Command createDynParam(Class<? extends AbstractCommandHandler> cls, String methodName) {
		Command desc = new Command();
		desc.clazz = cls;
		desc.methodName = methodName;
		desc.dynamicParam = true;
		desc.hasParam = true;

		checkNecessary(desc);
		return desc;
	}

	/**
	 * 检查必要性
	 * 
	 * @param desc
	 */
	public static void checkNecessary(Command desc) {
		Field[] fields = Command.class.getDeclaredFields();
		NotNull notNull = null;
		Object obj = null;
		try {
			for (Field field : fields) {
				notNull = field.getAnnotation(NotNull.class);
				if (null != notNull) {
					obj = field.get(desc);
					Assert.notNull(obj, field.getName());
				}
			}
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		}
	}

	public String getCommandName() {
		return commandName;
	}

	public void setCommandName(String commandName) {
		this.commandName = commandName;
	}

	public Class<? extends AbstractCommandHandler> getClazz() {
		return clazz;
	}

	public Command setClazz(Class<? extends AbstractCommandHandler> clazz) {
		this.clazz = clazz;
		return this;
	}

	public String getMethodName() {
		return methodName;
	}

	public Command setMethodName(String methodName) {
		this.methodName = methodName;
		return this;
	}

	public boolean isParamArray() {
		return paramArray;
	}

	public Command setParamArray(boolean paramArray) {
		this.paramArray = paramArray;
		return this;
	}

	public boolean hasParam() {
		return hasParam;
	}

	public Command setHasParam(boolean hasParam) {
		this.hasParam = hasParam;
		return this;
	}

	public int getAcceptArrayLength() {
		return acceptArrayLength;
	}

	public Command setAcceptArrayLength(int acceptArrayLength) {
		this.acceptArrayLength = acceptArrayLength;
		return this;
	}

	public boolean isDynamicParam() {
		return dynamicParam;
	}

	public Command setDynamicParam(boolean dynamicParam) {
		this.dynamicParam = dynamicParam;
		return this;
	}

	/**
	 * 执行命令
	 * 
	 * @param instance
	 * @param params
	 * @return
	 */
	public String execute(Object instance, String[] params) {
		return CommandFactory.executeCommand(instance, this, params);
	}
	
	@Override
	public String toString() {
		return "Command [commandName=" + commandName + ", clazz=" + clazz + ", methodName=" + methodName
				+ "]";
	}
	
}
