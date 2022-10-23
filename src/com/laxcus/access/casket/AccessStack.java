/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.casket;

import java.io.*;

import com.laxcus.access.schema.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 数据存取结果堆栈。<br><br>
 * “INSERT/LEAVE/DELETE/SELECT”命令的返回数据统一使用这个接口。<br>
 * 
 * 输出数据是一串字节数组，由JNI产生，包含存取操作需要的全部内容。<br>
 * 输出数据的第一个字符是状态，通过状态分别做出判断，有选择地读取后续参数。<br><br>
 * 
 * 状态有四种情况：<br>
 * 1. 数据在内存 <br>
 * 2. 数据在磁盘 <br>
 * 3. 没有找到。<br>
 * 4. 发生错误。<br><br><br>
 * 
 * 
 * 状态是“发生错误”时的数据格式：<br>
 * 1. 错误状态（1字节）<br>
 * 2. 错误码（4字节）<br><br><br>
 *
 *
 * 状态是“没有找到”时的数据格式：<br>
 * 1. 没有找到（1字节）<br><br><br>
 * 
 * 
 * 状态是“内存/磁盘”时的数据格式：<br>
 * 1. 数据状态（1字节，“内存/硬盘/没有找到/错误”四个选项）<br>
 * 2. 操作符（1字节，“INSERT/LEAVE/SELECT/DELETE”四个选项）<br>
 * 3. 行为辅助码（4字节，与操作行为关联） <br>
 * 4. 数据格式下标（8字节，数据的开始位置。内存是0，磁盘是文件开始位置）<br>
 * 5. 数据块编号（8字节） <br>
 * 6. 数据块形态（1字节，CACHE/CHUNK任意一种，CACHE是缓存未封闭，CHUNK是固态已经封闭）
 * 7. 行数（4字节）<br>
 * 8. 列数（2字节）<br>
 * 9. 内容数据长度（4字节，SELECT/DELETE 产生的数据） <br>
 * 10. 映像数据长度（4字节，INSERT/LEAVE/DELETE 产生的数据） <br>
 * 11. 数据表名长度（2字节，数据库和表名各占1个字节）
 * 12. 数据表名本身（变长，遵循表名格式）<br>
 * 13.内容数据（用于数据处理） <br>
 * 14.映像数据（用于分发到从站点） <br><br>
 * 
 * “数据表名本身”之前是固定长度，共39个字节。后面分别是数据表名、内容数据、映像数据，这三组数据长度由前面定义的参数指定。<br><br>
 * 
 * 数据输出：<br>
 * 1. INSERT有映像数据，没有内容数据。<br>
 * 2. LEAVE有映像数据，没有内容数据。<br>
 * 3. SELECT有内容数据，没有映像数据。<br>
 * 4. DELETE有内容数据和映像数据。<br>
 * 
 * 
 * @author scott.liang
 * @version 1.18 8/31/2015
 * @since laxcus 1.0
 */
public class AccessStack {

	/** 标识 **/
	private AccessStackFlag flag = new AccessStackFlag();

	/** 内容数据 **/
	private byte[] content;

	/** 映像数据 **/
	private byte[] reflex;

