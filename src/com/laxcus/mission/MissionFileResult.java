/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.mission;

import java.io.*;

import com.laxcus.util.*;

/**
 * 文件结果。<br>
 * 返回的数据，保存在文件里
 * 
 * @author scott.liang
 * @version 1.0 5/2/2013
 * @since laxcus 1.0
 */
public class MissionFileResult extends MissionResult {

	/** 数据在磁盘文件 **/
	private File file;

	/**
	 * 构造默认的文件结果
	 */
	public MissionFileResult() {
		super(MissionResultTag.FILE);
	}

	/**
	 * 构造文件结果，指定文件名
	 * @param e 磁盘文件名
	 */
	public MissionFileResult(File e) {
		this();
		setFile(e);
	}

	/**
	 * 设置文件数据
	 * @param e File实例
	 */
	public void setFile(File e) {
		file = e;
		// 类实例
		setThumb(File.class);
	}

	/**
	 * 返回文件数据
	 * @return File实例
	 */
	public File getFile() {
		return file;
	}

	/**
	 * 返回数据读取接口
	 * @return InputStream子类实例
	 */
	public InputStream getInputStream() throws IOException {
		if(file != null) {
			return new FileInputStream(file);
		} 
		throw new IOException("empty data!");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.driver.mission.TubResult#getProduct(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getObject(Class<T> clazz) {
		if (file == null) {
			return null;
		} else if (!Laxkit.isClassFrom(file, clazz)) {
			String e = String.format("%s != %s", file.getClass().getName(), clazz.getName());
			throw new ClassCastException(e);
		}
		// 返回类实例
		return (T) file;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.driver.mission.TubResult#destroy()
	 */
	@Override
	protected void destroy() {
		super.destroy();
		// 删除磁盘文件
		if (file != null) {
			// 文件存在，删除它
			if (file.exists()) {
				file.delete();
			}
			file = null;
		}
	}
	
}
