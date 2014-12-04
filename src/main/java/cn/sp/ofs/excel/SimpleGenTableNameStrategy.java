package cn.sp.ofs.excel;
/**
* @author 陈嘉镇
* @version 创建时间：2014-11-25 下午1:46:14
* @email benjaminchen555@gmail.com
*/
public class SimpleGenTableNameStrategy implements GenTableNameStrategy {
	private static final String preFix = "tab";
	private static int tableId;
	@Override
	public String genName() {
		synchronized (SimpleGenTableNameStrategy.class) {
			return preFix+(++tableId);
		}
	}
	@Override
	public String getPerFix() {
		return preFix;
	}

}
