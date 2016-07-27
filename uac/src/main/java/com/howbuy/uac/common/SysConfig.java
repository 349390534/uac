/**
 * 
 */
package com.howbuy.uac.common;

/**
 * @author qiankun.li
 * 
 */
public class SysConfig {

	private static SysConfig config = new SysConfig();

	private SysConfig() {
	}

	public static SysConfig getConfig() {
		return config;
	}
	
	/**
	 * 开发标识
	 */
	private static final boolean is_dev = true;
	

	public boolean isIs_dev() {
		return is_dev;
	}
	
}
