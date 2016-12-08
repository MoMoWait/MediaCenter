package com.rockchips.mediacenter.util;

import java.util.regex.Pattern;

/**
 * @author GaoFei
 * 验证工具
 */
public class ValidUtils {
	private ValidUtils(){
		
	}
	
	/**
	 * 验证是否为Samba地址
	 * @param address
	 * @return
	 */
	public static boolean isSambaAddress(String address){
		String patter = "^//(\\d{1,3}\\.){3}\\d{1,3}(/[^(/\\)]+)*$";
		return Pattern.matches(patter, address);
		//Pattern.matches(regularExpression, input)
		//return false;
	}
	
	/**
	 * 验证是否为NFS网络地址
	 * @param address
	 * @return
	 */
	public static boolean isNFSAddress(String address){
		String patter = "^(\\d{1,3}\\.){3}\\d{1,3}:(/[^(/\\)]+)*$";
		return Pattern.matches(patter, address);
	}
}
