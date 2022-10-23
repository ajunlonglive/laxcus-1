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
 * 扫描数据库调用器。<br>
 * 这个调用器只会接受来自WATCH站点的调用。
 * 
 * @author scott.liang
 * @version 1.0 12/2/2013
 * @since laxcus 1.0
 */
public class HomeScanSchemaInvoker extends HomeScanReferenceInvoker {

	/**
	 * 构造扫描数据库调用器，指定命令
	 * @param cmd 扫描数据库命令
	 */
	public HomeScanSchemaInvoker(ScanSchema cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ScanSchema getCommand() {
		return (ScanSchema) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ScanSchema cmd = getCommand();

		// 筛选出匹配的站点和命令
		Map<Node, ScanTable> cmds = (cmd.hasSites() ? choice(cmd) : full(cmd));

		// 发送给DATA站点
		return distribute(cmds);
	}

	/**
	 * 过滤出匹配的数据表名
	 * @param cmd ScanSchema实例
	 * @return 返回数据表名列表
	 */
	private List<Space> filte(ScanSchema cmd) {
		// 全部数据表
		List<Space> spaces = DataOnHomePool.getInstance().getSpaces();
		// 筛选出匹配的数据表
		ArrayList<Space> array = new ArrayList<Space>();
		for (Space e : spaces) {
			// 如果命令包含，保存它
			if (cmd.contains(e.getSchema())) {
				array.add(e);
			}
		}
		return array;
	}

	/**
	 * 筛选出匹配的数据表名和站点，生成命令和返回
	 * @param cmd ScanSchema实例
	 * @return 新的站点/命令映像
	 */
	private Map<Node, ScanTable> choice(ScanSchema cmd) {
		// 过滤数据表
		List<Space> spaces = filte(cmd);

		// 筛选出匹配的站点
		Map<Node, ScanTable> array = new TreeMap<Node, ScanTable>();
		for (Space space : spaces) {
			// 查找关联的DATA站点
			NodeSet set = DataOnHomePool.getInstance().findSites(space);
			List<Node> sites = (set == null ? null : set.show());
			// 忽略空指针
			if (sites == null) {
				continue;
			}
			// 找到匹配的DATA站点
			for (Node node : sites) {
				// 如果命令中没有指定站点，忽略，继续下一个
				if (!cmd.contains(node)) {
					continue;
				}
				// 生成新命令
				ScanTable sub = array.get(node);
				if (sub == null) {
					sub = new ScanTable();
					array.put(node, sub);
				}
				sub.add(space);
			}
		}
		return array;
	}

	/**
	 * 筛选出数据表名匹配的站点，生成命令返回
	 * @param cmd ScanSchema实例
	 * @return 新的站点/命令映像
	 */
	private Map<Node, ScanTable> full(ScanSchema cmd) {
		// 过滤数据表
		List<Space> spaces = filte(cmd);

		// 取出全部DATA站点，生成新命令
		Map<Node, ScanTable> array = new TreeMap<Node, ScanTable>();
		for (Space space : spaces) {
			// 查找关联的DATA站点
			NodeSet set = DataOnHomePool.getInstance().findSites(space);
			List<Node> sites = (set == null ? null : set.show());
			// 忽略空指针
			if (sites == null) {
				continue;
			}
			// 取出全部站点，生成命令
			for (Node node : sites) {
				ScanTable sub = array.get(node);
				if (sub == null) {
					sub = new ScanTable();
					array.put(node, sub);
				}
				sub.add(space);
			}
		}
		// 返回结果
		return array;
	}

}