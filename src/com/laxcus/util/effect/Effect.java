/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.effect;

import com.laxcus.util.lock.*;
import com.laxcus.util.naming.*;
import com.laxcus.xml.*;

/**
 * 并发工具基础类，提供 XML单元和互斥锁
 * 
 * @author scott.liang
 * @version 1.0 4/18/2009
 * @since laxcus 1.0
 */
public class Effect  {

	protected static String xmlHead = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
	
	/** 读写互斥锁 **/
	private MutexLock lock = new MutexLock();

	/**
	 * defaut
	 */
	public Effect() {
		super();
	}

	/**
	 * 单向锁定，当前一个单向锁未退出时，默认保持到取得锁定为止
	 * @return
	 */
	protected boolean lockSingle() {
		return lock.lockSingle();
	}

	/**
	 * 解除单向锁
	 * @return
	 */
	protected boolean unlockSingle() {
		return lock.unlockSingle();
	}

	/**
	 * 执行一个多向锁定
	 * @return
	 */
	protected boolean lockMulti() {
		return lock.lockMulti();
	}

	/**
	 * 解除一个多向锁定
	 * @return
	 */
	protected boolean unlockMulti() {
		return lock.unlockMulti();
	}
	
	protected String element(String key, String value) {
		return XML.element(key, value);
	}

	protected String element(String key, Naming value) {
		return XML.element(key, value);
	}

	protected String cdata_element(String key, String value) {
		return XML.cdata_element(key, value);
	}

	protected String cdata_element(String key, Naming value) {
		return XML.cdata_element(key, value);
	}

	protected String element(String key, short value) {
		return XML.element(key, String.valueOf(value));
	}

	protected String element(String key, int value) {
		return XML.element(key, String.valueOf(value));
	}

	
	protected String element(String key, long value) {
		return XML.element(key, String.valueOf(value));
	}
	
	protected String element(String key, float value) {
		return XML.element(key, String.valueOf(value));
	}

	protected String element(String key, double value) {
		return XML.element(key, String.valueOf(value));
	}


	protected String cdata_element(String key, long value) {
		return XML.cdata_element(key, String.valueOf(value));
	}

	protected byte[] toUTF8(String text) {
		return XML.toUTF8(text);
	}

}
