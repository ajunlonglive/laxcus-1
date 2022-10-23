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
 * 注册单元
 * 
 * 每个注册单元对应一个软件，它的全部参数，以及子级的文件夹，都保存在这里面。
 * 
 * @author scott.liang
 * @version 1.0 7/15/2021
 * @since laxcus 1.0
 */
public final class RElement implements Classable, Comparable<RElement> {

	/** 单元名称 **/
	private Naming name;

	/** 数组 **/
	private ArrayList<RToken> array = new ArrayList<RToken>();

	/**
	 * 构造默认的注册单元
	 */
	public RElement() {
		super();
	}

	/**
	 * 构造注册单元，解析参数
	 * @param reader 可类化读取器
	 */
	public RElement(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造注册单元，指定名称
	 * @param name 名称
	 */
	public RElement(Naming name) {
		this();
		setName(name);
	}

	/**
	 * 构造注册单元，指定名称
	 * @param name 名称
	 */
	public RElement(String name) {
		this();
		setName(new Naming(name));
	}

	/**
	 * 设置名称
	 * @param e
	 */
	public void setName(Naming e) {
		Laxkit.nullabled(e);
		name = e;
	}

	/**
	 * 返回名称
	 * @return
	 */
	public Naming getName(){
		return name;
	}
	
	/**
	 * 返回实例
	 * @param name
	 * @return
	 */
	public RToken find(Naming name, byte attribute) {
		for (RToken token : array) {
			// 判断属性一致
			boolean success = (token.getAttribute() == attribute 
					&& Laxkit.compareTo(token.getName(), name) == 0);
			if (success) {
				return token;
			}
		}
		return null;
	}

	/**
	 * 返回实例
	 * @param name
	 * @return
	 */
	public RToken find(String name, byte attribute) {
		return find(new Naming(name), attribute);
	}
	
//	/**
//	 * 根据路径，查找关联的实例
//	 * @param names 命名数组路径
//	 * @param defaultToken 默认值
//	 * @return 返回结果
//	 */
//	public RToken find(Naming[] names, byte attribute, RToken defaultToken) {
//		// 如果是空，返回默认值
//		if (names == null || names.length < 1) {
//			return defaultToken;
//		}
//
//		// 查找0下标实例
//		RToken token = find(names[0], attribute);
//		if (token == null) {
//			return defaultToken;
//		}
//
//		// 大于1时，查找子参数
//		if (names.length > 1) {
//			for (int i = 1; i < names.length; i++) {
//				// 必须是文件夹，才能够查找子参数
//				if (token.getClass() == RFolder.class) {
//					RFolder folder = (RFolder) token;
//					token = folder.find(names[i], attribute);
//					if (token == null) {
//						return defaultToken;
//					}
//				} else {
//					return defaultToken;
//				}
//			}
//		}
//		// 返回结果
//		return (token != null ? token : defaultToken);
//	}

	/**
	 * 根据路径，查找关联的实例
	 * @param names 命名数组路径
	 * @param defaultToken 默认值
	 * @return 返回结果
	 */
	public RToken find(Naming[] names, byte attribute, RToken defaultToken) {
		// 如果是空，返回默认值
		if (names == null || names.length < 1) {
			return defaultToken;
		}
		
		// 找到最后一级目录
		RFolder parent = null;
		for (int i = 0; i < names.length - 1; i++) {
			Naming name = names[i];
			RFolder folder = (parent != null ? parent.findFolder(name) : findFolder(name));
			if (folder == null) {
				return defaultToken;
			}
			// 赋值
			parent = folder;
		}

		// 最后一个
		Naming name = names[names.length - 1];
		RToken token = null;
		if (parent != null) {
			token = parent.find(name, attribute);
		} else {
			token = find(name, attribute);
		}
		return (token != null ? token : defaultToken);
	}

	/**
	 * 根据路径，查找关联的实例
	 * @param names 命名数组路径
	 * @param defaultToken 默认值
	 * @return 返回结果
	 */
	public RToken find(String[] names, byte attribute, RToken defaultToken) {
		// 如果是空，返回默认值
		if (names == null || names.length < 1) {
			return defaultToken;
		}

		Naming[] a = new Naming[names.length];
		for (int i = 0; i < names.length; i++) {
			a[i] = new Naming(names[i]);
		}
		return find(a, attribute, defaultToken);
	}

	/**
	 * 根据路径，查找关联的实例
	 * @param names 命名数组
	 * @return 返回结果，或者空指针
	 */
	public RToken find(Naming[] names, byte attribute) {
		return find(names, attribute, null);
	}

	/**
	 * 根据路径，查找关联的实例
	 * @param names 命名数组
	 * @return 返回结果，或者空指针
	 */
	public RToken find(String[] names, byte attribute) {
		return find(names, attribute, null);
	}

	/**
	 * 建立一个目录级
	 * @param names
	 * @return
	 */
	public RFolder createFloder(Naming[] names) {
		RFolder parent = null;
		// 生成目录
		for (int i = 0; i < names.length - 1; i++) {
			Naming name = names[i];
			// 建立目录
			RFolder folder = (parent != null ? parent.createFolder(name) : createFolder(name));
			// 赋值
			parent = folder;
		}

		// 生成参数
		Naming name = names[names.length - 1];
		if (parent != null) {
			return parent.createFolder(name);
		} else {
			return createFolder(name);
		}
	}

	/**
	 * 建立一个参数
	 * @param names
	 * @param type
	 * @return
	 */
	public RParameter createParameter(Naming[] names , byte type) {
		RFolder parent = null;
		// 生成目录
		for (int i = 0; i < names.length - 1; i++) {
			Naming name = names[i];
			// 建立目录
			RFolder folder = (parent != null ? parent.createFolder(name) : createFolder(name));
			// 赋值
			parent = folder;
		}

		// 生成参数
		Naming name = names[names.length-1];
		if(parent != null) {
			return parent.createParameter(name, type);
		} else {
			return createParameter(name, type);
		}
	}

	
	/**
	 * 保存一个
	 * @param token
	 * @return
	 */
	public boolean add(RToken token) {
		return array.add(token);
	}

	/**
	 * 删除一个
	 * @param token
	 * @return
	 */
	public boolean remove(RToken token) {
		return array.remove(token);
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
	 * 根据命名找到目录
	 * @param name 名称
	 * @return 返回RFolder实例，或者是空指针
	 */
	public RFolder findFolder(String name) {
		return findFolder(new Naming(name));
	}
	
	/**
	 * 根据路径，查找关联的实例
	 * @param names 命名数组
	 * @return 返回结果，或者空指针
	 */
	public RFolder findFolder(Naming[] names) {
		RToken token = find(names, RTokenAttribute.FOLDER, null);
		boolean success = (token != null && token.getClass() == RFolder.class);
		if (success) {
			return (RFolder) token;
		}
		return null;
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
	 * 根据命名找到参量
	 * @param name 命名
	 * @return 返回实例，或者空指针
	 */
	public RParameter findParameter(Naming[] names) {
		RToken token = find(names, RTokenAttribute.PARAMETER, null);
		boolean success = (token != null && Laxkit.isClassFrom(token, RParameter.class));
		if (success) {
			return (RParameter) token;
		}
		return null;
	}

	/**
	 * 找到参数
	 * @param names
	 * @param type
	 * @return
	 */
	public RParameter findParameter(Naming[] names, byte type) {
		RParameter param = findParameter(names);
		if (param == null) {
			return null;
		}
		return (param.getType() == type ? param : null);
	}
	
	/**
	 * 建立一个目录
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
	 * 
	 * @param name 命名
	 * @return 返回目录实例
	 */
	public RParameter createParameter(Naming name, byte type) {
		RParameter param = findParameter(name);
		if (param != null && param.getType() == type) {
			return param;
		}
		//  生成新的保存它
		param = RParameterCreator.createDefault(type);
		param.setName(name);
		boolean success = add(param);
		return (success ? param : null);
	}

	private void print(RFolder folder) {
		System.out.printf("Folder: %s\n", folder.getName());
		for (RToken token : folder.list()) {
			if (token.getClass() == RFolder.class) {
				print((RFolder) token);
			} else {
				System.out.printf("Param: %s\n", token.getName());
			}
		}
	}
	
	public void print() {
		for (RToken token : array) {
			if (token.getClass() == RFolder.class) {
				print((RFolder) token);
			} else {
				System.out.printf("param: %s\n", token.getName());
			}
		}
	}
	
	/**
	 * 删除单元
	 */
	public boolean remove(Naming name, byte attribute) {
		RToken token = find(name, attribute);
		if (token != null) {
			return array.remove(token);
		}
		return false;
	}

	/**
	 * 根据命名，删除参数
	 * @param names 命名
	 * @return 删除成功返回真，否则假
	 */
	public boolean remove(Naming[] names, byte attribute) {
		// 如果是空，返回默认值
		if (names == null || names.length < 1) {
			return false;
		}

		// 找到目录
		RFolder parent = null;
		int len = names.length - 1;
		for (int i = 0; i < len; i++) {
			Naming name = names[i];
			RFolder folder = (parent != null ? parent.findFolder(name) : findFolder(name));
			if (folder == null) {
				return false;
			}
			// 赋值
			parent = folder;
		}

		// 找到最后，删除！
		Naming name = names[names.length - 1];
		if (parent != null) {
			return parent.remove(name, attribute);
		} else {
			return remove(name, attribute);
		}
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

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(RElement that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(name, that.name);
	}

//	/* (non-Javadoc)
//	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
//	 */
//	@Override
//	public int build(ClassWriter writer) {
//		final int size = writer.size();
//
//		// 参数
//		writer.writeObject(name);
//		writer.writeInt(array.size());
//		for (RToken token : array) {
//			writer.writeDefault(token);
//		}
//		// 返回写入的数据长度
//		return writer.size() - size;
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
//	 */
//	@Override
//	public int resolve(ClassReader reader) {
//		int seek = reader.getSeek();
//
//		// 成员参数
//		name = new Naming(reader);
//		int size = reader.readInt();
//		for (int i = 0; i < size; i++) {
//			RToken token = (RToken) reader.readDefault();
//			add(token);
//		}
//
//		// 返回读取的字节长度
//		return reader.getSeek() - seek;
//	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();

		// 参数
		writer.writeObject(name);
		writer.writeInt(array.size());
		for (RToken token : array) {
			writer.writeObject(token);
		}
		// 返回写入的数据长度
		return writer.size() - size;
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
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();

		// 成员参数
		name = new Naming(reader);
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			RToken token = split(reader);
			add(token);
		}

		// 返回读取的字节长度
		return reader.getSeek() - seek;
	}
}