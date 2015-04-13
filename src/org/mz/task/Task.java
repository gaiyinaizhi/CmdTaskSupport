package org.mz.task;

import java.util.Set;

import javax.swing.JOptionPane;

import org.mz.command.definition.CommandDefinition;
import org.mz.command.factory.CommandFactory;
import org.mz.common.util.ClassUtil;
import org.mz.common.util.Common;
import org.mz.common.util.Config;
import org.mz.common.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Task {

	public static final String COMMANDS_DEFINITION_PACKAGE = "org.mz.selenium.command.definition";
	public static final String DEFAULT_CONFIG = "config.properties";

	protected static final Logger logger = LoggerFactory.getLogger(Task.class);

	protected String[] taskFiles;

	protected Frame jframe;

	protected Config config;

	public Task(String configFile) {
		try {
			setup(configFile);
		} catch (RuntimeException e) {
			logger.error("Error setup task information.", e);
			alert(e.getMessage());
		}
	}

	public void setExtProperty(Config config) {
	}

	/**
	 * 加载任务文件
	 * 
	 * @param config
	 * @return
	 */
	public String[] loadTasks(Config config) {
		String fileList = config.getProperty("task.files", null);
		if (null != fileList && !fileList.trim().isEmpty()) {
			return fileList.split(",");
		} else {
			throw new RuntimeException("任务列表不能为空！可通过增加启动参数或修改config/config*.properties中task.files项增加");
		}
	}

	/**
	 * 初始化任务环境
	 * 
	 * @param configFile
	 */
	public void setup(String configFile) {
		// 初始化日志系统
		Common.initLogback();
		// 初始化用户交互系统
		this.jframe = new Frame();
		
		// 初始化配置信息
		if (StringUtil.isEmpty(configFile)) {
			configFile = DEFAULT_CONFIG;
		}
		this.config = new Config(configFile);

		// 加载命令文件
		loadCommands(this.config);
		
		// 加载任务文件
		taskFiles = loadTasks(this.config);
		
		// 设置额外信息
		setExtProperty(this.config);
	}

	public String[] getTaskFiles() {
		return taskFiles;
	}

	public void setTaskFiles(String[] taskFiles) {
		this.taskFiles = taskFiles;
	}

	/**
	 * 加载命令
	 * 
	 * @param config
	 */
	private void loadCommands(Config config) {
		logger.debug("Start load commands.");
		Set<Class<?>> classes = ClassUtil.getClasses(COMMANDS_DEFINITION_PACKAGE);
		loadCommandClasses(classes);
		String extCommands = config.getProperty("task.ext.commands", null);
		if (StringUtil.isNotEmpty(extCommands)) {
			String[] packages = extCommands.split(",");
			for (String pkg : packages) {
				loadCommandClasses(ClassUtil.getClasses(pkg));
			}
		}

		logger.debug("End loading commands.");
	}

	/**
	 * 加载命令class
	 * 
	 * @param classes
	 */
	private void loadCommandClasses(Set<Class<?>> classes) {
		if (null == classes || classes.isEmpty()) {
			return;
		}
		for (Class<?> cls : classes) {
			if (CommandDefinition.class.isAssignableFrom(cls)) {
				CommandFactory.importRegistry(cls);
			}
		}
	}

	/**
	 * 提示框
	 * 
	 * @param message
	 */
	public void alert(String message) {
		JOptionPane.showMessageDialog(jframe, message);
	}

	/**
	 * 确认框
	 * 
	 * @param message
	 * @return
	 */
	public int confirm(String message) {
		return JOptionPane.showConfirmDialog(jframe, message);
	}

	/**
	 * 输入框
	 * 
	 * @param tips
	 * @return
	 */
	public String input(String tips) {
		return JOptionPane.showInputDialog(jframe, tips);
	}

	/**
	 * 获取配置信息
	 * 
	 * @return
	 */
	public Config getConfig() {
		return config;
	}

	public void setConfig(Config config) {
		this.config = config;
	}

	public static void main(String[] args) {
		Set<Class<?>> classes = ClassUtil.getClasses(COMMANDS_DEFINITION_PACKAGE);
		System.out.println(classes);
		for (Class<?> cls : classes) {
			if (CommandDefinition.class.isAssignableFrom(cls)) {
				CommandFactory.importRegistry(cls);
			}
		}
	}
}
