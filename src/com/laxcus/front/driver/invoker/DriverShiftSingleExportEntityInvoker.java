/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import java.io.*;

import com.laxcus.access.column.*;
import com.laxcus.access.diagram.*;
import com.laxcus.access.parse.*;
import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.front.driver.mission.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.charset.*;
import com.laxcus.util.io.*;
import com.laxcus.util.set.*;

/**
 * 单个数据块导出转发命令异步调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 9/23/2019
 * @since laxcus 1.0
 */
public class DriverShiftSingleExportEntityInvoker extends DriverInvoker {

	/** 行统计 **/
	private int resultRows;

	/**
	 * 构造单个数据块导出转发命令异步调用器，指定单个数据块导出转发命令
	 * @param cmd 单个数据块导出转发命令
	 */
	public DriverShiftSingleExportEntityInvoker(DriverMission mission) {
		super(mission);
		resultRows = 0;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftSingleExportEntity getCommand() {
		return (ShiftSingleExportEntity) super.getCommand();
	}
	
	/**
	 * 输出子命令
	 * @return SingleExportEntity实例
	 */
	private SingleExportEntity getSubCommand() {
		return (SingleExportEntity) getCommand().getCommand();
	}
	
	/**
	 * 反馈处理结果由调用端
	 * @param success 成功或者否
	 */
	private void reply(boolean success) {
		ShiftSingleExportEntity shift = getCommand();
		SingleExportEntity cmd = shift.getCommand();

		SingleExportEntityResult res = new SingleExportEntityResult(success,
				cmd.getFile(), cmd.getStub(), resultRows);

		SingleExportEntityHook hook = shift.getHook();
		// 设置结果和唤醒
		hook.setResult(res);
		hook.done();
	}

	/**
	 * 发送指令到CALL节点
	 * @return 成功返回真，否则假
	 */
	private boolean send() {
		// 检查注册用户有删除的权限
		SingleExportEntity cmd = getSubCommand();
		Space space = cmd.getSpace();
		// 1. 判断数据库存在
		boolean success = false;
		try {
			success = getStaffPool().hasTable(space);
		} catch (ResourceException e) {
			Logger.error(e);
		}
		if (!success) {
//			faultX(FaultTip.NOTFOUND_X, space);
			return false;
		}
		
		// 2. 判断有下载数据块的权限
		success = getStaffPool().canTable(space, ControlTag.EXPORT_ENTITY);
		if (!success) {
//			faultX(FaultTip.PERMISSION_MISSING_X, space);
			return false;
		}
		
		// 找到CALL站点
		NodeSet set = getStaffPool().findTableSites(space);
		// 顺序枚举一个CALL站点地址，保持调用平衡
		Node hub = (set != null ? set.next() : null);
		// 没有找到，弹出错误
		if (hub == null) {
//			faultX(FaultTip.NOTFOUND_SITE_X, space);
			return false;
		}

		// 发送到目标地址
		success = fireToHub(hub, cmd);
		
//		// 在状态栏显示，下载 ....
//		if (success) {
//			String prefix = getXMLContent("EXPORT-ENTITY/DOWNLOAD");
//			String tip = String.format("%s 0x%X", cmd.getSpace(), cmd.getStub());
//			String text = String.format(prefix, tip);
//			message(text);
//		}

		Logger.debug(this, "send", success, "send to %s", hub);

		return success;
	}

	/**
	 * 从CALL节点接收反馈数据
	 * @return 成功返回真，否则假
	 */
	private boolean receive() {
		int index = findEchoKey(0);
		// 不成功显示错误
		if (!isSuccessCompleted(index)) {
//			SingleExportEntity cmd = getSubCommand();
//			String rs = String.format("%s %X", cmd.getSpace(), cmd.getStub());
//			faultX(FaultTip.FAILED_X, rs);
			return false;
		}

		// 判断数据在磁盘文件中
		boolean ondisk = isEchoFiles();
		// 显示信息
		boolean success = false;
		try {
			if (ondisk) {
				File[] files = getAllFiles();
				success = export(files[0]);
			} else {
				byte[] b = collect();
				success = export(b, 0, b.length);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
			fault(e);
		}
		
//		// 成功，显示信息
//		if (success) {
//			String prefix = getXMLContent("EXPORT-ENTITY/ROWS-X");
//			SingleExportEntity cmd = getSubCommand();
//			String tip = String.format("%s", cmd.getFilename());
//			String text = String.format(prefix, tip);
//			message(text);
//		}
		
		// 完成
		return success;
	}
	
	/**
	 * 导出数据到磁盘文件
	 * @param src ECHO源文件
	 * @return 成功返回真，否则假
	 * @throws IOException
	 */
	private boolean export(File src) throws IOException {
		byte[] b = new byte[(int) src.length()];
		FileInputStream in = new FileInputStream(src);
		in.read(b);
		in.close();
		return export(b, 0, b.length);
	}
	
	/**
	 * 数据内容导出到磁盘文件
	 * @param b
	 * @param off
	 * @param len
	 * @return
	 * @throws IOException
	 */
	private boolean export(byte[] b, int off, int len) throws IOException {
		SingleExportEntity cmd = getSubCommand();
		int type = cmd.getType();
		File file = cmd.getFile();
		int charset = cmd.getCharset();

		Space space = cmd.getSpace();
		// 找到数据表
		Table table = getStaffPool().findTable(space);
		if (table == null) {
//			faultX(FaultTip.NOTFOUND_X, space);
			return false;
		}

		// 显示标题
		Sheet sheet = table.getSheet();
		
		StyleRowWriter writer = null;
		// 输出标题
		if (EntityStyle.isCSV(type)) {
			writer = new CSVRowWriter(file);
		} else if (EntityStyle.isTXT(type)) {
			writer = new TXTRowWriter(file);
		} else {
//			faultX(FaultTip.NOTSUPPORT_X, String.valueOf(type));
			return false;
		}
		// 定义字符集
		if (CharsetType.isCharset(charset)) {
			writer.setCharset(charset);
		}
		// 写标题
		writer.writeTitle(sheet);

		// 尺寸判断
		long last = (b != null && b.length > 0 ? b.length : 0);
		if (last < 1) {
			Logger.warning(this, "export", "byte length:%d", last);
			return true;
		}

		// 统计行数
		resultRows = 0;
		// 显示数据
		int seek = off;
		int end = off + len;
		while (seek < end) {
			MassFlag flag = new MassFlag();
			int size = flag.resolve(b, seek, end - seek);
			seek += size;
			// 判断长度溢出
			if(seek + flag.getLength() > end) {
				throw new ColumnException("%d + %d > %d", seek, flag.getLength(), end);
			}

			// 读取指定长度的数据
			RowCracker parser = new RowCracker(sheet);
			size = parser.split(b, seek, (int) flag.getLength());

			if (size != flag.getLength()) {
				throw new ColumnException("%d != %d", size, flag.getLength());
			}
			// 移动下标
			seek += size;
			
			// 统计行数
			resultRows += parser.size();
			
			// 分组方式，输出记录，防止占用太多内存空间
			while (parser.hasRows()) {
				Row[] rows = parser.efflux(2000);
				writer.writeContent(sheet, rows);
			}
		}
		
		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 发送指令到服务器
		boolean success = send();
		// 不成功，触发反馈结果
		if (!success) {
			reply(false);
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// 接收返回的数据
		boolean success = receive();
		// 触发结果
		reply(success);
		// 退出
		return useful(success);
	}
	
}