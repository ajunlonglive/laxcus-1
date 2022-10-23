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
import com.laxcus.task.archive.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.tip.*;

/**
 * 部署云应用包解析器
 * 
 * @author scott.liang
 * @version 1.0 9/1/2020
 * @since laxcus 1.0
 */
public class DeployCloudPackageParser extends SyntaxParser {

	/** 发布到本地 **/
	private final static String LOCAL = "^\\s*(?i)(TO\\s+LOCAL)(\\s*||\\s+.+)$";

	/** 延时检测时间 **/
	private final static String CHECKTIME = "^\\s*(?i)(?:-CHECKTIME)\\s+([1-9][0-9]*\\s*[\\w\\W]+?)(\\s*||\\s+.+)$";
	
	/**
	 * 构造默认的部署云应用包解析器
	 */
	protected DeployCloudPackageParser() {
		super();
	}

	/**
	 * 检测时间
	 * @param cmd
	 * @param input
	 */
	protected void splitItem(DeployCloudPackage cmd, String input) {
		while (input.trim().length() > 0) {
			// TO LOCAL参数
			Pattern pattern = Pattern.compile(DeployCloudPackageParser.LOCAL);
			Matcher matcher = pattern.matcher(input);
			if (matcher.matches()) {
				// 在本地发布
				cmd.setLocal(true);
				// 后缀参数
				input = matcher.group(2);
				continue;
			}
			
			// CHECKTIME参数
			pattern = Pattern.compile(DeployCloudPackageParser.CHECKTIME);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				// 时间
				String time = matcher.group(1);
				long ms = ConfigParser.splitTime(time, -1);
				if (ms < 0) {
					throwableNo(FaultTip.NOTRESOLVE_X, input);
				}
				// 检测时间
				cmd.setCheckTime(ms);

				// 后缀参数
				input = matcher.group(2);
				continue;
			}
			
			// 错误
			throwableNo(FaultTip.NOTRESOLVE_X, input);
		}
	}

	/**
	 * 从启动包(*.gtc)内容中读取软件名称，判断是系统签名
	 * @param content 组件包内容
	 * @return 返回真或者假
	 */
	private boolean isSystemGuideItem(byte[] content) {
		GuideComponentReader sub = new GuideComponentReader(content);
		WareTag tag = sub.readWareTag();
		if (tag != null) {
			Naming naming = tag.getNaming();
			return (Laxkit.compareTo(Sock.SYSTEM_WARE, naming) == 0);
		}
		return false;
	}
	
	/**
	 * 从组件包(*.dtc)内容中读取软件名称，判断是系统签名
	 * @param content 组件包内容
	 * @return 返回软件名称，没有是空指针！
	 */
	private boolean isSystemTaskItem(byte[] content) {
		TaskComponentReader sub = new TaskComponentReader(content);
		// 从内容中读取软件名称
		WareTag tag = sub.readWareTag();
		TaskPart part = sub.readTaskPart();

		// 判断软件是系统签名
		if (part != null && tag != null) {
			Naming naming = tag.getNaming();
			return (part.isSystemLevel() && Laxkit.compareTo(Sock.SYSTEM_WARE, naming) == 0);
		}
		// 不成功，返回空指针
		return false;
	}

	/**
	 * 判断全部是系统系统
	 * @param file
	 * @param stages
	 * @return
	 */
	protected boolean isSystemWare(File file, int[] stages) {
		int count = 0;
		CloudPackageReader reader = null;
		try {
			reader = new CloudPackageReader(file);

			// 读取启动引导包
			CloudPackageItem item = reader.readGTC();
			boolean success = isSystemGuideItem(item.getContent());
			if (success) {
				count++;
			}
			// 读取每个任务组件
			for (int i = 0; i < stages.length; i++) {
				item = reader.readDTC(stages[i]);
				if (item == null) {
					throwableNo(FaultTip.NOTFOUND_X, PhaseTag.translate(stages[i]));
				}
				success = this.isSystemTaskItem(item.getContent());
				if (success) {
					count++;
				}
			}
		} catch (Throwable e) {
			throwableNo(FaultTip.NOTSUPPORT_X, Laxkit.canonical(file));
		}

		return count == (1 + stages.length);
	}

	/**
	 * 判断有系统级应用
	 * @param file 文件名
	 * @param stages 阶段名称
	 * @return 返回真或者假
	 */
	protected boolean hasSystemTask(File file, int[] stages) {
		int count = 0;
		CloudPackageReader reader = null;
		try {
			reader = new CloudPackageReader(file);

			// 读取启动引导包
			CloudPackageItem item = reader.readGTC();
			boolean success = isSystemGuideItem(item.getContent());
			if (success) {
				count++;
			}
			// 读取每个任务组件
			for (int i = 0; i < stages.length; i++) {
				item = reader.readDTC(stages[i]);
				if (item == null) {
					throwableNo(FaultTip.NOTFOUND_X, PhaseTag.translate(stages[i]));
				}
				success = this.isSystemTaskItem(item.getContent());
				if (success) {
					count++;
				}
			}
		} catch (Throwable e) {
			throwableNo(FaultTip.NOTSUPPORT_X, Laxkit.canonical(file));
		}
		
		return (count > 0);
	}
}
