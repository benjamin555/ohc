package cn.sp.ofs.excel.utils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
* @author 陈嘉镇
* @version 创建时间：2014-7-2 下午3:09:31
* @email benjaminchen555@gmail.com
*/
public class DBUtils {

	public static List<List<String>> convertList(ResultSet rs) throws SQLException {
		List<List<String>> list = new ArrayList<List<String>>();

		ResultSetMetaData md = rs.getMetaData();

		int columnCount = md.getColumnCount(); //Map rowData;  
		
		//表头数据加到list前
		List<String> colData = new ArrayList<String>();
		for (int i = 1; i <= columnCount; i++) {
			colData.add(md.getColumnLabel(i));
		}
		list.add(colData);
		while (rs.next()) { 
			List<String> rowData = new ArrayList<String>();

			for (int i = 1; i <= columnCount; i++) {
				rowData.add(rs.getString(i));
			}
			list.add(rowData);
		}
		return list;
	}

	public static String cnTranslate(String cnSql) {
		return null;
	}


}
