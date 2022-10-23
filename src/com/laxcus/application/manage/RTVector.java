/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.application.manage;

import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.naming.*;

/**
 * 向量集合
 * 
 * @author scott.liang
 * @version 1.0 8/1/2021
 * @since laxcus 1.0
 */
public class RTVector  {

	/** 启动命令 **/
	Naming command;
	
	/** 成员数组 **/
	ArrayList<WKey> array = new ArrayList<WKey>();
	
	/**
	 * 构造向量集合
	 */
	public RTVector() {
		super();
	}
	
	/**
	 * 构造向量集合
	 * @param command 命令
	 */
	public RTVector(Naming command) {
		this();
		setCommand(command);
	}
	
	/**
	 * 设置命令
	 * @param e
	 */
	public void setCommand(Naming e) {
		Laxkit.nullabled(e);
		command = e;
	}

	/**
	 * 返回向量集合
	 * @return
	 */
	public Naming getCommand() {
		return command;
	}

	/**
	 * 保存单元
	 * @param key
	 */
	public void add(WKey key) {
		Laxkit.nullabled(key);
		// 保存！
		array.add(key);
	}
	
	/**
	 * 删除单元
	 * @param no 编号
	 * @return 判断成功或者否
	 */
	public boolean remove(WKey key) {
		Laxkit.nullabled(key);
		return array.remove(key);
	}
	
	/**
	 * 返回数组
	 * @return
	 */
	public WKey[] toArray() {
		WKey[] a = new WKey[array.size()];
		return array.toArray(a);
	}

	/**
	 * 输出!
	 * @return 返回WKey列表
	 */
	public List<WKey> list() {
		return new ArrayList<WKey>(array);
	}

	/**
	 * 返回成员数
	 * @return
	 */
	public int size() {
		return array.size();
	}
	
	/**
	 * 判断是空集合
	 * @return
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

}

//import java.util.*;
//
//import com.laxcus.util.*;
//import com.laxcus.util.lock.*;
//import com.laxcus.util.naming.*;
//
///**
// * 向量集合
// * 
// * @author scott.liang
// * @version 1.0 8/1/2021
// * @since laxcus 1.0
// */
//public class RTVector extends MutexHandler {
//
//	/** 启动命令 **/
//	Naming command;
//	
//	/** 成员数组 **/
//	ArrayList<RTElement> elements = new ArrayList<RTElement>();
//	
//	/**
//	 * 构造向量集合
//	 */
//	public RTVector() {
//		super();
//	}
//	
//	/**
//	 * 构造向量集合
//	 * @param command 命令
//	 */
//	public RTVector(Naming command) {
//		this();
//		setCommand(command);
//	}
//	
//	/**
//	 * 设置命令
//	 * @param e
//	 */
//	public void setCommand(Naming e) {
//		Laxkit.nullabled(e);
//		command = e;
//	}
//
//	/**
//	 * 返回向量集合
//	 * @return
//	 */
//	public Naming getCommand() {
//		return command;
//	}
//
//	/**
//	 * 保存单元
//	 * @param element
//	 */
//	public void add(RTElement element) {
//		Laxkit.nullabled(element);
//		// 保存！
//		super.lockSingle();
//		try {
//			elements.add(element);
//		} finally {
//			super.unlockSingle();
//		}
//	}
//	
//	/**
//	 * 删除单元
//	 * @param no 编号
//	 * @return 判断成功或者否
//	 */
//	public boolean remove(int no) {
//		ArrayList<RTElement> a = new ArrayList<RTElement>();
//
//		// 锁定
//		super.lockSingle();
//		try {
//			// 找到，保存它！
//			for (RTElement e : elements) {
//				if (e.getNo() == no) {
//					a.add(e);
//				}
//			}
//			// 删除
//			for (RTElement e : a) {
//				elements.remove(e);
//			}
//		} finally {
//			super.unlockSingle();
//		}
//		
//		return a.size() > 0;
//	}
//
//	/**
//	 * 输出!
//	 * @return 返回RTElement列表
//	 */
//	public List<RTElement> list() {
//		super.lockMulti();
//		try {
//			return new ArrayList<RTElement>(elements);
//		} finally {
//			super.unlockMulti();
//		}
//	}
//
//	public int size() {
//		return elements.size();
//	}
//}