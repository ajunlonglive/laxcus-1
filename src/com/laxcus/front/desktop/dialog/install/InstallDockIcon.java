/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.dialog.install;

/**
 * 部署图标到应用坞
 * 
 * @author scott.liang
 * @version 1.0 10/7/2021
 * @since laxcus 1.0
 */
class InstallDockIcon {

	boolean deploy;
	
	String text;
	
	String attachMenu;
	
	public InstallDockIcon(String text, boolean deploy) {
		super();
		setText(text);
		setDeploy(deploy);
	}
	
	public void setAttachMenu(String s) {
		attachMenu = s;
	}

	public String getAttachMenu() {
		return attachMenu;
	}

	public void setText(String s) {
		text = s;
	}

	public String getText() {
		return text;
	}

	public boolean isDeploy() {
		return deploy;
	}

	public void setDeploy(boolean b) {
		deploy = b;
	}
}