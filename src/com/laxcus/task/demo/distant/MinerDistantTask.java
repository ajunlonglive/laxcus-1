/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.demo.distant;

import java.io.*;

import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.distribute.contact.command.*;
import com.laxcus.distribute.contact.session.*;
import com.laxcus.task.*;
import com.laxcus.task.contact.distant.*;
import com.laxcus.util.*;
import com.laxcus.util.charset.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.hash.*;
import com.laxcus.site.*;

/**
 * DISTANT计算挖矿，基于“GENERATE”模型，产生矿码。
 * 
 * @author scott.liang
 * @version 1.0 12/26/2020
 * @since laxcus 1.0
 */
public class MinerDistantTask extends DistantGenerateTask {

	/**
	 * 构造DISTANT计算挖矿实例
	 */
	public MinerDistantTask() {
		super();
	}

	/**
	 * 判断SHA256散列码前面N个字符是0
	 * 
	 * @param hash SHA256散列码
	 * @return 符合要求返回真，否则假
	 */
	private boolean allow(SHA256Hash hash, int zero) {
		// 输出SHA256散列码的字节数组
		byte[] b = hash.get();
		// 从0下标开始
		for (int i = 0; i < zero; i++) {
			if (b[i] != 0) return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.contact.distant.DistantGenerateTask#process()
	 */
	@Override
	public long process() throws TaskException {
		DistantStep cmd = getCommand();
		DistantSession session = cmd.getSession();

		int zeros = findInteger(session, "zeros");
		int index = findInteger(session, "index"); // 索引，唯一，做为模值
		long begin = findLong(session, "begin");
		long end = findLong(session, "end");
		String prefix = findString(session, "PREFIX");

		//		Logger.debug(getIssuer(), this, "process", "本机矿码范围：%s / [%d - %d]", prefix, begin, end);

		// 中间数据写入器和临时缓存
		FluxWriter writer = fetchWriter();
		ClassWriter buf = new ClassWriter();
		Node local = writer.getLocal();

		// 被挖出的矿码数目
		int count = 0;

		UTF8 utf8 = new UTF8();

		// 生成散列码，判断首字符是0
		for (long seek = begin; seek <= end; seek++) {
			String text = String.format("%s%d", prefix, seek);
			// 转成UTF8编码
			byte[] b = utf8.encode(text);
			SHA256Hash hash = Laxkit.doSHA256Hash(b);
			// 符合要求...
			if (allow(hash, zeros)) {
				buf.writeObject(local);
				buf.writeObject(hash);
				buf.writeString(text);
				count++;
			}
		}

		//		Logger.debug(getIssuer(), this, "process", "挖出的矿码统计是：%d", count);

		// 建立数据写入器，存取模式根据命令的“内存/磁盘”选定
		if (buf.size() > 0) {
			// 以添加方式写入缓存
			byte[] b = buf.effuse();
			FluxField field = writer.append(index, count, b, 0, b.length);
			// 如果为null，即写入失败
			if (field == null) {
				throw new DistantTaskException("数据写入失败！");
			}
		}

		// 返回元数组字节长度
		byte[] b = effuse();
		return b.length;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.contact.distant.DistantTask#effuse()
	 */
	@Override
	public byte[] effuse() throws TaskException {
		FluxArea area = super.createFluxArea();
		long size = area.length();

		ClassWriter buff = new ClassWriter((int) size + 10);
		FluxReader reader = fetchReader();
		for (FluxField field : area.list()) {
			byte[] b = reader.read(field.getMod(), 0, (int) field.length());
			buff.write(b);
		}
		byte[] b = buff.effuse();

		// 输出数据
		buff.reset();
		// 前缀字节
		buff.writeInt(b.length);
		buff.write(b);
		return buff.effuse();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.contact.distant.DistantTask#flushTo(java.io.File)
	 */
	@Override
	public long flushTo(File file) throws TaskException {
		// 转为字节数组
		byte[] b = effuse();
		// 输出到指定的文件
		return	writeTo(file, false, b, 0, b.length);
	}

}