/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.register;

import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 文件夹
 * 包含任意多个子单元
 * 
 * @author scott.liang
 * @version 1.0 7/15/2021
 * @since laxcus 1.0
 */
public final class RFolder extends RToken {

	/** 子单元，只有属性是“FOLDER”才能保存 **/
	private ArrayList<RToken> array = new ArrayList<RToken>();

	/**
	 * 构造默认的文件夹
	 */
	public RFolder() {
		super();
		setAttribute(RTokenAttribute.FOLDER);
	}

	/**
	 * 生成文件夹副本
	 * @param that
	 */
	private RFolder(RFolder that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 建立一个文件夹，同时指定它的名称
	 * @param name 名称
	 */
	public RFolder(String name) {
		this();
		setName(name);
	}

	/**
	 * 建立一个文件夹，同时指定它的名称
	 * @param name 名称
	 */
	public RFolder(Naming name) {
		this();
		setName(name);
	}

	/**
	 * 从可类化读取中解析文件夹
	 * @param reader 可类化读取器
	 */
	public RFolder(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 保存一个
	 * 先删除旧的，再保存新的
	 * @param token
	 * @return
	 */
	public boolean add(RToken token) {
		array.remove(token);
		return array.add(token);
	}

	/**
	 * 删除单元
	 * @param name 名称
	 * @param attribute 属性
	 * @return 成功返回真，否则假
	 */
	public boolean remove(Naming name, byte attribute) {
		RToken token = find(name, attribute);
		if (token != null) {
			return array.remove(token);
		}
		return false;
	}

	/**
	 * 找到实例
	 * @param name
	 * @return
	 */
	public RToken find(Naming name, byte attribute) {
		for (RToken token : array) {
			boolean success = (token.getAttribute() == attribute && Laxkit
					.compareTo(token.getName(), name) == 0);
			if (success) {
				return token;
			}
		}
		return null;
	}

	/**
	 * 找到实例
	 * @param name
	 * @return
	 */
	public RToken find(String name, byte attribute) {
		return find(new Naming(name), attribute);
	}

	/**
	 * 找到目录
	 * @param names
	 * @return
	 */
	public RFolder findFolder(Naming[] names) {
		RFolder folder = findFolder(names[0]);
		if (folder == null) {
			return null;
		}

		for (int i = 1; i < names.length; i++) {
			folder = folder.findFolder(names[i]);
			if (folder == null) {
				return null;
			}
		}
		return folder;
	}

	/**
	 * 找到参量
	 * @param names
	 * @return 返回实例，或者空指针
	 */
	public RParameter findParameter(Naming[] names) {
		RFolder parent = null;
		for (int i = 0; i < names.length - 1; i++) {
			Naming name = names[i];
			parent = (parent != null ? parent.findFolder(name) : findFolder(name));
			if (parent == null) {
				return null;
			}
		}

		Naming name = names[names.length - 1];
		if (parent != null) {
			return parent.findParameter(name);
		} else {
			return findParameter(name);
		}
	}


	/**
	 * 根据命名找到目录
	 * @param name 名称
	 * @return 返回RFolder实例，或者是空指针
	 */
	public RFolder findFolder(Naming name) {
		RToken token = find(name, RTokenAttribute.FOLDER);
		boolean success = (token != null && token.getClass() == RFolder.class);
		if (success) {
			return (RFolder) token;
		}
		return null;
	}

	/**
	 * 根据命名找目录
	 * @param name 名称
	 * @return 返回RFolder实例，或者是空指针
	 */
	public RFolder findFolder(String name) {
		return findFolder(new Naming(name));
	}

	/**
	 * 根据命名找到参量
	 * @param name 命名
	 * @return 返回实例，或者空指针
	 */
	public RParameter findParameter(Naming name) {
		RToken token = find(name, RTokenAttribute.PARAMETER);
		boolean success = (token != null && Laxkit.isClassFrom(token, RParameter.class));
		if (success) {
			return (RParameter) token;
		}
		return null;
	}

	/**
	 * 根据命名找参量
	 * @param name
	 * @return
	 */
	public RParameter findParameter(String name) {
		return findParameter(new Naming(name));
	}

	/**
	 * 建立一个目录
	 * 如果目录存在，返回它，否则建立一个新的再返回
	 * 
	 * @param name 命名
	 * @return 返回目录实例
	 */
	public RFolder createFolder(Naming name) {
		RFolder folder = findFolder(name);
		if (folder != null) {
			return folder;
		}
		folder = new RFolder(name);
		boolean success = add(folder);
		return (success ? folder : null);
	}

	/**
	 * 建立一个参数
	 * 如果参数存在，返回它，否则建立一个新的再返回
	 * 
	 * @param name 命名
	 * @return 返回参数实例
	 */
	public RParameter createParameter(Naming name, byte type) {
		RParameter param = findParameter(name);
		if (param != null && param.getType() == type) {
			return param;
		}
		//  生成新的保存它
		param = RParameterCreator.createDefault(type);
		param.setName(name);
		// 保存!
		boolean success = add(param);
		return (success ? param : null);
	}


	/**
	 * 判断参数存在，包括文件夹和参数
	 * 
	 * @param name 名称
	 * @return 返回真或者假
	 */
	public boolean contains(Naming name, byte attribute) {
		return find(name, attribute) != null;
	}

	/**
	 * 判断参数存在，包括文件夹和参数
	 * 
	 * @param name 名称
	 * @return 返回真或者假
	 */
	public boolean contains(String name, byte attribute) {
		return find(name, attribute) != null;
	}

	/**
	 * 判断有一个参数
	 * @param name
	 * @param paramType
	 * @return 返回真或者假
	 */
	public boolean hasParameter(String name, byte paramType) {
		RParameter token = findParameter(name);
		if (token == null) {
			return false;
		}
		return token.getType() == paramType;
	}

	/**
	 * 判断有一个参数
	 * @param name 名称
	 * @param paramClazz 类
	 * @return 返回真或者假
	 */
	public boolean hasParameter(String name, Class<?> paramClazz) {
		RParameter param = findParameter(name);
		if (param == null) {
			return false;
		}
		return param.getClass() == paramClazz;
	}

	/**
	 * 判断有一组参数
	 * @param names
	 * @param clazzes
	 * @return
	 */
	public boolean hasParameter(String[] names, Class<?>[] clazzes) {
		if (names.length != clazzes.length) {
			return false;
		}

		for (int i = 0; i < names.length; i++) {
			boolean success = hasParameter(names[i], clazzes[i]);
			if (!success) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 输出全部
	 * @return
	 */
	public List<RToken> list() {
		return new ArrayList<RToken>(array);
	}

	/**
	 * 输出目录 
	 * @return
	 */
	public List<RFolder> getFolders() {
		ArrayList<RFolder> a = new ArrayList<RFolder>();
		for (RToken e : array) {
			if (Laxkit.isClassFrom(e, RFolder.class)) {
				a.add((RFolder) e);
			}
		}
		return a;
	}

	/**
	 * 输出参数
	 * @return
	 */
	public List<RParameter> getParameters() {
		ArrayList<RParameter> a = new ArrayList<RParameter>();
		for (RToken e : array) {
			if (Laxkit.isClassFrom(e, RParameter.class)) {
				a.add((RParameter) e);
			}
		}
		return a;
	}


	/**
	 * 返回整数参量
	 * @param name 名称
	 * @return 返回实例，没有是空指针
	 */
	public RBoolean findBoolean(String name) {
		RParameter param = findParameter(name);
		if (param != null && param.isBoolean()) {
			return (RBoolean) param;
		}
		return null;
	}

	/**
	 * 返回参量
	 * @param name 名称
	 * @return 返回实例，没有是空指针
	 */
	public RRaw findRaw(String name) {
		RParameter param = findParameter(name);
		if (param != null && param.isRaw()) {
			return (RRaw) param;
		}
		return null;
	}

	/**
	 * 返回参量
	 * @param name 名称
	 * @return 返回实例，没有是空指针
	 */
	public RString findString(String name) {
		RParameter param = findParameter(name);
		if (param != null && param.isString()) {
			return (RString) param;
		}
		return null;
	}

	/**
	 * 返回参量
	 * @param name 名称
	 * @return 返回实例，没有是空指针
	 */
	public RShort findShort(String name) {
		RParameter param = findParameter(name);
		if (param != null && param.isShort()) {
			return (RShort) param;
		}
		return null;
	}
	
	/**
	 * 返回整数参量
	 * @param name 名称
	 * @return 返回实例，没有是空指针
	 */
	public RInteger findInteger(String name) {
		RParameter param = findParameter(name);
		if (param != null && param.isInteger()) {
			return (RInteger) param;
		}
		return null;
	}

	/**
	 * 返回参量
	 * @param name 名称
	 * @return 返回实例，没有是空指针
	 */
	public RLong findLong(String name) {
		RParameter param = findParameter(name);
		if (param != null && param.isLong()) {
			return (RLong) param;
		}
		return null;
	}

	/**
	 * 返回参量
	 * @param name 名称
	 * @return 返回实例，没有是空指针
	 */
	public RFloat findFloat(String name) {
		RParameter param = findParameter(name);
		if (param != null && param.isFloat()) {
			return (RFloat) param;
		}
		return null;
	}


	/**
	 * 返回参量
	 * @param name 名称
	 * @return 返回实例，没有是空指针
	 */
	public RDouble findDouble(String name) {
		RParameter param = findParameter(name);
		if (param != null && param.isDouble()) {
			return (RDouble) param;
		}
		return null;
	}
	
	/**
	 * 返回参量
	 * @param name 名称
	 * @return 返回实例，没有是空指针
	 */
	public RDate findDate(String name) {
		RParameter param = findParameter(name);
		if (param != null && param.isDate()) {
			return (RDate) param;
		}
		return null;
	}

	/**
	 * 返回参量
	 * @param name 名称
	 * @return 返回实例，没有是空指针
	 */
	public RTime findTime(String name) {
		RParameter param = findParameter(name);
		if (param != null && param.isTime()) {
			return (RTime) param;
		}
		return null;
	}

	/**
	 * 返回参量
	 * @param name 名称
	 * @return 返回实例，没有是空指针
	 */
	public RTimestamp findTimestamp(String name) {
		RParameter param = findParameter(name);
		if (param != null && param.isTimestamp()) {
			return (RTimestamp) param;
		}
		return null;
	}

	/**
	 * 返回参量
	 * @param name 名称
	 * @return 返回实例，没有是空指针
	 */
	public RSerializable findSerializable(String name) {
		RParameter param = findParameter(name);
		if (param != null && param.isSerializable()) {
			return (RSerializable) param;
		}
		return null;
	}

	/**
	 * 返回参量
	 * @param name 名称
	 * @return 返回实例，没有是空指针
	 */
	public RClassable findClassable(String name) {
		RParameter param = findParameter(name);
		if (param != null && param.isClassable()) {
			return (RClassable) param;
		}
		return null;
	}

	/**
	 * 返回参量
	 * @param name 名称
	 * @return 返回实例，没有是空指针
	 */
	public RCommand findCommand(String name) {
		RParameter param = findParameter(name);
		if (param != null && param.isCommand()) {
			return (RCommand) param;
		}
		return null;
	}

	/**
	 * 判断是空集合
	 * @return
	 */
	public boolean isEmpty() {
		return array.isEmpty();
	}

	/**
	 * 返回数目
	 * @return
	 */
	public int size() {
		return array.size();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.register.RToken#duplicate()
	 */
	@Override
	public RFolder duplicate() {
		return new RFolder(this);
	}
	
//	/*
//	 * (non-Javadoc)
//	 * @see java.lang.Object#equals(java.lang.Object)
//	 */
//	@Override
//	public boolean equals(Object that) {
//		if (that == null || that.getClass() != getClass()) {
//			return false;
//		} else if (that == this) {
//			return true;
//		}
//		// 比较一致
//		return compareTo((RFolder) that) == 0;
//	}
	
	//	/* (non-Javadoc)
	//	 * @see com.laxcus.register.RToken#buildSuffix(com.laxcus.util.classable.ClassWriter)
	//	 */
	//	@Override
	//	protected void buildSuffix(ClassWriter writer) {
	//		// 子成员
	//		writer.writeInt(array.size());
	//		for (RToken token : array) {
	//			writer.writeDefault(token);
	//		}
	//	}
	//
	//	/* (non-Javadoc)
	//	 * @see com.laxcus.register.RToken#resolveSuffix(com.laxcus.util.classable.ClassReader)
	//	 */
	//	@Override
	//	protected void resolveSuffix(ClassReader reader) {
	//		int size = reader.readInt();
	//		for (int i = 0; i < size; i++) {
	//			RToken token = (RToken) reader.readDefault();
	//			array.add(token);
	//		}
	//	}

	/* (non-Javadoc)
	 * @see com.laxcus.register.RToken#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 子成员
		writer.writeInt(array.size());
		for (RToken token : array) {
			writer.writeObject(token);
		}
	}

	/**
	 * 解析参数
	 * 解析顺序是：
	 * 1. 属性
	 * 2. 命名
	 * 3. 参量类型（RParameter有，RFolder没有）
	 * @param reader
	 * @return
	 */
	private RToken split(ClassReader reader) {
		// 长度定义
		byte[] b = reader.current(4);
		int len = Laxkit.toInteger(b);

		// 读这一段数据
		b = reader.shift(4, len);

		ClassReader temp = new ClassReader(b);

		// 属性
		byte attribute = temp.read();
		// 判断是目录
		if (RTokenAttribute.isFolder(attribute)) {
			return new RFolder(reader);
		} 
		// 判断是变量
		else if (RTokenAttribute.isParameter(attribute)) {
			new Naming(temp); //  读出命名，见：RToken.resolve，虽然没有用，只为读出类型
			byte type = temp.read(); // 类型
			// 生成变量和解析它...
			RParameter param = RParameterCreator.createDefault(type);
			param.resolve(reader);
			return param;
		} else {
			throw new ClassableException("cannot be resolve!");
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.register.RToken#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			RToken token = split(reader);
			array.add(token);
		}
	}

}