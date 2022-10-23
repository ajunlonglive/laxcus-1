/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.pool;

import java.io.*;

import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * 磁盘服务管理池。<br>
 * 提供基于磁盘某个目录的管理池，用于监视、管理、热发布分布任务组件，或者其它。
 * 
 * @author scott.liang
 * @version 1.0 01/09/2012
 * @since laxcus 1.0
 */
public abstract class DiskPool extends VirtualPool {

	/** 指定根目录 */
	private File root;

	/**
	 * 构造默认的磁盘管理池
	 */
	protected DiskPool() {
		super();
	}

	/**
	 * 设置基础目录。要求目录存在。如果不存在，建立一个新的目录。
	 * @param path 根目录
	 * @return 目录存在，或者新建成功，返回真；否则假。
	 */
	public boolean setRoot(File path) {
		// 检查目录
		boolean success = (path.exists() && path.isDirectory());

		// 如果目录不存在时，建立它
		if (!success) {
			success = path.mkdirs();
		}
		// 如果成功，保存为根目录（规范格式！）
		if (success) {
			try {
				root = path.getCanonicalFile();
			} catch (IOException e) {
				Logger.error(e);
				success = false;
			}
		}
		
		Logger.debug(this, "setRoot", success, "%s", path);
		
		// 返回结果
		return success;
	}

	/**
	 * 设置磁盘管理池根目录
	 * @param path 根目录
	 * @return 成功返回真，否则假
	 */
	public boolean setRoot(String path) {
		path = ConfigParser.splitPath(path);
		return setRoot(new File(path));
	}

	/**
	 * 在指定根目录之下，通过后缀目录建立一个新目录。
	 * @param path 主目录
	 * @param subpath 子目录
	 * @return 成功返回<b>真</b>，否则<b>假</b>。
	 */
	public boolean setRoot(String path, String subpath) {
		path = ConfigParser.splitPath(path);
		return setRoot(new File(path, subpath));
	}

	/**
	 * 返回命名任务根目录
	 * @return
	 */
	public final File getRoot() {
		return root;
	}

	/**
	 * 取文件的规范路径，否则是绝对路径
	 * @param file File实例
	 * @return 字符串文件
	 */
	protected String canonical(File file) {
		return Laxkit.canonical(file);
	}
	
	/**
	 * 读出磁盘文件内容
	 * @param file 磁盘文件
	 * @return 返回字节数组，错误返回空值
	 */
	protected byte[] readContent(File file) {
		// 判断文件存在
		boolean success = (file.exists() && file.isFile());
		if (!success) {
			return null;
		}
		// 如果是空文件
		int len = (int) file.length();
		if (len < 1) {
			return null;
		}

		try {
			byte[] b = new byte[len];
			FileInputStream in = new FileInputStream(file);
			in.read(b);
			in.close();
			return b;
		} catch (IOException e) {
			Logger.error(e);
		}
		return null;
	}

	/**
	 * 以追加或者不追加的模式，将数据写入一个磁盘文件。
	 * 
	 * @param file 磁盘文件
	 * @param append 追加数据到文件末尾
	 * @param b 字节数组
	 * @return 成功返回真，否则假
	 */
	protected boolean writeContent(File file, boolean append, byte[] b) {
		boolean success = false;
		try {
			FileOutputStream out = new FileOutputStream(file, append);
			out.write(b);
			out.flush();
			out.close();
			success = true;
		} catch (IOException e) {
			Logger.error(e);
		}

		// 如果写入不成功，尝试删除这个文件
		if (!success) {
			file.delete();
		}

		return success;
	}
	
	/**
	 * 数据写入一个磁盘，如果是旧文件，覆盖它！
	 * 
	 * @param file 磁盘文件
	 * @param b 字节数组
	 * @return 成功返回真，否则假
	 */
	protected boolean writeContent(File file,byte[] b) {
		return writeContent(file, false, b);
	}
	
	/** JAR属性文件 **/
	private static final String JAR_SUFFIX = "^\\s*([\\w\\W]+)(?i)(\\.JAR)\\s*$";

	/**
	 * 判断是JAR包文件
	 * @param file 磁盘文件名
	 * @return 返回是或者否
	 */
	protected boolean isJAR(File file) {
		String path = canonical(file);
		return path.matches(DiskPool.JAR_SUFFIX);
	}
	
	/**
	 * 生成JAR文件属性类
	 * @param file 磁盘文件
	 * @return FileKey实例
	 */
	protected FileKey createJAR(File file) {
		String path = canonical(file);
		long length = file.length();
		long modified = file.lastModified();
		return new FileKey(path, length, modified);
	}

	/** LINUX SO 动态链接库文件 **/
	private static final String LINUX_SO_SUFFIX = "^\\s*([\\w\\W]+)(?i)(\\.SO)\\s*$";

	/** WINDOWS SO 动态链接库文件 **/
	private static final String WINDOWS_DLL_SUFFIX = "^\\s*([\\w\\W]+)(?i)(\\.DLL)\\s*$";

	/**
	 * 判断是JAR包文件
	 * @param file 磁盘文件名
	 * @return 返回是或者否
	 */
	protected boolean isLinkLibrary(File file) {
		String path = canonical(file);
		if (isLinux()) {
			return path.matches(DiskPool.LINUX_SO_SUFFIX);
		} else if (isWindows()) {
			return path.matches(DiskPool.WINDOWS_DLL_SUFFIX);
		}
		return false;
	}

	/**
	 * 生成动态链接库文件属性类
	 * @param file 磁盘文件
	 * @return FileKey实例
	 */
	protected FileKey createLibraryKey(File file) {
		String path = canonical(file);
		long length = file.length();
		long modified = file.lastModified();
		return new FileKey(path, length, modified);
	}
	
	/**
	 * 统计磁盘目录下的子目录和文件
	 * @param dir 磁盘目录
	 * @return 返回子级单元
	 */
	protected int countDirectory(File dir) {
		// 判断存在，不存在返回0
		boolean success = (dir.exists() && dir.isDirectory());
		if (!success) {
			return 0;
		}

		int count = 0;
		// 枚举全部
		File[] lists = dir.listFiles();
		for (int i = 0; lists != null && i < lists.length; i++) {
			File sub = lists[i];
			if (sub.isDirectory()) {
				int ret = countDirectory(sub);
				// 增加统计
				count += ret;
			} else if (sub.isFile()) {
				// 统计值加1 
				count += 1;
			}
		}
		return count;
	}
	
	/**
	 * 判断是空目录
	 * @param dir
	 * @return
	 */
	protected boolean isEmptyDirectory(File dir) {
		int count = countDirectory(dir);
		return count == 0;
	}

}