/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.dict;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.rebuild.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.each.*;
import com.laxcus.util.lock.*;
import com.laxcus.util.markable.*;

/**
 * 账号管理器。<BR><BR>
 * 
 * 针对一个目录下的多个条件，提供账号和账号关键参数的内存托管、磁盘读写处理。<BR>
 * 数据库名、数据表名、快捷组件名、触发器，因为占用空间小，在内存长期驻留，直到被要求删除。<BR>
 * 
 * @author scott.liang
 * @version 1.0 7/3/2018
 * @since laxcus 1.0
 */
public class AccountManager extends MutexHandler {

	/** 单个账号的最大空间尺寸，默认128K **/
	private static int accountSize = 128 * 1024;

	/** 一个账号数据块的最大空间尺寸，默认32M **/
	private static int blockSize = 32 * 1024 * 1024;

	/** 最大超时，默认5分钟 **/
	private static long timeout = 5 * 60 * 1000;

	/** 账号文件后缀 **/
	protected final static String SUFFIX = ".dict";

	/** 正则表达式 **/
	private final static String SUFFIX_REGEX = "^\\s*(?i)([1-9][0-9]*)(?:\\.dict)\\s*$";

	/**
	 * 设置账号的最大空间尺寸
	 * 
	 * @param who 数据尺寸
	 */
	public static void setAccountSize(int who) {
		if (who < 0) {
			throw new IllegalValueException("illegal account size:%d", who);
		}
		accountSize = who;
	}

	/**
	 * 返回账号的最大空间尺寸
	 * 
	 * @return 数据尺寸
	 */
	public static int getAccountSize() {
		return accountSize;
	}

	/**
	 * 设置账号数据块的规定尺寸
	 * 
	 * @param who 数据尺寸
	 */
	public static void setBlockSize(int who) {
		if (who < 0) {
			throw new IllegalValueException("illegal block size:%d", who);
		}
		blockSize = who;
	}

	/**
	 * 返回账号数据块的规定尺寸
	 * 
	 * @return 数据尺寸
	 */
	public static int getBlockSize() {
		return blockSize;
	}

	/**
	 * 设置账号的内存超时时间 
	 * @param ms 毫秒
	 */
	public static void setTimeout(long ms) {
		if (ms > 1000) {
			timeout = ms;
		}
	}

	/**
	 * 返回账号的内存超时时间
	 * @return 毫秒
	 */
	public static long getTimeout() {
		return timeout;
	}

	/** 根目录，配置文件分配 **/
	private File root;

	/** 管理器编号，启动时分配 **/
	private int index;

	/** 当前文件 **/
	private AccountFile current;

	/** 数据库名 -> 用户签名。为了保持数据库名的唯一性，由TOP站点对数据库名进行分配 **/
	private TreeMap<Fame, Siger> mapFames = new TreeMap<Fame, Siger>();

	/** 数据表名 -> 用户签名 **/
	private TreeMap<Space, Siger> mapSpaces = new TreeMap<Space, Siger>();

	/** 触发器 -> 用户签名 **/
	private TreeMap<SwitchTime, Siger> mapTimes = new TreeMap<SwitchTime, Siger>();

	/** 用户签名 -> 存储账号。暂时内存在内存，定时删除 **/
	private TreeMap<Siger, Account> mapAccounts = new TreeMap<Siger, Account>();

	/** 被删除的账号数目 **/
	private int deletes;

	/**
	 * 构造一个默认的账号管理器
	 */
	public AccountManager() {
		super();
	}

	/**
	 * 设置管理器编号
	 * @param i 管理器编号
	 */
	public void setIndex(int i) {
		index = i;
	}

	/**
	 * 返回管理器编号
	 * @return 管理器编号
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * 返回删除的账号数目，两种可能：<br>
	 * 1. 账号被删除。<br>
	 * 2. 账号尺寸增加，新的写入，旧的删除。<br>
	 * 
	 * @return 删除账号数目
	 */
	public int getDeletes() {
		return deletes;
	}

	/**
	 * 删除增1
	 */
	private void addDelete() {
		deletes++;
	}

	/**
	 * 设置账号管理器存储目录
	 * @param path 文件名
	 * @return 成功返回真，否则假
	 */
	public boolean setRoot(File path) {
		// 检查目录，如果不存在，则建立目录
		boolean success = (path.exists() && path.isDirectory());
		if (!success) {
			success = path.mkdirs();
		}
		if (success) {
			root = path;
		}
		return success;
	}

