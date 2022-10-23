/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.platform.listener;

/**
 * 命令核准适配器
 * 
 * @author scott.liang
 * @version 1.0 1/18/2022
 * @since laxcus 1.0
 */
public class CommandAuditorAdapter implements CommandAuditor {

	/**
	 * 构造命令核准适配器
	 */
	public CommandAuditorAdapter() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.platform.CommandAuditor#confirm()
	 */
	@Override
	public boolean confirm() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.platform.CommandAuditor#confirm(java.lang.String)
	 */
	@Override
	public boolean confirm(String content) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.platform.CommandAuditor#confirm(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean confirm(String title, String content) {
		// TODO Auto-generated method stub
		return false;
	}


}
