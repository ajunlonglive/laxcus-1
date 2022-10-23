/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.xml;

import java.io.*;

import com.laxcus.util.naming.*;

/**
 * XML生成器
 * 
 * @author scott.liang
 * @version 1.0
 * @since laxcus 1.0
 */
public final class XML {

	public final static String head_utf8 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

	public static String element(String key, String value) {
		return String.format("<%s>%s</%s>", key, value, key);
	}

	public static String element(String key, Naming value) {
		return String.format("<%s>%s</%s>", key, value.toString(), key);
	}
	
	public static String cdata_element(String key, String value) {
		return String.format("<%s><![CDATA[%s]]></%s>", key, value, key);
	}

	public static String cdata_element(String key, Naming value) {
		return String.format("<%s><![CDATA[%s]]></%s>", key, value.toString(), key);
	}
	
	public static String element(String key, short value) {
		return XML.element(key, String.valueOf(value));
	}

	public static String element(String key, int value) {
		return XML.element(key, String.valueOf(value));
	}

	public static String element(String key, long value) {
		return XML.element(key, String.valueOf(value));
	}

	public static String element(String key, float value) {
		return XML.element(key, String.valueOf(value));
	}

	public static String element(String key, double value) {
		return XML.element(key, String.valueOf(value));
	}

	public static String cdata_element(String key, long value) {
		return XML.cdata_element(key, String.valueOf(value));
	}

	public static byte[] toUTF8(String text) {
		try {
			return text.getBytes("UTF-8");
		} catch (UnsupportedEncodingException exp) {

		}
		return null;
	}
	
}
