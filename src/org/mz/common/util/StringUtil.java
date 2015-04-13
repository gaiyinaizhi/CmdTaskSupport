package org.mz.common.util;

public class StringUtil {

	public static boolean isNotEmpty(String str) {
		return null != str && !str.trim().isEmpty();
	}

	public static boolean isEmpty(String str) {
		return null == str || str.trim().isEmpty();
	}

	public static boolean isNotNote(String line) {
		return !(isNotEmpty(line) && line.trim().startsWith("//"));
	}

	public static boolean equals(String cmp, String beCmped) {
		if (null == cmp && null == beCmped) {
			return true;
		} else if ((null == cmp && null != beCmped) || (null != cmp && null == beCmped)) {
			return false;
		}

		return cmp.equals(beCmped);
	}
}
