/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access;

/**
 * 磁盘数据存取接口<br>
 * 提供基于磁盘的数据、数据参数、文件的读写操作。<br><br>
 * 
 * 基础说明：<br>
 * 1. 数据库文件以“数据块（MASS）”的形式存在。它的标准文件长度是64M，理论长度不超过4G。这个参数可以在用户建表时定义。<br>
 * 2. 数据块按照状态分为存储块（CHUNK）和缓存块（CACHE）两种。存储块内容可变，长度不变。缓存块的内容和长度均可变，以数据追加方式增长。<br>
 * 3. 缓存块可以执行“添加、删除、查询”的操作，存储块执行“删除、查询”的操作。<br> 
 * 4. 数据块与DATA节点权级匹配，即DATA主节点下面的数据块全部属于“主块”，DATA从节点全部属于“从块”。<br>
 * 5. DATA主节点一个表空间下面，有缓存块和存储块两种，且缓存块只有一个，存储块数量不限。<br>
 * 6. DATA从节点只有存储块，它们是DATA主节点存储块的备份。<br>
 * 7. 缓存块填满后，自动转为存储块，然后更新到DATA从站点。 <br>
 * 8. DATA主节点的数据块（缓存块和存储块），发生“添加、删除、更新”后，将同步备份到从站点。这个过程自动完成，不需要人工干涉。<br>
 * 9. 在存储块和缓存块之外，还有一个缓存映像块（CACHE REFLEX），它是缓存块的备份，只在DATA从节点上存在，不参与数据检索。当DATA主节点因为故障失效后，做为数据恢复的依据使用。<br>
 * 10. Access中的JNI函数，被DATA/BUILD站点使用。<br><br><br>
 * 
 * 函数说明：<br>
 * 1. Access中的JNI接口函数，返回值有四种类型：<1> 整数值 <2> 字符串 <3> 字节数组 <4> 布尔值。<br>
 * 2. 整数主要用来表述函数返回码。一般成功标识是0，也有大于0的情况。错误小于0，即是一个负数。具体见每个函数的说明。<br> 
 * 
 * @author scott.liang
 * @version 1.6 05/05/2014
 * @since laxcus 1.0
 */
