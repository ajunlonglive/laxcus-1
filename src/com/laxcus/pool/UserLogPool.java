/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.pool;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;

import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.charset.*;
import com.laxcus.util.range.*;

/**
 * 用户日志管理池 <br>
 * 
 * 记录站点在运行过程中产生的记录，这些记录将被保存到磁盘上，以备其它统计，如计费。
 * 
 * @author scott.liang
 * @version 1.0 01/02/2017
 * @since laxcus 1.0
 */
public final class UserLogPool extends DiskPool {

	/** 用户日志管理池静态句柄(全局唯一) **/
	private static UserLogPool selfHandle = new UserLogPool();

	/** 日志的时间格式 **/
	private SimpleDateFormat style = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.ENGLISH);

	/** 异步调用器日志后缀 **/
	private static String SUFFIX = ".ilog";

	/** 磁盘文件正则表达式 **/
	private final String regex = "^([\\w\\W]+?)\\((?:[0-9]+)\\)(?:\\.ilog)$";

	/** 文件长度 **/
	private long length;

	/** 当前文件 **/
	private File diskFile;

	/** 调用器日志记录 **/
	private ArrayList<UserLog> array = new ArrayList<UserLog>(2000);

	/**
	 * 构造用户日志管理池
	 */
	private UserLogPool() {
		super();
		// 一分钟检查一次
		setSleepTime(60);
		// 文件长度, 10M
		setLength(10 * 1024 * 1024);
	}

	/**
	 * 返回用户日志管理池管理池的静态句柄
	 * @return
	 */
	public static UserLogPool getInstance() {
		return UserLogPool.selfHandle;
	}

	/**
	 * 设置文件数据长度
	 * @param size
	 */
	public void setLength(long size) {
		length = size;
	}

	/**
	 * 返回文件数据长度
	 * @return
	 */
	public long getLength() {
		return length;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		// 如果没有定义根目录，是编程错误
		if (getRoot() == null) {
			throw new NullPointerException("cannot be set user log directory");
		}

		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info(this, "process", "into...");

		while (!isInterrupted()) {
			// 定时检查内存中的记录
			check();
			// 线程进行等待状态
			sleep();
		}

		Logger.info(this, "process", "exit");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		check();
	}

	/**
	 * 检查内存中的数据，然后把它们保存到磁盘上
	 */
	private void check() {
		StringBuilder buff = new StringBuilder();
		super.lockSingle();
		try {
			while (array.size() > 0) {
				UserLog log = array.remove(0);
				buff.append(log.toString());
				buff.append("\r\n");
			}
			// 日志转为UTF8编码，写入磁盘
			if (buff.length() > 0) {
				byte[] logs = new UTF8().encode(buff.toString());
				write(logs);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 保存日志
	 * @param log
	 */
	public void add(UserLog log) {
		boolean maxout = false;
		super.lockSingle();
		try {
			array.add(log);
			maxout = (array.size() >= 1900);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 唤醒线程
		if (maxout) {
			wakeup();
		}

		Logger.debug(this, "add", "command is %s", log.getLog().getCommand());
	}

	/**
	 * 查找某个范围的用户日志
	 * @param siger 用户签名
	 * @param range 时间范围
	 * @return 返回异步日志
	 */
	public List<EchoLog> find(Siger siger, LongRange range) {
		ArrayList<EchoLog> array = new ArrayList<EchoLog>();

		super.lockSingle();
		try {
			File[] files = findFile(range);
			// 读取这些文件，拿到匹配的日志
			for (int i = 0; files != null && i < files.length; i++) {
				List<EchoLog> e = read(siger, files[i], range);
				array.addAll(e);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		return array;
	}

	/**
	 * 查找一个用户的全部日志
	 * @param siger
	 * @return
	 */
	public List<EchoLog> find(Siger siger) {
		return find(siger, null);
	}

	/**
	 * 从磁盘读用户日志
	 * @param siger
	 * @param file
	 * @param range
	 * @return
	 */
	private List<EchoLog> read(Siger siger, File file, LongRange range) {
		ArrayList<EchoLog> array = new ArrayList<EchoLog>();

		try {
			FileReader reader = new FileReader(file);
			BufferedReader buff = new BufferedReader(reader);
			do {
				String line = buff.readLine();
				if (line == null) {
					break;
				}
				UserLog log = new UserLog(line);
				// 判断用户名称一致
				boolean success = (Laxkit.compareTo(siger, log.getIssuer()) == 0);
				if (success) {
					if (range == null) {
						array.add(log.getLog());
					} else if (range.inside(log.getLog().getLaunchTime())) {
						array.add(log.getLog());
					}
				}
			} while (true);
			buff.close();
			reader.close();
		} catch (IOException e) {
			Logger.error(e);
		}

		return array;
	}

	/**
	 * 在本地目录建立文件名
	 * @return 返回一个新的文件名，且在当前磁盘上不存在的
	 */
	private File createFile() {
		for (int index = 1; true; index++) {
			String name = String.format("%s(%d)%s", style.format(new Date()),
					index, UserLogPool.SUFFIX);
			File file = new File(getRoot(), name);
			// 文件不存在，返回它
			if (!file.exists()) {
				return file;
			}
		}
	}

	/**
	 * 保存日志
	 * @param logs
	 * @throws IOException
	 */
	private void write(byte[] logs) throws IOException {
		if (diskFile == null) {
			diskFile = this.createFile();
		} else if (diskFile.length() + logs.length > length) {
			diskFile = this.createFile();
		}

		// 添加写入
		FileOutputStream out = new FileOutputStream(diskFile, true);
		out.write(logs);
		out.flush();
		out.close();
	}

	/**
	 * 查找匹配日期的文件名
	 * @param range 日期范围
	 * @return 返回匹配的文件
	 */
	private File[] findFile(LongRange range) {
		File dir = getRoot();
		File[] files = dir.listFiles();

		ArrayList<File> a = new ArrayList<File>();
		for (File file : files) {
			boolean success = file.isFile();
			if (success) {
				success = matches(file, range);
			}
			if (success) {
				a.add(file);
			}
		}
		files = new File[a.size()];
		return a.toArray(files);
	}

	/**
	 * 判断匹配
	 * @param file
	 * @param range
	 * @return
	 */
	private boolean matches(File file, LongRange range) {
		String name = file.getName();

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(name);
		// 不匹配，返回假
		if(!matcher.matches()) {
			return false;
		}
		// 没有定义时间范围时，返回真
		if (range == null) {
			return true;
		}

		// 取出时间进行比较
		String format = matcher.group(1);
		try {
			Date time = style.parse(format);
			long current = time.getTime(); 
			return range.inside(current);
		} catch (ParseException e) {
			Logger.error(e);
		}
		return false;
	}

	//	private void test() {
	//		File dir = new File("f:/userlogs");
	//		super.setRoot(dir);
	//		Siger issuer = new Siger("89e495e7941cf9e40e6980d14a16bf023ccd4c91");
	//		LongRange range = new LongRange(0, 1483635904768L);
	//		List<EchoLog> logs = find(issuer, range) ;
	//		System.out.printf("echo log size is %d\n", logs.size());
	//	}
	//
	//	public static void main(String[] args) {
	//		UserLogPool.getInstance().test();
	//	}

	//	public static void main(String[] args) {
	//		String input = "2017_01_05_16_23_01";
	//		try {
	//		Date time = UserLogPool.getInstance().style.parse(input);
	//		System.out.println(time);
	//		} catch (ParseException e) {
	//			Logger.error(e);
	//		}
	//	}
}