	/**
	 * 构造默认的数据存取结果堆栈
	 */
	public AccessStack() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析数据存取结果堆栈
	 * @param reader 可类化数据读取器
	 */
	public AccessStack(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从字节数组中解析数据存取结果堆栈
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 */
	public AccessStack(byte[] b, int off, int len) {
		this(new ClassReader(b, off, len));
	}

	/**
	 * 从字节数组中解析数据存取结果堆栈
	 * @param b 字节数组
	 */
	public AccessStack(byte[] b) {
		this(b, 0, b.length);
	}

	/**
	 * 返回标识
	 * @return AccessStackFlag实例
	 */
	public AccessStackFlag getFlag() {
		return flag;
	}

	/**
	 * 返回处理状态
	 * @return 状态码
	 */
	public byte getState() {
		return flag.getState();
	}

	/**
	 * 返回处理状态的字符串说明
	 * @return 状态说明
	 */
	public String getStateText() {
		return flag.getStateText();
	}

	/**
	 * 判断操作成功。当数据在内存或者磁盘时，操作成功。
	 * 
	 * @return 条件成立返回“真”，否则“假”
	 */
	public boolean isSuccessful() {
		return flag.isSuccessful();
	}

	/**
	 * 判断数据在内存
	 * @return 条件成立返回“真”，否则“假”
	 */
	public boolean isMemory() {
		return flag.isMemory();
	}

	/**
	 * 判断数据在磁盘
	 * @return 条件成立返回“真”，否则“假”
	 */
	public boolean isDisk() {
		return flag.isDisk();
	}

	/**
	 * 判断没有找到
	 * @return 条件成立返回“真”，否则“假”
	 */
	public boolean isNotFound() {
		return flag.isNotFound();
	}

	/**
	 * 判断执行过程中发生错误
	 * @return 条件成立返回“真”，否则“假”
	 */
	public boolean isFault() {
		return flag.isFault();
	}

	/**
	 * 返回JNI错误码
	 * @return JNI错误码
	 */
	public int getFault() {
		return flag.getFault(); 
	}

	/**
	 * 返回数据操作符
	 * @return 数据操作符
	 */
	public byte getOperator() {
		return flag.getOperator();
	}

	/**
	 * 判断是INSERT操作
	 * @return 返回真或者假
	 */
	public boolean isInsert() {
		return flag.isInsert();
	}

	/**
	 * 判断是SELECT操作
	 * @return 返回真或者假
	 */
	public boolean isSelect() {
		return flag.isSelect();
	}

	/**
	 * 判断是DELETE操作
	 * @return 返回真或者假
	 */
	public boolean isDelete() {
		return flag.isDelete();
	}

	/**
	 * 判断是LEAVE操作
	 * @return 返回真或者假
	 */
	public boolean isLeave() {
		return flag.isLeave();
	}

	/**
	 * 返回辅助码
	 * @return 辅助码
	 */
	public int getHelp() {
		return flag.getHelp();
	}

	/**
	 * 判断INSERT填充“满”状态。
	 * @return 成立返回“真”，否则“假”。
	 */
	public boolean isInsertFull() {
		return flag.isInsertFull();
	}

	/**
	 * 返回数据开始下标
	 * @return 数据开始下标
	 */
	public long getOffset() {
		return flag.getOffset();
	}

	/**
	 * 返回行数
	 * @return 行数
	 */
	public int getRows() {
		return flag.getRows();
	}

	/**
	 * 返回列数
	 * @return 列数
	 */
	public short getColumns() {
		return flag.getColumns();
	}

	/**
	 * 返回内容数据尺寸
	 * @return 内容数据尺寸
	 */
	public int getContentSize() {
		return flag.getContentSize();
	}

	/**
	 * 判断是空内容
	 * @return 返回真或者假
	 */
	public boolean isEmptyContent() {
		return flag.isEmptyContent();
	}

	/**
	 * 返回映像数据尺寸
	 * @return 映像数据尺寸
	 */
	public int getReflexSize() {
		return flag.getReflexSize();
	}

	/**
	 * 判断是空映像数据
	 * @return 返回真或者假
	 */
	public boolean isEmptyReflex() {
		return flag.isEmptyReflex();
	}

	/**
	 * 返回数据块编号
	 * @return 数据块编号
	 */
	public long getStub() {
		return flag.getStub();
	}

	/**
	 * 返回数据块形态（CACHE/CHUNK）
	 * @return 数据块形态，CACHE/CHUNK任意一种
	 */
	public byte getStubStatus() {
		return flag.getStubStatus();
	}

	/**
	 * 判断数据块是缓存状态（CACHE状态）
	 * @return 返回真或者假
	 */
	public boolean isCacheStub() {
		return flag.isCacheStub();
	}

	/**
	 * 判断数据块是固态状态（CHUNK状态）
	 * @return 返回真或者假
	 */
	public boolean isChunkStub() {
		return flag.isChunkStub();
	}

	/**
	 * 返回数据表名
	 * @return 数据表名
	 */
	public Space getSpace() {
		return flag.getSpace();
	}

	/**
	 * 输出内容数据
	 * @return 内容数据
	 */
	public byte[] getContent() {
		return content;
	}

	/**
	 * 保存内容数据
	 * @param b 内容数据的字节数组
	 */
	private void setContent(byte[] b) {
		content = b;
	}

	/**
	 * 返回映像数据
	 * @return 映像数据的字节数组
	 */
	public byte[] getReflex() {
		return reflex;
	}

	/**
	 * 设置映像数据
	 * @param b 映像数据的字节数组
	 */
	private void setReflex(byte[] b) {
		reflex = b;
	}

	/**
	 * 按照指定的长度读内存中的数据
	 * @param reader 可类化数据读取器
	 * @return 返回读取的字节数组
	 */
	private byte[] readMemory(int size, ClassReader reader) {
		if (size < 0) {
			throw new IllegalValueException("illegal size:%d", size);
		} else if (size == 0) {
			return null;
		} else {
			return reader.read(size);
		}
	}

	/**
	 * 解析内存数据，包括索引下标、数据块编号、数据内容，数据映像
	 * @param reader 可类化读取器
	 */
	private void doMemory(ClassReader reader) {
		// 解析前缀
		flag.resolve(reader);
		// 下标必须是0
		if (flag.getOffset() != 0L) {
			throw new IllegalValueException("must be 0");
		}

		// 判断长度溢出
		if(reader.isReadout(getContentSize() + getReflexSize())) {
			throw new IllegalValueException("read out! %d + %d > %d",
					getContentSize(), getReflexSize(), reader.getLeft());
		}

		// 数据内容
		byte[] b = readMemory(getContentSize(), reader);
		setContent(b);
		// 数据溢像
		b = readMemory(getReflexSize(), reader);
		setReflex(b);
	}

	/**
	 * 从文件解析参数
	 * @param file 文件
	 * @param fileoff 指定文件下标
	 * @return 返回读取的字节数组长度
	 * @throws IOException, IllegalValueException
	 */
	public int doDisk(File file, long fileoff) throws IOException {
		// 文件不存在，弹出错误
		boolean success = (file.exists() && file.isFile());
		if (!success) {
			throw new FileNotFoundException(file.getAbsolutePath());
		}

		// 可类化数据读取器读出
		ClassReader reader = new ClassReader(file, fileoff);
		int seek = reader.getSeek();

		// 解析状态之后的前缀数据
		flag.resolve(reader);
		// 必须是磁盘模式
		if(!flag.isDisk()) {
			throw new IllegalValueException("cannot be 'DISK', %d", getState());
		}
		// 磁盘下标必须匹配
		if (flag.getOffset() != fileoff) {
			throw new IllegalValueException("illegal file offset:%d,%d", flag.getOffset(), fileoff);
		}

		// 判断长度溢出
		if(reader.isReadout(getContentSize() + getReflexSize())) {
			throw new IllegalValueException("read out! %d + %d > %d",
					getContentSize(), getReflexSize(), reader.getLeft());
		}

		// 数据内容
		byte[] b = readMemory(getContentSize(), reader);
		setContent(b);
		// 映像数据
		b = readMemory(getReflexSize(), reader);
		setReflex(b);

		return reader.getSeek() - seek;
	}

	/**
	 * 从文件中解析返回数据
	 * @param filename 文件名称
	 * @param fileoff 数据开始下标
	 * @return 返回解析的字节长度
	 * @throws IOException
	 */
	public int doDisk(String filename, long fileoff) throws IOException {
		return doDisk(new File(filename), fileoff);
	}

	/**
	 * 解析磁盘数据
	 * @param reader 可类化读取器
	 */
	private void doDisk(ClassReader reader) {
		// 读处理状态
		flag.setState(reader.read());
		if (!flag.isDisk()) {
			throw new IllegalValueException("cannot be 'DISK', %d", getState());
		}
		// 读文件名长度
		int size = reader.readInt();
		// 读文件名
		byte[] b = reader.read(size);
		String diskFile = new String(b);
		// 读文件下标
		long fileoff = reader.readLong();

		// 读文件
		try {
			File file = new File(diskFile);
			doDisk(file, fileoff);
		} catch (IOException e) {
			throw new IllegalValueException(e);
		}
	}

	/**
	 * 解析数据流
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 * @return 解析的字节长度
	 */
	public int resolve(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		return resolve(reader);
	}

	/**
	 * 从可类化数据读取器中解析输出数据
	 * @param reader 可类化数据读取器
	 * @return 返回解析的字节流长度
	 */
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();

		// 读状态
		flag.setState(reader.read());

		// 4种状态分别处理
		if (isMemory()) {
			reader.skip(-1); // 回退一个字节
			doMemory(reader);
		} else if (isDisk()) {
			reader.skip(-1); // 回退一个字节
			doDisk(reader);
		} else if (isNotFound()) {
			// 没有后续数据
		} else if (isFault()) {
			flag.setFault(reader.readInt()); // 读错误码
		}
		// 返回解析的数据流长度
		return reader.getSeek() - seek;
	}

