/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.system.select.to.generate;

import com.laxcus.access.index.section.*;
import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.*;
import com.laxcus.command.access.cast.*;
import com.laxcus.distribute.conduct.session.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.to.*;

/**
 * @author scott.liang
 * @version 1.0 8/12/2012
 * @since laxcus 1.0
 */
public class SubSelectInToGenerateTask extends SQLToGenerateTask {

	/**
	 * 构造SubSelectInToGenerateTask实例
	 */
	public SubSelectInToGenerateTask() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.conduct.to.ToGenerateTask#divide(byte[], int, int)
	 */
	@Override
	public void divide(byte[] b, int off, int len) throws TaskException {
		ToSession session = super.getSession();
		if (session == null) {
			throw new ToTaskException("cannot be find ToSession");
		}
		// 取出在INIT阶段定义的，后续TO阶段需要的列分组区域
		ColumnSector sector = session.getIndexSector();
		if (sector == null) {
			throw new ToTaskException("cannot be find IndexSector");
		}
		// 取出SELECT句柄
		CastSelect cmd = (CastSelect) session.getCommand();
		if (cmd == null) {
			throw new ToTaskException("cannot be find CastSelect");
		}
		Select select = cmd.getSelect();
		// 找到表配置
		Space space = select.getSpace();
		Table table = findTable(space); 
		// 取出显示序列表
		Sheet sheet = select.getListSheet().getColumnSheet(table);
		// 必须只有1列
		if (sheet.size() != 1) {
			throw new ToTaskException("sheet size:%d, must be 1!", sheet.size());
		}

		//		// 列空间
		//		short columnId = sheet.get(0).getColumnId();
		//		Dock dock = new Dock(space, columnId);

		// 根据这个列进行数据分片
		int seek = off;
		int end = off + len;
		// 写入
		while(seek < end) {
			// 解析数据头标记
			MassFlag flag = new MassFlag();
			int size = flag.resolve(b, seek, end - seek);
			seek += size;
			if (space.compareTo(flag.getSpace()) != 0) {
				throw new ToTaskException(" %s != %s", space, flag.getSpace());
			}
			// 判断数据溢出
			if(seek + flag.getLength() > end){
				throw new ToTaskException("%d + %d > %d", seek, flag.getLength(), end);
			}
			// 解析数据和保存
			//			size = splitTo(sheet, dock, sector, b, seek, (int) flag.getLength());
			size = splitTo(sheet, sector, b, seek, (int) flag.getLength());
			// 判断解析长度
			if (size != flag.getLength()) {
				throw new ToTaskException("%d != %d", size, flag.getLength());
			}
			// 移动下标
			seek += size;
		}
	}

	//	/* (non-Javadoc)
	//	 * @see com.laxcus.task.conduct.to.ToGenerateTask#divide(java.io.File)
	//	 */
	//	@Override
	//	public void divide(File file) throws TaskException {
	//		ToSession session = super.getSession();
	//		if (session == null) {
	//			throw new ToTaskException("cannot be find session");
	//		}
	//		// 取出在INIT阶段定义的，后续TO阶段需要的列分组区域
	//		IndexSector sector = session.getIndexSector();
	//		if (sector == null) {
	//			throw new ToTaskException("cannot be find sector");
	//		}
	//		// 取出SELECT句柄
	//		CastSelect cmd = (CastSelect) session.getCommand(); 
	//		if (cmd == null) {
	//			throw new ToTaskException("cannot be find select");
	//		}
	//		Select select = cmd.getSelect();
	//		// 找到表配置
	//		Space space = select.getSpace();
	//		Table table = findTable(space); 
	//		// 取出显示序列表
	//		Sheet sheet = select.getListSheet().getColumnSheet(table);
	//		// 必须只有1列
	//		if (sheet.size() != 1) {
	//			throw new ToTaskException("sheet size:%d, must be 1!", sheet.size());
	//		}
	//
	//		long seek = 0;
	//		long end = file.length();
	//		try {
	//			FileInputStream reader = new FileInputStream(file);
	//
	//			while (seek < end) {
	//				MassFlag flag = new MassFlag();
	//				int size = flag.resolve(reader);
	//				seek += size;
	//				if (space.compareTo(flag.getSpace()) != 0) {
	//					throw new ToTaskException(" %s != %s", space, select.getSpace());
	//				}
	//				// 判断数据溢出
	//				if (seek + flag.getLength() > end) {
	//					throw new ToTaskException("%d + %d > %d", seek, flag.getLength(), end);
	//				}
	//
	//				// 读数据到磁盘
	//				byte[] b = new byte[(int) flag.getLength()];
	//				size = reader.read(b, 0, b.length);
	//				if (size != b.length) {
	//					throw new ToTaskException("%d != %d", size, b.length);
	//				}
	//				seek += size;
	//
	//				// 分析和保存数据
	//				//				size = splitTo(sheet, dock, sector, b, 0, b.length);
	//				size = splitTo(sheet, sector, b, 0, b.length);
	//				if (size != flag.getLength()) {
	//					throw new ToTaskException("%d != %d", size, flag.getLength());
	//				}
	//			}
	//			reader.close();
	//		} catch (IOException e) {
	//			throw new ToTaskException(e);
	//		}
	//	}

	//	/*
	//	 * (non-Javadoc)
	//	 * @see com.laxcus.task.conduct.to.ToTask#assemble()
	//	 */
	//	@Override
	//	public long assemble() throws TaskException {
	//		return 0;
	//	}

}