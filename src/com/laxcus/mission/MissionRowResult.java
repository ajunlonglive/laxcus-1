/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.mission;

import java.io.*;
import java.util.*;

import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;
import com.laxcus.log.client.*;
import com.laxcus.util.classable.*;

/**
 * 行记录数据结果 <br>
 * 
 * 解析一组行数据
 * 
 * @author scott.liang
 * @version 1.0 5/2/2013
 * @since laxcus 1.0
 */
public class MissionRowResult extends MissionBufferResult {

	/** 数据读取接口 **/
	private InputStream in;

	/** 排列表 **/
	private Sheet sheet;

	/**
	 * 构造默认的行记录数据结果
	 */
	public MissionRowResult() {
		super();
	}

	/**
	 * 构造行记录数据结果
	 * @param sheet
	 */
	public MissionRowResult(Sheet sheet) {
		this();
		setSheet(sheet);
	}

	/**
	 * 设置表
	 * @param e
	 */
	public void setSheet(Sheet e) {
		sheet = e;
	}
	
	/**
	 * 返回表
	 * @return
	 */
	public Sheet getSheet() {
		return sheet;
	}

	/**
	 * 判断有字节
	 * @return 返回真或者假
	 * @throws IOException
	 */
	public boolean hasLeft() throws IOException {
		return (in != null && in.available() > 0);
	}

	/**
	 * 以“块”为单位，解析一批行记录
	 * @param e 排列表
	 * @return 行记录数组
	 * @throws IOException
	 * @throws RowParseException
	 * @throws IndexOutOfBoundsException
	 */
	public List<Row> split(Sheet e) throws IOException, RowParseException , IndexOutOfBoundsException{
		// 获得数据读取流
		if (in == null) {
			in = getInputStream();
		}

		// 空指针
		if (sheet == null && e == null) {
			throw new NullPointerException();
		}

		// 更新表
		if(sheet == null || sheet != e) {
			sheet = e;
		}

		// 解析标头
		MassFlag flag = new MassFlag();
		flag.resolve(in);

		// 尺寸不足
		if (in.available() < flag.getLength()) {
			String s = String.format("left size:%d < scale size:%d",
					in.available(), flag.getLength());
			throw new IndexOutOfBoundsException(s);
		}

		// 读指定长度的数据
		byte[] b = new byte[(int) flag.getLength()];
		in.read(b, 0, b.length);

		// 解析行记录数据
		ClassReader reader = new ClassReader(b);
		RowCracker parser = new RowCracker(sheet);
		parser.split(reader);

		// 返回行数据
		return  parser.flush();
	}

	/**
	 * 以“块”为单位，解析一批行记录
	 * @return 行记录数组
	 * @throws IOException
	 * @throws RowParseException
	 * @throws IndexOutOfBoundsException
	 */
	public List<Row> split() throws IOException, RowParseException, IndexOutOfBoundsException {
		return split(null);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.driver.mission.TubResult#destroy()
	 */
	public void destroy() {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				Logger.error(e);
			}
			in = null;
		}

		// 释放上级资源
		super.destroy();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.driver.mission.TubResult#getProduct(java.lang.Class)
	 */
	@Override
	public <T> T getObject(Class<T> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

}
