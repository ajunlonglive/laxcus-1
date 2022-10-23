/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.system.modulate.sift;

import java.io.*;
import java.util.*;

import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.*;
import com.laxcus.distribute.establish.mid.*;
import com.laxcus.distribute.establish.session.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.establish.sift.*;
import com.laxcus.util.classable.*;

/**
 * 数据优化的“SIFT”阶段任务。<br><br>
 * 
 * 数据优化SIFT将对数据块进行MARSHAL/EDUCT操作，把数据重新整理后，输出元数据
 * 
 * @author scott.liang
 * @version 1.1 1/9/2013
 * @since laxcus 1.0
 */
public class ModulateSiftTask extends SiftTask {

	/**
	 * 构造默认的SIFT阶段任务
	 */
	public ModulateSiftTask() {
		super();
	}

	/**
	 * 从字节数组中解析行记录
	 * @param space 数据表名
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 * @return 返回解析后的行记录列表
	 */
	private List<Row> split(Space space, byte[] b, int off, int len) throws TaskException {
		SiftTrustor trustor = super.getSiftTrustor();
		long invokerId = super.getInvokerId();
		Table table = trustor.findSiftTable(invokerId, space);
		boolean success = (table != null);
		if (!success) {
			Logger.error(this, "split", "cannot be find '%s'", space);
			return null;
		}

		Sheet sheet = table.getSheet();
		ClassReader reader = new ClassReader(b, off, len);

		RowCracker cracker = new RowCracker(sheet);
		cracker.split(reader);
		return cracker.flush();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.sift.SiftTask#implement()
	 */
	@Override
	public byte[] implement() throws TaskException {
		SiftSession session = getSession();

		List<SiftHead> list = session.list();
		// 只能有一个
		if (list.size() != 1) {
			throw new SiftTaskException("sizeout, %d", list.size());
		}

		SiftHead head = list.get(0);
		// 构建表空间
		EstablishFlag flag = head.getFlag();
		Space space = flag.getSpace();
		Dock dock = new Dock(space);

		// 用户签名	
		long invokerId = super.getInvokerId();
		SiftTrustor trustor = super.getSiftTrustor();
		// 执行数据排序操作
		long length = trustor.marshal(invokerId, dock);

		Logger.debug(getIssuer(), this, "implement", "marshal '%s' size is %d", dock, length);

		// 判断出错
		if (length < 0L) {
			trustor.unmarshal(invokerId, space);
			throw new SiftTaskException("cannot be marshal");
		}

		long seek = 0L;
		int maxsize = 0x100000; // 1M
		while (seek < length) {
			int len = (int) (length - seek > maxsize ? maxsize : length - seek);

			// 循环执行EDUCE操作
			byte[] b = trustor.educe(invokerId, space, len);

			Logger.debug(getIssuer(), this, "implement", "'%s', seek:%d, readlen:%d, educe bytes:%d",
					space, seek, len, (b == null ? -1 : b.length));

			// 判断出错
			if (b == null) {
				trustor.unmarshal(invokerId, space);
				throw new SiftTaskException("cannot be educt, at '%s#%d/%d'", space, length, seek);
			}
			// 已经读完
			if(b.length == 0) {
				int ret = trustor.unmarshal(invokerId, space);
				Logger.warning(this, "implement", "%s is complted! return code:%d", space, ret);
				break;
			}

			// 下一次的位置
			seek += b.length;

			//			// debug code, start
			//			try {
			//				FileOutputStream out = new FileOutputStream("/notes/educe.bin", true);
			//				out.write(b);
			//				out.close();
			//			} catch(IOException e) {
			//				Logger.error(e);
			//			}
			//			// debug code, end

			// 解析行数据
			List<Row> rows = this.split(space, b, 0, b.length);
			// 解析数据，重新写入
			Insert insert = new Insert(space);
			insert.addAll(rows);

			// 注入记录
			byte[] results = trustor.insert(invokerId, insert);
			Logger.debug(getIssuer(), this, "implement", "insert %s size is %d", space,
					(results == null ? -1 : results.length));

			//			// debug code, start
			//			try {
			//				FileOutputStream out = new FileOutputStream("/notes/educt_inject.bin", true);
			//				out.write(results);
			//				out.close();
			//			} catch (IOException e) {
			//				Logger.error(e);
			//			}
			//			// debug code, end
		}

		// 强制输出
		int ret = trustor.rush(invokerId, space);
		Logger.debug(getIssuer(), this, "implement", "rush '%s' result is:%d", space, ret);

		// 删除磁盘上过期的数据
		for (Long stub : head.getStubs()) {
			ret = trustor.deleteChunk(invokerId, space, stub);
			Logger.debug(getIssuer(), this, "implement", "delete chunk :%x result is:%d", stub, ret);
		}

		// 产生数据
		SiftField field = trustor.detect(invokerId, space);

		// 建立元数据映像区和输出
		Node node =	trustor.getLocal(invokerId);
		SiftArea area = new SiftArea(node); 
		area.add(field);
		// 输出
		return area.build();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.sift.SiftTask#implementTo(java.io.File)
	 */
	@Override
	public void implementTo(File file) throws TaskException {
		byte[] b = this.implement();
		try {
			FileOutputStream out = new FileOutputStream(file);
			out.write(b);
			out.close();
		} catch (IOException e) {
			throw new SiftTaskException(e);
		}
	}
}