	/**
	 * 设置账号管理器存储目录
	 * @param path 根目录
	 * @return 成功返回真，否则假
	 */
	public boolean setRoot(String path) {
		String dir = ConfigParser.splitPath(path);
		return setRoot(new File(dir));
	}

	/**
	 * 返回账号管理器存储目录
	 * @return 账号管理器存储目录
	 */
	public File getRoot() {
		return root;
	}

	/**
	 * 输出当前文件
	 * @return 账号文件实例
	 */
	protected AccountFile getCurrentFile() {
		return current;
	}

	/**
	 * 输出这个管理器下面的全部文件编号
	 * 
	 * @return 返回整型数组。如果没有，是一个0数组的整型数组。
	 */
	protected int[] getFileSerials() {
		ArrayList<Integer> array = new ArrayList<Integer>();
		// 枚兴当前磁盘目录下的全部文件
		File[] files = root.listFiles();
		// 逐一提取参数名
		for (File file : files) {
			String name = file.getName();
			Logger.debug(this, "getFileSerials", "file is %s", file);

			// 用正则表达式判断文件名
			Pattern pattern = Pattern.compile(SUFFIX_REGEX);
			Matcher matcher = pattern.matcher(name);
			if (!matcher.matches()) {
				Logger.debug(this, "getFileSerials", "ignore %s", name);
				continue;
			}

			// 取出文件数字
			int no = Integer.parseInt(matcher.group(1));
			array.add(no);
		}
		// 转换
		int size = array.size();
		int[] serials = new int[size];
		for (int index = 0; index < size; index++) {
			serials[index] = array.get(index).intValue();
		}
		return serials;
	}

	/**
	 * 加载一个根目录下的全部账号文件
	 * @param root 根目录
	 * @return 加载的账号列表，失败返回空指针
	 */
	protected List<AccountDock> loadAccounts() throws IOException {
		ArrayList<AccountDock> array = new ArrayList<AccountDock>(102440);

		File[] files = root.listFiles();
		// 枚举和检查每个账号存储文件，提取里面的信息
		for (File file : files) {
			String name = file.getName();
			Logger.debug(this, "loadAccounts", "file is %s", file);

			// 用正则表达式判断文件名
			Pattern pattern = Pattern.compile(SUFFIX_REGEX);
			Matcher matcher = pattern.matcher(name);
			if (!matcher.matches()) {
				Logger.debug(this, "loadAccounts", "ignore %s", name);
				continue;
			}

			// 取出文件数字
			int no = Integer.parseInt(matcher.group(1));

			// 截取最后一个文件
			if (current == null) {
				current = new AccountFile(no, file);
			} else if (current.getNo() < no) {
				current.setNo(no);
				current.setFile(file);
			}

			// 加载一个账号文件
			List<AccountDock> sub = readAccounts(no, file);
			if(sub == null) {
				return null;
			}
			// 统计加载的账号数目
			array.addAll(sub);
		}
		return array;
	}

