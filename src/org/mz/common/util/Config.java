package org.mz.common.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Config {

	private Properties config = new Properties();

	public Config(String configFileName) {
		Assert.notEmpty(configFileName, "配置文件不能为空！");
		try {
			config.load(new FileInputStream(Common.CONFIG_PATH + configFileName));
		} catch (FileNotFoundException e) {
			Common.error("配置文件不存在！" + Common.CONFIG_PATH + configFileName);
		} catch (IOException e) {
			Common.error("配置读取失败！" + Common.CONFIG_PATH + configFileName);
		}
	}

	public String getProperty(String key, String defaultValue) {
		return config.getProperty(key, defaultValue);
	}

	public String getProperty(String key) {
		return config.getProperty(key, "");
	}
}
