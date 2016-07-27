/**
 * 
 */
package com.howbuy.uac.common;

/**
 * @author qiankun.li
 *
 */
public class UnicodeUtil {

    public static String ascii2native(String ascii) {  
        int n = ascii.length() / 6;  
        StringBuilder sb = new StringBuilder(n);  
        for (int i = 0, j = 2; i < n; i++, j += 6) {  
            String code = ascii.substring(j, j + 4);  
            char ch = (char) Integer.parseInt(code, 16);  
            sb.append(ch);  
        }  
        return sb.toString();  
    }  
    public static void main(String[] args) {
    	//String s = "{"code":0,"data":{"country":"\u4e2d\u56fd","country_id":"CN","area":"\u534e\u4e1c","area_id":"300000","region":"\u6d59\u6c5f\u7701","region_id":"330000","city":"","city_id":"-1","county":"","county_id":"-1","isp":"\u534e\u6570","isp_id":"1000140","ip":"100.73.208.14"}}";
    	String s = "\u4e2d\u56fd";
    	String rs = ascii2native(s);
    	System.out.println(rs);
	}
}
