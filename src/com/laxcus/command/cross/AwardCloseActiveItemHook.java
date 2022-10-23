/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cross;

import com.laxcus.command.*;
import com.laxcus.site.Node;

/**
 * 通知被授权人撤销激活单元钩子
 * 
 * @author scott.liang
 * @version 1.0 5/27/2019
 * @since laxcus 1.0
 */
public class AwardCloseActiveItemHook extends CommandHook {
	
	/** ACCOUNT 站点地址 **/
	private Node remote;

	/**
	 * 销毁分布资源
	 */
	protected void destroy() {
		super.destroy();
		remote = null;
	}

	/**
	 * 构造通知被授权人撤销激活单元钩子
	 */
	public AwardCloseActiveItemHook() {
		super();
	}

	/**
	 * 设置已经接收到的ACCOUNT站点
	 * @param e Node实例 
	 */
	public void setAccountSite(Node e) {
		remote = e;
	}

	/**
	 * 返回ACCOUNT站点
	 * @return Node实例 
	 */
	public Node getAccountSite() {
		return remote;
	}

	/**
	 * 返回通知被授权人撤销激活单元结果
	 * @return 授权单元实例，或者空指针
	 */
	public ShareCrossProduct getProduct() {
		return (ShareCrossProduct) super.getResult();
	}

}