/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.custom;

import java.io.*;

import org.w3c.dom.*;

import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.xml.*;

/**
 * 自定义命令/调用器解析器 <br><br>
 * 
 * 解析第三方用户自己开发、部署到LAXCUS集群上的COMMAND/INVOKER对，以及自定义命令解析器
 * 
 * @author scott.liang
 * @version 1.0 6/12/2017
 * @since laxcus 1.0
 */
final class CustomTokenParser {

	/**
	 * 构造默认的自定义命令/调用器解析器
	 */
	public CustomTokenParser() {
		super();
	}

	/**
	 * 从XML文档中解析自定义命令/调用器
	 * @param document XML文档
	 * @return 解析成功返回真，否则假
	 */
	private boolean split(Document document) {
		// 解析自定义命令解码器
		String clazz = XMLocal.getXMLValue(document.getElementsByTagName(CustomMark.COMMAND_CRACKER));
		Logger.debug(this, "split", "command cracker %s", clazz);

		boolean success = (clazz != null && clazz.length() > 0);
		if (!success) {
			return false;
		}

		success = false;
		try {
			// 设置命令解析器
			CustomCreator.setCracker(clazz);
			success = true;
		} catch (ClassNotFoundException e) {
			Logger.error(e);
		} catch (InstantiationException e) {
			Logger.error(e);
		} catch (IllegalAccessException e) {
			Logger.error(e);
		}
		if (!success) {
			return false;
		}

		// 自定义标签文件在JAR包的路径
		String tokens = XMLocal.getXMLValue(document.getElementsByTagName(CustomMark.COMMAND_TOKENS));
		if (tokens != null && tokens.trim().length() > 0) {
			CustomConfig.setTokenPath(tokens);
		}
		Logger.info(this, "split", "token file %s", CustomConfig.getTokenPath());

		// 解析自定义命令/调用器对
		NodeList nodes = document.getElementsByTagName(CustomMark.CUSTOM_ITEM);
		int size = nodes.getLength();
		for (int i = 0; i < size; i++) {
			Element element = (Element) nodes.item(i);

			String command = XMLocal.getValue(element, CustomMark.COMMAND);
			String invoker = XMLocal.getValue(element, CustomMark.INVOKER);
			// 保存参数
			CustomCreator.add(command, invoker);

			Logger.debug(this, "split", "add \"%s - %s\"", command, invoker);
		}

		return true;
	}

	/**
	 * 从传入的XML内容中解析自定义命令/调用器
	 * @param content XML内容
	 * @return 成功返回真，否则假
	 */
	public boolean split(byte[] content) {
		Document document = XMLocal.loadXMLSource(content);
		if (document == null) {
			return false;
		}
		return split(document);
	}

	/**
	 * 解析自定义命令/调用器
	 * @param file 自定义命令/调用器文件
	 * @return 成功返回真，否则假
	 */
	public boolean split(File file) {
		Document document = XMLocal.loadXMLSource(file);
		if (document == null) {
			Logger.debug(this, "split", "cannot be load %s", file);
			return false;
		}
		return split(document);
	}

	/**
	 * 解析自定义命令/调用器
	 * @param filename 自定义命令/调用器文件
	 * @return 成功返回真，否则假
	 */
	public boolean split(String filename) {
		Laxkit.nullabled(filename);
		// 把转义字符换为系统规定字符
		filename = ConfigParser.splitPath(filename);
		// 不允许空字符串
		if (filename.isEmpty()) {
			return false;
		}
		// 生成文件
		return split(new File(filename));
	}
}