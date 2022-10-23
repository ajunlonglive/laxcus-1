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
import com.laxcus.command.cloud.task.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.tip.*;

/**
 * 生成CONTACT应用包解析器
 * 
 * 语法1：BUILD CONTACT PACKAGE 生成文件 IMPORT BY 读取文件 <BR>
 * 
 * @author scott.liang
 * @version 1.0 2/16/2020
 * @since laxcus 1.0
 */
public class BuildContactPackageParser extends SyntaxParser {

	/** 读取配置文件，导入数据 **/
	private final static String REGEX = "^\\s*(?i)(?:BUILD\\s+CONTACT\\s+PACKAGE)\\s+([\\w\\W]+?)\\s+(?i)(?:IMPORT\\s+BY)\\s+([\\w\\W]+)\\s*$";

	/**
	 * 构造默认的生成CONTACT应用包解析器
	 */
	public BuildContactPackageParser() {
		super();
	}

	/**
	 * 检查传入的参数是否匹配"BUILD CONTACT PACKAGE"语句
	 * @param simple 简单判断
	 * @param input  输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if(simple) {
			return isCommand("BUILD CONTACT PACKAGE", input);
		}
		Pattern pattern = Pattern.compile(BuildContactPackageParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析文件
	 * @param input 输入语句
	 * @param online 在线模式
	 * @return 返回解析的命令，不成立是空指针
	 */
	public BuildContactPackage split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}

		Pattern pattern = Pattern.compile(BuildContactPackageParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		// 如果不匹配，返回空指针
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		// 生成命令
		BuildContactPackage cmd = new BuildContactPackage();
		cmd.setPrimitive(input);

		// 写入的磁盘文件
		String prefix = matcher.group(1);
		// 没有定义后缀，加上它！
		if (!prefix.matches(BuildContactPackage.SUFFIX_REGEX)) {
			prefix += BuildContactPackage.SUFFIX;
		}
		File file = new File(prefix);
		// 如果存在，弹出异常
		boolean exists = (file.exists() && file.isFile());
		// 如果在线模式，弹出菜单，用户自行判断覆盖！
		if (exists && online) {
			// 判断是否要求覆盖
			boolean override = true;
			if (isConsole()) {
				override = confirm(MessageTip.OVERRIDE_FILE_CONSOLE_X, prefix);
			} else {
				override = confirm(MessageTip.OVERRIDE_FILE_GUI, prefix);
			}
			
			// 不覆盖，弹出错误！
			if (!override) {
				throwableNo(FaultTip.EXISTED_X, prefix);
			} else {
				cmd.setOverride(true);
			}
		}
		// 设置磁盘文件路径
		cmd.setWriter(file);

		// 解析XML格式的脚本文件内容
		String filename = matcher.group(2);
		// 读取，生成实例
		readScript(filename, cmd);

		// 参数不足，弹出异常
		if (!cmd.isFull()) {
			throwable(FaultTip.PARAMETER_MISSING);
		}
		return cmd;
	}

	/**
	 * 读取脚本文件
	 * @param filename
	 * @param cmd
	 */
	private void readScript(String filename, BuildContactPackage cmd) {
		File file = new File(filename);
		boolean success = (file.exists() && file.isFile());
		if (!success) {
			throwableNo(FaultTip.NOTFOUND_X, filename);
		}

		// 读取XML脚本中的数据内存
		PackageScriptReader reader = null;
		try {
			reader = new PackageScriptReader(file);
		} catch (IOException e) {
			// 弹出不能解析文件错误
			throwableNo(FaultTip.NOTRESOLVE_X, filename);
		}

		// 必须是CONTACT！
		success = reader.isContact();
		if (!success) {
			throwableNo(FaultTip.NOTRESOLVE_X, filename);
		}

		// 自读文件
		ReadmePackageElement readme = reader.readReadme();
		if (readme == null) {
			throwableNo(FaultTip.NOTRESOLVE_X, filename);
		}
		cmd.setReadmeElement(readme);
		// 启动包
		CloudPackageElement guide = reader.readGuide();
		if(guide == null) {
			throwableNo(FaultTip.NOTRESOLVE_X, filename);
		}
		cmd.setGuideElement(guide);
		
		// FORK
		CloudPackageElement fork = reader.readTask(PhaseTag.FORK);
		if (fork == null) {
			throwableNo(FaultTip.NOTRESOLVE_X, filename);
		}
		cmd.setForkElement(fork);
		// MERGE
		CloudPackageElement merge = reader.readTask(PhaseTag.MERGE);
		if (merge == null) {
			throwableNo(FaultTip.NOTRESOLVE_X, filename);
		}
		cmd.setMergeElement(merge);
		// DISTANT
		CloudPackageElement distant = reader.readTask(PhaseTag.DISTANT);
		if (distant == null) {
			throwableNo(FaultTip.NOTRESOLVE_X, filename);
		}
		cmd.setDistantElement(distant);
		// NEAR
		CloudPackageElement near = reader.readTask(PhaseTag.NEAR);
		if (near == null) {
			throwableNo(FaultTip.NOTRESOLVE_X, filename);
		}
		cmd.setNearElement(near);
	}
}