/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.register;

import java.util.*;

import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.lock.*;
import com.laxcus.util.naming.*;

/**
 * 注册表
 * 
 * @author scott.liang
 * @version 1.0 7/15/2021
 * @since laxcus 1.0
 */
public final class RTable extends MutexHandler implements Classable, Comparable<RTable> {

	/** 单元名称 **/
	private Naming name;
	
	/** 单元数组 **/
	private ArrayList<RElement> array = new ArrayList<RElement>();

	/**
	 * 构造注册表
	 */
	private RTable() {
		super();
	}
	
	/**
	 * 构造注册表，解析参数
	 * @param reader 可类化读取器
	 */
	protected RTable(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 构造注册表，指定名称
	 * @param name
	 */
	protected RTable(Naming name) {
		this();
		setName(name);
	}
	
	/**
	 * 构造注册表，指定名称
	 * @param name
	 */
	protected RTable(String name) {
		this();
		setName(new Naming(name));
	}
	
	/**
	 * 设置名称
	 * @param e
	 */
	private void setName(Naming e) {
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
	 * 只能保存一个
	 * @param element
	 * @return 成功返回真，否则假
	 */
	public boolean add(RElement element) {
		super.lockSingle();
		try {
			array.remove(element);
			return array.add(element);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return false;
	}

	/**
	 * 删除注册成员
	 * @param element
	 * @return
	 */
	public boolean remove(RElement element) {
		super.lockSingle();
		try {
			return array.remove(element);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return false;
	}

	/**
	 * 删除成员
	 * @param name
	 * @return
	 */
	public boolean remove(Naming name) {
		ArrayList<RElement> a = new ArrayList<RElement>();
		
		// 锁定
		super.lockSingle();
		try {
			Iterator<RElement> iterator = array.iterator();
			while (iterator.hasNext()) {
				RElement element = iterator.next();
				// 判断匹配
				if (Laxkit.compareTo(element.getName(), name) == 0) {
					a.add(element);
				}
			}
			// 删除全部
			if (a.size() > 0) {
				array.removeAll(a);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return a.size() > 0;
	}
	
	/**
	 * 找到注册成员
	 * @param name
	 * @return
	 */
	public RElement find(Naming name) {
		// 锁定
		super.lockMulti();
		try {
			for (RElement e : array) {
				if (Laxkit.compareTo(e.getName(), name) == 0) {
					return e;
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}
	
	/**
	 * 判断包含
	 * @param name 名称
	 * @return 返回真或者假
	 */
	public boolean contains(Naming name) {
		return find(name) != null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(RTable that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(name, that.name);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter w) {
		final int size = w.size();

		// 参数
		w.writeObject(name);
		w.writeInt(array.size());
		for (RElement element : array) {
			w.writeObject(element);
		}
		// 返回写入的数据长度
		return w.size() - size;
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
			RElement element = new RElement(reader);
			add(element);
		}
		
		// 返回读取的字节长度
		return reader.getSeek() - seek;
	}

}