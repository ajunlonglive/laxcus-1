/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.runtime;

import java.util.*;

import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * 注册成员合集。<br>
 * 已经注册的成员。
 * 
 * @author scott.liang
 * @version 1.0 1/11/2020
 * @since laxcus 1.0
 */
public class RayRegisterMemberBasket extends RaySiteMemberBasket {

	/** 注册成员合集 **/
	private static RayRegisterMemberBasket selfHandle = new RayRegisterMemberBasket();

	/** 签名 -> 用户名称的明文 **/
	private TreeMap<Siger, String> plainTexts = new TreeMap<Siger, String>();

	/**
	 * 初始化注册成员合集
	 */
	private RayRegisterMemberBasket() {
		super();
	}

	/**
	 * 返回注册成员合集的静态句柄
	 * @return 注册成员合集实例
	 */
	public static RayRegisterMemberBasket getInstance() {
		return RayRegisterMemberBasket.selfHandle;
	}

	/**
	 * 清除全部记录
	 */
	public void clear() {
		// 调用上级
		super.clear();

		// 锁定删除签名
		super.lockSingle();
		try {
			plainTexts.clear();
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 保存用户签名和明文
	 * @param siger 用户签名
	 * @param name 明文
	 */
	public void putPlainText(Siger siger, String name) {
		super.lockSingle();
		try {
			plainTexts.put(siger, name);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 查找用户明文
	 * @param siger 用户签名
	 * @return 明文字符串
	 */
	public String findPlainText(Siger siger) {
		super.lockMulti();
		try {
			return plainTexts.get(siger);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 判断有用户明文
	 * @param siger 签名
	 * @return 返回真或者假
	 */
	public boolean hasPlainText(Siger siger) {
		return findPlainText(siger) != null;
	}

	/**
	 * 删除用户明文
	 * @param siger 签名
	 * @return 返回真或者假
	 */
	public boolean removePalinText(Siger siger) {
		boolean success = false;
		super.lockSingle();
		try {
			success = (plainTexts.remove(siger) != null);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

}