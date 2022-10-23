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
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * “DROP FIELD”的本地转发命令。
 * 这个命令由HOME站点的CALL站点管理池发出，目标是它下属的CALL站点，通知它们删除此这个站点关联的元数据。
 * 
 * @author scott.liang
 * @version 1.1 6/2/2015
 * @since laxcus 1.0
 */
public class ShiftDropField extends ShiftCommand {

	private static final long serialVersionUID = -8118111915781640881L;

	/** 目标地址集合 **/
	private TreeSet<Node> sites = new TreeSet<Node>();
	
	/**
	 * 根据传入的“DROP FIELD”命令实例，生成它的数据副本
	 * @param that ShiftDropField实例
	 */
	protected ShiftDropField(ShiftDropField that) {
		super(that);
		sites.addAll(that.sites);
	}

	/**
	 * 构造“DROP FIELD”的本地转发命令，指定全部参数
	 * @param a 站点地址数组
	 * @param cmd “DROP FIELD”命令实例
	 */
	public ShiftDropField(Collection<Node> a, DropField cmd) {
		super(cmd);
		addAll(a);
	}

	/**
	 * 保存一个目标站点
	 * @param e Node实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(Node e) {
		Laxkit.nullabled(e);

		return sites.add(e);
	}

	/**
	 * 保存一批目标站点
	 * @param a Node数组
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<Node> a) {
		int size = sites.size();
		for (Node e : a) {
			add(e);
		}
		return sites.size() - size;
	}

	/**
	 * 输出全部目标地址
	 * @return Node列表
	 */
	public List<Node> getSites() {
		return new ArrayList<Node>(sites);
	}

	/**
	 * 返回“DROP FIELD”命令实例
	 * @return DropField实例
	 */
	public DropField getCommand() {
		return (DropField) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShiftDropField duplicate() {
		return new ShiftDropField(this);
	}
}
