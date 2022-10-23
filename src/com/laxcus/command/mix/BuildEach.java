/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.mix;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 计算EACH签名命令。<br>
 * 
 * 如果是文本内容将转为UTF8编码后，再计算它的散列值。如果是文件内容，直接计算散列值。
 * 
 * @author scott.liang
 * @version 1.0 5/23/2018
 * @since laxcus 1.0
 */
public class BuildEach extends Command {

	private static final long serialVersionUID = 8536048831727390092L;

	/** UTF8算法 **/
	public final static int UTF8 = 1;

	/** UTF16算法 **/
	public final static int UTF16 = 2;

	/** UTF32算法 **/
	public final static int UTF32 = 3;

//	/** SHA512算法 **/
//	public final static int SHA512 = 4;

	/** 算法类型 **/
	private int family;

	/** 忽略大小写，默认是“真” **/
	private boolean ignore;

	/** 计算文本 **/
	private String plant;

	/**
	 * 根据传入的计算EACH签名命令，生成它的数据副本
	 * @param that 计算EACH签名实例
	 */
	private BuildEach(BuildEach that) {
		super(that);
		family = that.family;
		ignore = that.ignore;
		plant = that.plant;
	}

	/**
	 * 建立计算EACH签名命令
	 */
	public BuildEach() {
		super();
		setIgnore(true); // 默认忽略大小写
	}

	/**
	 * 建立计算EACH签名命令，指定参数
	 * @param text 散列文本
	 */
	public BuildEach(String text) {
		super();
		setPlant(text);
	}

	/**
	 * 设置算法类型
	 * @param who 算法类型
	 */
	public void setFamily(int who) {
		switch (who) {
		case BuildEach.UTF8:
		case BuildEach.UTF16:
		case BuildEach.UTF32:
			break;
		default:
			throw new IllegalValueException("illegal family:%d", who);
		}
		family = who;
	}

	/**
	 * 返回算法类型
	 * @return 算法类型
	 */
	public int getFamily() {
		return family;
	}

	/**
	 * 判断是UTF8算法
	 * @return 返回真或者假
	 */
	public boolean isUTF8() {
		return family == BuildEach.UTF8;
	}

	/**
	 * 判断是UTF16算法
	 * @return 返回真或者假
	 */
	public boolean isUTF16() {
		return family == BuildEach.UTF16;
	}

	/**
	 * 判断是UTF32算法
	 * @return 返回真或者假
	 */
	public boolean isUTF32() {
		return family == BuildEach.UTF32;
	}

//	/**
//	 * 判断是SHA512算法
//	 * @return 返回真或者假
//	 */
//	public boolean isSHA512() {
//		return family == BuildEach.SHA512;
//	}

	/**
	 * 设置计算文本
	 * @param e 计算文本
	 */
	public void setPlant(String e) {
		Laxkit.nullabled(e);
		plant = e;
	}

	/**
	 * 返回计算文本
	 * @return 计算文本
	 */
	public String getPlant() {
		return plant;
	}

	/**
	 * 设置忽略大小写
	 * @param b 忽略大小写
	 */
	public void setIgnore(boolean b) {
		ignore = b;
	}

	/**
	 * 判断忽略大小写
	 * @return 返回真或者假
	 */
	public boolean isIgnore() {
		return ignore;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public BuildEach duplicate() {
		return new BuildEach(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(family);
		writer.writeBoolean(ignore);
		writer.writeString(plant);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		family = reader.readInt();
		ignore = reader.readBoolean();
		plant = reader.readString();
	}

}