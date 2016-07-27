/**
 * 
 */
package com.howbuy.uac.common;

import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author qiankun.li
 * 
 */
public class ConnectionDB implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1698541551333287680L;

	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionDB.class);

	private static final String driver = "com.mysql.jdbc.Driver";

	private static final String url = "jdbc:mysql://10.70.70.27/uaa?useUnicode=true&characterEncoding=utf-8";

	private static final String userName = "uaa";

	private static final String password = "uaa_20150108";

	private static final String url_dev = "jdbc:mysql://192.168.220.157/uaa?useUnicode=true&characterEncoding=utf-8";

	private static final String userName_dev = "admin";

	private static final String password_dev = "123";

	private static DataSource dataSource;

	static {
		
		/*try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			LOGGER.error("加载驱动异常,{}", e);
		}*/
		
		Properties properties = new Properties();
		try {
			properties.put("driverClassName", driver);
			properties.put("initialSize",4);
			properties.put("maxTotal", 50);
			properties.put("maxIdle", 40);
			properties.put("minIdle", 4);
			//The indication of whether objects will be validated by the idle object evictor (if any). If an object fails to validate, it will be dropped from the pool
			properties.put("testWhileIdle", true);
			properties.put("validationQuery", "select 1");
			//The minimum amount of time an object may sit idle in the pool before it is eligable for eviction by the idle object evictor (if any). 
			properties.put("minEvictableIdleTimeMillis", 1000 * 60 * 5);//五分钟检查一次
			if (SysConfig.getConfig().isIs_dev()) {
				properties.put("username", userName_dev);
				//properties.put("user", userName_dev);
				properties.put("password", password_dev);
				properties.put("url", url_dev);
				dataSource = PoolingDataSource.getDataSource().getPoolDataSource(properties);
				//PoolingDataSource.getDataSource().setUpDriver(url_dev, properties);
				//dataSource=	PoolingDataSource.getDataSource().setupDataSource(url_dev, properties);
			}else{
				//properties.put("user", userName);
				properties.put("username", userName);
				properties.put("password", password);
				properties.put("url", url);
				dataSource = PoolingDataSource.getDataSource().getPoolDataSource(properties);
				//PoolingDataSource.getDataSource().setUpDriver(url, properties);
				//dataSource = PoolingDataSource.getDataSource().setupDataSource(url, properties);
			}
		} catch (Exception e) {
			LOGGER.error("init PoolDataSource error,{}", e);
		}
		
	}

	/**
	 * 获取数据库连接
	 * 
	 * @return
	 */
	public Connection getConnection() {
		Connection connectionTmp = null;
		// 获取连接
		try {
			/*if (is_dev) {
				connnection = DriverManager.getConnection(url_dev,
						userName_dev, password_dev);
			} else {
				connnection = DriverManager.getConnection(url, userName,
						password);
			}*/
			//connnection = DriverManager.getConnection("jdbc:apache:commons:dbcp:storm_pool");
			connectionTmp = dataSource.getConnection();
		} catch (SQLException e) {
			LOGGER.error("getConnection error,{}", e);
		}
		return connectionTmp;
	}

	/**
	 * insert update delete SQL语句的执行的统一方法
	 * 
	 * @param sql
	 *            SQL语句
	 * @param params
	 *            参数数组，若没有参数则为null
	 * @return 受影响的行数
	 */
	public int executeUpdate(String sql, List<Object[]> list) {
		long start = System.currentTimeMillis();
		// 受影响的行数
		int affectedLine = 0;
		int num = 0;
		PreparedStatement preparedStatement = null;
		Connection connection = null;
		try {
			// 获得连接
			connection = this.getConnection();
			if (null == connection) {
				LOGGER.error("connnection is null");
				return -1;
			}
			//手动提交事务
			//connnection.setAutoCommit(false);
			// 调用SQL
			preparedStatement = connection.prepareStatement(sql);
			LOGGER.debug("the sql is :"+sql);
			// 参数赋值

			if(null!=list){
				num = list.size();
				int object_index = 1;
				for(Object[] params:list){
					for (int i = 0; i < params.length; i++) {
						preparedStatement.setObject(object_index, params[i]);
						object_index ++ ;
					}
					//preparedStatement.addBatch();//添加一个批量执行,每个批处理sql都是一个可以单独执行的sql语句
				}
				// 执行
				affectedLine = preparedStatement.executeUpdate();
			}

		} catch (SQLException e) {
			/*try {
				connnection.rollback();
			} catch (SQLException e1) {
				LOGGER.error("executeUpdate connnection.rollback() exception", e);
			}*/
			LOGGER.error("executeUpdate exception", e);
			e.printStackTrace();
		} finally {
			// 释放资源
			closeAll(null, preparedStatement, null, connection);
		}
		long end = System.currentTimeMillis();
		LOGGER.debug("insert data count["+num+"]used "+(end - start) / 1000f + "s!"); 
		return affectedLine;
	}

	/**
	 * 获取结果集，并将结果放在List中
	 * 
	 * @param sql
	 *            SQL语句
	 * @return List 结果集
	 */
	public List<Map<String, Object>> excuteQuery(String sql, Object[] params) {
		// 执行SQL获得结果集
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet=null;
		try {
			// 获得连接
			connection = this.getConnection();
			if (null == connection) {
				LOGGER.error("connnection is null");
				return null;
			}
			// 调用SQL
			preparedStatement = connection.prepareStatement(sql);

			// 参数赋值
			if (params != null) {
				for (int i = 0; i < params.length; i++) {
					preparedStatement.setObject(i + 1, params[i]);
				}
			}
			// 执行
			resultSet = preparedStatement.executeQuery();

		} catch (SQLException e) {
			LOGGER.error("executeQueryRS exception ", e);
		}
		if (resultSet == null) {
			return null;
		}
		// 创建ResultSetMetaData对象
		ResultSetMetaData rsmd = null;

		// 结果集列数
		int columnCount = 0;
		try {
			rsmd = resultSet.getMetaData();

			// 获得结果集列数
			columnCount = rsmd.getColumnCount();
		} catch (SQLException e1) {
			LOGGER.error("excuteQuery exception ", e1);
		}

		// 创建List
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		try {
			// 将ResultSet的结果保存到List中
			while (resultSet.next()) {
				Map<String, Object> map = new HashMap<String, Object>();
				for (int i = 1; i <= columnCount; i++) {
					map.put(rsmd.getColumnLabel(i), resultSet.getObject(i));
				}
				list.add(map);
			}
		} catch (SQLException e) {
			LOGGER.error("excuteQuery exception ", e);
		}finally{
			closeAll(resultSet, preparedStatement, null, connection);
		}

		return list;
	}

	/**
	 * 存储过程带有一个输出参数的方法
	 * 
	 * @param sql
	 *            存储过程语句
	 * @param params
	 *            参数数组
	 * @param outParamPos
	 *            输出参数位置
	 * @param SqlType
	 *            输出参数类型
	 * @return 输出参数的值
	 */
	public Object excuteQuery(String sql, Object[] params, int outParamPos,
			int SqlType) {
		Object object = null;
		Connection connnection = null;
		CallableStatement callableStatement  = null;
		try {
			connnection = this.getConnection();
			if (null == connnection) {
				LOGGER.error("connnection is null");
				return null;
			}
			// 调用存储过程
			callableStatement= connnection.prepareCall(sql);

			// 给参数赋值
			if (params != null) {
				for (int i = 0; i < params.length; i++) {
					callableStatement.setObject(i + 1, params[i]);
				}
			}

			// 注册输出参数
			callableStatement.registerOutParameter(outParamPos, SqlType);

			// 执行
			callableStatement.execute();

			// 得到输出参数
			object = callableStatement.getObject(outParamPos);

		} catch (SQLException e) {
			LOGGER.error("excuteQuery exception ", e);
		} finally {
			// 释放资源
			closeAll(null, null, callableStatement, connnection);
		}

		return object;
	}

	/**
	 * 关闭所有资源
	 */
	private void closeAll(ResultSet resultSet,PreparedStatement preparedStatement,CallableStatement callableStatement,Connection connnection) {
		// 关闭结果集对象
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException e) {
				LOGGER.error("closeAll exception ", e);
			}
		}

		// 关闭PreparedStatement对象
		if (preparedStatement != null) {
			try {
				preparedStatement.close();
			} catch (SQLException e) {
				LOGGER.error("closeAll exception ", e);
			}
		}

		// 关闭CallableStatement 对象
		if (callableStatement != null) {
			try {
				callableStatement.close();
			} catch (SQLException e) {
				LOGGER.error("closeAll exception ", e);
			}
		}

		// 关闭Connection 对象
		if (connnection != null) {
			try {
				connnection.close();
			} catch (SQLException e) {
				LOGGER.error("closeAll exception ", e);
			}
		}
	}

}
