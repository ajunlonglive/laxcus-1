/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.watch;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 查询LAXCUS集群成员。<br>
 * 成员包括注册成员/在线成员、节点地址。
 * 
 * WATCH节点在登录到HOME/BANK子域集群时使用。
 * 
 * @author scott.liang
 * @version 1.0 1/11/2020
 * @since laxcus 1.0
 */
public final class AskClusterMember extends Command {

	private static final long serialVersionUID = -3702451064998518286L;

	/** WATCH节点地址 **/
	private Node remote;

	/**
	 * 构造默认的查询LAXCUS集群成员命令
	 */
	public AskClusterMember() {
		super();
	}

	/**
	 * 构造默认的查询LAXCUS集群成员命令
	 */
	public AskClusterMember(boolean sound) {
		this();
		setSound(sound);
	}
	
	/**
	 * 构造查询LAXCUS集群成员命令，指定WATCH节点地址地址
	 * @param remote WATCH节点地址地址
	 */
	public AskClusterMember(Node remote) {
		this();
		setRemote(remote);
	}

	/**
	 * 生成传入实例的数据副本
	 * @param that AskClusterMember实例
	 */
	private AskClusterMember(AskClusterMember that) {
		super(that);
		remote = that.remote;
	}

	/**
	 * 设置WATCH节点
	 * @param e WATCH节点
	 */
	public void setRemote(Node e) {
		Laxkit.nullabled(e);
		remote = e;
	}

	/**
	 * 返回WATCH节点（命令被发送的节点）
	 * @return WATCH节点
	 */
	public Node getRemote() {
		return remote;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public AskClusterMember duplicate() {
		return new AskClusterMember(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInstance(remote);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		remote = reader.readInstance(Node.class);
	}

}