package cn.sp.ofs.excel.utils;

import java.text.MessageFormat;

/**
* @author 陈嘉镇
* @version 创建时间：2014-6-12 下午2:52:33
* @email benjaminchen555@gmail.com
*/
public class StringUtils {
	private static String[] ILLEGAL_CHAR = new String[] { ",", "/", "\\", "(", ")", "'", ".", ";", ":", "?" };

	private static String[] LEGAL_CHAR = new String[] { "，", "／", "＼", "（", "）", "‘", "。", "；", "：", "？" };

	private static String[] CN_SIGN = new String[] { "，", "／", "＼", "（", "）", "‘", "。", "；", "：", "？" };
	private static String[] EN_SIGN = new String[] { ",", "/", "\\", "(", ")", "'", ".", ";", ":", "?" };

	
	public static String formatMsg(String pattern, String... objs) {
		MessageFormat formatter = new MessageFormat("");
		formatter.applyPattern(pattern);
		String content = formatter.format(objs);
		return content;
	}
	
	
	/**
	 * 替换sql的非法字符
	 * @param s
	 * @return
	 */
	public static String replaceSqlIllegal(String s) {
		if (org.apache.commons.lang3.StringUtils.isEmpty(s))
			return s;
		for (int i = 0; i < ILLEGAL_CHAR.length; i++) {
			if (org.apache.commons.lang3.StringUtils.contains(s, ILLEGAL_CHAR[i]))
				s = org.apache.commons.lang3.StringUtils.replace(s, ILLEGAL_CHAR[i], LEGAL_CHAR[i]);
		}

		return s;
	}

	/**
	 * 替换中文字符 ， ？等
	 * @param s
	 * @return
	 */
	public static String replaceCNSign(String s) {
		if (org.apache.commons.lang3.StringUtils.isEmpty(s))
			return s;
		for (int i = 0; i < CN_SIGN.length; i++) {
			if (org.apache.commons.lang3.StringUtils.contains(s, CN_SIGN[i]))
				s = org.apache.commons.lang3.StringUtils.replace(s, CN_SIGN[i], EN_SIGN[i]);
		}
		return s;
	}
	
}
