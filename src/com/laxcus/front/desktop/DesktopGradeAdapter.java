/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop;

import com.laxcus.platform.listener.*;

/**
 * 级别适配器
 * 
 * @author scott.liang
 * @version 1.0 3/6/2022
 * @since laxcus 1.0
 */
public class DesktopGradeAdapter implements GradeListener {

	/**
	 * 构造级别适配器
	 */
	public DesktopGradeAdapter() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.platform.listener.GradeListener#isAdministrator()
	 */
	@Override
	public boolean isAdministrator() {
		return DesktopLauncher.getInstance().isAdministrator();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.platform.listener.GradeListener#isUser()
	 */
	@Override
	public boolean isUser() {
		return DesktopLauncher.getInstance().isUser();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.platform.listener.GradeListener#isOnline()
	 */
	@Override
	public boolean isOnline() {
		return DesktopLauncher.getInstance().isLogined();
	}

}
