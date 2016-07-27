/**
 * 
 */
package com.howbuy.uac.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang.time.DateUtils;

/**
 * @author qiankun.li
 *
 */
public class DateUtil {

	static final String partenUs = "[dd/MMM/yyyy:HH:mm:ss Z]";
	public static Date formart(String dateTime,String... partens){
		try {
			return DateUtils.parseDate(dateTime,partens);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Date formartUS(String dateTime){
		SimpleDateFormat dateFormat = new SimpleDateFormat(partenUs,Locale.US);        
		try {
			return dateFormat.parse(dateTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
}
