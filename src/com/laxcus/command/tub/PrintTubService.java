/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.tub;

import com.laxcus.command.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 打印运行状态的边缘计算服务
 * 
 * @author scott.liang
 * @version 1.0 6/21/2019
 * @since laxcus 1.0
 */
public class PrintTubService extends Command {
	
	private static final long serialVersionUID = -1337496011782632924L;

	/** 命名集合  **/
	private Naming[] namings;

	/**
	 * 构造默认的打印运行状态的边缘计算服务
	 */
	public PrintTubService() {
		super();
	}

	/**
	 * 生成打印运行状态的边缘计算服务的数据副本
	 * @param that 打印运行状态的边缘计算服务实例
	 */
	private PrintTubService(PrintTubService that) {
		super(that);
		namings = that.namings;
	}

	/**
	 * 构造打印运行状态的边缘计算服务，指定命名集
	 * @param namings 命名集
	 */
	public PrintTubService(Naming[] namings) {
		this();
		setNamings(namings);
	}

	/**
	 * 从可类化数据读取器中解析打印运行状态的边缘计算服务命令
	 * @param reader 可类化数据读取器
	 */
	public PrintTubService(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置命名集
	 * @param e Naming[]实例
	 */
	public void setNamings(Naming[] e) {
		namings = e;
	}

	/**
	 * 返回命名集
	 * @return Naming[]实例
	 */
	public Naming[] getNamings() {
		return namings;
	}

	/**
	 * 显示全部
	 * @return
	 */
	public boolean isAll() {
		return (namings == null || namings.length == 0);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public PrintTubService duplicate() {
		return new PrintTubService(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#toString()
	 */
	@Override
	public String toString() {
		StringBuilder bf = new StringBuilder();

		int size = (namings != null ? namings.length : 0);
		for (int i = 0; i < size; i++) {
			if (bf.length() > 0) bf.append(',');
			bf.append(namings[i].toString());
		}
		if (bf.length() == 0) {
			return "ALL";
		}
		return bf.toString();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		int size = (namings != null ? namings.length : 0);
		writer.writeInt(size);
		for (int i = 0; i < size; i++) {
			writer.writeString(namings[i].toString());
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		int size = reader.readInt();
		namings = (size == 0 ? null : new Naming[size]);
		for (int i = 0; i < size; i++) {
			namings[i] = new Naming(reader.readString());
		}
	}

}