	/**
	 * 读取一个文件中的全部账号
	 * @param no 磁盘文件编号
	 * @param file 文件实例
	 * @return 返回账号坐标集合
	 * @throws IOException
	 */
	private List<AccountDock> readAccounts(int no, File file) throws IOException {
		// 读磁盘文件
		byte[] b = new byte[(int) file.length()];
		FileInputStream in = new FileInputStream(file);
		in.read(b);
		in.close();

		// 可类化读取器，逐一解析参数
		ClassReader reader = new ClassReader(b);
		// 统计值
		ArrayList<AccountDock> array = new ArrayList<AccountDock>(blockSize / accountSize);

		// 逐一解析账号，保存到内存
		while (reader.hasLeft()) {
			// 磁盘下标位置
			int diskoff = reader.getSeek();
			// 读前缀数字
			AccountTag tag = new AccountTag(reader);

			// 校验和发生错误，参数不能读取，退出
			if (!tag.checkLookup()) {
				Logger.error(this, "readAccounts", "tag checksum error:%X - %X", tag.lookup(), tag.getTagSum());
				return null;
			}

			// 如果无效，跳过剩余字节
			if (tag.isDisabled()) {
				reader.skip(tag.getLength() - AccountTag.capacity());
				addDelete();
				continue;
			}

			// 读取长度
			byte[] content = reader.read(tag.getContentLength());
			// 跳过剩余的填空字节
			reader.skip(tag.getPadding());

			long sum = EachTrustor.sign(content);
			// 校验和出错，退出！
			if (sum != tag.getContentSum()) {
				Logger.error(this, "readAcounts", "content checksum error:%X - %X", sum, tag.getContentSum());
				continue;
			}

			// 从标记化读取器中读取
			MarkReader markReader = new MarkReader(content);
			Account account = new Account(markReader);
			Siger siger = account.getUsername().duplicate();

			// 磁盘坐标
			DiskDock dd = new DiskDock(no, diskoff, tag.getLength());
			// 账号坐标
			AccountDock dock = new AccountDock(siger, dd);

			// 保存数据库名
			for (Fame fame : account.getFames()) {
				addSchema(siger, fame);
			}
			// 保存表名
			for (Space space : account.getSpaces()) {
				addTable(siger, space);
			}
			// 优化触发器
			for (SwitchTime time : account.getSwitchTimes()) {
				addSwitchTime(siger, time);
			}

			// 保存记录
			array.add(dock);
		}

		Logger.debug(this, "readAccounts", "%s account size:%d", file, array.size());

		// 返回统计值
		return array;
	}

