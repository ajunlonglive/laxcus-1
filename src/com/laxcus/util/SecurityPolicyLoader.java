/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util;

import java.io.*;
import java.security.*;

import com.laxcus.log.client.*;

/**
 * 系统安全许可加载器
 * 
 * @author scott.liang
 * @version 1.0 6/17/2018
 * @since laxcus 1.0
 */
public class SecurityPolicyLoader {

	/**
	 * 重新加载安全许可配置文件。<br>
	 * 当管理员修改了节点conf目录下的“site.policy”文件中的权限并希望生效时，调用这个方法。<br>
	 * “reload2”与“reload”方法的区别是，它通过Provider（提供者类）读取磁盘上的“site.policy”文件。当“reload”方法中的policy.refresh()方法实例是个空操作时，这个方法显示读取磁盘数据更保险。<br>
	 * 
	 * @return 成功返回真，否则假
	 */
	public static boolean reload2() {
		Policy policy = SecurityPolicyLoader.current();
		boolean success = (policy != null);
		if (success) {
			success = SecurityPolicyLoader.loadPolicy(policy);
			if (success) {
				success = SecurityPolicyLoader.setSystemPolicy(policy);
			}
		}
		return success;
	}

	/**
	 * 重新加载安全许可配置文件。<br>
	 * 当管理员修改了节点conf目录下的“site.policy”文件中的权限并希望生效时，调用这个方法。<br>
	 * 
	 * @return 成功返回真，否则假
	 */
	public static boolean reload() {
		Policy policy = SecurityPolicyLoader.current();
		boolean success = (policy != null);
		if (success) {
			policy.refresh(); // 可能是空操作
			success = SecurityPolicyLoader.setSystemPolicy(policy);
		}
		return success;
	}

	/**
	 * 输出一个Policy对象
	 * @return 成功返回 Policy 对象，否则是空指针
	 */
	private static Policy current() {
		try {
			return Policy.getInstance("JavaPolicy", null);
		} catch (Throwable e) {
			Logger.fatal(e);
		}
		return null;
	}

	/**
	 * 设置系统范围的Policy对象
	 * @param policy Policy对象
	 * @return 成功返回真，否则假
	 */
	private static boolean setSystemPolicy(Policy policy) {
		try {
			Policy.setPolicy(policy);
			return true;
		} catch (Throwable e) {
			Logger.fatal(e);
		}
		return false;
	}
	
	/**
	 * 加载配置到新的许可协议中
	 * @param policy 新提取的许可协议
	 * @return 加载成功返回真，否则假
	 */
	private static boolean loadPolicy(Policy policy) {
		// 找到安全配置文件
		final String key = "java.security.policy"; // 在启动配置中声明
		String value = System.getProperty(key);
		// 没有找到，返回FALSE。
		if (value == null) {
			return false;
		}
		File file = new File(value);
		if (!file.exists()) {
			return false;
		}

		Provider provider = policy.getProvider();
		if (provider == null) {
			return false;
		}

		// 加载新的
		try {
			FileInputStream in = new FileInputStream(file);
			provider.load(in);
			in.close();
			return true;
		} catch (Throwable e) {
			Logger.error(e);
		}
		return false;
	}

//	/**
//	 * 重新加载安全许可配置文件。<br>
//	 * 当管理员修改了节点conf目录下的“site.policy”文件中的权限并希望生效时，调用这个方法。<br>
//	 * 
//	 * @return 成功返回真，否则假
//	 */
//	public static boolean reload() {
//		Policy policy = Policy.getPolicy();
//		boolean success = (policy != null);
//		if (success) {
//			policy.refresh();
//		}
//		return success;
//	}
	
//	/**
//	 * 重新加载安全许可配置文件。<br>
//	 * 当管理员修改了节点conf目录下的“site.policy”文件中的权限并希望生效时，调用这个方法。<br>
//	 * 
//	 * @return 成功返回真，否则假
//	 */
//	public static boolean reload() {
//		Policy policy = current();
//		boolean success = (policy != null);
//		if (success) {
//			show(policy);
//			
//			policy.refresh();
//			
//			print("--", "-----");
//			load(policy);
//			show(policy);
//		}
//		return success;
//	}
	
//	/**
//	 * 重新加载安全许可配置文件。<br>
//	 * 当管理员修改了节点conf目录下的“site.policy”文件中的权限并希望生效时，调用这个方法。<br>
//	 * 
//	 * @return 成功返回真，否则假
//	 */
//	public static boolean reload() {
////		showAllProvider();
//
//		Policy policy = SecurityPolicyLoader.current();
//		boolean success = (policy != null);
//		if (success) {
//			success = SecurityPolicyLoader.loadPolicy(policy);
//			if (success) {
//				success = SecurityPolicyLoader.setSystemPolicy(policy);
//			}
//
////			if (success) {
////				SecurityPolicyLoader.show(policy);
////			}
//		}
//		return success;
//	}
	

//	private static void print(String name, String value) {
//		String log = String.format("SecurityPolicyLoader.print, %s - %s", name, value);
//		Logger.debug(log);;
//	}
//	
//	private static void show(Policy policy) {
//		print("policy class", policy.getClass().getName());
//		print("policy type", policy.getType());
//		
//		Provider p = policy.getProvider();
//		if(p == null) {
//			print("provider class", "this is null pointer");
//			return;
//		}
//		
//		print("provider class", p.getClass().getName());
//		print("name", p.getName());
//		print("info", p.getInfo());
//		print("version", Double.toString( p.getVersion()));
//		
//		Iterator<Map.Entry<Object, Object>> iterator = p.entrySet().iterator();
//		while(iterator.hasNext()) {
//			Map.Entry<Object, Object> entry = iterator.next();
//			print(entry.getKey().toString(), entry.getValue().toString());
//		}
//	}
//	
//	private static void showAllProvider() {
//		Provider[] s = Security.getProviders();
//		if(s == null) {
//			return;
//		}
//		for(Provider p : s) {
//			print("provider class", p.getClass().getName());
//			print("name", p.getName());
//			print("info", p.getInfo());
//			print("version", String.valueOf( p.getVersion()));
//			
//			Iterator<Map.Entry<Object, Object>> iterator = p.entrySet().iterator();
//			while(iterator.hasNext()) {
//				Map.Entry<Object, Object> entry = iterator.next();
//				print(entry.getKey().toString(), entry.getValue().toString());
//			}
//			
//			print(" ------- ", "--------");
//		}
//	}
	
//	private static Policy handleException(NoSuchAlgorithmException nsae)
//			throws NoSuchAlgorithmException {
//		Throwable cause = nsae.getCause();
//		if (cause instanceof IllegalArgumentException) {
//			throw (IllegalArgumentException)cause;
//		}
//		throw nsae;
//	}
	 
//	private static void load(Policy policy) {
//		String key = "java.security.policy";
//		String value = System.getProperty(key);
//		File file = new File(value);
//		if(!file.exists()) {
//			print("not found", value);
//			return;
//		} else {
//			print("policy disk file", value);
//		}
//
//		if (policy.getProvider() == null) {
//			print("fuck", "provider is null pointer!");
//			return;
//		}
//		
//		try {
//			FileReader in = new FileReader(file);
//			policy.getProvider().load(in);
//			in.close();
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//	}
}