/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import java.io.*;
import java.util.*;

import com.laxcus.command.access.table.*;
import com.laxcus.access.schema.Space;
import com.laxcus.access.schema.Table;
import com.laxcus.law.rule.RuleOperator;
import com.laxcus.law.rule.TableRuleItem;
import com.laxcus.log.client.Logger;
import com.laxcus.util.charset.*;
import com.laxcus.util.tip.FaultTip;

/**
 * 数据导入命令调用器。<br>
 * 把本地磁盘上的数据导入到计算机集群。
 * 
 * @author scott.liang
 * @version 1.0 5/11/2019
 * @since research 1.0
 */
public class MeetImportEntityInvoker extends MeetRuleInvoker {
	
	/**
	 * 构造数据导入命令调用器，指定命令
	 * @param cmd 数据导入命令
	 */
	public MeetImportEntityInvoker(ImportEntity cmd) {
		super(cmd);
		initRule();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ImportEntity getCommand() {
		return (ImportEntity) super.getCommand();
	}

	/**
	 * 建立表规则
	 */
	private void initRule() {
		ImportEntity cmd = getCommand();
		Space space = cmd.getSpace();
		// 保存成“共享写”的事务规则
		TableRuleItem item = new TableRuleItem(RuleOperator.SHARE_WRITE, space);
		addRule(item);
	}
	
	/**
	 * 如果没有定义，检查文件字符集
	 * @return 检测成功返回真，否则假
	 */
	private int checkCharset(int who, File file) {
		// 判断定义了字符集
		if (CharsetType.isCharset(who)) {
			return who;
		}

		// 实时检查字符集
		CharsetChecker checker = new CharsetChecker();
		String charset = checker.check(file);
		boolean success = (charset != null);
		if (success) {
			who = CharsetType.translate(charset);
			success = (!CharsetType.isNone(who));
			if (success) {
				return who;
			}
		}
		
		Logger.debug(this, "checkCharset", success, "charset is %s", charset);

		return CharsetType.NONE;
	}
	
	/**
	 * 判断表存在！
	 * @param space 表名
	 * @return 返回真或者假
	 */
	private boolean hasTable(Space space) {
		Table table = getStaffPool().findTable(space);
		return (table != null);
	}
	
	/**
	 * 执行事务阶段中的数据处理 <br>
	 * 这个方法由子类根据各自需求去实现。
	 * 
	 * @return 当数据处理工作全部完成时，返回真（无论数据处理是错误或者失败）；否则假。<b>特别说明：数据处理错误也要返回“真”。<b>
	 */
	@Override
	protected boolean process() {
		ImportEntity cmd = getCommand();
		
		// 判断表存在
		Space space = cmd.getSpace();
		if (!hasTable(space)) {
			faultX(FaultTip.NOTFOUND_X, space);
			return true; // 出错，停止处理
		}
		
		List<File> files = cmd.list();
		
		// 命令集
		ArrayList<SingleImportEntity> a = new ArrayList<SingleImportEntity>();
		
		// 检查字符集，任意一个有错误，忽略全部！
		for (File file : files) {
			int charset = checkCharset(cmd.getCharset(), file);
			if (CharsetType.isNone(charset)) {
				faultX(FaultTip.ILLEGAL_CHARSET_X, file);
				continue;
			}
			// 保存实例
			SingleImportEntity sub = new SingleImportEntity(space);
			sub.setFile(file);
			sub.setType(cmd.getType());
			sub.setCharset(charset);
			sub.setRows(cmd.getRows());
			a.add(sub);
		}
		
		ArrayList<SingleImportEntityResult> array = new ArrayList<SingleImportEntityResult>();
		// 逐一发送
		for(SingleImportEntity sub : a) {
			// 生成钩子和转发命令
			SingleImportEntityHook hook = new SingleImportEntityHook();
			ShiftSingleImportEntity shift = new ShiftSingleImportEntity(sub, hook);
			shift.setIssuer(getIssuer());
			
			// 交给句柄处理
			boolean success = press(shift);
			if(!success) {
				SingleImportEntityResult res = new SingleImportEntityResult(false, sub.getFile(), -1);
				array.add(res);
				continue;
			}
			// 进行等待
			hook.await();
			SingleImportEntityResult res = hook.getProduct();
			if (res != null) {
				array.add(res);
			}
		}

		// 打印结果
		printResult(array);

		// 退出！
		return true;
	}
	
	/**
	 * 打印写入记录
	 * @param array 记录结果
	 */
	private void printResult(List<SingleImportEntityResult> array) {
		// 打印时间
		printRuntime();
		// 生成表格标题
		createShowTitle(new String[] { "IMPORT-ENTITY/STATUS",
				"IMPORT-ENTITY/TABLE", "IMPORT-ENTITY/FILE",
				"IMPORT-ENTITY/ROWS" });

		ImportEntity cmd = getCommand();

		long total = 0;
		for (SingleImportEntityResult e : array) {
			if (e.getRows() > 0) {
				total += e.getRows();
			}
			String filename = e.getFile().toString();
			Object[] a = new Object[] { e.isSuccessful(), cmd.getSpace(), filename, e.getRows() };
			// 写入磁盘
			printRow(a);
		}

		// 统计值
		Object[] a = new Object[] { "", cmd.getSpace(), "--", total };
		printRow(a);

		// 输出全部记录
		flushTable();
	}
	
}