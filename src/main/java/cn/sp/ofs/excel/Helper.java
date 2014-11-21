package cn.sp.ofs.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.jxls.reader.XLSReader;
import net.sf.jxls.reader.XLSReaderImpl;
import net.sf.jxls.reader.XLSSheetReaderImpl;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.sp.ofs.excel.model.Column;
import cn.sp.ofs.excel.model.Table;
import cn.sp.ofs.excel.utils.DBConnectionPool;
import cn.sp.ofs.excel.utils.DBUtils;


/**
* @author 陈嘉镇
* @version 创建时间：2014-6-12 上午10:41:08
* @email benjaminchen555@gmail.com
*/
@SuppressWarnings({ "rawtypes", "unchecked" })
public class Helper {

	private Logger logger = LoggerFactory.getLogger(getClass());
	private static int tableId;
	
	/**
	 * 是否调试模式，调试模式下table会保留在数据库。
	 */
	private boolean debug;

	public String[] transformDDL(InputStream inputXLS,int skipRow) {
		List<Table> tList = read2TableModel(inputXLS,skipRow);
		return getCreatedSQL(tList);
	}

	/**
	 * 加载表结构
	 * @param inputXLS
	 * @param skipRow 
	 */
	private List<Table> read2TableModel(InputStream inputXLS, int skipRow) {
		List<Table> tables = new ArrayList<Table>();
		XLSReader mainReader = new XLSReaderImpl();
		mainReader.addSheetReader(0, new XLSSheetReaderImpl());
		try {

			Workbook workbook = WorkbookFactory.create(inputXLS);
			for (int sheetNo = 0; sheetNo < workbook.getNumberOfSheets(); sheetNo++) {
				Table table = readSheetToTable(workbook, sheetNo,skipRow);
				tables.add(table);
			}

		} catch (IllegalAccessException e) {
			logger.error("error", e);
		} catch (InvocationTargetException e) {
			logger.error("error", e);
		} catch (InvalidFormatException e) {
			logger.error("error", e);
		} catch (IOException e) {
			logger.error("error", e);
		}

		return tables;
	}

	/**
	 * 获取表创建语句
	 * @return
	 */
	private String[] getCreatedSQL(List<Table> tList) {
		Assert.assertNotNull(tList);
		String[] ret = new String[tList.size()];
		for (int i = 0; i < tList.size(); i++) {
			ret[i] = tList.get(i).geCreateDDL();
		}
		return ret;
	}

	private Table readSheetToTable(Workbook workbook, int sheetNo, int skipRow) throws IllegalAccessException,
			InvocationTargetException {
		Sheet sheet = workbook.getSheetAt(sheetNo);
		
		Row row = getTitleRow(skipRow, sheet);
		
		
		logger.info("LastCellNum:{}", row.getLastCellNum());
		Table table = new Table();
		table.setRowSize(sheet.getLastRowNum()+1-skipRow);
		table.setName(genTableName());
		List<Column> cols = suitColumns(row);
		table.setCols(cols);
		
		//设置cols数据
		for (int j = 0; j < cols.size(); j++) {
			Column col = cols.get(j);
			List<String> datas = getColumnData(skipRow, sheet, table, j);
			col.setDatas(datas);
		}
	
		
		
		return table;
	}

	protected List<String> getColumnData(int skipRow, Sheet sheet, Table table, int j) {
		List<String>datas = new ArrayList<String>();
		for (int i = sheet.getFirstRowNum()+skipRow; i <=sheet.getLastRowNum(); i++) {
			Row row2 = sheet.getRow(i);
			if (row2==null) {
				logger.warn("跳过行：{}",i);
				//更新rowSize
				table.setRowSize(table.getRowSize()-1);
				continue;
			}
			Cell cell = row2.getCell(j);
			if (cell==null) {
				datas.add("");
				continue;
			}else {
				if (cell.getCellType()==Cell.CELL_TYPE_NUMERIC) {
					if (cell instanceof XSSFCell) {
						XSSFCell x = (XSSFCell)cell;
						datas.add(x.getRawValue());
					}else {
						datas.add( cell.toString());
					}
				}else {
					datas.add( cell.toString());
				}
				
				
			}
		}
		return datas;
	}

