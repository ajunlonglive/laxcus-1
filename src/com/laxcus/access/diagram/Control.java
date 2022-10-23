/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.diagram;

import java.io.Serializable;
import java.util.*;

import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;

/**
 * 用户资源控制。<br><br>
 * 
 * 用户资源控制匹配一个具体的“Permit”，保存它的操作选项。
 * 数据操作选项的定义和说明见“ControlTag”。
 * 
 * 
 * @author scott.liang
 * @version 1.1 3/12/2015
 * @since laxcus 1.0
 */
public final class Control implements Classable, Markable, Serializable, Cloneable {

	private static final long serialVersionUID = 8188294004346263577L;

	/** 操作选项编号集合 **/
	private TreeSet<java.lang.Short> array = new TreeSet<java.lang.Short>();

	/**
	 * 将用户资源控制选项参数写入可类化数据存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeInt(array.size());
		for (short who : array) {
			writer.writeShort(who);
		}
		return writer.size() - size;
	}

	/**
	 * 从可类化数据读取器中解析用户资源控制选项参数
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();

		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			short who = reader.readShort();
			array.add(who);
		}
		return reader.getSeek() - seek;
	}

	/**
	 * 根据传入的用户资源控制，生成它的副本
	 * @param that 用户资源控制实例
	 */
	private Control(Control that) {
		this();
		array.addAll(that.array);
	}

