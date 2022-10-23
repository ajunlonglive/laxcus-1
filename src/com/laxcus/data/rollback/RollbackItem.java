/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.rollback;

import java.io.*;

import com.laxcus.echo.invoke.*;
import com.laxcus.util.*;

/**
 * 回滚文件单元
 * 
 * @author scott.liang
 * @version 1.1 7/23/2016
 * @since laxcus 1.0
 */
public abstract class RollbackItem implements Cloneable, Comparable<RollbackItem>, Serializable {

	private static final long serialVersionUID = -5048856923694364644L;

	/** 文件路径 **/
	private String path;

	/** 调用器编号 **/
	private long invokerId;

	/**
	 * 构造默认的回滚文件单元
	 */
	protected RollbackItem() {
		super();
	}

	/**
	 * 生成回滚文件单元的数据副本
	 * @param that
	 */
	protected RollbackItem(RollbackItem that) {
		this();
		path = that.path;
		invokerId = that.invokerId;
	}

	/**
	 * 设置文件路径
	 * @param e
	 */
	public void setPath(String e) {
		path = e;
	}

	/**
	 * 返回文件路径
	 * @return
	 */
	public String getPath() {
		return path;
	}

	/**
	 * 返回文件
	 * @return
	 */
	public File getFile() {
		if (path == null) {
			return null;
		}
		return new File(path);
	}

	/**
	 * 判断文件存在
	 * @return
	 */
	public boolean exists() {
		if (path == null) {
			return false;
		}
		File e = new File(path);
		return e.exists();
	}

	/**
	 * 删除磁盘文件
	 * @return - 成功返回真，否则假
	 */
	public boolean delete() {
		if (path == null) {
			return false;
		}
		File file = new File(path);
		boolean success = file.exists();
		if (success) {
			success = file.delete();
		}
		return success;
	}

	/**
	 * 设置调用器编号。小于0是错误
	 * @param who
	 */
	public final void setInvokerId(long who) {
		if (InvokerIdentity.isInvalid(who)) {
			throw new IllegalValueException("illegal invoker identity %d", who);
		}
		invokerId = who;
	}

	/**
	 * 返回调用器编号
	 * @return
	 */
	public final long getInvokerId() {
		return invokerId;
	}

	/**
	 * 根据调用器编号，判断异步调用器处于存活中
	 * @return - 返回真或者假（调用器编号大于等于0是存活中）
	 */
	public final boolean isLiving() {
		return InvokerIdentity.isValid(invokerId);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(RollbackItem that) {
		if (that == null) {
			return 1;
		}
		
		// 首先比较文件的最后修改时期，以最新的排在前面。否则比较调用器编号
		boolean success = (path != null && that.path != null);
		if (success) {
			File f1 = new File(path);
			File f2 = new File(that.path);
			success = (f1.exists() && f2.exists());
			// 时间最大排在最前面
			if (success) {
				long time1 = f1.lastModified();
				long time2 = f2.lastModified();
				return (time1 < time2 ? 1 : (time1 > time2 ? -1 : 0));
			}
		}
		
		// 比较调用器编号
		return Laxkit.compareTo(invokerId, that.invokerId);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) (invokerId >>> 32 ^ invokerId);
	}

	/**
	 * 生成回滚文件单元的数据副本
	 * @return
	 */
	public abstract RollbackItem duplicate();
}