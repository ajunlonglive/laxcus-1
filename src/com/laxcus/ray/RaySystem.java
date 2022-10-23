/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray;

import java.io.*;

/**
 *
 * @author scott.liang
 * @version 1.0 8/4/2021
 * @since laxcus 1.0
 */
public class RaySystem {

	/**
	 * 建立目录
	 * @return
	 */
	public static File createRuntimeDirectory() {
		String bin = System.getProperty("user.dir");
		bin += "/../runtime";
		File file = new File(bin);
		boolean success = (file.exists() && file.isDirectory());
		if (!success) {
			success = file.mkdirs();
		}
		return (success ? file : null);
	}
	
}
