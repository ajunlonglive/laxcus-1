/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.watch;

import com.laxcus.site.*;
import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 检索站点在线资源。<br><br>
 * 
 * 被管理员使用，从WATCH站点发出，目标是除WATCH/FRONT之外的所有节点。<br>
 * 被检索的站点允许不在WATCH站点监视范围内，即允许WATCH跨管理使用。<br>
 * 
 * @author scott.liang
 * @version 1.0 4/22/2018
 * @since laxcus 1.0
 */
public final class SeekOnlineResource extends Command {

	private static final long serialVersionUID = -5759362974204640798L;

	/** 目标站点 **/
	private Node node;

	/**
	 * 构造默认的检索站点在线资源
	 */
	public SeekOnlineResource() {
		super();
	}

	/**
	 * 构造检索站点在线资源，指定目标站点地址
	 * @param node 目标站点地址
	 */
	public SeekOnlineResource(Node node) {
		this();
		setSite(node);
	}

	/**
	 * 生成传入实例的数据副本
	 * @param that SeekOnlineResource实例
	 */
	private SeekOnlineResource(SeekOnlineResource that) {
		super(that);
		node = that.node;
	}

	/**
	 * 设置目标节点
	 * @param e 目标节点
	 */
	public void setSite(Node e) {
		Laxkit.nullabled(e);
		node = e;
	}

	/**
	 * 返回目标节点（命令被发送的节点）
	 * @return 目标节点
	 */
	public Node getSite() {
		return node;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SeekOnlineResource duplicate() {
		return new SeekOnlineResource(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
	
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		
	}

}