/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.field;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.site.Node;

/**
 * 筛选元数据并投递到CALL站点。<br>
 * 
 * 这个命令由HOME站点发出，目标是DATA/WORK/BUILD站点，要求这三个站点将指定账号下的元数据，用“PushXXXField”命令投递到指定的CALL站点。
 * 
 * @author scott.liang
 * @version 1.1 7/12/2015
 * @since laxcus 1.0
 */
public final class SelectFieldToCall extends Command {
	
	private static final long serialVersionUID = 1134944343715219298L;

	/** CALL站点地址 **/
	private Node callSite;
	
	/** 用户签名 **/
	private Set<Siger> array = new TreeSet<Siger>();

	/**
	 * 构造默认的投递命令
	 */
	private SelectFieldToCall() {
		super();
	}

	/**
	 * 根据传入命令生成它的浅层数据副本
	 * @param that SelectFieldToCall实例
	 */
	private SelectFieldToCall(SelectFieldToCall that) {
		super(that);
		callSite = that.callSite;
		array.addAll(that.array);
	}

	/**
	 * 构造投递命令，指定CALL站点
	 * @param callSite Node实例
	 */
	public SelectFieldToCall(Node callSite) {
		this();
		setCallSite(callSite);
	}

	/**
	 * 从可类化数据读取器中解析投递命令
	 * @param reader 可类化数据读取器
	 * @since laxcus l.1
	 */
	public SelectFieldToCall(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置CALL站点地址
	 * @param e Node实例
	 */
	public void setCallSite(Node e) {
		Laxkit.nullabled(e);

		callSite = e;
	}

	/**
	 * 返回CALL站点地址
	 * @return Node实例
	 */
	public Node getCallSite() {
		return callSite;
	}

	/**
	 * 保存账号签名，不允许空指针
	 * @param e Siger实例
	 * @return 返回真或者假
	 */
	public boolean add(Siger e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 输出全部账号签名
	 * @return Siger列表
	 */
	public List<Siger> list() {
		return new ArrayList<Siger>(array);
	}

	/**
	 * 统计账号签名数目
	 * @return 账号签名数目
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SelectFieldToCall duplicate() {
		return new SelectFieldToCall(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(callSite);
		writer.writeInt(array.size());
		for (Siger e : array) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		callSite = new Node(reader);
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Siger e = new Siger(reader);
			array.add(e);
		}
	}

}