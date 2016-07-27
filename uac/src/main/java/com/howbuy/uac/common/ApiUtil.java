/**
 * 
 */
package com.howbuy.uac.common;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author qiankun.li
 * 
 */
public class ApiUtil {
	private final static String ipTaobao ="http://ip.taobao.com/service/getIpInfo.php?ip=%s";
	public static String getIpinfoFromTb(String ip){
		String url = String.format(ipTaobao, ip);
		String ipRes = HttpUtil.getHttpUtil().requestGet(url);
		return ipRes;
	}
	private static String MD5(String md5) {
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			byte[] array = md.digest(md5.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
			}
			return sb.toString();
		} catch (java.security.NoSuchAlgorithmException e) {
		}
		return null;
	}

	public static String baiduApi(String ip) {
		try {
			String sk = "22yTYUVrlWinhsHmufRw4XDWuBQo66A8";
			String ak = "Xa7ROeavwWt7irXivUvzGPgjkmZnpxQn";
			String param1 = "ak=" + ak + "&ip=" + ip;
			String basicuri = "/location/ip?" + param1 + sk;
			String sn = MD5(URLEncoder.encode(basicuri, "utf-8"));
			String geturl = "http://api.map.baidu.com/location/ip?ak="+ak+"&ip="+ip+"&sn="+sn;
			String ret = HttpUtil.getHttpUtil().requestGet(geturl);
			System.out.println(ret);
			return ret;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) {
		String ip="175.161.99.0";
		baiduApi(ip);
	}
}