final class Access {

//	static {
//		try {
//			System.loadLibrary("laxcusdb");
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		}
//	}

	/**
	 * 将一段数据追加到指定文件的末尾 
	 * 
	 * @param filename 文件名称
	 * @param b 字节数组
	 * @param off 数组开始下标
	 * @param len 数组有效长度
	 * @return  返回大于等于0(>=0)的数据写入的文件下标，否则是负数（错误码）。
	 */
	static native long append(String filename, byte[] b, int off, int len);

	/**
	 * 将一段数据写入文件的指定下标位置。成功写入的前提是文件必须存在，且下标位置是有效的。
	 * 
	 * @param filename 文件名称
	 * @param fileoff 文件下标位置
	 * @param b 数据的字节数组
	 * @param off 数组开始下标
	 * @param len 数据有效长度
	 * @return  返回大于0的数据写入的文件下标，否则是负数（错误码）
	 */
	static native long write(String filename, long fileoff, byte[] b, int off, int len);

	/**
	 * 从文件的指定下标位置，读取一段数据和返回。成功读取的前提是文件必须存在，且下标位置是是有效的。
	 * 
	 * @param filename 文件名称
	 * @param fileoff 指定的文件下标
	 * @param len 指定的读取长度
	 * @return  返回读取的字节数组，失败是空指针。
	 */
	static native byte[] read(String filename, long fileoff, int len);

	/**
	 * 获取一个文件的长度
	 * @param filename 文件名称
	 * @return  返回文件长度，发生错误返回负数（错误码）
	 */
	static native long length(String filename);

	/**
	 * 写入数据到磁盘 <br>
	 * 
	 * 说明: <br>
	 * 1. 写入操作只发生在DATA主站点。<br>
	 * 2. 写入成功后将输出映像数据，然后由主站点把映像数据同步到从站点。<br>
	 * 
	 * @param primitive INSERT命令的字节数组
	 * @return  AccessStack的字节数组
	 */
	static native byte[] insert(byte[] primitive);

	/**
	 * 从磁盘删除数据。<br>
	 * 
	 * 说明：<br>
	 * 1. 删除操作只发生在DATA主站点 <br>
	 * 2. 删除完成后，DATA主站点把被删除的数据映射到DATA从站点。<br>
	 * 
	 * @param primitive DELETE命令的字节数组
	 * @return  AccessStack字节数组
	 */
	static native byte[] delete(byte[] primitive);

	/**
	 * 从磁盘检索数据。<br>
	 * 检索操作发生在主/从两个站点，每次操作一个数据块。<br>
	 * 
	 * @param primitive SELECT命令的字节数组
	 * @return  AccessStack字节数组
	 */
	static native byte[] select(byte[] primitive);

	/**
	 * 撤销之前通过“insert”方法写入的数据。 <br>
	 * 
	 * 说明：<br>
	 * 1.“leave”只针对“insert”写入的数据，实现基于硬盘的数据回滚。  <br>
	 * 2.“leave”操作发生在分布环境的三段协商处理过程中，
	 * 当某个DATA节点发生网络故障或者写入失败时，
	 * 其它DATA节点在CALL/WORK节点的控制下，撤销之前已经写入硬盘的数据。<br>
	 * 
	 * @param schema 数据库名
	 * @param table 数据表名
	 * @param stub 数据块编号
	 * @param reflex INSERT映像数据，根据它实现数据回滚
	 * @return  AccessStack字节数组
	 */
	static native byte[] leave(String schema, String table, long stub,
			byte[] reflex);


	/**
	 * 返回缓存映像块编号记录 <br><br>
	 * 
	 * 缓存映像块编号集合由“DATA从站点”产生，通过HOME站点转发，保存到关联表空间的CALL站点下面。<br>
	 * 在一个从站点的一个表空间下面，可以保存来自多个DATA主站点的映像数据（数据表名相同，数据块编号不同）。<br><br>
	 * 
	 * 
	 * 缓存映像块编号记录由多个单元组成，每个单元的格式如下：<br>
	 * 1. 数据表名 <br>
	 * 2. 数据块成员数目（4字节） <br>
	 * 3. 数据块编号（8字节），任意多个，由数据块成员数目决定<br> 
	 * 
	 * @return  返回映像数据的字节数组。如果没有，字节数组长度为0。如果出错，返回空指针。
	 */
	static native byte[] getCacheReflexStubs();

	/**
	 * 判断缓存映像块存在
	 * @param schema 数据库名
	 * @param table 数据表名
	 * @param stub 数据块编号
	 * @return 返回真或者假
	 */
	static native boolean hasCacheReflex(String schema, String table, long stub);

	/**
	 * 删除缓存映像块（快照数据）
	 * 
	 * @param schema 数据库名
	 * @param table 数据表名
	 * @param stub 数据块编号
	 * @return  成功返回0，否则是负数（错误码）。
	 */
	static native int deleteCacheReflex(String schema, String table, long stub);

	/**
	 * 保存缓存映像数据。<br>
	 * 缓存块的映像数据通常是以“追加”的方式写到数据块文件末尾，偶尔也有更新的情况发生。
	 * 
	 * @param schema 数据库名
	 * @param table 数据表名
	 * @param stub 数据块编号
	 * @param reflex 映像数据
	 * @return  第一次写入返回0，否则返回1；失败返回负数（错误码）。
	 */
	static native int setCacheReflex(String schema, String table, long stub, byte[] reflex);

	/**
	 * 保存存储映像数据<br>
	 * 存储块的映像数据是以“覆盖”的方式写入数据块文件，这种写入不改变文件长度。
	 * 
	 * @param schema 数据库名
	 * @param table 数据表名
	 * @param stub 数据块编号
	 * @param reflex 映像数据
	 * @return  成功返回0，否则是负数（错误码）。
	 */
	static native int setChunkReflex(String schema, String table, long stub, byte[] reflex);

	/**
	 * 返回主表缓存块的磁盘冗余数据长度。<br>
	 * 缓存块冗余数据即是被删除但是保存在磁盘上的数据，清除缓存块冗余数据用“compact”方法。<br><br>
	 * 
	 * @param schema 数据库名
	 * @param table 表名
	 * @return 返回大于竽0的数字。失败返回负数
	 */
	static native long getRedundancy(String schema, String table);

	/**
	 * 删除主表缓存块的磁盘冗余数据，调整到最小有效尺寸。
	 * 
	 * @param schema 数据库名
	 * @param table 表名
	 * @return 成功，大于等于0返回。否则小于0（错误码）
	 */
	static native int compact(String schema, String table);

	/**
	 * 将一个主表下面的缓存块，强制转换为存储块，即从未封闭状态转为封闭状态。
	 * 因为每个主表下面只有一个缓存块，所以这个操作只涉及一个缓存块。
	 * 
	 * @param schema 数据库名
	 * @param table 数据表名
	 * @return 成功，大于等于0返回；否则是小于0（错误码）
	 */
	static native int rush(String schema, String table);

	/**
	 * 对一个表下面的全部数据块进行数字签名，包含CACHE/CHUNK/CACHEREFLEX三种。<br><br>
	 * 
	 * 数据块签名格式：<br>
	 * <1> 数据块签名数目 <br>
	 * <2> 数据块签名 <br><br>
	 * 
	 * 数据块签名单元格式：<br>
	 * <2.1> 数据块编号（8字节）<br>
	 * <2.2> 数据块类型（CACHE/CHUNK/CACHEREFLEX 1字节）<br>
	 * <2.3> 文件最后修改日期(8字节) <br>
	 * <2.4> 数据块签名（MD5码，16字节）<br><br>
	 * 
	 * @param schema 数据库名
	 * @param table 数据表名
	 * @return  返回数据块签名的字节数组。如果出错，返回空指针
	 */
	static native byte[] sign(String schema, String table);

	/**
	 * 对一个数据块进行数字签名。包括CACHE/CHUNK/CACHEREFLEX三种一种。<br><br>
	 * 
	 * 数据块签名格式：<br>
	 * <1> 数据块编号（8字节）<br>
	 * <2> 数据块类型（CACHE/CHUNK/CACHEREFLEX 1字节）<br>
	 * <3> 文件最后修改日期（8字节）<br>
	 * <4> 数据块签名（MD5码位，16字节）<br><br>
	 * 
	 * @param schema 数据库名
	 * @param table 数据表名
	 * @param stub 数据块编号
	 * @return  数据块签名的字节数组（33字节）。如果出错，返回空指针。
	 */
	static native byte[] affix(String schema, String table, long stub);

	/**
	 * 根据授权初始化数据存取接口 <br>
	 * 这个方法是数据存取操作的第一步，只有这个方法调用成功之后，才能进行调用其它方法。
	 * 
	 * @param license 授权许可证
	 * @return  成功返回0，否则是负数（错误码）
	 */
	static native int initialize(byte[] license);

	/**
	 * 数据优化操作，对应“REGULATE schema.table”命令。<br>
	 * 
	 * 数据优化将一个表下属全部“CHUNK”状态数据块，以指定列为键值重新排序，然后输出回硬盘。优化过程只删除数据块中的过期数据（被DELETE删除但是仍然保留在磁盘上的数据），有效的数据和数据块编号仍然不变。<br>
	 * 
	 * 如果列编号是0，将使用默认的主键进行排序；否则将使用本次指定的列编号进行排序。<br>
	 * 
	 * 在JNI层面，数据优化每次只处理一个数据块，即把一个数据块调入内存，清除垃圾数据后，再输出回硬盘，这样可以减少内存占用。<br>
	 * 一个64M的数据块，数据优化时间大约是1秒钟左右，所以计算下来，总的时间也不会太长。<br><br>
	 * 
	 * @param schema 数据库名
	 * @param table 数据表名
	 * @param columnId 列编号
	 * @return  返回优化后的数据块编号集合（stub数组）；失败，返回空指针。
	 */
	static native long[] regulate(String schema, String table, short columnId);

	/**
	 * 设置数据存取层上的最大并发任务数目
	 * @param how 任务数目
	 * @return  成功返回0，否则是负数。
	 */
	static native int setWorker(int how);

	/**
	 * 设置数据优化的根目录(regulate操作后的数据存储目录)
	 * 
	 * @param path 本地目录
	 * @return  成功返回0；否则是负数（错误码）
	 */
	static native int setRegulateDirectory(String path);

	/**
	 * 设置缓存数据块的根目录<br>
	 * 缓存块目录只允许一个<br><br>
	 * 
	 * @param path 本地目录
	 * @return  成功返回0；否则是负数（错误码）
	 */
	static native int setCacheDirectory(String path);

	/**
	 * 设置存储数据块的根目录 <br>
	 * 存储数据目录可以有多个硬盘、多下目录。<br><br>
	 * 
	 * @param path 本地目录
	 * @return  成功返回0；否则是负数（错误码）
	 */
	static native int setChunkDirectory(String path);

	/**
	 * 根据表配置，在磁盘上建立一个全新的数据空间。如果磁盘有旧的同名记录，它们将被删除。
	 * 
	 * @param primitive "Table.buildX"方法原语
	 * @return  成功返回>=0，否则是负数（错误码）
	 */
	static native int createSpace(byte[] primitive);

	/**
	 * 删除表空间下属的全部目录以及目录下的数据块文件
	 * @param schema 数据库名
	 * @param table 数据表名
	 * @return  成功，返回被删除的数据块数量；否则是负数（错误码）
	 */
	static native int deleteSpace(String schema, String table);

	/**
	 * 初始化数据空间，但是不启动（注册数据）
	 * 
	 * @param primitive 数据表资源参数
	 * @return  成功返回0，否则是负数（错误码）
	 */
	static native int initSpace(byte[] primitive);

	/**
	 * 初始化数据空间，并且启动
	 * 
	 * @param primitive 表配置参数
	 * @return  返回被加载的数据块数目（包括一个CACHE状态和N个CHUNK状态的数据块）；不成功返回负数（错误码）
	 */
	static native int loadSpace(byte[] primitive);

	/**
	 * 停止和关闭一个表空间服务（只是停止和关闭，不删除磁盘数据！）
	 * @param schema 数据库名
	 * @param table 数据表名
	 * @return  返回被停止的数据块数目
	 */
	static native int stopSpace(String schema, String table);

	/**
	 * 判断一个表空间存在
	 * @param schema 数据库名
	 * @param table 数据表名
	 * @return  存在返回真，否则假
	 */
	static native boolean hasSpace(String schema, String table);

	/**
	 * 返回全部数据表名。结果是字节数组，用可类化解析。<br><br>
	 * 
	 * 格式：<br>
	 * 1. 表空间数量（4字节）<br>
	 * 2. 数据表名（new Space(ClassReader)解析）<br><br>
	 * 
	 * @return 返回表空间的字节数组。如果没有，字节数组长度是0。如果失败，返回空指针。
	 */
	static native byte[] getAllSpaceLogs();

	/**
	 * 启动数据存取服务，包括工作线程
	 * @param init 启动参数
	 * @return  成功返回0，否则是负数（错误码）
	 */
	static native int launch(byte[] init);

	/**
	 * 判断启动成功。
	 * @return  成功返回“真”，否则“假”。
	 */
	static native boolean isLaunched();

	/**
	 * 关闭数据存取服务，和停止工作线程
	 * @return  成功返回0，否则是负数（错误码）
	 */
	static native int stop();

	/**
	 * 保存一个未使用的数据块编号
	 * @param stub 数据块编号
	 * @return  成功返回0，否则是负数（错误码）
	 */
	static native int addStub(long stub);

	/**
	 * 统计没有使用的数据块编号数量
	 * @return  返回正整数，如果出错是负数（错误码）
	 */
	static native int getCountFreeStubs();

	/**
	 * 返回没有使用的数据块编号
	 * @return  返回一个长整型数组，没有数组是零长度；如果出错是空值。
	 */
	static native long[] getFreeStubs();

	/**
	 * 返回已经使用的数据块编号
	 * @return  返回长整型数组，没有数组是零长度；如果出错是空值。
	 */
	static native long[] getUsedStubs();

	/**
	 * 返回磁盘上的全部表空间索引记录，包括缓存状态和封闭状态的数据块索引。
	 * @return  成功，返回数据块索引字节数组；失败，返回空指针。
	 */
	static native byte[] getAllIndexLogs();

	/**
	 * 查询一个表空间下的全部数据块索引信息，包括缓存状态和封闭状态的数据块索引。
	 * @param schema 数据库名
	 * @param table 数据表名
	 * @return  成功，返回数据块索引字节数组；失败，返回空指针。
	 */
	static native byte[] findIndexLogs(String schema, String table);

	/**
	 * 统计数据存取可用的磁盘容量。<br>
	 * 
	 * 返回一个长整型数组。下标0是磁盘自由空间尺寸，下标1是已经使用的空间尺寸。<br><br>
	 * 
	 * index 0: free size
	 * index 1: used size
	 * @return  返回长整型数组。
	 */
	static native long[] getDiskCapacity();

	/**
	 * 设置一个表空间下的数据块标准尺寸
	 * 
	 * @param schema 数据库名
	 * @param table 数据表名
	 * @param size 新的数据块尺寸
	 * @return  成功返回0，否则是负数（错误码）
	 */
	static native int setChunkSize(String schema, String table, int size);

	/**
	 * 查询一个“CHUNK”状态的数据块文件路径
	 * @param schema 数据库名
	 * @param table 数据表名
	 * @param stub 数据块编号
	 * @return  返回数据块文件路径。如果文件不存在，是空指针。
	 */
	static native String findChunkPath(String schema, String table, long stub);

	/**
	 * 查询一个“CACHE”状态的数据块文件路径
	 * @param schema 数据库名
	 * @param table 数据表名
	 * @param stub 数据块编号
	 * @return  返回数据块文件路径。如果文件不存在，是空指针。
	 */
	static native String findCachePath(String schema, String table, long stub);

	/**
	 * 查询一个“CACHE REFLEX”状态的数据块文件路径
	 * @param schema 数据库名
	 * @param table 数据表名
	 * @param stub 数据块编号
	 * @return  返回数据块文件路径。如果文件不存在，是空指针。
	 */
	static native String findCacheReflexPath(String schema, String table, long stub);

	/**
	 * 根据数据表名和数据块文件名称，加载这个存储块
	 * 
	 * @param schema 数据库名
	 * @param table 数据表名
	 * @param filename 文件路径
	 * @return  成功返回0，否则是负数（错误码）
	 */
	static native int loadChunk(String schema, String table, String filename);

	/**
	 * 判断指定的表和数据块存在
	 * 
	 * @param schema 数据库名
	 * @param table 数据表名
	 * @param stub 数据块编号
	 * @return  存在返回真，否则假
	 */
	static native boolean hasChunk(String schema, String table, long stub);

	/**
	 * 从磁盘上，删除指定表和编号的数据块
	 * @param schema 数据库名
	 * @param table 数据表名
	 * @param stub 数据块编号
	 * @return  删除成功，返回大于等于0；否则是负数（错误码）
	 */
	static native int deleteChunk(String schema, String table, long stub);

	/**
	 * 将指定表空间下面的一个数据块，改为“主”状态。这个操作只限DATA主节点。
	 * @param schema 数据库名
	 * @param table 数据表名
	 * @param stub 数据块编号
	 * @return  成功返回0，否则是负数
	 */
	static native int toPrime(String schema, String table, long stub);

	/**
	 * 将指定表空间下的一个数据块，改为“从”状态。这个操作只限DATA从节点。
	 * @param schema 数据库名
	 * @param table 数据表名
	 * @param stub 数据块编号
	 * @return  成功返回0，否则是负数
	 */
	static native int toSlave(String schema, String table, long stub);

	/**
	 * 返回一个数据块总行数（包括有效和失效的行数）
	 * @param schema 数据库名
	 * @param table 数据表名
	 * @param stub 数据块编号
	 * @return  返回行数；失败返回负数（错误码）
	 */
	static native long getRows(String schema, String table, long stub);

	/**
	 * 返回一个数据块有效行数
	 * @param schema 数据库名
	 * @param table 数据表名
	 * @param stub 数据块编号
	 * @return  返回行数；失败返回负数（错误码）
	 */
	static native long getAvailableRows(String schema, String table, long stub);

	/**
	 * 根据数据表名，返回它的全部数据块编号<br>
	 * 说明：数据块是已经封闭的文件。<br>
	 * @param schema 数据库名
	 * @param table 数据表名
	 * @return  返回数据块编号数组
	 */
	static native long[] getChunkStubs(String schema, String table);

	/**
	 * 根据数据表名，返回它的缓存块编号  <br>
	 * 说明：缓存块是没有封闭的数据集合。DATA主节点上，每个表空间都只有一个缓存块。DATA从节点和BUILD节点没有缓存块。<br>
	 * @param schema 数据库名
	 * @param table 数据表名
	 * @return  返回数据块编号，无效是0。
	 */
	static native long getCacheStub(String schema, String table);

	/**
	 * 根据表空间和数据块编号，确定一个数据块的缓存映像文件路径。<br><br>
	 * 
	 * 说明：<br>
	 * 1. 这个操作只发生在DATA从站点上，如果是DATA主站点，将返回空指针。<br>
	 * 2. 如果数据块已经存在，无论是CACHE/CHUNK数据块，都返回空指针。<br>
	 * 3. 没有以上问题，将产生一个基于缓存映像目录下的缓存映像数据块文件名。<br>
	 * 
	 * @param schema 数据库名
	 * @param table 数据表名
	 * @param stub 缓存映像数据块编号
	 * @return  返回一个基于缓存映像目录的文件名，否则是空指针。
	 */
	static native String doCacheReflexFile(String schema, String table, long stub);

	/**
	 * 根据数据表名和数据块编号，确定一个数据块的文件路径。<br><br>
	 * 
	 * 存在两种情况：<br>
	 * 1. 如果数据块存在，返回它的磁盘文件名。<br>
	 * 2. 如果数据块不存在，系统将选择目录空间余量，产生一个新的文件名。新产生的文件名在磁盘上没有对应的数据块。<br><br>
	 * 
	 * @param schema 数据库名
	 * @param table 数据表名
	 * @param stub 数据块编号
	 * @return  成功，返回这个数据块的存取路径；否则是空指针。
	 */
	static native String doChunkFile(String schema, String table, long stub);

	/**
	 * 设置运行站点等级。<br><br>
	 * 
	 * 站点等级与所属DATA/BUILD站点对应。根据站点等级，将实现不同的数据存取操作。<br>
	 * DATA节点分为主/从两种节点，BUILD节点全部属于主节点。<br>
	 * <b>特别注意：站点等级在站点启动时设置，一经定义不允许再改变！！！</b><br>
	 * 
	 * @param rank 站点级别(MASTER SITE / SLAVE SITE)，见RankTag定义
	 * @return  成功返回0，否则是负数（错误码）
	 */
	static native int setRank(int rank);

	/**
	 * 将一个表空间下的全部索引，加载到内存中。加载后，可以提高数据检索速度。<br><br>
	 * 
	 * 说明：<br>
	 * 在数据存储层，使用分支预测算法进行数据检索，并不存在数据库意义上的索引。所以采用“索引”这个词，是出于语义兼容的考虑。<br>
	 * 分支预测只需要一次简单的数据计算即可得到检索结果，避免了二叉树递次缩小的问题，所以处理效率远高于数据库的索引。<br>
	 * 为分支测试提供计算依据的是分支位序，分支位序16个字节。这里所谓的加载索引，是把分支位序导入内存。<br><br>
	 * 
	 * @param schema 数据库名
	 * @param table 数据表名
	 * @return  返回加载的数据块数量，不成功是负数（错误码）
	 */
	static native int loadIndex(String schema, String table);

	/**
	 * 从内存中卸载一个表的全部索引
	 * @param schema 数据库名
	 * @param table 数据表名
	 * @return  返回被卸载的数据块数量，不成功是负数（错误码）
	 */
	static native int stopIndex(String schema, String table);

	/**
	 * 加载一个表的实体数据到内存中。<br><br>
	 * 
	 * <b>加载实体数据，实际是把数据的磁盘读写变成内存读写，对提高数据检索效率有立竿见影的效果，
	 * 特别适合流式处理或者内存计算项目使用，但同时也非常消耗内存，需慎重使用。</b><br><br>
	 * 
	 * @param schema 数据库名
	 * @param table 数据表名
	 * @return  返回被加载的数据块数目；否则返回负数（错误码）。
	 */
	static native int loadEntity(String schema, String table);

	/**
	 * 卸载一个表在内存中的全部实体数据 <br>
	 * 
	 * @param schema 数据库名
	 * @param table 数据表名
	 * @return  返回被卸载的数据块数目，如果失败是负数（错误码）
	 */
	static native int stopEntity(String schema, String table);

	/**
	 * 根据数据库表名，返回存储“数据优化”后的数据块的目录。优化后的数据是封装状态（CHUNK）。
	 * @param schema 数据库名
	 * @param table 数据表名
	 * @return  返回本地系统的规范化路径；不存在或者出错是空指针
	 */
	static native String getRegulatePath(String schema, String table);

	/**
	 * 根据数据库表名，返回存放“CACHE”状态数据块的目录。（“CACHE”是数据块的未封闭状态）
	 * 
	 * @param schema 数据库名
	 * @param table 数据表名
	 * @return  返回本地系统的规范化路径；不存在或者出错是空指针
	 */
	static native String getCachePath(String schema, String table);

	/**
	 * 根据数据库表名和指定的排列下标，返回存放“CHUNK”状态数据块的目录。
	 * 因为“CHUNK”状态目录可以分别存在多个，所以要说明排列下标。（“CHUNK”是数据块的封闭状态）
	 * 
	 * @param schema 数据库名
	 * @param table 数据表名
	 * @param index 目录排列下标，从0开始，不允许是负数。
	 * @return  返回本地系统的规范化路径；不存在或者出错是空指针
	 */
	static native String getChunkPath(String schema, String table, int index);

	/**
	 * 将一个表空间下的全部数据块，按照指定列编号进行升序排序和排列。如果列编号是0，将用主键进行排序。<br><br>
	 * 
	 * “MARSHAL”执行后，将在内存中形成对一个表数据的映像，但是映像数据不改变实体数据在磁盘的存储形态。<br>
	 * 
	 * @param schema 数据库名
	 * @param table 数据表名
	 * @param columnId 列编号
	 * @return  返回重新排序后的数据长度；等于0是没有找到；失败小于0（错误码）
	 */
	static native long marshal(String schema, String table, short columnId);

	/**
	 * 取消数据排列，同时清除内存镜像数据。
	 * 
	 * @param schema 数据库名
	 * @param table 数据表名
	 * @return  成功返回0，否则是负数错误码。
	 */
	static native int unmarshal(String schema, String table);

	/**
	 * 在“marshal”排序后读取磁盘数据。
	 * 
	 * @param schema 数据库名
	 * @param table 数据表名
	 * @param readlen 指定读取尺寸
	 * @return  返回指定长度的字节数组，如果读完返回0长度数组，出错返回空指针。
	 */
	static native byte[] educe(String schema, String table, int readlen);

	/**
	 * 根据数据块编号查找它对应的数据表名
	 * @param stub 数据块编号
	 * @return  返回数据表名的字节数组，用“new Space(ClassReader)”解析；如果不存在，返回空指针。
	 */
	static native byte[] findStubFrom(long stub);

	/**
	 * 设置一个列存储模型表的数据压缩倍数。<br>
	 * 这项操作只对当前和以后生成的数据块有效。<br>
	 * 声明：压缩倍数与数据所占用的磁盘空间呈反比，即压缩倍数越大，数据占用的磁盘空间越小。但是在输出过程中，因为统一以行存储模型输出，所以会占用较多的内存。<br><br>
	 * 
	 * @param schema 数据库名
	 * @param table 表名
	 * @param multiple 压缩倍数。必须是大于或者等于1，数字越高，压缩比越大。
	 * @return 成功返回0，失败返回错误码（小于0）
	 */
	static native int setDSMReduce(String schema, String table, int multiple);

	/**
	 * 设置所有列存储模型表的数据压缩倍数。<br>
	 * 这项操作只对当前和以后生成的数据块有效。<br>
	 * 声明：压缩倍数与数据所占用的磁盘空间呈反比，即压缩倍数越大，数据占用的磁盘空间越小。但是在输出过程中，因为统一以行存储模型输出，所以会占用较多的内存。<br><br>
	 * 
	 * @param multiple 压缩倍数。属于是大于或者等于1，数字越高，压缩比越大。
	 * @return 返回设置的DSM表统计值
	 */
	static native int setDSMReduce(int multiple);
	
	/**
	 * 返回数据表列存储模型的压缩倍数。<br>
	 * 此操作只对列存储模型的数据表有效。<br>
	 * 
	 * @param schema 数据库名
	 * @param table 表名
	 * @return 返回数据表的压缩倍数，表没有找到返回0，小于0是错误。
	 */
	static native int getDSMReduce(String schema, String table);
}