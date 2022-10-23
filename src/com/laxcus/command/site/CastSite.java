/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 广播注册站点。<br><br>
 * 
 * 这个命令由TOP/HOME/BANK的管理站点发出，目标是TOP/HOME/BANK自己的关联监视站点，或者下属的WATCH站点。
 * TOP/HOME/BANK站点告知它们，某个站点被“加入/退出/销毁”。其中“加入/退出”属于正常状态，“销毁”是故障状态。
 * 
 * @author scott.liang
 * @version 1.1 11/15/2015
 * @since laxcus 1.0
 */
public abstract class CastSite extends Command {

	private static final long serialVersionUID = 5329249581579337112L;

	/** 注册站点地址 **/
	private Node site;

	/**
	 * 构造默认和私有的广播注册站点命令。
	 */
	protected CastSite() {
		super();
	}

	/**
	 * 根据传入的广播注册站点命令实例，生成它的数据副本
	 * 
	 * @param that 广播注册站点实例
	 */
	protected CastSite(CastSite that) {
		super(that);
		site = that.site;
	}

	/**
	 * 构造广播注册站点命令实例，指定注册站点地址
	 * 
	 * @param site 注册站点地址
	 */
	protected CastSite(Node site) {
		this();
		setSite(site);
	}

	/**
	 * 设置注册站点地址
	 * @param e Node实例
	 */
	public void setSite(Node e) {
		Laxkit.nullabled(e);

		site = e;
	}

	/**
	 * 返回注册站点地址
	 * @return Node实例
	 */
	public Node getSite() {
		return site;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(site);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		site = new Node(reader);
	}

}