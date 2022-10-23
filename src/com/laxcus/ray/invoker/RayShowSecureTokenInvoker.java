/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import java.util.*;

import com.laxcus.command.secure.*;
import com.laxcus.fixp.secure.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.hash.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 显示密钥令牌调用器
 * 
 * @author scott.liang
 * @version 1.0 2/13/2021
 * @since laxcus 1.0
 */
public class RayShowSecureTokenInvoker extends RayInvoker {

	/**
	 * 构造显示密钥令牌调用器，指定命令
	 * @param cmd 显示密钥令牌命令
	 */
	public RayShowSecureTokenInvoker(ShowSecureToken cmd) {
		super(cmd);
		// 指定快速处理
		cmd.setQuick(true);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShowSecureToken getCommand() {
		return (ShowSecureToken) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 发送到注册站点，再由注册站点分发命令
		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int index = findEchoKey(0);
		ShowSecureTokenProduct product = null;
		// 判断成功
		if (isSuccessObjectable(index)) {
			try {
				product = getObject(ShowSecureTokenProduct.class, index);
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		boolean success = (product != null);
		// 打印结果
		if (success) {
			print(product);
		} else {
			faultX(FaultTip.FAILED_X, getCommand());
		}

		return useful(success);
	}

	/**
	 * 显示节点数目
	 * @param count
	 */
	private void printCount(int count) {
		String key = getXMLContent("SHOW-SECURE-TOKEN/COUNT");
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, key));
		item.add(new ShowIntegerCell(1, count));
		addShowItem(item);
	}

	/**
	 * 显示节点数目
	 * @param site
	 */
	private void printSite(Node site) {
		String key = getXMLContent("SHOW-SECURE-TOKEN/SITE");
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, key));
		item.add(new ShowStringCell(1, site));
		addShowItem(item);
	}

	/**
	 * 令牌名称
	 * @param naming
	 */
	private void printName(Naming naming) {
		String key = getXMLContent("SHOW-SECURE-TOKEN/NAME");
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, key));
		item.add(new ShowStringCell(1, naming));
		addShowItem(item);
	}

	/**
	 * 安全检查类型
	 * @param family
	 */
	private void printCheck(int family) {
		String naming = SecureType.translate(family);
		String key = getXMLContent("SHOW-SECURE-TOKEN/CHECK");
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, key));
		item.add(new ShowStringCell(1, naming));
		addShowItem(item);
	}

	/**
	 * 处理模式
	 * @param mode
	 */
	private void printMode(int mode) {
		String naming = SecureMode.translate(mode);
		String key = getXMLContent("SHOW-SECURE-TOKEN/MODE");
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, key));
		item.add(new ShowStringCell(1, naming));
		addShowItem(item);
	}

	/**
	 * 地址范围
	 * @param range
	 */
	private void printRange(SecureRange range) {
		String key = getXMLContent("SHOW-SECURE-TOKEN/RANGE");
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, key));
		item.add(new ShowStringCell(1, range.toString()));
		addShowItem(item);
	}

	/**
	 * 私钥
	 * @param modulus 系数
	 * @param exponent 指数
	 */
	private void printPrivateKey(SHA256Hash modulus, SHA256Hash exponent) {
		String key = getXMLContent("SHOW-SECURE-TOKEN/PRIVATE-KEY/MODULUES");
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, key));
		item.add(new ShowStringCell(1, modulus.getHexText()));
		addShowItem(item);

		key = getXMLContent("SHOW-SECURE-TOKEN/PRIVATE-KEY/EXPONENT");
		item = new ShowItem();
		item.add(new ShowStringCell(0, key));
		item.add(new ShowStringCell(1, exponent.getHexText()));
		addShowItem(item);
	}

	/**
	 * 公钥
	 * @param modulus 系数
	 * @param exponent 指数
	 */
	private void printPublicKey(SHA256Hash modulus, SHA256Hash exponent) {
		String key = getXMLContent("SHOW-SECURE-TOKEN/PUBLIC-KEY/MODULUES");
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, key));
		item.add(new ShowStringCell(1, modulus.getHexText()));
		addShowItem(item);

		key = getXMLContent("SHOW-SECURE-TOKEN/PUBLIC-KEY/EXPONENT");
		item = new ShowItem();
		item.add(new ShowStringCell(0, key));
		item.add(new ShowStringCell(1, exponent.getHexText()));
		addShowItem(item);
	}

	/**
	 * 打印结果
	 * @param item
	 */
	private void print(ShowSecureTokenProduct product){
		// 显示运行时间
		printRuntime();

		// 生成标题
		createShowTitle(new String[] { "SHOW-SECURE-TOKEN/T1", "SHOW-SECURE-TOKEN/T2" });

		// 集群节点
		printCount(product.size());
		
		ArrayList<Node> array = new ArrayList<Node>();
		ShowSecureToken cmd = this.getCommand();
		// 输出全部...
		if (cmd.isAll()) {
			array.addAll(product.getSites());
		} else {
			array.addAll(cmd.list());
		}
		
		for (Node node : array) {
			ShowSecureTokenItem e = product.find(node);
			if (e == null) {
				continue;
			}
			printGap(2);
			// 服务器地址
			int index = 0;
			// 显示参数
			for (SecureTokenSlat slat : e.list()) {
				if (index > 0) printSubGap(2);
				printSite(e.getSite());
				printName(slat.getName());
				printCheck(slat.getFamily());
				printMode(slat.getMode());
				for (SecureRange range : slat.getRanges()) {
					printRange(range);
				}
				printPrivateKey(slat.getPrivateModulus(), slat.getPrivateExponent());
				printPublicKey(slat.getPublicModulus(), slat.getPublicExponent());
				index++;
			}
		}
		// 显示全部记录
		flushTable();
	}

	//	/**
	//	 * 打印结果
	//	 * @param item
	//	 */
	//	private void print(List<ShowSecureTokenItem> array) {
	//		// 显示运行时间
	//		printRuntime();
	//
	//		// 生成标题
	//		createShowTitle(new String[] { "SHOW-SECURE-TOKEN/STATUS", "SHOW-SECURE-TOKEN/SITE" });
	//
	//		// 集群节点
	//		printCount(array.size());
	//		if (array.size() > 0) {
	//			printGap(2);
	//		}
	//		
	//		for (ShowSecureTokenItem e : array) {
	//			ShowItem item = new ShowItem();
	//			// 图标
	//			item.add(createConfirmTableCell(0, e.isSuccessful()));
	//			item.add(new ShowStringCell(1, e.getSite()));
	//			// 保存单元
	//			addShowItem(item);
	//		}
	//		// 显示全部记录
	//		flushTable();
	//	}

}