	/**
	 * 获取标题行
	 * @param skipRow
	 * @param sheet
	 * @return
	 */
	private Row getTitleRow(int skipRow, Sheet sheet) {
		Row row ;
		if (skipRow==0) {
			 row = sheet.getRow(0);
		}else {
			 row = sheet.getRow(skipRow-1);
		}
		return row;
	}

	/**
	 * 设置列定义信息
	 * @param row
	 * @return
	 */
	private List<Column> suitColumns(Row row) {
		List<Column> cols = new ArrayList<Column>();
		for (int i = 0; i < row.getLastCellNum(); i++) {
			Column col = new Column();
			col.setName("c" + (i + 1));
			col.setComment(row.getCell(i).toString());
			cols.add(col);
		}
		return cols;
	}

	/**
	 * 生成唯一的表名
	 * @return
	 */
	private synchronized String genTableName() {
		return "tab"+(++tableId);
	}

	
	public List<Table>  insert2DB(InputStream inputXLS, int skipRow) {
		List<Table> tables = read2TableModel(inputXLS,skipRow);
		String[] ss = getCreatedSQL(tables);
		createTable2DB(ss);
		String[] ss2 = getInsertSQL(tables);
		insertData2DB(ss2);
		return tables;
	}

	private String[] getInsertSQL(List<Table> tables) {
		List<String> insertSqlList = new ArrayList<String>();
		for (Table table : tables) {
			String[] ssStrings = table.getInsertSql();
			for (String string : ssStrings) {
				insertSqlList.add(string);
			}
			
		}
		return insertSqlList.toArray(new String[]{});
	}

	private void insertData2DB(String[] ss2) {
		Connection connection = DBConnectionPool.getConnection();
		boolean defaultAutoCommit = true;
		Statement stat = null;
		try {
			defaultAutoCommit = connection.getAutoCommit();
			connection.setAutoCommit(false); 
			stat = connection.createStatement();
			
			for (String s : ss2) {
				stat.addBatch(s);
			}
			stat.executeBatch();
			connection.commit();
		} catch (SQLException e) {
			logger.error("error.",e);
			try {
				connection.rollback();
			} catch (SQLException e1) {
				logger.error("error.",e1);
			}
		}finally{
			try {
				connection.setAutoCommit(defaultAutoCommit);
			} catch (SQLException e1) {
				logger.error("error.",e1);
			} 
			if (stat!=null) {
				try {
					stat.close();
				} catch (SQLException e) {
					logger.error("error.",e);
				}
			}
		}
	}