	/**
	 * 构造默认的用户资源控制
	 */
	public Control() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析用户资源控制选项参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public Control(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从标记化读取器中取出用户资源控制选项参数
	 * @param reader 标记化读取器
	 */
	public Control(MarkReader reader) {
		this();
		reader.readObject(this);
	}

	/**
	 * 设置一组控制选项
	 * @param whos  控制选项数组
	 * @return 返回新增成员数目
	 */
	private int set(short[] whos) {
		int size = array.size();
		for (int i = 0; whos != null && i < whos.length; i++) {
			add(whos[i]);
		}		
		return array.size() - size;
	}

	/**
	 * 判断是允许的操作选项
	 * @param whos  全部操作选项
	 * @return  全部允许返回真，否则假
	 */
	public boolean allow(short[] whos) {
		int count = 0;
		for (short who : whos) {
			boolean success = array.contains(new java.lang.Short(who));
			if (success) count++;
		}
		return count == whos.length;
	}

	/**
	 * 判断是允许的操作选项
	 * @param who 操作选项
	 * @return  返回真或者假
	 */
	public boolean allow(short who) {
		if (who == ControlTag.DBA) {
			return allow(ControlTag.DBA_OPTIONS);
		} else {
			return allow(new short[] { who });
		}
	}

	/**
	 * 判断是表级用户资源控制选项
	 * @param who  操作选项
	 * @return  返回真或者假
	 */
	private boolean isTableOption(short who) {
		for (int i = 0; i < ControlTag.TABLE_OPTIONS.length; i++) {
			if (ControlTag.TABLE_OPTIONS[i] == who) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 设置表级用户资源控制。在设置前，对传入的参数进行检查，如果存在不匹配的现象，拒绝操作。
	 * @param whos 操作选项数组
	 * @return 设置成功返回“真”，否则“假”。
	 */
	private boolean setTableOption(short[] whos) {
		boolean all = false;
		// 检测参数是否全部有效
		for (short who : whos) {
			if (who == ControlTag.ALL) {
				all = true;
			} else if (!isTableOption(who)) {
				return false;
			}
		}
		if (all) {
			set(ControlTag.TABLE_OPTIONS);
		} else {
			set(whos);
		}
		return true;
	}

	/**
	 * 判断是数据库用户资源控制选项
	 * @param who 操作选项
	 * @return 匹配返回“真”，否则“假”。
	 */
	private boolean isSchemaOption(int who) {
		for (int i = 0; i < ControlTag.SCHEMA_OPTIONS.length; i++) {
			if (ControlTag.SCHEMA_OPTIONS[i] == who) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 检查/设置数据库权限
	 * @param whos 操作选项数组
	 * @return 返回真或者假
	 */
	private boolean setSchemaOption(short[] whos) {
		boolean all = false;
		// 检测参数是否全部有效
		for (int who : whos) {
			if (who == ControlTag.ALL) {
				all = true;
			} else if (!isSchemaOption(who)) {
				return false;
			}
		}
		if (all) {
			set(ControlTag.SCHEMA_OPTIONS);
		} else {
			set(whos);
		}
		return true;
	}

	/**
	 * 判断用户账号用户资源控制选项
	 * @param who 操作选项
	 * @return 成功返回真，否则假
	 */
	private boolean isUserOption(short who) {
		for (int i = 0; i < ControlTag.DBA_OPTIONS.length; i++) {
			if (ControlTag.DBA_OPTIONS[i] == who) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 检查/设置注册用户权限
	 * @param whos 操作选项数组
	 * @return 成功返回真，否则假
	 */
	private boolean setUserOption(short[] whos) {
		boolean dba = false;
		boolean all = false;
		boolean member = false;
		// 检查参数是否合法，必须是在指定选项内
		for (short who : whos) {
			if (who == ControlTag.DBA) {
				dba = true;
			} else if (who == ControlTag.ALL) {
				all = true;
			} else if (who == ControlTag.MEMBER) {
				member = true;
			} else if (!isUserOption(who)) {
				return false;
			}
		}
		if (dba) {
			set(ControlTag.DBA_OPTIONS);
		} else if (all) {
			set(ControlTag.ALL_OPTIONS);
		} else if(member) {
			set(ControlTag.MEMBER_OPTIONS);
		} else {
			set(whos);
		}
		return true;
	}

	/**
	 * 根据权限级别，设置操作选项
	 * @param rank 操作级别
	 * @param whos 操作选项数组
	 * @return 成功返回真，否则假
	 */
	public boolean setOption(int rank, short[] whos) {
		boolean success = false;
		switch (rank) {
		case PermitTag.USER_PERMIT:
			success = setUserOption(whos);
			break;
		case PermitTag.SCHEMA_PERMIT:
			success = setSchemaOption(whos);
			break;
		case PermitTag.TABLE_PERMIT:
			success = setTableOption(whos);
			break;
		}
		return success;
	}

	/**
	 * 增加一个操作选项
	 * @param who 操作选项
	 * @return 成功返回真，否则假
	 */
	public boolean add(short who) {
		return who > 0 && array.add(new java.lang.Short(who));
	}

	/**
	 * 增加一组操作选项
	 * @param a 操作选项集合
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<java.lang.Short> a) {
		int size = array.size();
		for (java.lang.Short e : a) {
			add(e.shortValue());
		}
		return array.size() - size;
	}

	/**
	 * 增加一组操作选项
	 * @param e 用户资源控制
	 * @return 返回新增成员数目
	 */
	public int addAll(Control e) {
		return addAll(e.array);
	}

	/**
	 * 删除一个控制选项
	 * @param who 控制选项
	 * @return 删除成功返回真，否则假
	 */
	public boolean remove(short who) {
		return array.remove(new java.lang.Short(who));
	}

	/**
	 * 删除一组控制选项选项
	 * @param a 控制选项集合
	 * @return 返回删除的成员数目
	 */
	public int removeAll(Collection<Short> a) {
		int size = array.size();
		array.removeAll(a);
		return size - array.size();
	}

	/**
	 * 删除一组资源控制选项
	 * @param e 控制选项集合
	 * @return 返回删除的成员数目
	 */
	public int removeAll(Control e) {
		return removeAll(e.array);
	}

	/**
	 * 返回全部资源控制选项
	 * @return 资源控制列表
	 */
	public List<Short> list() {
		return new ArrayList<Short>(array);
	}
	
	/**
	 * 输出资源控制选项数组
	 * @return short数组
	 */
	public short[] toArray() {
		List<Short> all = list();
		short[] a = new short[all.size()];
		for (int i = 0; i < all.size(); i++) {
			a[i] = all.get(i).shortValue();
		}
		return a;
	}

	/**
	 * 返回当前用户资源控制选项的成员数目
	 * @return 整型值
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断集合是否空
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 返回当前用户资源控制的数据副本
	 * @return Control实例
	 */
	public Control duplicate() {
		return new Control(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}


	//	public static void main(String[] args) {
	//		Control a1 = new Control();
	//		a1.add(ControlOption.CONDUCT);
	//		a1.add(ControlOption.ALTER_USER);
	//
	//		ClassWriter writer = new ClassWriter();
	//		a1.build(writer);
	//		byte[] b = writer.effuse();
	//		System.out.printf("size is %d\n", b.length);
	//
	//		ClassReader reader = new ClassReader(b);
	//		a1.resolve(reader);
	//		System.out.printf("seek is %d\n", reader.getSeek());
	//	}
}