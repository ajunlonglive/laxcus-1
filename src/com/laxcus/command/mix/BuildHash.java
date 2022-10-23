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
 * 计算散列码命令。<br>
 * 
 * 如果是文本内容将转为UTF8编码后，再计算它的散列值。如果是文件内容，直接计算散列值。
 * 
 * @author scott.liang
 * @version 1.1 09/09/2015
 * @since laxcus 1.0
 */
public class BuildHash extends Command {

	private static final long serialVersionUID = 596470668829488031L;

	/** MD5算法 **/
	public final static int MD5 = 1;

	/** SHA1算法 **/
	public final static int SHA1 = 2;

	/** SHA256算法 **/
	public final static int SHA256 = 3;

	/** SHA512算法 **/
	public final static int SHA512 = 4;

	/** 算法类型 **/
	private int family;

	/** 忽略大小写，默认是“真” **/
	private boolean ignore;

	/** 计算文本 **/
	private String plant;

	/**
	 * 根据传入的计算散列码命令，生成它的数据副本
	 * @param that 计算散列码实例
	 */
	private BuildHash(BuildHash that) {
		super(that);
		family = that.family;
		ignore = that.ignore;
		plant = that.plant;
	}

	/**
	 * 建立计算散列码命令
	 */
	public BuildHash() {
		super();
		setIgnore(true); // 默认忽略大小写
	}

	/**
	 * 建立计算散列码命令，指定参数
	 * @param family 算法类型
	 * @param text 散列文本
	 */
	public BuildHash(int family, String text) {
		super();
		setFamily(family);
		setPlant(text);
	}

	/**
	 * 设置算法类型
	 * @param who 算法类型
	 */
	public void setFamily(int who) {
		switch (who) {
		case BuildHash.MD5:
		case BuildHash.SHA1:
		case BuildHash.SHA256:
		case BuildHash.SHA512:
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
	 * 判断是MD5算法
	 * @return 返回真或者假
	 */
	public boolean isMD5() {
		return family == BuildHash.MD5;
	}

	/**
	 * 判断是SHA1算法
	 * @return 返回真或者假
	 */
	public boolean isSHA1() {
		return family == BuildHash.SHA1;
	}

	/**
	 * 判断是SHA256算法
	 * @return 返回真或者假
	 */
	public boolean isSHA256() {
		return family == BuildHash.SHA256;
	}

	/**
	 * 判断是SHA512算法
	 * @return 返回真或者假
	 */
	public boolean isSHA512() {
		return family == BuildHash.SHA512;
	}

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
	public BuildHash duplicate() {
		return new BuildHash(this);
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