	private void createTable2DB(String[] ss) {
		Connection connection = DBConnectionPool.getConnection();
		Statement stat = null;
		try {
			for (String s : ss) {
				
				stat = connection.createStatement();
				logger.info("ddl:{}",s);
				stat.executeUpdate(s);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			
		}finally{
			if (stat!=null) {
				try {
					stat.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * 查询excel
	 * @param sql
	 * @param dataExcels
	 * @return
	 * @throws FileNotFoundException 
	 * @throws SQLException 
	 */
	public Workbook query(String sql, File[] dataExcels,int[]skipRows) throws FileNotFoundException, SQLException {
//		excel 导入数据库
		List<Table> ts = new ArrayList<Table>();
		for (int i = 0;i<dataExcels.length;i++) {
			File dataExcel = dataExcels[i];
			InputStream inputXLS = new FileInputStream(dataExcel);
			ts .addAll(insert2DB(inputXLS, skipRows[i]));
		}
		
//		将sql中的table前缀,替换为实际的table名
		sql = replaceTablePrefix(sql,ts);
		
//		将sql中的列部分添加中文注释
		 sql = addComment(sql,ts);
		
		
//		执行sql获取bean集合
		Statement statement = DBConnectionPool.getConnection().createStatement();
		ResultSet rs =statement.executeQuery(sql);
		
		//查询后dorp掉table，减少资源的占用
		if (!debug) {
			dropTables(ts);
		}
		
		List<List<String>> ls =DBUtils.convertList(rs);	
		
		
//		将bean集合转换为excel文件
		Workbook w  = convertList2Excel(ls);
		
		return w;
	}

	private String replaceTablePrefix(String sql, List<Table> ts) {
		for (int i = 1; i <=ts.size(); i++) {
			Table table = ts.get(i-1);
			//替换
			sql = sql.replace("t"+i, table.getName());
		}
		
		logger.info("sql:{}",sql);
		return sql;
	}
	

	private void dropTables(List<Table> ts) throws SQLException {
		String sql2 = "drop table {0}";
		Connection connection = DBConnectionPool.getConnection();
		Statement statement = connection.createStatement();
		for (Table table : ts) {
			String sql3 = cn.sp.ofs.excel.utils.StringUtils.formatMsg(sql2, table.getName());
			logger.info("sql:{}",sql3);
			statement.execute(sql3);
		}
	}

	public String addComment(String sql, List<Table> ts) {
		//获取select和from之间的部分
		String mString = sql.substring(sql.indexOf("select")+7, sql.indexOf(" from"));
		//distinct
		if (mString.indexOf("distinct")>=0) {
			mString= mString.replace("distinct", " ");
		}
		
		//处理
		String pString = mString;
		String[] ps = pString.split(",");
		List<String> pList = new ArrayList<String>();
		for (String p : ps) {
			if (p.indexOf(".*")>=0) {
				//全选择查询
				p = findAllSelect(ts,p.trim());
			}else if (p.toLowerCase().indexOf("as")>=0) {
//				do nothing
			}else {
				String c = findComment(ts,p.trim());
				if (StringUtils.isNotBlank(c)) {
					p+=" as \""+c+"\"";
				}	
			}
			pList.add(p);
		}
		pString = StringUtils.join(pList,",");
		
		//替换
		sql = sql.replace(mString.trim(), pString);
		logger.info("sql:{}",sql);
		return sql;
	}

	/**
	 * 在集合中查找全选择
	 * @param ts
	 * @param p
	 */
	private String findAllSelect(List<Table> ts, String p) {
		Pattern pat = Pattern.compile("tab\\d+\\.\\*");
		Matcher m = pat.matcher(p);
		boolean b = m.matches();
		Assert.assertTrue("字符串格式不正确", b);
		String[] ss = p.split("\\.");
		String t = ss[0];
		Table table = findTable(ts, t);
		if (table==null) {
			return "";
		}
		return table.getAllSelect();
		
	}

	/**
	 * 在集合中查找注释
	 * @param ts
	 * @param p 字符串格式为t1.c1
	 * @return
	 */
	private String findComment(List<Table> ts, String p) {
		Pattern pat = Pattern.compile("tab\\d+\\.c\\d+");
		Matcher m = pat.matcher(p);
		boolean b = m.matches();
		Assert.assertTrue("字符串格式不正确", b);
		String[] ss = p.split("\\.");
		String t = ss[0];
		String c = ss[1];
		Table table = findTable(ts, t);
		if (table==null) {
			return "";
		}
		return table.findComment(c);
	}

	/**
	 * 查找表格
	 * @param ts
	 * @param name 表格名称
	 */
	private Table findTable(List<Table> ts, String t) {
		Table ta = null;
		for (Table table : ts) {
			if (t.equals(table.getName())) {
				ta = table;
			}
		}
		return ta;
	}

	public Workbook convertBeansExcel(List<Column> cols,int dataRows) {
		Workbook workbook = new HSSFWorkbook();
		Sheet sheet = workbook.createSheet("Sheet1");
		for (int i = 0; i < dataRows; i++) {
			Row row = sheet.createRow(i);
			for (int j = 0; j < cols.size(); j++) {
				Column col = cols.get(j);
				Cell c = row.createCell(j);
				List<String>datas = col.getDatas();
				if (i==0) {
					c.setCellValue(col.getName());
				}else {
					c.setCellValue(datas.get(i-1));
				}
				
			}
		}
		return workbook;
		
	}
	
	public Workbook convertList2Excel(List<List<String>>  rs) {
		Workbook workbook = new HSSFWorkbook();
		Sheet sheet = workbook.createSheet("Sheet1");
		for (int i = 0; i < rs.size(); i++) {
			Row row = sheet.createRow(i);
			List<String> cList = rs.get(i);
			for (int k = 0; k < cList.size(); k++) {
				Cell c = row.createCell(k);
				c.setCellValue(cList.get(k));
			}
			
		}
		return workbook;
		
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
}
