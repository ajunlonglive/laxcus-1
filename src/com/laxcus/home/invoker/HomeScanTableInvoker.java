/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.scan.*;
import com.laxcus.home.pool.*;
import com.laxcus.site.*;
import com.laxcus.util.set.*;

/**
 * 扫描数据表调用器。<br>
 * 这个调用器接受来自WATCH/TOP站点的调用。TOP站点的ScanSchema命令会转换成AnayseTable命令再发送给HOME站点。<br>
 * 
 * @author scott.liang
 * @version 1.0 12/2/2013
 * @since laxcus 1.0
 */
public class HomeScanTableInvoker extends HomeScanReferenceInvoker {

	/**
	 * 构造扫描数据表调用器，指定命令
	 * @param cmd 扫描数据表命令
	 */
	public HomeScanTableInvoker(ScanTable cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ScanTable getCommand() {
		return (ScanTable) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ScanTable cmd = getCommand();
		// 筛选出匹配的站点和命令
		Map<Node, ScanTable> cmds = (cmd.hasSites() ? choice(cmd) : full(cmd));
		// 发送给DATA站点
		return distribute(cmds);
	}

	/**
	 * 筛选出匹配的数据表名和站点，生成命令和返回
	 * @param cmd ScanTable实例
	 * @return 新的站点/命令映像
	 */
	private Map<Node, ScanTable> choice(ScanTable cmd) {
		// 准备发布的站点和命令
		Map<Node, ScanTable> cmds = new TreeMap<Node, ScanTable>();
		// 全部数据表
		List<Space> spaces = DataOnHomePool.getInstance().getSpaces();

		// 筛选出匹配的数据表
		ArrayList<Space> array = new ArrayList<Space>();
		for (Space e : spaces) {
			// 如果命令包含，保存它
			if(cmd.contains(e)) {
				array.add(e);
			}
		}

		// 筛选出匹配的站点
		for (Space space : array) {
			// 查找关联的DATA站点
			NodeSet set = DataOnHomePool.getInstance().findSites(space);
			List<Node> nodes = (set == null ? null : set.show());
			if(nodes == null){
				continue;
			}
			// 找到匹配的DATA站点
			for (Node node : nodes) {
				// 如果命令中没有指定，忽略，继续下一个
				if (!cmd.contains(node)) {
					continue;
				}
				// 生成新命令
				ScanTable sub = cmds.get(node);
				if (sub == null) {
					sub = new ScanTable();
					cmds.put(node, sub);
				}
				sub.add(space);
			}
		}
		return cmds;
	}

	/**
	 * 筛选出数据表名匹配的站点，生成命令返回
	 * @param cmd ScanTable实例
	 * @return 新的站点/命令映像
	 */
	private Map<Node, ScanTable> full(ScanTable cmd) {
		// 准备发布的站点和命令
		Map<Node, ScanTable> cmds = new TreeMap<Node, ScanTable>();
		// 全部数据表
		List<Space> spaces = DataOnHomePool.getInstance().getSpaces();

		// 筛选出匹配的数据表
		ArrayList<Space> array = new ArrayList<Space>();
		for (Space e : spaces) {
			// 如果命令包含，保存它
			if(cmd.contains(e)) {
				array.add(e);
			}
		}

		// 取出全部DATA站点，生成新命令
		for (Space space : array) {
			// 查找关联的DATA站点
			NodeSet set = DataOnHomePool.getInstance().findSites(space);
			List<Node> nodes = (set == null ? null : set.show());
			if (nodes == null) { 
				continue;
			}
			for (Node node : nodes) {
				ScanTable sub = cmds.get(node);
				if (sub == null) {
					sub = new ScanTable();
					cmds.put(node, sub);
				}
				sub.add(space);
			}
		}
		// 返回结果
		return cmds;
	}

}