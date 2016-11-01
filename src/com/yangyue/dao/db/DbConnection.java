package com.yangyue.dao.db;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

/**
 * 数据库连接类
 * 
 * @author 刘向峰
 * 
 */
public class DbConnection {
	

	//oracle连接写法
	private static String diver=null;
	private static String url=null;
	private static String username=null;
	private static String password=null;

	static {
		try{
			Properties properties = new Properties();
			properties.load(new FileReader(new File("config/DataCenter.properties")));
			diver=properties.getProperty("mysql.driver");
			url=properties.getProperty("mysql.url");
			username=properties.getProperty("mysql.username");
			password=properties.getProperty("mysql.password");
		}catch (IOException e){
			System.out.println("读取配置文件失败:"+e.getMessage());
		}
	}


	static{
		try {
			//加载驱动
			Class.forName(diver);
		} catch (ClassNotFoundException e) {
			System.out.println("加载不到驱动！");
			e.printStackTrace();
		}
	}


	/**
	 * 获取连接
	 * 
	 * @return connection
	 */
	public static Connection getConn() {
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url,username,password);
		} catch (Exception e) {
			System.out.println("连接错误，请检查您的URL、用户名和密码！");
			e.printStackTrace();
		}
		return conn;
	}

	/**
	 * 关闭资源
	 * 
	 * @param connection
	 * @param statement
	 * @param resultSet
	 */
	public static void close(Connection connection, Statement statement,
			ResultSet resultSet) {
		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
			if (statement != null) {
				statement.close();
				statement = null;
			}
			if (resultSet != null) {
				resultSet.close();
				resultSet = null;
			}
		} catch (SQLException e) {
			System.out.println("关闭资源时出现错误！");
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		System.out.println(new DbConnection().getConn());
	}
}
