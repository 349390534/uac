/**
 * 
 */
package com.howbuy.uac;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.howbuy.uac.dao.IpDao;
import com.howbuy.uac.per.UacIpData;

/**
 * @author qiankun.li
 *
 */
public class IpDaoTest {

	private IpDao dao = new IpDao();
	
	public void getIpInfoTest(){
		String ip = "1.14.162.28";
		dao.getIpInfo(ip);
	}
	public void addIpDataInfoTest(){
		List<UacIpData> list =new ArrayList<UacIpData>();
		UacIpData data = new UacIpData();
		data.setCity("杭州");
		data.setProvince("浙江");
		data.setReqIp("1.14.162.28");
		data.setReqTime(new Date());
		list.add(data);
		dao.addIpDataInfo(list);
	}
	
	public static void main(String[] args) {
		IpDaoTest daoTest = new IpDaoTest();
		
		//daoTest.getIpInfoTest();
		daoTest.addIpDataInfoTest();
	}
}
