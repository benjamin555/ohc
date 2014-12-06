package cn.sp.ofs.excel.utils;
import java.sql.Connection;
import java.sql.SQLException;

import org.h2.jdbcx.JdbcConnectionPool;
/**
 * DBConnectionPool.java
 * 功能:测试H2提供的JDBC连接池
 * @author boonya
 * @version 1.0 2013-03-11
 */
public class DBConnectionPool{
	
	private static final String JDBC_H2_MEM = "jdbc:h2:mem:ofs-help";
	private static final String JDBC_H2_DISK = System.getenv("JDBC_H2_DISK");
	private static JdbcConnectionPool pool=JdbcConnectionPool.create(JDBC_H2_MEM, "sa", "");
	
	static{
		pool.setLoginTimeout(10000);//建立连接超时时间
		pool.setMaxConnections(100);//建立连接最大个数
	}
	
	public static JdbcConnectionPool getJDBCConnectionPool(){
		return pool;
	}
	
	public static Connection getConnection(){
		try {
			return pool.getConnection();
		} catch (SQLException e) {
			System.out.println("DBConnectionPool create connection is failed:");
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) {
		System.out.println("Timeout :"+DBConnectionPool.getJDBCConnectionPool().getLoginTimeout()+" ,connection count:"+DBConnectionPool.getJDBCConnectionPool().getMaxConnections());
		System.out.println(DBConnectionPool.getConnection());
	}
	

}