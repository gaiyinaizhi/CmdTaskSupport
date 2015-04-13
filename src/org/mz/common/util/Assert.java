package org.mz.common.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Assert {
	
	private static final Logger logger = LoggerFactory.getLogger(Assert.class);

	public static void lengthEqual(List<?> list, int length) {
		if (list == null || list.size() != length) {
			String errMsg = "参数长度不为" + length;
			logger.error(errMsg);
			throw new RuntimeException(errMsg);
		}
	}
	
	public static void lengthEqual(String[] list, int length) {
		if (list == null || list.length != length) {
			String errMsg = "参数长度不为" + length;
			logger.error(errMsg);
			throw new RuntimeException(errMsg);
		}
	}
	
	public static void hasArrayContent(Object[] arr, String tips) {
		if (null == arr || arr.length == 0) {
			logger.error(tips);
			throw new RuntimeException(tips);
		}
	}
	
	public static void notEmpty(String str, String tips) {
		if (null == str || str.trim().isEmpty()) {
			logger.error(tips);
			throw new RuntimeException(tips);
		}
	}
	
	public static void notNull(Object obj, String name) {
		if (null == obj) {
			String tip = name + "不能为空！";
			logger.error(tip);
			throw new RuntimeException(tip);
		}
	}

}
