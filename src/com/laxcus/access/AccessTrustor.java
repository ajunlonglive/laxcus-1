/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access;

import java.util.*;

import com.laxcus.access.casket.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.stub.index.*;
import com.laxcus.access.stub.sign.*;
import com.laxcus.command.access.*;
import com.laxcus.command.stub.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.hash.*;

/**
 * 本地存取委托器 <br><br>
 * 
 * 通过本地存取委托器，所有操作都要在获得安全授权的情况下，才能访问本地磁盘资源。杜绝分布任务组件可能的非法访问。<br>
 * 安全授权在站点的“conf/site.policy”文件中设置。<br>
 * 
 * @author scott.liang
 * @version 1.3 12/2/2016
 * @since laxcus 1.0
 */
public final class AccessTrustor {

	/**
	 * 进行安全许可检查，防止分布任务组件非法调用JNI接口函数。<br>
	 * 在“conf/site.policy” 文件中设置安全许可，前缀是“using.”。
	 * 
	 * @param method 被调用的函数名称 
	 */
	private static void check(String method) {
		// 安全检查
		SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
			String name = String.format("using.%s", method);
			sm.checkPermission(new AccessPermission(name));
		}
	}

	/**
	 * 进行安全许可检查，防止分布任务组件非法调用JNI接口函数。<br>
	 * 在“conf/site.policy” 文件中设置安全许可，前缀是“using.”，后面有用户签名。
	 * 
	 * @param method 被调用的函数名称 
	 */
	private static void check(String method, Siger siger) {
		// 安全检查
		SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
			String name = String.format("using.%s", method);
			sm.checkPermission(new AccessPermission(name, siger.toString()));
		}
	}

	/**
	 * 将一段数据追加到指定文件的末尾 
	 * 
	 * @param filename 文件名称
	 * @param b 字节数组
	 * @param off 数组开始下标
	 * @param len 数组有效长度
	 * @return 返回大于等于0(>=0)的数据写入的文件下标，否则是负数（错误码）。
	 */
	public static long append(String filename, byte[] b, int off, int len) {
		AccessTrustor.check("append");
		return Access.append(filename, b, off, len);
	}

	/**
	 * 将一段数据写入文件的指定下标位置。成功写入的前提是文件必须存在，且下标位置是有效的。
	 * 
	 * @param filename 文件名称
	 * @param fileoff 文件下标位置
	 * @param b 数据的字节数组
	 * @param off 数组开始下标
	 * @param len 数据有效长度
	 * @return 返回大于0的数据写入的文件下标，否则是负数（错误码）
	 */
	public static long write(String filename, long fileoff, byte[] b, int off, int len) {
		AccessTrustor.check("write");
		return Access.write(filename, fileoff, b, off, len);
	}

	/**
	 * 从文件的指定下标位置，读取一段数据和返回。成功读取的前提是文件必须存在，且下标位置是是有效的。
	 * 
	 * @param filename 文件名称
	 * @param fileoff 指定的文件下标
	 * @param len 指定的读取长度
	 * @return 返回读取的字节数组，失败是空指针。
	 */
	public static byte[] read(String filename, long fileoff, int len) {
		AccessTrustor.check("read");
		return Access.read(filename, fileoff, len);
	}

	/**
	 * 获取一个文件的长度
	 * @param filename 文件名称
	 * @return 返回文件长度，发生错误返回负数（错误码）
	 */
	public static long length(String filename) {
		AccessTrustor.check("length");
		return Access.length(filename);
	}

	/**
	 * 数据写入操作
	 * @param casket INSERT封装包
	 * @return AccessStack字节数组
	 */
	public static byte[] insert(InsertCasket casket) {
		// 诊断INSERT权限
		AccessTrustor.check("insert");
		// 写入磁盘
		byte[] primitive = casket.build();
		return Access.insert(primitive);
	}

	/**
	 * 数据写入操作
	 * @param cmd 命令
	 * @return AccessStack字节数组
	 */
	public static byte[] insert(Insert cmd) {
		InsertCasket casket = new InsertCasket(cmd);
		return AccessTrustor.insert(casket);
	}

	/**
	 * 数据写入操作
	 * @param siger 用户签名
	 * @param casket INSERT封装包
	 * @return AccessStack字节数组
	 */
	public static byte[] insert(Siger siger, InsertCasket casket) {
		// 诊断INSERT权限
		AccessTrustor.check("insert", siger);
		// 写入磁盘
		byte[] primitive = casket.build();
		return Access.insert(primitive);
	}

	/**
	 * 数据写入操作
	 * @param siger 用户签名
	 * @param cmd 命令
	 * @return AccessStack字节数组
	 */
	public static byte[] insert(Siger siger, Insert cmd) {
		InsertCasket casket = new InsertCasket(cmd);
		return AccessTrustor.insert(siger, casket);
	}

	/**
	 * 撤销之前通过“insert”方法写入的数据。 <br><br>
	 * 
	 * 特别声明：此方法只针对“insert”写入的数据。<br>
	 * “leave”操作发生在分布环境的三个协商处理中，当某个DATA节点发生网络故障或者写入失败时，其它DATA节点在CALL/WORK节点的控制下，被要求撤销之前已经成功写入的数据。<br>
	 * “leave”实质就是一个基于硬盘的数据回滚操作。<br>
	 * 
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @param reflex INSERT映像数据，根据它实现数据回滚
	 * @return AccessStack字节数组
	 */
	public static byte[] leave(Space space, long stub, byte[] reflex) {
		AccessTrustor.check("leave");
		String schema = space.getSchemaText();
		String table = space.getTableText();
		return Access.leave(schema, table, stub, reflex);
	}

	/**
	 * 撤销之前通过“insert”方法写入的数据。 <br><br>
	 * 
	 * 特别声明：此方法只针对“insert”写入的数据。<br>
	 * “leave”操作发生在分布环境的三个协商处理中，当某个DATA节点发生网络故障或者写入失败时，其它DATA节点在CALL/WORK节点的控制下，被要求撤销之前已经成功写入的数据。<br>
	 * “leave”实质就是一个基于硬盘的数据回滚操作。<br>
	 * 
	 * @param siger 用户签名
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @param reflex INSERT映像数据，根据它实现数据回滚
	 * @return AccessStack字节数组
	 */
	public static byte[] leave(Siger siger, Space space, long stub, byte[] reflex) {
		AccessTrustor.check("leave", siger);
		String schema = space.getSchemaText();
		String table = space.getTableText();
		return Access.leave(schema, table, stub, reflex);
	}

	/**
	 * 检测语法安全
	 * @param query 查询语句
	 * @throws AccessException
	 */
	private static void checkWhere(Query query) throws AccessException {
		boolean success = query.getWhere().isSupportJNI();
		if (!success) {
			throw new AccessException("illegal where!");
		}
	}

	/**
	 * 检索操作
	 * @param casket SELECT封装包
	 * @return AccessStack字节数组
	 */
	public static byte[] select(SelectCasket casket) throws AccessException {
		AccessTrustor.check("select");
		// 检测查询参数
		AccessTrustor.checkWhere(casket.getSelect());

		byte[] primitive = casket.build();
		return Access.select(primitive);
	}

	/**
	 * 检索操作
	 * @param select  SELECT命令
	 * @param stub 被检索的数据
	 * @return AccessStack字节数组
	 */
	public static byte[] select(Select select, long stub) throws AccessException {
		SelectCasket casket = new SelectCasket(select, stub);
		return AccessTrustor.select(casket);
	}

	/**
	 * 检索操作
	 * @param siger 用户签名
	 * @param casket SELECT封装包
	 * @return AccessStack字节数组
	 */
	public static byte[] select(Siger siger, SelectCasket casket) throws AccessException {
		AccessTrustor.check("select", siger);

		// 检测查询参数
		AccessTrustor.checkWhere(casket.getSelect());

		byte[] primitive = casket.build();
		return Access.select(primitive);
	}

	/**
	 * 检索操作
	 * @param siger 用户签名
	 * @param select SELECT命令
	 * @param stub 被检索的数据
	 * @return AccessStack字节数组
	 */
	public static byte[] select(Siger siger, Select select, long stub) throws AccessException {
		SelectCasket casket = new SelectCasket(select, stub);
		return AccessTrustor.select(siger, casket);
	}

	/**
	 * 删除操作
	 * @param casket DELETE封装包
	 * @return AccessStack字节数组
	 * @throws AccessException 
	 */
	public static byte[] delete(DeleteCasket casket) throws AccessException {
		// 判断有删除操作权限
		AccessTrustor.check("delete");
		// 检查SQL WHERE语句是否被支持
		AccessTrustor.checkWhere(casket.getDelete());

		byte[] primitive = casket.build();
		return Access.delete(primitive);
	}

	/**
	 * 删除操作
	 * @param cmd DELETE命令
	 * @param stub 数据块编号
	 * @return 被删除的数据
	 * @throws AccessException 
	 */
	public static byte[] delete(Delete cmd, long stub) throws AccessException {
		DeleteCasket casket = new DeleteCasket(cmd, stub);
		return AccessTrustor.delete(casket);
	}

	/**
	 * 删除操作
	 * @param siger 用户签名
	 * @param casket DELETE封装包
	 * @return AccessStack字节数组
	 * @throws AccessException 
	 */
	public static byte[] delete(Siger siger, DeleteCasket casket) throws AccessException {
		AccessTrustor.check("delete", siger);
		// 检查SQL WHERE语句是否被支持
		AccessTrustor.checkWhere(casket.getDelete());

		byte[] primitive = casket.build();
		return Access.delete(primitive);
	}

	/**
	 * 删除操作
	 * @param siger 用户签名
	 * @param cmd DELETE命令
	 * @param stub 数据块编号
	 * @return 被删除的数据
	 * @throws AccessException 
	 */
	public static byte[] delete(Siger siger, Delete cmd, long stub) throws AccessException {
		DeleteCasket casket = new DeleteCasket(cmd, stub);
		return AccessTrustor.delete(siger, casket);
	}

	/**
	 * 返回缓存映像块编号记录 <br><br>
	 * 
	 * @param local 本地站点地址
	 * 
	 * @return CacheReflexStub列表。如果出错，返回空指针
	 */
	public static List<CacheReflexStub> getCacheReflexStubs(Node local) {
		AccessTrustor.check("getCacheReflexStubs");

		// 从JNI接口中读取数据
		byte[] b = Access.getCacheReflexStubs();
		// 1. 出错
		if (b == null) {
			return null;
		}
		ArrayList<CacheReflexStub> array = new ArrayList<CacheReflexStub>();
		// 2. 没有，返回空集合
		if (b.length == 0) {
			return array;
		}
		// 3. 解析缓存映像数据块
		ClassReader reader = new ClassReader(b);
		while (reader.getLeft() > 0) {
			// 数据表名
			Space space = new Space(reader);
			CacheReflexStub item = new CacheReflexStub(local, space);
			// 数据块总数
			int size = reader.readInt();
			for (int i = 0; i < size; i++) {
				long stub = reader.readLong();
				item.addStub(stub);
			}
			array.add(item);
		}
		// 返回集合
		return array;
	}

	/**
	 * 判断缓存映像块（快照数据）存在
	 * 
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @return 存在返回真，否则假
	 */
	public static boolean hasCacheReflex(Space space, long stub) {
		AccessTrustor.check("hasCacheReflex");

		String schema = space.getSchemaText();
		String table = space.getTableText();
		return Access.hasCacheReflex(schema, table, stub);
	}

	/**
	 * 删除缓存映像块（快照数据）
	 * 
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @return 成功返回0，否则是负数（错误码）。
	 */
	public static int deleteCacheReflex(Space space, long stub) {
		AccessTrustor.check("deleteCacheReflex");

		String schema = space.getSchemaText();
		String table = space.getTableText();
		return Access.deleteCacheReflex(schema, table, stub);
	}

	/**
	 * 保存缓存映像数据。
	 * 
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @param reflex 映像数据
	 * @return 第一次写入返回0，否则返回1；失败返回负数（错误码）。
	 */
	public static int setCacheReflex(Space space, long stub, byte[] reflex) {
		AccessTrustor.check("setCacheReflex");

		String schema = space.getSchemaText();
		String table = space.getTableText();
		return Access.setCacheReflex(schema, table, stub, reflex);
	}

	/**
	 * 保存存储映像数据<br>
	 * 
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @param reflex 映像数据
	 * @return 成功返回0，否则是负数（错误码）。
	 */
	public static int setChunkReflex(Space space, long stub, byte[] reflex) {
		AccessTrustor.check("setChunkReflex");

		String schema = space.getSchemaText();
		String table = space.getTableText();
		return Access.setChunkReflex(schema, table, stub, reflex);
	}

	/**
	 * 返回主表缓存块的磁盘冗余数据长度。<br>
	 * 缓存块冗余数据即是被删除但是保存在磁盘上的数据，清除缓存块冗余数据用“compact”方法。<br><br>
	 * 
	 * @param space 数据表
	 * @return 返回大于竽0的数字，失败返回负数。
	 */
	public static long getRedundancy(Space space) {
		AccessTrustor.check("getRedundancy");

		String schema = space.getSchemaText();
		String table = space.getTableText();
		return Access.getRedundancy(schema, table);
	}

	/**
	 * 删除主表缓存块的磁盘冗余数据，调整到最小有效尺寸。
	 * 
	 * @param space 表名
	 * @return 成功，大于等于0返回。否则小于0（错误码）
	 */
	public static int compact(Space space) {
		AccessTrustor.check("compact");

		String schema = space.getSchemaText();
		String table = space.getTableText();
		return Access.compact(schema, table);
	}

	/**
	 * 将一个主表下面的缓存块，强制转换为存储块，即从未封闭状态转为封闭状态。
	 * @param space 数据表名
	 * @return 成功，大于等于0返回；否则是小于0（错误码）
	 */
	public static int rush(Space space) {
		AccessTrustor.check("rush");

		String schema = space.getSchemaText();
		String table = space.getTableText();
		return Access.rush(schema, table);
	}

	/**
	 * 对一个表下面的数据块进行数字签名
	 * @param space 数据表名
	 * @return 返回数据块签名列表，如果出错，返回空值
	 */
	public static List<StubSign> sign(Space space) {
		AccessTrustor.check("sign");

		String schema = space.getSchemaText();
		String table = space.getTableText();
		byte[] b = Access.sign(schema, table);
		// 出错
		if (b == null) {
			Logger.error("AccessTrustor.sign, do %s failed", space);
			return null;
		}

		// 解析数据块签名
		ClassReader reader = new ClassReader(b);
		int size = reader.readInt();
		// 检查尺寸溢出
		int capacity = size * StubSign.volume();
		if (reader.isReadout(capacity)) {
			Logger.error("AccessTrustor.sign, readout! %d > %d ", capacity, reader.getLeft());
			return null;
		}

		// 读数据
		ArrayList<StubSign> array = new ArrayList<StubSign>(size);
		for (int i = 0; i < size; i++) {
			long stub = reader.readLong();	 	// 数据块编号
			byte status = reader.read(); 		// 状态(CACHE/CHUNK)
			long lastModified = reader.readLong(); // 最后修改时间
			byte[] md5 = reader.read(MD5Hash.volume()); // MD5编码
			MD5Hash hash = new MD5Hash(md5);
			// 建立实例和保存
			StubSign sign = new StubSign(stub, status, lastModified, hash);
			array.add(sign);
		}

		return array;
	}

	/**
	 * 对一个表下面的某个数据块进行数字签名。<br>
	 * 这个数据块可以是CACHE/CHUNK/CACHEREFLEX状态中的其中一种。<br><br>
	 * 
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @return 返回数字块签名。如果出错返回空指针。
	 */
	public static StubSign affix(Space space, long stub) {
		AccessTrustor.check("sign");

		String schema = space.getSchemaText();
		String table = space.getTableText();
		byte[] b = Access.affix(schema, table, stub);
		// 出错
		if (b == null) {
			Logger.error("AccessTrustor.affix, do %s#%x failed", space, stub);
			return null;
		}

		// 解析数据块签名
		ClassReader reader = new ClassReader(b);
		// 只能33个字节
		if (reader.isReadout(StubSign.volume())) {
			Logger.error("AccessTrustor.affix, readout! %d != 33 ", reader.getLeft());
			return null;
		}

		long number = reader.readLong(); // 数据块编号
		byte status = reader.read(); // 状态
		long lastModified = reader.readLong(); // 最后修改时间
		byte[] md5 = reader.read(MD5Hash.volume()); // MD5编码
		MD5Hash hash = new MD5Hash(md5);
		// 生成实例返回
		return new StubSign(number, status, lastModified, hash);
	}

	/**
	 * 根据授权初始化数据存取接口 <br>
	 * 这个方法是数据存取操作的第一步，只有这个方法调用成功之后，才能进行调用其它方法。
	 * 
	 * @param license 授权许可证
	 * @return 成功返回0，否则是负数（错误码）
	 */
	public static int initialize(byte[] license) {
		AccessTrustor.check("initialize");

		return Access.initialize(license);
	}

	/**
	 * 数据优化。<br>
	 * @param dock 列空间
	 * @return 返回优化后的数据块编号集合（stub数组）；失败，返回空指针。
	 */
	public static long[] regulate(Dock dock) {
		AccessTrustor.check("regulate");

		Space space = dock.getSpace();
		String schema = space.getSchemaText();
		String table = space.getTableText();
		short columnId = dock.getColumnId();
		return Access.regulate(schema, table, columnId);
	}

	/**
	 * 设置数据存取层上的最大并发任务数目
	 * @param how 任务数目
	 * @return 成功返回0，否则是负数。
	 */
	public static int setWorker(int how) {
		AccessTrustor.check("setWorker");

		return Access.setWorker(how);
	}

	/**
	 * 设置数据优化的根目录(regulate操作后的数据存储目录)
	 * 
	 * @param path 本地目录
	 * @return 成功，返回0；否则是负数（错误码）
	 */
	public static int setRegulateDirectory(String path) {
		AccessTrustor.check("setRegulateDirectory");
		return Access.setRegulateDirectory(path);
	}

	/**
	 * 设置缓存数据块的根目录<br>
	 * 缓存块目录只允许一个<br><br>
	 * 
	 * @param path 本地目录
	 * @return 成功，返回0；否则是负数（错误码）
	 */
	public static int setCacheDirectory(String path) {
		AccessTrustor.check("setCacheDirectory");
		return Access.setCacheDirectory(path);
	}

	/**
	 * 设置存储数据块的根目录 <br>
	 * 存储数据目录可以有多个硬盘、多下目录。<br><br>
	 * 
	 * @param path 本地目录
	 * @return 成功，返回0；否则是负数（错误码）
	 */
	public static int setChunkDirectory(String path) {
		AccessTrustor.check("setChunkDirectory");
		return Access.setChunkDirectory(path);
	}

	/**
	 * 根据表配置，在磁盘上建立一个全新的数据空间。如果磁盘有旧的同名记录，它们将被删除。
	 * 
	 * @param table 表配置
	 * @return 成功返回>=0，否则是负数（错误码）
	 */
	public static int createSpace(Table table) {
		AccessTrustor.check("createSpace");
		byte[] primitive = table.build();
		return Access.createSpace(primitive);
	}

	/**
	 * 删除表空间下属的全部目录以及目录下的数据块文件
	 * @param space 数据表名
	 * @return 成功，返回被删除的数据块数量；否则是负数（错误码）
	 */
	public static int deleteSpace(Space space) {
		AccessTrustor.check("deleteSpace");
		String schema = space.getSchemaText();
		String table = space.getTableText();
		return Access.deleteSpace(schema, table);
	}

	/**
	 * 初始化数据空间，但是不启动（注册数据）
	 * 
	 * @param table 数据表
	 * @return 成功返回0，否则是负数（错误码）
	 */
	public static int initSpace(Table table) {
		AccessTrustor.check("initSpace");
		byte[] primitive = table.build();
		return Access.initSpace(primitive);
	}

	/**
	 * 初始化数据空间，并且启动
	 * 
	 * @param table 表配置
	 * @return 返回被加载的数据块数目（包括一个CACHE状态和N个CHUNK状态的数据块）；不成功返回负数（错误码）
	 */
	public static int loadSpace(Table table) {
		AccessTrustor.check("loadSpace");
		byte[] primitive = table.build();
		return Access.loadSpace(primitive);
	}

	/**
	 * 停止和关闭一个表空间服务 
	 * @param space 数据表名
	 * @return 返回被停止的数据块数目
	 */
	public static int stopSpace(Space space) {
		AccessTrustor.check("stopSpace");
		String schema = space.getSchemaText();
		String table = space.getTableText();
		return Access.stopSpace(schema, table);
	}

	/**
	 * 判断表空间存在
	 * @param space 数据表名
	 * @return 存在返回真，否则假
	 */
	public static boolean hasSpace(Space space) {
		AccessTrustor.check("hasSpace");
		String schema = space.getSchemaText();
		String table = space.getTableText();
		return Access.hasSpace(schema, table);
	}

	/**
	 * 获取全部数据表名
	 * @return Space集合
	 */
	public static List<Space> getAllSpaceLogs() {
		AccessTrustor.check("getAllSpaceLogs");

		byte[] b = Access.getAllSpaceLogs();
		// 出错
		if (b == null) {
			return null;
		}
		// 读取
		ArrayList<Space> array = new ArrayList<Space>();
		// 2. 零长度
		if (b.length == 0) {
			return array;
		}
		// 3. 解析
		ClassReader reader = new ClassReader(b);
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Space space = new Space(reader);
			array.add(space);
		}
		return array;
	}

	/**
	 * 启动数据存取服务，包括工作线程
	 * @param init 启动参数
	 * @return 成功返回0，否则是负数（错误码）
	 */
	public static int launch(byte[] init) {
		AccessTrustor.check("launch");
		return Access.launch(init);
	}

	/**
	 * 判断启动成功。
	 * @return 成功返回“真”，否则“假”。
	 */
	public static boolean isLaunched() {
		AccessTrustor.check("isLaunched");
		return Access.isLaunched();
	}

	/**
	 * 关闭数据存取服务，和停止工作线程
	 * @return 成功返回0，否则是负数（错误码）
	 */
	public static int stop() {
		AccessTrustor.check("stop");
		return Access.stop();
	}

	/**
	 * 保存一个未使用的数据块编号
	 * @param stub 数据块编号
	 * @return 成功返回0，否则是负数（错误码）
	 */
	public static int addStub(long stub) {
		AccessTrustor.check("addStub");
		return Access.addStub(stub);
	}

	/**
	 * 统计没有使用的数据块编号数量
	 * @return 返回正整数，如果出错是负数（错误码）
	 */
	public static int getCountFreeStubs() {
		AccessTrustor.check("getCountFreeStubs");
		return Access.getCountFreeStubs();
	}

	/**
	 * 返回没有使用的数据块编号
	 * @return 返回一个长整型数组，没有数组是零长度；如果出错是空值。
	 */
	public static long[] getFreeStubs() {
		AccessTrustor.check("getFreeStubs");
		return Access.getFreeStubs();
	}

	/**
	 * 返回已经使用的数据块编号
	 * @return 返回长整型数组，没有数组是零长度；如果出错是空值。
	 */
	public static long[] getUsedStubs() {
		AccessTrustor.check("getUsedStubs");
		return Access.getUsedStubs();
	}

	/**
	 * 返回磁盘上的全部表空间索引记录，包括缓存状态和封闭状态的数据块索引。
	 * @return StubArea列表
	 */
	public static List<StubArea> getAllIndexLogs() {
		AccessTrustor.check("getAllIndexLogs");

		byte[] b = Access.getAllIndexLogs();
		if (Laxkit.isEmpty(b)) {
			return null;
		}

		// 解析索引数据
		ArrayList<StubArea> array = new ArrayList<StubArea>();
		ClassReader reader = new ClassReader(b);
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			StubArea area = new StubArea(reader);
			array.add(area);
		}

		return array;
	}

	/**
	 * 查询一个表空间下的全部数据块索引信息，包括缓存状态和封闭状态的数据块索引。
	 * @param space 数据表名
	 * @return 成功，返回数据块索引元数据。失败，返回空指针。
	 */
	public static byte[] findIndexLogs(Space space) {
		AccessTrustor.check("findIndexLogs");
		String schema = space.getSchemaText();
		String table = space.getTableText();
		return Access.findIndexLogs(schema, table);
	}

	/**
	 * 查询一个表空间下的全部数据块索引信息，包括缓存状态和封闭状态的数据块索引。
	 * 
	 * @param space 数据表名
	 * @return 返回StubArea；如果失败，返回空指针
	 */
	public static StubArea findIndex(Space space) {
		AccessTrustor.check("findIndex");

		byte[] b = AccessTrustor.findIndexLogs(space);
		if (Laxkit.isEmpty(b)) {
			return null;
		}

		ClassReader reader = new ClassReader(b);
		return new StubArea(reader);
	}

	/**
	 * 统计数据存取可用的磁盘容量。<br>
	 * 
	 * 返回一个长整型数组。下标0是磁盘自由空间尺寸，下标1是已经使用的空间尺寸。<br><br>
	 * 
	 * index 0: free size
	 * index 1: used size
	 * @return 返回长整型数组。
	 */
	public static long[] getDiskCapacity() {
		AccessTrustor.check("getDiskCapacity");
		return Access.getDiskCapacity();
	}

	/**
	 * 返回剩余磁盘空间（以字节计算）
	 * @return 长整型
	 */
	public static long getDiskFreeCapacity() {
		long[] a = AccessTrustor.getDiskCapacity();
		return a[0];
	}

	/**
	 * 返回已经使用的磁盘空间（以字节计算）
	 * @return 长整型
	 */
	public static long getDiskUsedCapacity() {
		long[] a = AccessTrustor.getDiskCapacity();
		return a[1];
	}


	/**
	 * 设置一个表空间下的数据块标准尺寸
	 * @param space 数据表名
	 * @param size 新的数据块尺寸
	 * @return 成功返回0，否则是负数（错误码）
	 */
	public static int setChunkSize(Space space, int size) {
		AccessTrustor.check("setChunkSize");
		String schema = space.getSchemaText();
		String table = space.getTableText();
		return Access.setChunkSize(schema, table, size);
	}

	/**
	 * 查询一个“CHUNK”状态的数据块文件路径
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @return 返回数据块文件路径
	 */
	public static String findChunkPath(Space space, long stub) {
		AccessTrustor.check("findChunkPath");
		String schema = space.getSchemaText();
		String table = space.getTableText();
		return Access.findChunkPath(schema, table, stub);
	}

	/**
	 * 查询一个“CACHE”状态的数据块文件路径
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @return 返回数据块文件路径。如果文件不存在，是空指针。
	 */
	public static String findCachePath(Space space, long stub) {
		AccessTrustor.check("findCachePath");
		String schema = space.getSchemaText();
		String table = space.getTableText();
		return Access.findCachePath(schema, table, stub);
	}

	/**
	 * 查询一个缓存映像块文件路径
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @return 返回数据块文件路径。如果文件不存在，是空指针。
	 */
	public static String findCacheReflexPath(Space space, long stub) {
		AccessTrustor.check("findCacheReflexPath");
		String schema = space.getSchemaText();
		String table = space.getTableText();
		return Access.findCacheReflexPath(schema, table, stub);
	}

	/**
	 * 根据数据表名和数据块文件名称，加载这个数据块
	 * 
	 * @param space 数据表名
	 * @param filename 文件路径
	 * @return 成功返回0，否则是负数（错误码）
	 */
	public static int loadChunk(Space space, String filename) {
		AccessTrustor.check("loadChunk");

		String schema = space.getSchemaText();
		String table = space.getTableText();
		return Access.loadChunk(schema, table, filename);
	}

	/**
	 * 根据数据表名和数据块编号，加载这个数据块。
	 * 
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @return 成功返回0，否则是负数（错误码）
	 */
	public static int loadChunk(Space space, long stub) {
		boolean success = AccessTrustor.hasChunk(space, stub);
		if (!success) {
			return -1;
		}
		String filename = AccessTrustor.doChunkFile(space, stub);
		return AccessTrustor.loadChunk(space, filename);
	}

	/**
	 * 判断指定的表和数据块存在
	 * 
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @return 返回真或者假
	 */
	public static boolean hasChunk(Space space, long stub) {
		AccessTrustor.check("hasChunk");
		String schema = space.getSchemaText();
		String table = space.getTableText();
		return Access.hasChunk(schema, table, stub);
	}

	/**
	 * 删除数据块
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @return 成功返回大于等于0，否则是负数
	 */
	public static int deleteChunk(Space space, long stub) {
		AccessTrustor.check("deleteChunk");
		String schema = space.getSchemaText();
		String table = space.getTableText();
		return Access.deleteChunk(schema, table, stub);
	}

	/**
	 * 将指定表空间下面的一个数据块，改为“主”状态。这个操作只限DATA主节点。
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @return 成功返回真，否则假
	 */
	public static boolean toPrime(Space space, long stub) {
		AccessTrustor.check("toPrime");
		String schema = space.getSchemaText();
		String table = space.getTableText();
		int ret = Access.toPrime(schema, table, stub);
		return (ret >= 0);
	}

	/**
	 * 将指定表空间下的一个数据块，改为“从”状态。这个操作只限DATA从节点。
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @return 成功返回真，否则假
	 */
	public static boolean toSlave(Space space, long stub) {
		AccessTrustor.check("toSlave");
		String schema = space.getSchemaText();
		String table = space.getTableText();
		int ret = Access.toSlave(schema, table, stub);
		return (ret >= 0);
	}

	/**
	 * 返回一个数据块总行数（包括有效和失效的行数）
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @return 返回行数；失败返回负数（错误码）
	 */
	public static long getRows(Space space, long stub) {
		AccessTrustor.check("getRows");
		String schema = space.getSchemaText();
		String table = space.getTableText();
		return Access.getRows(schema, table, stub);
	}

	/**
	 * 返回一个数据块有效行数
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @return 返回行数；失败返回负数（错误码）
	 */
	public static long getAvailableRows(Space space, long stub) {
		AccessTrustor.check("getAvailableRows");
		String schema = space.getSchemaText();
		String table = space.getTableText();
		return Access.getAvailableRows(schema, table, stub);
	}

	/**
	 * 根据数据表名，返回它的全部数据块编号<br>
	 * @param space 数据表名
	 * @return 返回数据块编号数组
	 */
	public static long[] getChunkStubs(Space space) {
		AccessTrustor.check("getChunkStubs");
		String schema = space.getSchemaText();
		String table = space.getTableText();
		return Access.getChunkStubs(schema, table);
	}

	/**
	 * 根据数据表名，返回它的缓存块编号
	 * @param space 数据表名
	 * @return 返回数据块编号，无效是0。
	 */
	public static long getCacheStub(Space space) {
		AccessTrustor.check("getCacheStub");
		String schema = space.getSchemaText();
		String table = space.getTableText();
		return Access.getCacheStub(schema, table);
	}

	/**
	 * 根据表空间和数据块编号，确定一个数据块的缓存映像文件路径。<br><br>
	 * 
	 * 说明：<br>
	 * 1. 这个操作只发生在DATA从站点上，如果是DATA主站点，将返回空指针。<br>
	 * 2. 如果数据块已经存在，无论是CACHE/CHUNK数据块，都返回空指针。<br>
	 * 3. 没有以上问题，将产生一个基于缓存映像目录下的缓存映像数据块文件名。<br>
	 * 
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @return 返回一个基于缓存映像目录的文件名，否则是空指针。
	 */
	public static String doCacheReflexFile(Space space, long stub) {
		AccessTrustor.check("doCacheReflexFile");
		String schema = space.getSchemaText();
		String table = space.getTableText();
		return Access.doCacheReflexFile(schema, table, stub);
	}

	/**
	 * 根据数据表名和数据块编号，确定一个数据块的文件路径。<br><br>
	 * 
	 * 存在两种情况：<br>
	 * 1. 如果数据块存在，返回它的磁盘文件名。<br>
	 * 2. 如果数据块不存在，系统将选择目录空间余量，产生一个新的文件名。新产生的文件名在磁盘上没有对应的数据块。<br><br>
	 * 
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @return 成功，返回这个数据块的存取路径；否则是空指针。
	 */
	public static String doChunkFile(Space space, long stub) {
		AccessTrustor.check("doChunkFile");
		String schema = space.getSchemaText();
		String table = space.getTableText();
		return Access.doChunkFile(schema, table, stub);
	}

	/**
	 * 设置运行站点等级。<br><br>
	 * 
	 * 站点等级与所属DATA/BUILD站点对应。根据站点等级，将实现不同的数据存取操作。<br>
	 * DATA节点分为主/从两种节点，BUILD节点全部属于主节点。<br>
	 * <b>特别注意：站点等级在站点启动时设置，一经定义不允许再改变！！！</b><br>
	 * 
	 * @param rank 站点级别(PRIME SITE / SLAVE SITE)
	 * @return 成功返回0，否则是负数（错误码）
	 */
	public static int setRank(int rank) {
		AccessTrustor.check("setRank");
		return Access.setRank(rank);
	}

	/**
	 * 将一个表空间下的全部索引，加载到内存中。加载后，可以提高数据检索速度。
	 * 
	 * @param space 数据表名
	 * @return 返回加载的数据块数量，不成功是负数（错误码）
	 */
	public static int loadIndex(Space space) {
		AccessTrustor.check("loadIndex");
		String schema = space.getSchemaText();
		String table = space.getTableText();
		return Access.loadIndex(schema, table);
	}

	/**
	 * 从内存中卸载一个表的全部索引
	 * @param space 数据表名
	 * @return 返回被卸载的数据块数量，不成功是负数（错误码）
	 */
	public static int stopIndex(Space space) {
		AccessTrustor.check("stopIndex");
		String schema = space.getSchemaText();
		String table = space.getTableText();
		return Access.stopIndex(schema, table);
	}

	/**
	 * 加载一个表的数据实体到内存中。<br>
	 * 
	 * @param space 数据表名
	 * @return 返回被加载的数据块数目；否则返回负数（错误码）。
	 */
	public static int loadEntity(Space space) {
		AccessTrustor.check("loadEntity");
		String schema = space.getSchemaText();
		String table = space.getTableText();
		return Access.loadEntity(schema, table);
	}

	/**
	 * 卸载一个表在内存中的全部实体数据 <br>
	 * 
	 * @param space 数据表名
	 * @return 返回被卸载的数据块数目，如果失败是负数（错误码）
	 */
	public static int stopEntity(Space space) {
		AccessTrustor.check("stopEntity");
		String schema = space.getSchemaText();
		String table = space.getTableText();
		return Access.stopEntity(schema, table);
	}

	/**
	 * 根据数据库表名，返回存储“数据优化”后的数据块的目录。优化后的数据是封装状态（CHUNK）。
	 * 
	 * @param space 数据表名
	 * @return 返回本地系统的规范化路径；不存在或者出错是空指针
	 */
	public static String getRegulatePath(Space space) {
		AccessTrustor.check("getRegulatePath");
		String schema = space.getSchemaText();
		String table = space.getTableText();
		return Access.getRegulatePath(schema, table);
	}

	/**
	 * 根据数据库表名，返回存放“CACHE”状态数据块的目录。（“CACHE”是数据块的未封闭状态）
	 * 
	 * @param space 数据表名
	 * @return 返回本地系统的规范化路径；不存在或者出错是空指针
	 */
	public static String getCachePath(Space space) {
		AccessTrustor.check("getCachePath");
		String schema = space.getSchemaText();
		String table = space.getTableText();
		return Access.getCachePath(schema, table);
	}

	/**
	 * 根据数据库表名和指定的排列下标，返回存放“CHUNK”状态数据块的目录。
	 * 因为“CHUNK”状态目录可以分别存在多个，所以要说明排列下标。（“CHUNK”是数据块的封闭状态）
	 * 
	 * @param space 数据表名
	 * @param index 目录排列下标，从0开始，不允许是负数。
	 * @return 返回本地系统的规范化路径；不存在或者出错是空指针
	 */
	public static String getChunkPath(Space space, int index) {
		AccessTrustor.check("getChunkPath");
		String schema = space.getSchemaText();
		String table = space.getTableText();
		return Access.getChunkPath(schema, table, index);
	}

	/**
	 * 按照指定的列空间，对磁盘上的数据进行排序整合（一个表下全部数据块进行综合排序并且输出）
	 * @param dock 列空间
	 * @return 返回重新排序后的数据长度；等于0是没有找到；失败小于0（错误码）
	 */
	public static long marshal(Dock dock) {
		AccessTrustor.check("marshal");
		Space space = dock.getSpace();
		String schema = space.getSchemaText();
		String table = space.getTableText();
		short columnId = dock.getColumnId();
		return Access.marshal(schema, table, columnId);
	}

	/**
	 * 取消数据排列操作，同时清除内存镜像数据。
	 * @param space 数据表名
	 * @return 成功返回0，否则是负数错误码。
	 */
	public static int unmarshal(Space space) {
		AccessTrustor.check("unmarshal");
		String schema = space.getSchemaText();
		String table = space.getTableText();
		return Access.unmarshal(schema, table);
	}

	/**
	 * 整合数据后分段输出。每次读取整合排序后的一段数据
	 * @param space 数据表名
	 * @param readlen 要求输出的数据字节长度
	 * @return 返回输出的数据
	 */
	public static byte[] educe(Space space, int readlen) {
		AccessTrustor.check("educe");
		String schema = space.getSchemaText();
		String table = space.getTableText();
		return Access.educe(schema, table, readlen);
	}

	/**
	 * 查找数据块对应的数据表名
	 * @param stub 数据块编号
	 * @return 返回数据表名，如果没有是空指针
	 */
	public static Space findStubSpace(long stub) {
		AccessTrustor.check("findStubSpace");

		byte[] b = Access.findStubFrom(stub);
		if (Laxkit.isEmpty(b)) {
			return null;
		}
		ClassReader reader = new ClassReader(b);
		return new Space(reader);
	}

	/**
	 * 设置列存储模型表的数据压缩倍数。<br>
	 * 这项操作只对当前和以后生成的数据块有效。<br>
	 * 声明：压缩倍数与数据所占用的磁盘空间呈反比，即压缩倍数越大，数据占用的磁盘空间越小。但是在输出过程中，因为统一以行存储模型输出，所以会占用较多的内存。<br><br>
	 * 
	 * @param space 数据表
	 * @param multiple 压缩倍数
	 * @return 成功返回0，失败返回错码（小于0）
	 */
	public static int setDSMReduce(Space space, int multiple) {
		AccessTrustor.check("setDSMReduce");
		String schema = space.getSchemaText();
		String table = space.getTableText();

		return Access.setDSMReduce(schema, table, multiple);
	}

	/**
	 * 设置所有列存储模型表的数据压缩倍数。<br>
	 * 这项操作只对当前和以后生成的数据块有效。<br>
	 * 声明：压缩倍数与数据所占用的磁盘空间呈反比，即压缩倍数越大，数据占用的磁盘空间越小。但是在输出过程中，因为统一以行存储模型输出，所以会占用较多的内存。<br><br>
	 * 
	 * @param multiple 压缩倍数
	 * @return 返回设置成功的DSM表统计。
	 */
	public static int setDSMReduce(int multiple) {
		AccessTrustor.check("setDSMReduce");
		return Access.setDSMReduce(multiple);
	}

	/**
	 * 返回列存储数据表的压缩倍数。
	 * 此操作只对列存储模型的数据表有效。
	 * 
	 * @param space 数据表名
	 * @return 返回数据表的压缩倍数，没有找到表返回0，小于0是错误。
	 */
	public static int getDSMReduce(Space space) {
		AccessTrustor.check("getDSMReduce");
		String schema = space.getSchemaText();
		String table = space.getTableText();

		return Access.getDSMReduce(schema, table);
	}
}