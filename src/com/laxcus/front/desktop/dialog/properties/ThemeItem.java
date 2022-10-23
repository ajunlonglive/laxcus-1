/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.dialog.properties;

import java.io.*;

import com.laxcus.util.skin.*;

/**
 *
 * @author scott.liang
 * @version 1.0 2021-7-21
 * @since laxcus 1.0
 */
class ThemeItem implements Serializable {

	private static final long serialVersionUID = 1422585602601354183L;
	
	private SkinToken token;
	
	public ThemeItem() {
		super();
	}
	
	public ThemeItem(SkinToken token) {
		this();
		this.setToken(token);
	}
	
	public void setToken(SkinToken s) {
		token = s;
	}
	
	public SkinToken getToken(){
		return this.token;
	}
	
	public String getText() {
		return token.getTitle();
	}
	 
}
