/**
 * 
 */
package com.howbuy.uac.ipdata;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.howbuy.uac.common.ApiUtil;
import com.howbuy.uac.common.DateUtil;
import com.howbuy.uac.dao.IpDao;
import com.howbuy.uac.per.IpApiInfo;
import com.howbuy.uac.per.UacIpData;

/**
 * @author qiankun.li
 *
 */
public class AnalysisIpFile {

	private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AnalysisIpFile.class);
	
	private final IpDao dao = new IpDao();
	
	private UacIpData getIpInfoFromBd(String ip){
		IpApiInfo apiInfo =  dao.getIpInfo(ip);
		if(null!=apiInfo){
			System.out.println("ip "+ip +" read from db ");
			UacIpData ipdata = new UacIpData();
			String info = apiInfo.getIpInfo();
			JSONObject data=JSONObject.parseObject(info)
					.getJSONObject("content").getJSONObject("address_detail");
			String province = data.getString("province");
			String city = data.getString("city");
			ipdata.setCity(city);
			ipdata.setProvince(province);
			ipdata.setReqIp(ip);
			//ipdata.setReqTime(reqTime);
			return ipdata;
		}else{
			System.out.println("ip "+ip +" read from baiduApi ");
			String apiRes;
			JSONObject json;
			String code=null;
			try {
				apiRes = ApiUtil.baiduApi(ip);
				json = JSONObject.parseObject(apiRes);
				code = json.getString("status");
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			if("0".equals(code)){
				UacIpData ipdata = new UacIpData();
				JSONObject data=json.getJSONObject("content").getJSONObject("address_detail");
				String province = data.getString("province");
				String city = data.getString("city");
				ipdata.setCity(city);
				ipdata.setProvince(province);
				ipdata.setReqIp(ip);
				//ipdata.setReqTime(reqTime);
				//IP信息保存入库
				List<Object[]> list = new ArrayList<Object[]>();
				list.add(new Object[]{ip,apiRes});
				dao.addIpInfo(list);
				return ipdata;
			}else{
				System.out.println(apiRes);
			}
		}
		return null;
	}
	private UacIpData getIpInfoFromTb(String ip){
		UacIpData ipdata = new UacIpData();
		String ipRes= ApiUtil.getIpinfoFromTb(ip);
		JSONObject json = JSONObject.parseObject(ipRes);
		String code = json.getString("code");
		if("0".equals(code)){
			JSONObject data=json.getJSONObject("data");
			String country = data.getString("country");
			String area = data.getString("area");
			String region = data.getString("region");
			String city = data.getString("city");
			String isp = data.getString("isp");
			//String ip = data.getString("ip");
			ipdata.setArea(area);
			ipdata.setCity(city);
			ipdata.setCountry(country);
			ipdata.setIsp(isp);
			ipdata.setProvince(region);
			ipdata.setReqIp(ip);
			//ipdata.setReqTime(reqTime);
		}
		return ipdata;
	}
 	private String[] cutLine(String line){
		String[] sp = line.split("\\ ");
		return sp;
	}
	/**读取内容入库
	 * @param fc
	 */
	public void doFileContentAndInDb(String file,List<String> fc){
		System.out.println(System.currentTimeMillis()+" begin intoDbIpInfo");
		List<UacIpData> uacList=new ArrayList<UacIpData>();
		int line=0;
		for(String fl:fc){
			//分批入库
			if(uacList.size()==2000){
				dao.addIpDataInfo(uacList);
				uacList.clear();
			}
			line++;
			System.out.println("file:"+file+",read line:"+line);
			String[] ct = cutLine(fl);
			String dt = ct[0]+" "+ct[1];
			String ip = ct[2].replace("\"", "");
			Date date = DateUtil.formartUS(dt);
			//UacIpData ipdata = getIpInfoFromTb(ip);
			UacIpData ipdata = getIpInfoFromBd(ip);
			if(null==ipdata)continue;
			ipdata.setReqTime(date);
			uacList.add(ipdata);
		}
		//入库
		dao.addIpDataInfo(uacList);
		uacList.clear();
		System.out.println(System.currentTimeMillis()+" begin intoDbIpInfo");

	}
	
	/**
	 * IP信息入库
	 * @param ipList 去重的IP集合
	 */
	private void intoDbIpInfo(List<String> ipList){
		System.out.println(System.currentTimeMillis()+" begin intoDbIpInfo");
		List<Object[]> ipListVar = new ArrayList<Object[]>();
		for(String ip:ipList){
			
			if(ipListVar.size()==2000){
				dao.addIpInfo(ipListVar);
				ipListVar.clear();
			}
			IpApiInfo apiInfo =  dao.getIpInfo(ip);
			if(null!=apiInfo)continue;
			String apiRes = ApiUtil.baiduApi(ip);
			if(StringUtils.isNotBlank(apiRes)){
				JSONObject json = JSONObject.parseObject(apiRes);
				if(null==json)continue;
				String code = json.getString("status");
				if("0".equals(code)){
					ipListVar.add(new Object[]{ip,apiRes});
				}
			}
		}
		dao.addIpInfo(ipListVar);
		System.out.println(System.currentTimeMillis()+" end intoDbIpInfo");

	}
	
	/**
	 * @param fc
	 * @return 返回去重的IP集合
	 */
	public List<String> distinctIp(List<String> fc){
		System.out.println(System.currentTimeMillis()+",distinctIp begin");
		Set<String> ips = new HashSet<String>();
		for(String fl:fc){
			String[] ct = cutLine(fl);
			String ip = ct[2].replace("\"", "");
			ips.add(ip);
		}
		List<String> ipList = new ArrayList<String>();
		ipList.addAll(ips);
		System.out.println(System.currentTimeMillis()+",distinctIp end ipList size is "+ipList.size());
		return ipList;
	}
	
	public void ipToDb(List<String> ipList){
		System.out.println(System.currentTimeMillis()+",ipToDb begin");
		if(CollectionUtils.isNotEmpty(ipList)){
			List<Object[]> dbLit = new ArrayList<Object[]>();
			for(String ip:ipList){
				Object[] obj = new Object[]{ip};
				dbLit.add(obj);
			}
			dao.insertIpData(dbLit);
			System.out.println(System.currentTimeMillis()+",ipToDb end");
		}
	}
	
	
	/**
	 * 入口函数
	 */
	public void readReqIpFileMain(){
		File file = new File("E:/work_temp/log/");
		if(file.isDirectory()){
			File[] fs = file.listFiles();
			if(fs!=null && fs.length>=1){
				for(File f:fs){
					if(f.isDirectory())continue;
					try {
						String fileName = f.getName();
						System.out.println("begin file:"+fileName);
						List<String> ipTList=FileUtils.readLines(f);
						//1：将每个文件去重IP入库
						List<String> ipListDis=distinctIp(ipTList);
						ipToDb(ipListDis);
						//2：读取IP数据请求百度api 保存数据库
						//intoDbIpInfo(ipListDis);
						//3： 解析IP信息入库
						doFileContentAndInDb(fileName,ipTList);
						System.out.println("end file:"+fileName);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public static void main(String[] args) {
		AnalysisIpFile ai = new AnalysisIpFile();
		ai.readReqIpFileMain();
	}
	
}
