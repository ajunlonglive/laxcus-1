/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.io.*;
import java.util.regex.*;

import com.laxcus.command.cloud.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.tip.*;

/**
 * 部署CONTACT软件包解析器
 * 
 * @author scott.liang
 * @version 1.0 3/14/2020
 * @since laxcus 1.0
 */
public class DeployContactPackageParser extends DeployCloudPackageParser {

	/** 表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:DEPLOY\\s+CONTACT\\s+PACKAGE)\\s+(?:[\\\"]?)([\\w\\W]+?)(?:[\\\"]?)(?i)(\\s*|\\s+[\\w\\W]*)$";

	/**
	 * 构造默认的部署CONTACT软件包解析器
	 */
	public DeployContactPackageParser() {
		super();
	}

	/**
	 * 检查传入的参数是否匹配语句
	 * @param simple 简单判断
	 * @param input  输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if(simple) {
			return isCommand("DEPLOY CONTACT PACKAGE", input);
		}
		Pattern pattern = Pattern.compile(DeployContactPackageParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析发布命令
	 * 
	 * @param input 输入语句
	 * @return 返回DeploySwiftPackage实例
	 */
	public DeployContactPackage split(String input) {
		Pattern pattern = Pattern.compile(DeployContactPackageParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		// 不匹配是错误
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		DeployContactPackage cmd = new DeployContactPackage();
		cmd.setPrimitive(input);

		// 磁盘文件
		String path = matcher.group(1);
		// 判断是CONTACT软件包
		if (!path.matches(BuildContactPackage.SUFFIX_REGEX)) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, path);
		}

		File file = new File(path);
		// 如果存在，弹出异常
		boolean success = (file.exists() && file.isFile());
		if(!success) {
			throwableNo(FaultTip.NOTFOUND_X, path);
		}
		// 设置磁盘文件路径
		cmd.setFile(file);

		// 解析子类信息
		String suffix = matcher.group(2);
		splitItem(cmd, suffix);

		// 判断是系统软件包
		boolean system = isSystemWare(file, PhaseTag.contact());
		if (system) {
			cmd.setSystemWare(true);
		} else {
			// 统计有
			success = hasSystemTask(file, PhaseTag.contact());
			if (success) {
				throwableNo(FaultTip.NOTSUPPORT_X, path);
			}
		}
		
		// 返回解析命令
		return cmd;
	}

}