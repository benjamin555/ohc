package cn.sp.ofs.excel;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.sp.ofs.excel.model.Column;
import cn.sp.ofs.excel.model.Table;
import cn.sp.ofs.excel.utils.DBConnectionPool;

/**
* @author 陈嘉镇
* @version 创建时间：2014-6-12 上午9:45:45
* @email benjaminchen555@gmail.com
*/
public class HelperTest {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	private static File[] dataExcels ;
    private static String data1FileName = "src/test/resources/data1.xls";
    private static String data2FileName = "src/test/resources/data2.xls";
    private static String destFileName = "ex_output.xls";
    static{
    	dataExcels = new File[2];
    	dataExcels[0] = new File(data1FileName);
    	dataExcels[1] = new File(data2FileName);
    }
    
    @Before
    public void before() throws SQLException {
		//清空数据库表
    		Connection  c = DBConnectionPool.getConnection();
    		Statement statement = c.createStatement();
    		String sql = "DROP ALL OBJECTS";
    		logger.info("sql:{}",sql);
			statement.execute(sql );
			c.commit();
	}
	
	@Test
	public void testTransformDDL() throws Exception {
		
		for (int i = 0; i < dataExcels.length; i++) {
			File dataExcel = dataExcels[i];
			InputStream inputXLS = new FileInputStream(dataExcel);
			Helper h = new Helper();
			String[] ddl = h.transformDDL(inputXLS,2);
			for (int j = 0; j < ddl.length; j++) {
				String d = ddl[j];
				logger.info("ddl:{}",d);

			}
		}
	}
	
	
	@Test
	public void testQuery() throws Exception {
		
		String sql = "select distinct t1.*,t2.c2 as ssdfas,t2.c5,t2.c6,t2.c7,t4.* from t1 join t2 on t2.c1 =t1.c1 join t4 on t1.c1=t4.c1 order by t1.c1 ";
		Helper h = new Helper();
		int[] skipRows = new int[]{2,2,2,2};
		Workbook w =  h.query(sql,dataExcels,skipRows );
		OutputStream os = new BufferedOutputStream(new FileOutputStream(new File(destFileName)));
		w.write(os );
		os.flush();
		os.close();
	}
	
	@Test
	public void testConvertBeans2Excel() throws Exception {
		
		Helper h = new Helper();
		List<Column> cols = new ArrayList<Column>();
		Column e = new Column();
		e.setName("col1");
		List<String> datas = new ArrayList<String>();
		datas.add("11");
		datas.add("12");
		datas.add("13");
		datas.add("14");
		e.setDatas(datas );
		cols.add(e );
		
		Column e2 = new Column();
		e2.setName("col2");
		List<String>datas2 = new ArrayList<String>();
		datas2.add("21");
		datas2.add("22");
		datas2.add("23");
		datas2.add("24");
		e2.setDatas(datas2 );
		cols.add(e2);
		
		
		Column e3 = new Column();
		e3.setName("col3");
		List<String>datas3 = new ArrayList<String>();
		datas3.add("31");
		datas3.add("32");
		datas3.add("33");
		datas3.add("34");
		e3.setDatas(datas3 );
		cols.add(e3);
		
		Workbook w = h.convertBeansExcel(cols,4);
		OutputStream os = new BufferedOutputStream(new FileOutputStream(new File(destFileName)));
		w.write(os );
		os.flush();
		os.close();
		
	}
	
	@Test
	/**
	 * 测试添加注释
	 * @throws Exception
	 */
	public void testAddComment() throws Exception {
		Helper h = new Helper();
		List<Table> ts = new ArrayList<Table>();
		Table t1 = new Table();
		t1.setName("t1");
		List<Column> cols = new ArrayList<Column>();
		Column c = new Column();
		c.setName("c1");
		c.setComment("列1");
		cols.add(c );
		c = new Column();
		c.setName("c2");
		c.setComment("列2");
		cols.add(c );
		t1.setCols(cols );
		ts.add(t1 );
		String sql = "select t1.c1,t1.c2 from t1";
		sql = h.addComment(sql, ts);
		Assert.assertTrue("select t1.c1 as \"列1\",t1.c2 as \"列2\" from t1".equals(sql));
		
		//带*号的
		sql = "select t1.c1,t1.* from t1";
		sql = h.addComment(sql, ts);
		Assert.assertTrue("select t1.c1 as \"列1\",t1.c1 as \"列1\",t1.c2 as \"列2\" from t1".equals(sql));
		
		//带as 的
		sql = "select t1.c1 as 列一,t1.* from t1";
		sql = h.addComment(sql, ts);
		Assert.assertTrue("select t1.c1 as 列一,t1.c1 as \"列1\",t1.c2 as \"列2\" from t1".equals(sql));
	}
	
	
	

}
