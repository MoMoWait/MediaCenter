package com.rockchips.mediacenter.util;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;
/**
 * @author GaoFei
 * 用于执行Shell命令
 */
public class ShellUtils {
	
	public static final String TAG = ShellUtils.class.getSimpleName();
	
	/**
	 * 执行mount命令获取返回结果
	 * @return
	 */
	public static List<String> getMountMsgs(){
		String line = null;
		List<String> strlist = new ArrayList<String>();
		try {
			Process pro = Runtime.getRuntime().exec("mount");
			BufferedReader br = new BufferedReader(new InputStreamReader(pro.getInputStream()));
			while ((line = br.readLine())!=null){
				strlist.add(line);
			}
		}catch (Exception e) {
			e.printStackTrace();
			//Log.i(TAG, "getMountMsg->" + e);
		}
		return strlist;
	}
	
	/**
	 * 通过执行df命令获取返回结果
	 * @return
	 */
	public static List<String> getDfMsgs(){
		String line = null;
		List<String> strlist = new ArrayList<String>();
		try {
			Process pro = Runtime.getRuntime().exec("df");
			BufferedReader br = new BufferedReader(new InputStreamReader(pro.getInputStream()));
			while ((line = br.readLine())!=null){
				strlist.add(line);
			}
		}catch (Exception e) {
			e.printStackTrace();
			//Log.i(TAG, "getDfMsgs->" + e);
		}
		return strlist;
	}
	
	/**
	 * 通过执行ls命令获取结果
	 * @param dirPath
	 * @return
	 */
	public static List<String> getLsMsgs(String dirPath){
		String line = null;
		List<String> strlist = new ArrayList<String>();
		try {
			Process pro = Runtime.getRuntime().exec("ls " + dirPath);
			BufferedReader br = new BufferedReader(new InputStreamReader(pro.getInputStream()));
			while ((line = br.readLine())!=null){
				strlist.add(line);
			}
		}catch (Exception e) {
			e.printStackTrace();
			//Log.i(TAG, "getLsMsgs->" + e);
		}
		return strlist;
	
	}
}
