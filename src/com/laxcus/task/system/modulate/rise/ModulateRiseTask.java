/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.system.modulate.rise;

import java.io.*;

import com.laxcus.distribute.establish.mid.*;
import com.laxcus.task.*;
import com.laxcus.task.establish.rise.*;

/**
 * 数据优化的“RISE”阶段任务。<br>
 * 
 * 取出RISE会话中的参数，从BUILD站点下载数据块和更新，重新发布。
 * 
 * @author scott.liang
 * @version 1.1 1/23/2013
 * @since laxcus 1.0
 */
public class ModulateRiseTask extends RiseTask {

	/**
	 * 构造数据优化的“RISE”阶段任务
	 */
	public ModulateRiseTask() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.rise.RiseTask#convert()
	 */
	@Override
	public byte[] convert() throws TaskException {
		RiseArea area = super.defaultConvert();
		return  area.build();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.rise.RiseTask#convertTo(java.io.File)
	 */
	@Override
	public void convertTo(File file) throws TaskException {
		byte[] b = this.convert();
		try {
			FileOutputStream out = new FileOutputStream(file);
			out.write(b);
			out.close();
		} catch (IOException e) {
			throw new RiseTaskException(e);
		}
	}

}