/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.mix;

/**
 * JAVA虚拟机读取器
 * 
 * @author scott.liang
 * @version 1.0 12/22/2020
 * @since laxcus 1.0
 */
public class JREReader {

	public JREReader() {
		super();
	}
	
	/**
	 * 读取系统参数值
	 * @param key 键值
	 * @param subKey 备用键值
	 * @return 返回参数值
	 */
	private String readValue(String key, String subKey) {
		String value = System.getProperty(key);
		if (value == null || value.trim().isEmpty()) {
			if (subKey != null) {
				value = System.getProperty(subKey);
			}
		}
		return value;
	}
	
	/**
	 * 返回JAVA虚拟机信息
	 * @return  JREInfoItem实例
	 */
	public JREInfoItem read() {
		JREInfoItem item = new JREInfoItem();

		String value = readValue("java.vm.vendor", "java.vendor");
		item.setVendor(value);

		value = readValue("java.runtime.version", "java.specification.version");
		item.setVersion(value);

		value = readValue("os.arch", null);
		item.setArch(value);

		value = readValue("os.name", null);
		item.setOsname(value);

		value = readValue("java.vm.name", null);
		item.setVmname(value);

		return item;
	}

}
