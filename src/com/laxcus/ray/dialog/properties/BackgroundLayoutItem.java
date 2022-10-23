/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.dialog.properties;

import java.io.*;

/**
 *
 * @author scott.liang
 * @version 1.0 2021-6-17
 * @since laxcus 1.0
 */
class BackgroundLayoutItem implements Serializable {

	private static final long serialVersionUID = 1422585602601354183L;

	private int layout;
	
	private String text;
	
	public BackgroundLayoutItem() {
		super();
	}
	
	public BackgroundLayoutItem(String text, int layout) {
		this();
		this.setText(text);
		this.setLayout(layout);
	}
	
	public void setLayout(int who) {
		this.layout = who;
	}
	
	public int getLayout(){
		return this.layout;
	}
	
	public void setText(String s) {
		text = s;
	}
	
	public String getText(){
		return this.text;
	}
	 
}
