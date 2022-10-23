/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.system.modulate.scan;

import java.io.*;

import com.laxcus.distribute.establish.mid.*;
import com.laxcus.task.*;
import com.laxcus.task.establish.scan.*;

/**
 * 数据优化的“SCAN”阶段 <br>
 * 
 * 数据优化的SCAN阶段将扫描STABLE状态数据块（只是CHUNK状态）索引，并且把结果做为元数据输出。
 * 
 * @author scott.liang
 * @version 1.1 1/22/2013
 * @since laxcus 1.0
 */
public class ModulateScanTask extends ScanTask {

	/**
	 * 构造数据优化的“SCAN”阶段任务
	 */
	public ModulateScanTask() {
		super();
	}

//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.task.establish.scan.ScanTask#scan()
//	 */
//	@Override
//	public ScanArea scan() throws TaskException {
//		return super.defaultScan();
//	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.establish.scan.ScanTask#analyse()
	 */
	@Override
	public byte[] analyse() throws TaskException {
		ScanArea area = super.defaultScan();
		return area.build();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.establish.scan.ScanTask#analyseTo(java.io.File)
	 */
	@Override
	public void analyseTo(File file) throws TaskException {
		byte[] b = analyse();
		try {
			FileOutputStream out = new FileOutputStream(file);
			out.write(b);
			out.close();
		} catch (IOException e) {
			throw new ScanTaskException(e);
		}
	}

}