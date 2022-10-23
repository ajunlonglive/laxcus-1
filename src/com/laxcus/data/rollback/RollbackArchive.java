/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.rollback;

import java.io.*;

import com.laxcus.util.*;

/**
 * 回滚资源配置
 * 
 * @author scott.liang
 * @version 1.0 11/29/2013
 * @since laxcus 1.0
 */
public final class RollbackArchive {
	
	/** 回滚文件存储目录 **/
	private static File RootPath;

	/**
	 * 设置回滚文件存储目录
	 * @param dir - 目录
	 */
	public static void setDirectory(File dir) {
		Laxkit.nullabled(dir);
		
		// 如果目录不存在，建立它
		if (!(dir.exists() && dir.isDirectory())) {
			boolean success = dir.mkdirs();
			if (!success) {
				throw new IllegalValueException("cannot create \"%s\"", dir);
			}
		}
		RollbackArchive.RootPath = dir.getAbsoluteFile();
	}

	/**
	 * 设置回滚文件存储目录
	 * 
	 * @param path - 目录路径
	 */
	public static void setDirectory(String path) {
		path = ConfigParser.splitPath(path);
		RollbackArchive.setDirectory(new File(path));
	}

	/**
	 * 返回回滚文件存储目录
	 * 
	 * @return
	 */
	public static File getDirectory() {
		return RollbackArchive.RootPath;
	}
	
	/** 插入文件后缀 **/
	public final static String INSERT_SUFFIX = ".ins";

	/** 删除文件后缀 **/
	public final static String DELETE_SUFFIX = ".del";

	/** 更新插入文件后缀 **/
	public final static String UPDEL_SUFFIX = ".updel";

	/** 更新删除文件后缀 **/
	public final static String UPINS_SUFFIX = ".upins";
	
	
}