package org.mz.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Common {

	private static final Logger logger = LoggerFactory.getLogger(Common.class);

	public static String ROOT_PATH = (new File("")).getAbsolutePath() + File.separator;
	public static String SCRIPT_PATH = ROOT_PATH + "script" + File.separator;
	public static String DATA_PATH = ROOT_PATH + "data" + File.separator;
	public static String TASK_PATH = ROOT_PATH + "task" + File.separator;
	public static String LOG_PATH = ROOT_PATH + "logs" + File.separator;
	public static String CONFIG_PATH = ROOT_PATH + "config" + File.separator;

	public static final String LINE_SEPARATOR = System.getProperty("line.separator");

	private static ObjectMapper mapper = new ObjectMapper();

	public static void error(String tips) {
		logger.error(tips);
		throw new RuntimeException(tips);
	}

	public static void error(String tips, Throwable e) {
		logger.error(tips, e);
		throw new RuntimeException(tips, e);
	}

	/**
	 * 初始化logback
	 */
	public static void initLogback() {
		// 通过getILoggerFactory()方法得到logger上下文件环境，logback默认获得当前应用的logger context
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory(); // 得到当前应用中logger上下文
		try {
			JoranConfigurator configurator = new JoranConfigurator(); // 定义一个(JoranConfigurator)配置器
			configurator.setContext(context); // 将当前应用的loggercontext的关联到到configurator对象
			context.reset(); // 清除以前的配置器中的所有内容
			configurator.doConfigure(CONFIG_PATH + "logback.xml");
		} catch (JoranException je) {
			logger.error("JoranException occur at:" + je.getMessage()); // 将此处异常也记录到日志
			je.printStackTrace(); // 在控制打印出异常跟踪信息
		}
		// 打印出logger context中的error和供气ing,在此处作用相当于catch中的je.printStackTrace（）;
		StatusPrinter.printInCaseOfErrorsOrWarnings(context);
	}

	public static String getLoggerLevel() {
		String level = "ERROR";
		if (logger instanceof ch.qos.logback.classic.Logger) {
			return ((ch.qos.logback.classic.Logger) logger).getLevel().levelStr;
		}

		return level;
	}

	/**
	 * 读取脚本文件
	 * 
	 * @param file
	 * @return
	 */
	public static List<String> readTaskFileList(String file) {
		List<String> lines = new ArrayList<String>();
		BufferedReader br = null;
		try {
			InputStream is = new FileInputStream(TASK_PATH + file);
			br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String tmpLine = null;
			while ((tmpLine = br.readLine()) != null) {
				lines.add(tmpLine);
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException("文件" + SCRIPT_PATH + file + "未找到！");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("不支持UTF-8格式读取文件！");
		} catch (IOException e) {
			throw new RuntimeException("文件" + SCRIPT_PATH + file + "读取失败！");
		} finally {
			close(br);
		}

		return lines;
	}

	public static String loadFile(String file) {
		try {
			return readFileToString(new FileInputStream(file));
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}

		return null;
	}

	public static String readFileToString(InputStream is) throws IOException {
		BufferedReader br = null;
		StringBuffer fileContent;
		try {
			br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String tmpLine = null;
			fileContent = new StringBuffer(1024);
			while ((tmpLine = br.readLine()) != null) {
				fileContent.append(tmpLine).append(LINE_SEPARATOR);
			}
		} finally {
			close(br);
		}

		logger.debug("the file content is: {}", fileContent);
		return fileContent.toString();
	}

	public static void close(Reader r) {
		if (null != r) {
			try {
				r.close();
			} catch (IOException e) {
				throw new RuntimeException("关闭文件流失败！");
			}
		}
	}

	public static void close(Writer r) {
		if (null != r) {
			try {
				r.close();
			} catch (IOException e) {
				throw new RuntimeException("关闭文件流失败！");
			}
		}
	}

	/**
	 * 判断数据是否为JSON数据格式
	 * 
	 * @param object
	 * @return
	 */
	public static boolean isJsonData(String object) {
		if (StringUtil.isEmpty(object)) {
			return false;
		}
		try {
			mapper.readValue(object, Map.class);
			return true;
		} catch (JsonParseException e) {
		} catch (JsonMappingException e) {
		} catch (IOException e) {
		}

		return false;
	}

	/**
	 * 读取JSON数据
	 * 
	 * @param object
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, String> getJsonData(String object) {
		if (StringUtil.isEmpty(object)) {
			return null;
		}
		try {
			return mapper.readValue(object, Map.class);
		} catch (JsonParseException e) {
		} catch (JsonMappingException e) {
		} catch (IOException e) {
		}

		return null;
	}
}