	//	public static void main(String[] args) {
	//		File file = new File("d:/roll.bin");
	//
	//		ShiftStack stack = new ShiftStack();
	//		try {
	//			stack.doDisk(file, 0);
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		}
	//		System.out.println("finished!");
	//
	//		System.out.printf("file length:%d, head prefix size:%d, %s, content size:%d, reflex size:%d", 
	//				file.length(), stack.prelen(), stack.getSpace(), stack.getContentSize(), stack.getReflexSize());
	//	}

	//	public static void main2(String[] args) {
	//		File filename = new File("i:/leave.bin");
	//		ClassReader reader = null;
	//		try {
	//			reader = new ClassReader(filename);
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		}
	//
	//		ShiftStack stack = new ShiftStack(reader);
	//		System.out.printf("state:%d, operator:%d, fault:%d, face:%d, stub:%x, offset:%d, content size:%d, reflex size:%d\n",
	//				stack.getState(), stack.getOperator(), stack.getFault(), stack.getStubStatus(),
	//				stack.getStub(), stack.getOffset(), stack.getContentSize(), stack.getReflexSize());
	//
	//		System.out.printf("rows:%d, columns:%d\n", stack.getRows(), stack.getColumns());
	//		System.out.printf("content length:%d, reflex length:%d\n", stack.getContentSize(), stack.getReflexSize());
	//
	//		System.out.printf("cache is %s, chunk is %s\n", stack.isCacheStub(), stack.isChunkStub());
	//	}

	//	public static void main(String[] args) {
	//		// File filename = new File("d:/act.bin");
	//		//		File filename = new File("d:/delete2.deflex");
	//
	//		File filename = new File("i:/5f_8000000000000000.deflex");
	//		ClassReader reader = null;
	//		try {
	//			reader = new ClassReader(filename);
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		}
	//		//		ShiftStack a = new ShiftStack(reader);
	//
	//		ShiftStack a = new ShiftStack();
	//		try {
	//			a.doDisk(filename, 0L);
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		}
	//		System.out.printf("error code:%d\n", a.getFault());
	//		System.out.println("okay!\n\n");
	//		System.out.printf("seek:%d, length:%d\n", reader.getSeek(), reader.getLength());
	//		System.out.printf("%x, cache:%s - chunk:%s\n", a.getStub(), a.isCache(), a.isChunk() );
	//		// System.out.printf("[%s]\n", new String(a.getContent()));
	//
	//		//			byte[] b = a.getContent();
	//		//			reader = new ClassReader(b);
	//		//			AnswerFlag flag = new AnswerFlag(reader);
	//		//			System.out.printf("stream size:%d\n", flag.getStreamSize());
	//		//			System.out.printf("mod is %d\n", flag.getMod());
	//		//			System.out.printf("rows is %d\n", flag.getRows());
	//		//			System.out.printf("column is %d\n", flag.getColumns());
	//		//			System.out.printf("space is %s\n", flag.getSpace());
	//		//			System.out.printf("sm is %d\n", flag.getStorage());
	//		//			System.out.printf("seek is %d\n", reader.getSeek());
	//		//			// System.out.printf("[%s]\n", new String(a.getReflex()));
	//	}


}