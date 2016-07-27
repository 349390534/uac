/**
 * 
 */
package com.howbuy.uac.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.howbuy.uac.common.ConnectionDB;
import com.howbuy.uac.per.IpApiInfo;
import com.howbuy.uac.per.UacIpData;

/**
 * @author qiankun.li
 * 
 */
public class IpDao {

	private static final Logger LOGGER = LoggerFactory.getLogger(IpDao.class);

	private final ConnectionDB connectionDB = new ConnectionDB();

	/**
	 * 通用数据库插入
	 * 
	 * @param sql
	 * @param paramList
	 * @return
	 */
	private int doInsert(String sql, List<Object[]> paramList, int len) {

		if (paramList.size() == 0)
			return -1;

		StringBuilder paramHolderBuilder = new StringBuilder();

		paramHolderBuilder.append("(");

		for (int i = 0; i < len; i++) {
			paramHolderBuilder.append("?,");
		}
		paramHolderBuilder.deleteCharAt(paramHolderBuilder.length() - 1);

		paramHolderBuilder.append(")");
		String paramHolder = paramHolderBuilder.toString();

		StringBuffer sqlBf = new StringBuffer(sql);
		for (int i = 0; i < paramList.size(); i++) {
			sqlBf.append(paramHolder);
			sqlBf.append(",");
		}
		String sqlInsert = sqlBf.toString();
		sqlInsert = sqlInsert.substring(0, sqlInsert.length() - 1);
		LOGGER.debug("sql:{}", sqlInsert);
		int i = connectionDB.executeUpdate(sqlInsert, paramList);
		LOGGER.info("insertList executeUpdate num is " + i);
		System.out.println("insertList executeUpdate num is "+i);
		return i;
	}

	public int insertIpData(List<Object[]> paramList) {

		if (paramList.size() == 0)
			return -1;
		final String insertIpSql = "INSERT INTO source_ip (ip) VALUES ";

		return doInsert(insertIpSql, paramList, 1);
	}
	
	public IpApiInfo getIpInfo(String ip){
		String sql = "SELECT ip,ip_info ipInfo FROM ip_api_info where ip = ?";
		List<Map<String, Object>> list = connectionDB.excuteQuery(sql, new Object[]{ip});
		if(CollectionUtils.isEmpty(list))return null;
		IpApiInfo apiInfo = new IpApiInfo();
		Map<String,Object> map = list.get(0);
		String ipVar = map.get("ip").toString();
		String ipInfo = map.get("ipInfo").toString();
		apiInfo.setIp(ipVar);
		apiInfo.setIpInfo(ipInfo);
		return apiInfo;
	}
	
	
	public int addIpInfo(Map<String,String> ipInfoMap){
		String sql = "INSERT INTO ip_api_info (ip,ip_info) VALUES ";
		List<Object[]> paramList = new ArrayList<Object[]>();
		Set<Entry<String, String>> set = ipInfoMap.entrySet();
		for(Entry<String, String> en :set){
			String ip = en.getKey();
			String ipInfo = en.getValue();
			Object[] p = new Object[]{ip,ipInfo};
			paramList.add(p);
		}
		return doInsert(sql, paramList, 2);
	}
	public int addIpInfo(List<Object[]> paramList){
		String sql = "INSERT INTO ip_api_info (ip,ip_info) VALUES ";
		return doInsert(sql, paramList, 2);
	}

	public int addIpDataInfo(List<UacIpData> uacList){
		if(CollectionUtils.isEmpty(uacList))return -1;
		String sql = "INSERT INTO uac_req_ip_data (province,city,req_ip,req_time) VALUES ";
		List<Object[]> paramList = new ArrayList<Object[]>();
		for(UacIpData ipd :uacList){
			Object[] obj = new Object[]{ipd.getProvince(),ipd.getCity(),ipd.getReqIp(),ipd.getReqTime()};
			paramList.add(obj);
		}
		return doInsert(sql, paramList, 4);
	}
}
