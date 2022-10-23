/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com  All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.launch.job;

import org.w3c.dom.*;

import com.laxcus.launch.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.task.*;
import com.laxcus.task.mid.*;
import com.laxcus.util.*;
import com.laxcus.xml.*;

/**
 * 工作站点启动器。提供与工作站点相关的基础服务。<br>
 * 工作站点包括:CALL、DATA、WORK、BUILD。CALL站点具有双重身份，即是工作站点，又是网关站点。
 * 
 * @author scott.liang
 * @version 1.3 07/12/2014
 * @since laxcus 1.0
 */
public abstract class JobLauncher extends MemberLauncher {

	/**
	 * 构造工作站点启动器
	 */
	protected JobLauncher() {
		super();
		// 默认记录日志
		setPrintFault(true);
	}

	/**
	 * 设置磁盘管理池根目录
	 * @param document Document实例
	 * @param tag 标记
	 * @param pool DiskPool实例
	 * @param subpath XML路径
	 * @return 设置成功返回真，否则假
	 */
	protected boolean setRootPath(Document document, String tag, DiskPool pool, String subpath) {
		// 解析安全配置文件
		String path = XMLocal.getXMLValue(document.getElementsByTagName(tag));
		// 判断目录有效
		if (path == null || path.isEmpty()) {
			return false;
		}

		boolean success = false;
		// 如果在这个目录下指定子目录时
		if (subpath != null) {
			success = pool.setRoot(path, subpath);
		} else {
			success = pool.setRoot(path);
		}
		// 如果成功，目录保存定时检测集合里
		if (success) {
			addDeviceDirectory(pool.getRoot());
		}
		return success;
	}

	/**
	 * 设置分布任务组件目录
	 * @param document Document实例
	 * @param pool TaskPool子类实例
	 * @param subpath XML路径
	 * @return 设置成功返回真，否则假
	 */
	protected boolean setTaskDeployPath(Document document, TaskPool pool, String subpath) {
		String tag = OtherMark.TASK_DIRECTORY;  // 在local.xml文件中定义
		return setRootPath(document, tag, pool, subpath);
	}

//	/**
//	 * 设置码位计算器组件目录
//	 * @param document XML文档
//	 * @param pool PrefixScalerPool实例
//	 * @param subpath 指定XML路径
//	 * @return 设置成功返回真，否则假
//	 */
//	protected boolean setScaleDeployPath(Document document, ScalerBufferPool pool, String subpath) {
//		String tag = OtherMark.SCALER_DIRECTORY;  // 在local.xml文件中定义
//		return setRootPath(document, tag, pool, subpath);
//	}

	/**
	 * 解析内存容量。
	 * @param input 字符串
	 * @param defaultValue 默认值
	 * @return 返回解析内容容量的长整型值
	 */
	private long splitCacheSize(String input, long defaultValue) {
		if (input == null) {
			return defaultValue;
		}
		// 判断最大空间是按照计算机内存比例分配, 例: 20% or 35%
		boolean success = ConfigParser.isRate(input);
		if (success) {
			double output_value = ConfigParser.splitRate(input, defaultValue);
			double size = (double) Runtime.getRuntime().maxMemory() / output_value;
			return new Double(size).longValue();
		}
		// 解析容量
		return ConfigParser.splitLongCapacity(input, defaultValue);
	}
	
	/**
	 * 设置分布任务组件的中间数据存取目录
	 * @param pool 中间数据管理池
	 * @return 成功返回真，否则假
	 */
	protected boolean setTaskMidPath(Document document, MidPool pool) {
		// 设置根目录
		boolean success = setRootPath(document, OtherMark.MIDDLE_DIRECTORY, pool, null);
		if (!success) {
			Logger.error(this, "cannot be set '%s'", OtherMark.MIDDLE_DIRECTORY);
			return false;
		}

		// 保存中间数据存取目录，定时检测磁盘空间，不足将报警
		addDeviceDirectory(pool.getRoot());

		// 中间数据管理池的最大缓存尺寸，它被内存计算使用，是可选项。默认是1M

		// 解析安全配置文件
		String value = XMLocal.getXMLValue(document.getElementsByTagName(OtherMark.MIDDLE_MAX_CACHESIZE));
		long size = splitCacheSize(value, 0x100000);
		pool.setMaxMemory(size);

		// 分配给每个用户的最大内存容量。默认是10K
		value = XMLocal.getXMLValue(document.getElementsByTagName(OtherMark.MIDDLE_USER_CACHESIZE));
		size = splitCacheSize(value, 10240);
		pool.setMemberMemorySize(size);

		Logger.debug(this, "setTaskMidPath",
				"middle directory:%s, max cache size:%d, user cache size:%d",
				pool.getRoot(), pool.getMaxMemory(), pool.getMemberMemorySize());

		return true;
	}

}