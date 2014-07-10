package cn.sp.ofs.excel.model;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.sp.ofs.excel.utils.StringUtils;

/**
* @author 陈嘉镇
* @version 创建时间：2014-6-12 下午2:13:11
* @email benjaminchen555@gmail.com
*/
@SuppressWarnings("unchecked")
public class Table {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private String name;

	private List<Column> cols;

	private int rowSize;

	public String geCreateDDL() {
		Assert.assertNotNull(name);
		Assert.assertNotNull(cols);
		StringBuffer pattern = new StringBuffer();
		StringBuffer columnPattern = new StringBuffer();
		for (int i = 0; i < cols.size(); i++) {
			Column col = cols.get(i);
			if (i == cols.size() - 1) {
				columnPattern.append(col.getDDL());
			} else {
				columnPattern.append(col.getDDL() + ",");
			}

		}

		pattern.append(" create table " + name + " ( ").append(columnPattern.toString()).append(" ) ");

		return pattern.toString();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Column> getCols() {
		return cols;
	}

	public void setCols(List<Column> cols) {
		this.cols = cols;
	}

	public int getRowSize() {
		return rowSize;
	}

	public void setRowSize(int rowSize) {
		this.rowSize = rowSize;
	}

	@SuppressWarnings("unchecked")
	public String[] getInsertSql() {
		String pattern = " insert into {0} ( {1} ) values( {2} ) ";
		String columnString = getColumnString();
		List<String> insertSqlList = new ArrayList<String>();
		int rowSize = this.getRowSize();

		List<String> cellList = new ArrayList<String>();
		for (int i = 0; i < rowSize; i++) {

			for (int j = 0; j < cols.size(); j++) {
				Column column = cols.get(j);
				List<String> colData = column.getDatas();
				cellList.add(colData.get(i));
			}

			String rowString = getValueString(cellList);
			String insertSql = StringUtils.formatMsg(pattern, this.getName(), columnString, rowString);
			insertSqlList.add(insertSql);
			cellList.clear();
		}

		return insertSqlList.toArray(new String[] {});
	}

	private String getValueString(List<String> cellList) {
		List<String> nList = new ArrayList<String>();
		for (String cellVal : cellList) {
			cellVal = "'" + cellVal + "'";
			nList.add(cellVal);
		}
		String join = org.apache.commons.lang3.StringUtils.join(nList);
		return join.substring(1, join.length() - 1);
	}

	private String getColumnString() {
		List<String> colNameList = new ArrayList<String>();
		for (Column col : cols) {
			colNameList.add(col.getName());
		}
		String join = org.apache.commons.lang3.StringUtils.join(colNameList);
		return join.substring(1, join.length() - 1);
	}

	/**
	 * 查找列注释
	 * @param p 字符串格式为c1
	 * @return
	 */
	public String findComment(String p) {
		String comment = null;
		List<Column> cs = this.getCols();
		for (Column column : cs) {
			if (p.equals(column.getName())) {
				comment = column.getComment();
				break;
			}
		}
		return comment;
	}

	/**
	 * 获取用于select语句的全选择
	 * @return
	 */
	public String getAllSelect() {
		List<String> cList = new ArrayList<String>();
		for (int i = 0; i < this.cols.size(); i++) {
			String s = this.getName()+"."+cols.get(i).getName();
			String comment = cols.get(i).getComment();
			if (org.apache.commons.lang3.StringUtils.isNotBlank(comment)) {
				s+=" as \""+comment+"\"";
			}
			cList.add(s);
		}
		return org.apache.commons.lang3.StringUtils.join(cList,",");
	}

}