	/**
	 * 删除账号
	 * @param siger 签名
	 */
	public boolean removeAccount(Siger siger) {
		// 删除账号
		super.lockSingle();
		try {
			return mapAccounts.remove(siger) != null;
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 保存账号
	 * @param account
	 */
	public void addAccount(Account account) {
		super.lockSingle();
		try {
			// 保存账号
			account.refreshTime();
			mapAccounts.put(account.getUsername(), account);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 判断数据库存在
	 * @param fame 数据库名
	 * @return 返回真或者假
	 */
	public boolean hasSchema(Fame fame) {
		super.lockMulti();
		try {
			return mapFames.get(fame) !=null;
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 保存数据库名
	 * @param fame 数据库名
	 */
	public boolean addSchema(Siger siger, Fame fame) {
		super.lockSingle();
		try {
			return mapFames.put(fame, siger) == null;
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return false;
	}

	/**
	 * 删除数据库名
	 * @param fame 数据库名
	 */
	public boolean removeSchema(Fame fame) {
		// 删除数据库名
		super.lockSingle();
		try {
			return mapFames.remove(fame)!=null;
		} finally {
			super.unlockSingle();
		}
	}
	
	/**
	 * 删除一个账号下的所有数据库
	 * @param siger 用户签名
	 * @return 返回删除的统计数
	 */
	public int removeSchema(Siger siger) {
		ArrayList<Fame> a = new ArrayList<Fame>();

		super.lockSingle();
		try {
			Iterator<Map.Entry<Fame, Siger>> iterator = mapFames.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Fame, Siger> entry = iterator.next();
				Siger value = entry.getValue();
				if (Laxkit.compareTo(siger, value) == 0) {
					a.add(entry.getKey());
				}
			}
			// 删除内存中的记录
			for (Fame e : a) {
				mapFames.remove(e);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		return a.size();
	}

	/**
	 * 查找表签名
	 * @param space 数据表名
	 * @return 返回用户签名，没有是空指针
	 */
	public Siger findTable(Space space) {
		super.lockMulti();
		try {
			return mapSpaces.get(space) ;
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 判断数据表存在
	 * @param space 数据表名
	 * @return 返回真或者假
	 */
	public boolean hasTable(Space space) {
		return findTable(space) != null;
	}

	/**
	 * 保存数据表名
	 * @param siger 账号签名
	 * @param space 数据表名
	 */
	public boolean addTable(Siger siger, Space space) {
		super.lockSingle();
		try {
			return mapSpaces.put(space, siger) == null;
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return false;
	}

	/**
	 * 删除内存里的数据表名
	 * @param space 数据表名
	 */
	public boolean removeTable(Space space) {
		boolean success = false;
		// 删除数据表名
		super.lockSingle();
		try {
			success = (mapSpaces.remove(space) != null);
		} finally {
			super.unlockSingle();
		}
		return success;
	}
	
	/**
	 * 删除一个账号下的所有数据表
	 * @param siger 用户签名
	 * @return 返回删除的统计数
	 */
	public int removeTable(Siger siger) {
		ArrayList<Space> a = new ArrayList<Space>();

		super.lockSingle();
		try {
			Iterator<Map.Entry<Space, Siger>> iterator = mapSpaces.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Space, Siger> entry = iterator.next();
				Siger value = entry.getValue();
				if (Laxkit.compareTo(siger, value) == 0) {
					a.add(entry.getKey());
				}
			}
			// 删除内存中的记录
			for (Space e : a) {
				mapSpaces.remove(e);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		return a.size();
	}

	/**
	 * 保存优化触发器
	 * @param time 优化触发器
	 */
	public boolean addSwitchTime(Siger siger, SwitchTime time) {
		super.lockSingle();
		try {
			return mapTimes.put(time, siger) == null;
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return false;
	}

	/**
	 * 删除优化触发器
	 * @param time 优化触发器
	 */
	public boolean removeSwitchTime(SwitchTime time) {
		// 删除优化触发器
		super.lockSingle();
		try {
			return mapTimes.remove(time) != null;
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 删除一个账号下的所有触发器
	 * @param siger 用户签名
	 * @return 返回删除的统计数
	 */
	public int removeSwitchTime(Siger siger) {
		ArrayList<SwitchTime> a = new ArrayList<SwitchTime>();

		super.lockSingle();
		try {
			Iterator<Map.Entry<SwitchTime, Siger>> iterator = mapTimes.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<SwitchTime, Siger> entry = iterator.next();
				Siger value = entry.getValue();
				if (Laxkit.compareTo(siger, value) == 0) {
					a.add(entry.getKey());
				}
			}
			// 删除内存中的记录
			for (SwitchTime e : a) {
				mapTimes.remove(e);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		return a.size();
	}

	/**
	 * 根据签名，查找内存中的账号
	 * @param siger 用户签名
	 * @return 返回账号副本（注意，是副本！！！），或者空指针
	 */
	public Account findMemory(Siger siger) {
		super.lockMulti();
		try {
			Account account = mapAccounts.get(siger);
			if (account != null) {
				account.refreshTime();
				return account.duplicate();
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 数据格式：
	 * 1. 状态码
	 * 2. 总长度
	 * 3. 账号内容长度
	 * 4. 校验码
	 * 5. 内容
	 * 6. 填充
	 * @param content 内容长度
	 * @return 返回最大空间尺寸
	 */
	private int length(int content) {
		int max = (blockSize / accountSize);
		for (int radix = 1; radix <= max; radix++) {
			// 计算填充位
			int padding = ((accountSize * radix) - AccountTag.capacity() - content);
			// 填充位大于0时才有效
			if (padding > 0) {
				return AccountTag.capacity() + content + padding;
			}
		}
		return -1;
	}

	/**
	 * 格式化成指定长度
	 * @param content 账号内容
	 * @return 返回填充后的数据
	 */
	private byte[] format(byte[] content) {
		int length = length(content.length);
		if(length < 1) {
			return null;
		}

		// 校验和
		long checksum = EachTrustor.sign(content);

		// 账号标记
		AccountTag tag = new AccountTag(AccountTag.ENABLED, length,
				content.length, checksum);

		//		tag.setStatus(AccountTag.ENABLED);
		//		tag.setContentLength(content.length);
		//		tag.setContentSum(checksum);
		//		tag.setLength(length);

		// 生成前缀校验和
		tag.setTagSum(tag.lookup());

		byte[] b = tag.build();
		int seek = 0;
		// 空间长度
		byte[] out = new byte[length];
		Arrays.fill(out, (byte) 0);
		// 标记
		System.arraycopy(b, 0, out, seek, b.length);
		seek += b.length;
		// 账号数据
		System.arraycopy(content, 0, out, seek, content.length);

		// 输出
		return out;
	}

	/**
	 * 格式化账号到规定长度
	 * @param account 账号实例
	 * @return 返回字节数组
	 */
	private byte[] format(Account account) {
		MarkWriter writer = new MarkWriter();
		writer.writeObject(account);
		byte[] b = writer.toAll();
		// 格式化处理
		return format(b);
	}

	/**
	 * 根据编号生成一个固定格式的文件名
	 * @param no 编号
	 * @return 文件实例
	 */
	private File createFile(int no) {
		String name = String.format("%d%s", no, AccountManager.SUFFIX);
		return new File(root, name);
	}

	/**
	 * 删除一个位置的记录
	 * @param dock 坐标
	 * @return 成功返回真，否则假
	 */
	private boolean eraseDisk(DiskDock dock) throws IOException {
		File file = createFile(dock.getNo());
		boolean success = (file.exists() && file.isFile());
		if (success) {
			success = (file.length() >= dock.getOffset() + dock.getLength());
		}
		if (!success) {
			Logger.error(this, "eraseDisk", "argument error!");
			return false;
		}
		// 磁盘IO
		RandomAccessFile access = new RandomAccessFile(file, "rws");
		// 移到账号开始位置，读取头部数据
		byte[] b = new byte[AccountTag.capacity()];
		access.seek(dock.getOffset());
		int len = access.read(b);
		if (len != b.length) {
			access.close();
			return false;
		}
		// 解析数据
		AccountTag tag = new AccountTag(b);
		// 校验和检查
		if (!tag.checkLookup()) {
			Logger.error(this, "eraseDisk", "checksum error:%X - %X", tag.getTagSum(), tag.lookup());
			access.close();
			return false;
		}
		// 判断是失效
		if (tag.isDisabled()) {
			Logger.error(this, "eraseDisk", "this is DISABLED!");
			access.close();
			return false;
		}

		// 修改状态码和更新校验和
		tag.setStatus(AccountTag.DISABLE);
		tag.setTagSum(tag.lookup());

		// 加到账号开始位置，写入失效的标记头
		b = tag.build();
		access.seek(dock.getOffset());
		access.write(b);
		access.close();

		return true;
	}

	/**
	 * 从内存中读一段数据
	 * @param dock 磁盘坐标
	 * @return 返回读取的磁盘数据，参数错误返回空指针
	 * @throws IOException 读取过程中发生IO异常
	 */
	private byte[] readDisk(DiskDock dock) throws IOException {
		File file = createFile(dock.getNo());
		boolean success = (file.exists() && file.isFile());
		if (success) {
			success = (file.length() >= dock.getOffset() + dock.getLength());
		}
		if (success) {
			byte[] b = new byte[dock.getLength()];
			FileInputStream in = new FileInputStream(file);
			in.skip(dock.getOffset());
			in.read(b);
			in.close();
			return b;
		}
		return null;
	}

	/**
	 * 向磁盘末端写入数据
	 * @param b 数据长度
	 * @return 返回数据坐标
	 * @throws IOException
	 */
	private DiskDock appendDisk(byte[] b) throws IOException {
		if (current == null || current.isBlockout(blockSize)) {
			current = StaffOnAccountPool.getInstance().applyFile(this);
		}
		return current.append(b);
	}

	/**
	 * 覆盖旧的数据。两种可能：
	 * 1. 如果长度一致，覆盖旧数据。
	 * 2. 长度不一致，添加到新的文件末尾，旧记录删除。
	 * 
	 * @param dock 原磁盘坐标
	 * @param b 字节数组
	 * @return 返回新的坐标
	 * @throws IOException
	 */
	private DiskDock updateDisk(DiskDock dock, byte[] b) throws IOException {
		if (b.length == dock.getLength()) {
			File file = createFile(dock.getNo());
			boolean success = (file.exists() && file.isFile());
			if (success) {
				success = (file.length() >= dock.getOffset() + dock.getLength());
			}
			if (success) {
				RandomAccessFile out = new RandomAccessFile(file, "rws");
				out.seek(dock.getOffset());
				out.write(b);
				out.close();
				return dock;
			}
		} else {
			// 新记录写入最后一个文件末尾
			DiskDock next = appendDisk(b);
			// 把旧记录删除
			eraseDisk(dock);
			// 删除增1
			addDelete();
			// 返回新的下标记录
			return next;
		}
		// 以上不成功，返回空指针
		return null;
	}

	/**
	 * 在当前文件尾部，追加一个账号记录
	 * @param user 用户
	 * @return 返回文件位置
	 */
	public AccountDock createAccount(Account account) {
		// 格式化账号空间
		byte[] b = format(account);

		AccountDock dock = null;

		// 单向锁定，写入
		super.lockSingle();
		try {
			DiskDock dd = appendDisk(b);
			if (dd != null) {
				dock = new AccountDock(account.getUsername(), dd);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 保存到缓存
		if (dock != null) {
			addAccount(account);
		}

		return dock;
	}

	/**
	 * 删除账号
	 * @param dock 账号坐标
	 * @return 成功返回真，否则假
	 */
	public boolean dropAccount(AccountDock dock) {
		boolean success = false;
		// 锁定删除
		super.lockSingle();
		try {
			success = eraseDisk(dock.getDock());
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "dropAccount", success, "%s", dock);
		
		// 以上成功，删除内存里的记录
		if (success) {
			Siger siger = dock.getSiger();
			removeTable(siger);
			removeSchema(siger);
			removeSwitchTime(siger);
			removeAccount(siger);
		}

		return success;
	}

	/**
	 * 从指定位置读取一个账号
	 * @param dock 账号坐标
	 * @return 返回账号实例，或者空指针
	 */
	public Account readAccount(AccountDock dock) {
		Logger.debug(this, "readAccount", "no:%d, dock offset:%d", dock.getNo(), dock.getOffset());

		// 查找内存中的账号
		Account account = findMemory(dock.getSiger());
		if (account != null) {
			return account;
		}

		// 读取磁盘中的账号
		byte[] b = null;
		super.lockSingle();
		try {
			b = readDisk(dock.getDock());
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		if (b == null) {
			return null;
		}

		// 参数判断
		ClassReader reader = new ClassReader(b);
		AccountTag tag = new AccountTag(reader);
		// 校验和失败
		if (!tag.checkLookup()) {
			Logger.error(this, "readAccount", "checksum error:%X - %X", tag.lookup(), tag.getTagSum());
			return null;
		}

		byte[] content = reader.read(tag.getContentLength());
		// 检查校验和
		long sum = EachTrustor.sign(content);
		if (sum != tag.getContentSum()) {
			return null;
		}

		// 从标记化读取器中解析参数
		MarkReader markReader = new MarkReader(content);
		account = new Account(markReader);

		// 保存到缓存
		if(account != null) {
			addAccount(account);
		}

		return account;
	}

	/**
	 * 向指定位置更新一个文件
	 * @param origin 原始位置
	 * @param account 账号
	 * @return 返回新的位置记录，不成功返回空指针
	 */
	public AccountDock updateAccount(AccountDock origin, Account account) {
		// 把账号数据格式成字节数组
		byte[] b = format(account);

		AccountDock dest = null;

		// 更新账号记录
		super.lockSingle();
		try {
			DiskDock dd = updateDisk(origin.getDock(), b);
			if (dd != null) {
				dest = new AccountDock(origin.getSiger(), dd);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 成功，保存到内存
		if (dest != null) {
			addAccount(account);
		}

		// 返回新的位置记录
		return dest;
	}

	/**
	 * 定时检查超时不用的账号，将它们从内存中删除
	 */
	public void checkTimeoutAccount() {
		int size = mapAccounts.size();
		if (size == 0) {
			return;
		}

		ArrayList<Siger> array = new ArrayList<Siger>(size);
		super.lockSingle();
		try {
			Iterator<Map.Entry<Siger, Account>> iterator = mapAccounts.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Siger, Account> entry = iterator.next();
				Account account = entry.getValue();
				// 判断已经超时
				if (account.isTimeout(timeout)) {
					array.add(entry.getKey());
				}
			}
			// 从内存中删除过期账号
			for (Siger dock : array) {
				mapAccounts.remove(dock);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 检查时间触发器达到指定时间
	 * @return 返回达到要求的命令集合
	 */
	public List<PressRegulate> checkSwitchTime() {
		// 命令集合
		ArrayList<PressRegulate> array = new ArrayList<PressRegulate>();
		// 返回空集合
		if (mapTimes.isEmpty()) {
			return array;
		}

		// 锁定处理
		super.lockSingle();
		try {
			Iterator<Map.Entry<SwitchTime, Siger>> iterator = mapTimes.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<SwitchTime, Siger> entry = iterator.next();
				SwitchTime time = entry.getKey();
				// 判断达到触发时间
				if (time.isTouched()) {
					// 生成命令，保存它
					PressRegulate cmd = new PressRegulate(time.getDock());
					cmd.setIssuer(entry.getValue());
					// 计算下一次触发时间
					time.nextTouchTime();
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "checkSwitchTime", "touch command size:%d", array.size());

		return array;
	}
}