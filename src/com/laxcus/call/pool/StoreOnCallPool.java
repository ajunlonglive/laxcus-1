/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.pool;

import java.io.*;
import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.command.cloud.store.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.cyber.*;
import com.laxcus.util.datetime.*;

/**
 * 云存储管理池
 * 
 * @author scott.liang
 * @version 1.0 10/25/2021
 * @since laxcus 1.0
 */
public class StoreOnCallPool extends VirtualPool {

	private static StoreOnCallPool selfHandle = new StoreOnCallPool();

	/** 根目录集合 **/
	private ArrayList<File> roots = new ArrayList<File>();

	/** 签名 -> 用户磁盘 **/
	private TreeMap<Siger, SDisk> disks = new TreeMap<Siger, SDisk>();

	/**
	 * 构造默认的云存储管理池
	 */
	private StoreOnCallPool() {
		super();
	}

	/**
	 * 返回实例
	 * @return
	 */
	public static StoreOnCallPool getInstance() {
		return StoreOnCallPool.selfHandle;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info(this, "process", "into...");

		while (!isInterrupted()) {
			delay(5000);
		}

		Logger.info(this, "process", "exit...");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// TODO Auto-generated method stub

	}

	//	/**
	//	 * 扫描磁盘
	//	 * @param root
	//	 */
	//	public void scanDisk(File root) {
	//		File[] files = root.listFiles();
	//		int size = (files != null ? files.length : 0);
	//		for (int i = 0; i < size; i++) {
	//			File file = files[i];
	//			if (!file.isDirectory()) {
	//				continue;
	//			}
	//			String name = file.getName();
	//			// 判断是用户签名
	//			boolean success = Siger.validate(name);
	//			if (!success) {
	//				continue;
	//			}
	//
	//			// 磁盘
	//			Siger sign = new Siger(name);
	//			SDisk disk = new SDisk();
	//			disk.setRoot(file);
	//
	//			// 把这个目录下的文件/目录，全部保存到磁盘对象里（未实现！）
	//
	//			// 锁定
	//			super.lockSingle();
	//			try {
	//				disks.put(sign, disk);
	//			} catch (Throwable e) {
	//				Logger.fatal(e);
	//			} finally {
	//				super.unlockSingle();
	//			}
	//		}
	//	}

//	/**
//	 * 扫描指定目标下的磁盘
//	 * @param disk
//	 */
//	private void scanDisk(SDisk disk) {
//		File dir = disk.getRoot();
//	}
//
//	/**
//	 * 扫描磁盘记录
//	 */
//	public void scanDisks() {
//		// 锁定
//		super.lockSingle();
//		try {
//			Iterator<Map.Entry<Siger, SDisk>> iterator = disks.entrySet().iterator();
//			while (iterator.hasNext()) {
//				Map.Entry<Siger, SDisk> entry = iterator.next();
//				SDisk disk = entry.getValue();
//				// 加载资源
//				scanDisk(disk);
//			}
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			super.unlockSingle();
//		}
//	}
	
	/**
	 * 计算磁盘空间容量
	 * @return 当前磁盘目录的文件容量
	 */
	private long[] evaludate(File root) {
		boolean success = (root != null && root.isDirectory());
		if (!success) {
			return null;
		}
		long capacity = 0L;
		long dirs = 0;
		long files = 0;

		// 统计
		File[] elements = root.listFiles();
		int size = (elements != null ? elements.length : 0);
		for (int i = 0; i < size; i++) {
			File file = elements[i];
			// 是目录，或者文件
			if (file.isDirectory()) {
				dirs++;
				long[] vs = evaludate(file);
				if (vs != null) {
					capacity += vs[0];
					dirs += vs[1];
					files += vs[2];
				}
			} else if (file.isFile()) {
				capacity += file.length();
				files++;
			}
		}
		// 返回磁盘文件容量
		return new long[] { capacity, dirs, files};
	}

	/**
	 * 生成云空间
	 * @param siger
	 * @return
	 */
	private CloudField createCloudField(Siger siger) {
		Refer refer = StaffOnCallPool.getInstance().findRefer(siger);
		if (refer == null) {
			return null;
		}

		long maxCapacity = refer.getUser().getCloudSize();

		// 找到根目录
		SDisk disk = findDisk(siger);
		if (disk == null) {
			return null;
		}
		// 计算当前使用空间
		long[] vs = evaludate(disk.getRoot());
		if (vs == null) {
			return null;
		}
		// long usedCapacity = evaludate(disk.getRoot());

		// 返回结果
		CloudField field = new CloudField(siger);
		field.setMaxCapacity(maxCapacity);
		field.setUsedCapacity(vs[0]);
		field.setDirectires((int) vs[1]);
		field.setFiles((int) vs[2]);
		return field;
	}
	
	/**
	 * 生成全部云空间
	 * @return CloudField列表
	 */
	public List<CloudField> createCloudFields() {
		ArrayList<CloudField> array = new ArrayList<CloudField>();
		
		List<Siger> users = getUsers();
		
		// 取得实例
		for (Siger siger : users) {
			CloudField field = createCloudField(siger);
			if (field != null) {
				array.add(field);
			}
		}
		
		return array;
	}
	
	/**
	 * 增加一个根目录
	 * @param path
	 * @return
	 */
	public boolean addRoot(String path) {
		// 检查目录
		File rt = new File(path);
		if (roots.contains(rt)) {
			return false;
		}
		// 判断存在
		boolean success = (rt.exists() && rt.isDirectory());
		// 如果目录不存在时，建立它
		if (!success) {
			success = rt.mkdirs();
		}
		// 如果成功，保存为根目录（规范格式！）
		if (success) {
			roots.add(rt);
		}

		Logger.debug(this, "addRoot", success, "%s", path);

		// 返回结果
		return success;
	}

	/**
	 * 返回当前的签名
	 * @return
	 */
	public List<Siger> getUsers() {
		super.lockMulti();
		try {
			return new ArrayList<Siger>(disks.keySet());
		} finally {
			super.unlockMulti();
		}
	}
	
	/**
	 * 转义日期格式
	 * @param date
	 * @return
	 */
	private long translate(long date) {
		Date dt = new Date(date);
		//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
		//		Logger.debug(this, "translate", "date is %s", sdf.format(dt));
		return SimpleTimestamp.format(dt);
	}
	
	/**
	 * 扫描某个账号磁盘下的目录和文件
	 * @param root 目录
	 * @param parent 父级虚拟目录
	 */
	private void scanDirectory(File system, File root, VPath parent) {
		boolean success = (root.exists() && root.isDirectory());
		if (!success) {
			return;
		}
		String s1 = Laxkit.canonical(system);
		
		// 扫描子目录
		File[] files = root.listFiles();
		int size = (files != null ? files.length : 0);
		for (int i = 0; i < size; i++) {
			File member = files[i];
			// 名称和日期
			String s2 = Laxkit.canonical(member);
			if (!s2.startsWith(s1)) {
				continue;
			}
			
			// 去掉根路径后的后缀
			String suffix = s2.substring(s1.length());
			suffix = translate(suffix); // 转义分隔符
			// 最后修改...
			long lastModified = translate(member.lastModified());

			// 目录或者文件
			if (member.isDirectory()) {
				VPath dir = new VPath(parent.getLevel() + 1, VPath.DIRECTORY, suffix);
				dir.setLastModified(lastModified);
				parent.add(dir);
				// 扫描子级
				scanDirectory(system, member, dir);
			} else if (member.isFile()) {
				VPath file = new VPath(parent.getLevel() + 1, VPath.FILE, suffix);
				file.setLength(member.length());
				file.setLastModified(lastModified);
				parent.add(file);
			}
		}
	}
	

//	private int doLevel(File file, File root) {
//		int level = 0;
//		do {
//			// 一致时，退出
//			if (file.compareTo(root) == 0) {
//				break;
//			}
//			level++;
//			// 取上级目录
//			File parent = file.getParentFile();
//			if (parent == null || root.compareTo(parent) == 0) {
//				break;
//			}
//			file = parent;
//		} while (true);
//		// 返回级别
//		return level;
//	}
	
	private int doLevel(File file, File root) {
		int level = 0;
		do {
			// 一致时，退出
			if (file.compareTo(root) == 0) {
				break;
			}
			level++;
			// 取上级目录
			file = file.getParentFile();
			if (file == null || root.compareTo(file) == 0) {
				break;
			}
		} while (true);
		// 返回级别
		return level;
	}
	
	/**
	 * 根据签名和实路径，获得一个虚拟路径及下属的子路径
	 * @param issuer 用户签名
	 * @param file 文件实例
	 * @return 返回VPath，失败是空指针
	 */
	public VPath doVPath(Siger issuer, File file) {
		SDisk disk = findDisk(issuer);
		if (disk == null) {
			return null;
		}
		if (!file.exists()) {
			return null;
		}
		
		File root = disk.getRoot();
		int level = doLevel(file, root);
		
		// 虚拟路径，去掉实路径部分
		String s1 = Laxkit.canonical(root);
		String s2 = Laxkit.canonical(file);
		if (!s2.startsWith(s1)) {
			return null;
		}
		String suffix = s2.substring(s1.length());
		suffix = translate(suffix); // 转义分隔符
		
		// 生成结果
		byte type = (file.isDirectory() ? VPath.DIRECTORY : VPath.FILE);
		long lastModified = translate(file.lastModified());
		VPath path = new VPath(level, type, suffix);
		path.setLastModified(lastModified);
		if (file.isFile()) {
			path.setLength(file.length());
		}
		
		// 扫描目录
		scanDirectory(root, file, path);
		// 返回结果
		return path;
	}

	/**
	 * 扫描某个用户的目录
	 * @param issuer 用户签名
	 * @return 返回VPath对象，或者空指针
	 */
	public VPath scanDisk(Siger issuer, String suffix) {
		Logger.debug(this, "scanDisk", "scan %s %s", issuer, suffix);

		SDisk disk = findDisk(issuer);
		if (disk == null) {
			return null;
		}
		
		File root = disk.getRoot();

		VPath path = null;
		// 如果是从根目录扫描
		if (suffix == null || suffix.length() == 0 || suffix.equals("/")) {
			path = new VPath(0, VPath.DISK, disk.getPath()); // 层次从0开始
			path.setLastModified(translate(root.lastModified()));
			// 扫描目录
			scanDirectory(root, root, path);
		} else {
			suffix = translate(suffix); // 转义分隔符
			File file = new File(root, suffix);
//			boolean b = (dir.exists() && dir.isDirectory());
			
			// 判断存在
			if (!file.exists()) {
				Logger.error(this, "scanDisk", "not found %s", Laxkit.canonical(file));
				return null;
			}
			
			// 扫描目录或者文件
			if (file.isDirectory()) {
				int level = doLevel(file, root);
				path = new VPath(level, VPath.DIRECTORY, suffix);
				path.setLastModified(translate(file.lastModified()));
				// 扫描目录
				scanDirectory(root, file, path);
			} else if (file.isFile()) {
				int level = doLevel(file, root);
				path = new VPath(level, VPath.DIRECTORY, suffix);
				path.setLastModified(translate(file.lastModified()));
				path.setLength(file.length());
			}
		}
		
		return path;
	}

	/**
	 * 找到磁盘
	 * @param siger 用户签名
	 * @return 返回SDisk实例，或者空指针
	 */
	public SDisk findDisk(Siger siger) {
		// 锁定
		super.lockSingle();
		try {
			return disks.get(siger);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return null;
	}

	/**
	 * 判断有某个用户的磁盘
	 * @param siger 用户签名
	 * @return 返回真或者假
	 */
	public boolean hasDisk(Siger siger) {
		return findDisk(siger) != null;
	}

	/**
	 * 寻找有足够空间的磁盘目录
	 * @param siger 用户签名
	 * @param capacity 磁盘空间
	 * @return 返回真或者假
	 */
	private File nextDisk(Siger siger, long capacity) {
		if (roots.isEmpty()) {
			return null;
		}
		int size = roots.size();
		for (int i = 0; i < size; i++) {
			File file = roots.get(i);
			// 判断磁盘空间足够时，返回
			if (file != null) {
				return file;
			}
		}
		return null;
	}

	/**
	 * 建立用户和它的目录
	 * @param siger
	 * @return
	 */
	public boolean createDisk(Siger siger) {
		// 判断用户已经存在
		if (hasDisk(siger)) {
			return true;
		}

		// 找到对应和用户账号
		User user = StaffOnCallPool.getInstance().findUser(siger);
		if (user == null) {
			return false;
		}
		// 云存储空间
		long capacity = user.getCloudSize();
		// 如果云存储空间没有定义，返回假，忽略它
		if (capacity < 1) {
			return false;
		}

		// 找到一个有足够宰的磁盘
		File rt = nextDisk(siger, capacity);
		if (rt == null) {
			return false;
		}

		// 生成本地磁盘目录
		File real = new File(rt, siger.toString());
		boolean success = (real.exists() && real.isDirectory());
		if (!success) {
			success = real.mkdirs();
		}
		if (success) {
			// 锁定，保存
			super.lockSingle();
			try {
				SDisk disk = new SDisk(real);
				disks.put(siger, disk);
			} catch (Throwable e) {
				Logger.fatal(e);
			} finally {
				super.unlockSingle();
			}
		}

		return success;
	}

	/**
	 * 删除磁盘目录
	 * @param dir
	 * @return
	 */
	private boolean deleteDirectory(File dir) {
		boolean success = (dir.exists() && dir.isDirectory());
		if (!success) {
			return false;
		}
		File[] files = dir.listFiles();
		int size = (files != null ? files.length : 0);
		for (int i = 0; i < size; i++) {
			File file = files[i];
			if (file.isFile()) {
				success = file.delete();
				// 不成功，返回假
				if (!success) {
					return false;
				}
			} else if (file.isDirectory()) {
				success = deleteDirectory(file);
				if (!success) {
					return false;
				}
			}
		}

		// 删除根目录
		return dir.delete();
	}

	/**
	 * 删除一个用户账号的云存储目录
	 * @param siger 用户签名
	 * @return 成功返回真，否则假
	 */
	public boolean dropDisk(Siger siger) {
		SDisk disk = null;
		// 锁定
		super.lockSingle();
		try {
			disk = disks.remove(siger);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		// 不存在，忽略，返回假
		if (disk == null) {
			return false;
		}

		// 删除磁盘
		return deleteDirectory(disk.getRoot());
	}
	
	/**
	 * 转义字符串
	 * @param path
	 * @return
	 */
	private String translate(String path) {
		return path.replace(File.separatorChar, '/');
	}

	/**
	 * 生成一个本地文件实例
	 * @param root 根目录
	 * @param siger 签名
	 * @return 返回File实例
	 */
	private File getLocal(File root, String path) {
		String suffix = translate(path);
		File file = new File(root, suffix);
		File dir = file.getParentFile();
		// 生成目录
		boolean success = (dir.exists() && dir.isDirectory());
		// 如果目录不存在，生成它
		if (!success) {
			success = dir.mkdirs();
		}
		// 返回磁盘文件对象，无效是空指针
		if (success) {
			return file;
		}
		return null;
	}

//	/**
//	 * 生成目录
//	 * @param siger 签名
//	 * @param path 磁盘目录
//	 * @return 返回生成的实例，或者空指针
//	 */
//	public SDirectory createDirectory(Siger siger, String path) {
//		SDisk disk = findDisk(siger);
//		if (disk == null) {
//			return null;
//		}
//
//		File file = getLocal(disk.getRoot(), path);
//		boolean success = (file.exists() && file.isDirectory());
//		if (!success) {
//			success = file.mkdirs();
//		}
//		if (!success) {
//			return null;
//		}
//
//		return new SDirectory(file, path);
//
//		//		// 返回目录
//		//		SDirectory dir = new SDirectory(file, path);
//		//		// 锁定
//		//		super.lockSingle();
//		//		try {
//		//			disk.add(dir);
//		//		} catch (Throwable e) {
//		//			Logger.fatal(e);
//		//		} finally {
//		//			super.unlockSingle();
//		//		}
//		//		return dir;
//	}
	
	/**
	 * 生成目录
	 * @param siger 签名
	 * @param path 磁盘目录
	 * @return 返回生成的实例，或者空指针
	 */
	public VPath createDirectory(Siger siger, String path) {
		SDisk disk = findDisk(siger);
		if (disk == null) {
			return null;
		}
		
		File root = disk.getRoot();

		File file = getLocal(root, path);
		boolean success = (file.exists() && file.isDirectory());
		if (!success) {
			success = file.mkdirs();
		}
		if (!success) {
			return null;
		}
		
		int level = doLevel(file, root);
		String s1 = Laxkit.canonical(root);
		String s2 = Laxkit.canonical(file);
		String suffix = s2.substring(s1.length());
		suffix = translate(suffix); // 转义分隔符
		long lastModified = translate(file.lastModified());

		VPath dir = new VPath(level, VPath.DIRECTORY, suffix);
		dir.setLastModified(lastModified);
		return dir;
	}
	
	/**
	 * 生成一个文件
	 * @param root 根目录
	 * @param path 子路径
	 * @return
	 */
	private File makeFile(File root, String path) {
		String suffix = translate(path);
		return new File(root, suffix);
	}

//	/**
//	 * 删除指定的目录
//	 * @param siger
//	 * @param path
//	 * @return
//	 */
//	public SDirectory dropDirectory(Siger siger, String path) {
//		SDisk disk = findDisk(siger);
//		if (disk == null) {
//			return null;
//		}
//
//		File dir = makeFile(disk.getRoot(), path);
//		// 判断目录存在
//		boolean success = (dir.exists() && dir.isDirectory());
//		// 删除目录
//		if (success) {
//			success = deleteDirectory(dir);
//		}
//		return (success ? new SDirectory(path) : null);
//	}

	
//	/**
//	 * 删除磁盘文件
//	 * @param issuer
//	 * @param path
//	 * @return
//	 */
//	public SFile dropFile(Siger issuer, String path) {		
//		SDisk disk = findDisk(issuer);
//		if (disk == null) {
//			Logger.error(this, "dropFile", "cannot be find root %s", issuer);
//			return null;
//		}
//
//		VPath dest = null;
//		
//		File file = makeFile(disk.getRoot(), path);
//		// 判断存在
//		boolean success = (file.exists() && file.isFile());
//		// 删除文件
//		if (success) {
//			dest = doVPath(issuer, file);
//			success = file.delete();
//		} 
//		
//		Logger.debug(this, "dropFile", success, "delete %s", file);
//		
//		return (success ? new SFile(path) : null);
//	}
	
	/**
	 * 删除指定的目录
	 * @param issuer 用户签名
	 * @param path 目录
	 * @return 返回VPath
	 */
	public VPath dropDirectory(Siger issuer, String path) {
		SDisk disk = findDisk(issuer);
		if (disk == null) {
			return null;
		}

		VPath dest = null;
		File dir = makeFile(disk.getRoot(), path);
		// 判断目录存在
		boolean success = (dir.exists() && dir.isDirectory());
		// 删除目录
		if (success) {
			dest = doVPath(issuer, dir);
			if (dest == null) {
				return null;
			}
			// 删除目录
			success = deleteDirectory(dir);
		}
		Logger.debug(this, "dropDirectory", success, "delete %s", dir);
		
		return (success ? dest : null);
	}

	/**
	 * 删除磁盘文件
	 * @param issuer
	 * @param path
	 * @return
	 */
	public VPath dropFile(Siger issuer, String path) {		
		SDisk disk = findDisk(issuer);
		if (disk == null) {
			Logger.error(this, "dropFile", "cannot be find root %s", issuer);
			return null;
		}

		VPath dest = null;

		File file = makeFile(disk.getRoot(), path);
		// 判断存在
		boolean success = (file.exists() && file.isFile());
		// 删除文件
		if (success) {
			// 生成结果
			dest = doVPath(issuer, file);
			if (dest == null) {
				return null;
			}
			// 删除
			success = file.delete();
		}
		
		Logger.debug(this, "dropFile", success, "delete %s", file);
		
		return (success ? dest : null);
	}

	/**
	 * 获得磁盘容量
	 * @param root
	 * @return
	 */
	public long getDiskCapacity(File root) {
		// 判断有
		boolean b = (root.exists() && root.isDirectory());
		if (!b) {
			return 0;
		}
		long count = 0;
		File[] files = root.listFiles();
		int size = (files == null ? 0 : files.length);
		for (int i = 0; i < size; i++) {
			File file = files[i];
			if (file.isDirectory()) {
				count += getDiskCapacity(file);
			} else if (file.isFile()) {
				count += file.length();
			}
		}
		return count;
	}

	/**
	 * 返回剩余空间
	 * @param siger 签名
	 * @return 返回剩余空间
	 */
	public long getFreeCapacity(Siger siger) {
		SDisk disk = findDisk(siger);
		if (disk == null || disk.getRoot() == null) {
			return 0;
		}

		// 找到资源引用实例
		Refer refer = StaffOnCallPool.getInstance().findRefer(siger);
		if (refer == null) {
			return 0;
		}

		long capacity = refer.getUser().getCloudSize();
		long count = getDiskCapacity(disk.getRoot());
		// 扫描这个磁盘
		return capacity - count;
	}
	
	/**
	 * 返回实例的用户文件
	 * @param issuer
	 * @param path
	 * @return
	 */
	public File getLocalFile(Siger issuer, String path) {
		SDisk disk = findDisk(issuer);
		if (disk == null) {
			return null;
		}

		File file = getLocal(disk.getRoot(), path);
		boolean success = (file.exists() && file.isFile());
		return (success ? file : null);
	}

	/**
	 * 判断有文件
	 * @param issuer
	 * @param path
	 * @return
	 */
	public boolean hasFile(Siger issuer, String path) {
		SDisk disk = findDisk(issuer);
		if (disk == null) {
			return false;
		}

		File file = getLocal(disk.getRoot(), path);
		return (file.exists() && file.isFile());
	}

//	/**
//	 * 文件写入磁盘
//	 * @param content
//	 * @param issuer
//	 * @param path
//	 * @return
//	 */
//	public boolean writeFile(byte[] content, Siger issuer, String path) {
//		SDisk disk = findDisk(issuer);
//		if (disk == null) {
//			Logger.error(this, "writeFile", "cannot be find '%s'", issuer);
//			return false;
//		}
//		// 本地文件
//		File dest = getLocal(disk.getRoot(), path);
//		if (dest == null) {
//			return false;
//		}
//		Logger.debug(this, "writeFile", "write to %s", dest.toString());
//
//		// 写入磁盘
//		try {
//			FileOutputStream out = new FileOutputStream(dest);
//			out.write(content);
//			out.flush();
//			out.close();
//			return true;
//		} catch (IOException e) {
//			Logger.error(e);
//		}
//		return false;
//	}
//
//	/**
//	 * 文件写入磁盘
//	 * @param source 源文件
//	 * @param issuer
//	 * @param path
//	 * @return
//	 */
//	public boolean writeFile(File source, Siger issuer, String path) {
//		SDisk disk = findDisk(issuer);
//		if (disk == null) {
//			Logger.error(this, "writeFile", "cannot be find '%s'", issuer);
//			return false;
//		}
//		// 本地文件
//		File dest = getLocal(disk.getRoot(), path);
//		if (dest == null) {
//			return false;
//		}
//		Logger.debug(this, "writeFile", "%s move to %s", source.toString(),
//				dest.toString());
//
//		// 写入磁盘
//		byte[] b = new byte[10240];
//		try {
//			long count = 0;
//			long length = source.length();
//			FileInputStream in = new FileInputStream(source);
//			FileOutputStream out = new FileOutputStream(dest);
//			do {
//				// 读取文件
//				int len = in.read(b);
//				if (len < 0) {
//					break;
//				}
//				// 写入磁盘
//				out.write(b, 0, len);
//				count += len;
//				if (count >= length) {
//					break;
//				}
//			} while (true);
//			// 输出
//			out.flush();
//			// 关闭
//			in.close();
//			out.close();
//			return (count >= length);
//		} catch (IOException e) {
//			Logger.error(e);
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		}
//		return false;
//	}
	
	/**
	 * 文件写入磁盘
	 * @param content
	 * @param issuer
	 * @param path
	 * @return
	 */
	public VPath writeFile(byte[] content, Siger issuer, String path) {
		SDisk disk = findDisk(issuer);
		if (disk == null) {
			Logger.error(this, "writeFile", "cannot be find '%s'", issuer);
			return null;
		}
		// 本地文件
		File dest = getLocal(disk.getRoot(), path);
		if (dest == null) {
			return null;
		}
		Logger.debug(this, "writeFile", "write to %s", dest.toString());

		// 写入磁盘
		try {
			FileOutputStream out = new FileOutputStream(dest);
			out.write(content);
			out.flush();
			out.close();
			return doVPath(issuer, dest);
		} catch (IOException e) {
			Logger.error(e);
		}
		return null;
	}

	/**
	 * 文件写入磁盘
	 * @param source 源文件
	 * @param issuer
	 * @param path
	 * @return
	 */
	public VPath writeFile(File source, Siger issuer, String path) {
		SDisk disk = findDisk(issuer);
		if (disk == null) {
			Logger.error(this, "writeFile", "cannot be find '%s'", issuer);
			return null;
		}
		// 本地文件
		File dest = getLocal(disk.getRoot(), path);
		if (dest == null) {
			return null;
		}
		Logger.debug(this, "writeFile", "%s move to %s", source.toString(),
				dest.toString());

		// 写入磁盘
		byte[] b = new byte[10240];
		try {
			long count = 0;
			long length = source.length();
			FileInputStream in = new FileInputStream(source);
			FileOutputStream out = new FileOutputStream(dest);
			do {
				// 读取文件
				int len = in.read(b);
				if (len < 0) {
					break;
				}
				// 写入磁盘
				out.write(b, 0, len);
				count += len;
				if (count >= length) {
					break;
				}
			} while (true);
			// 输出
			out.flush();
			// 关闭
			in.close();
			out.close();
			if (count >= length) {
				return doVPath(issuer, dest);
			}
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}
		return null;
	}
	
	/**
	 * 修改文件名
	 * @param issuer
	 * @param path
	 * @param name
	 * @return
	 */
	public SFile renameFile(Siger issuer, String path, String name) {
		SDisk disk = findDisk(issuer);
		if (disk == null) {
			return null;
		}

		File real = makeFile(disk.getRoot(), path);
		// 判断目录存在
		boolean success = (real.exists() && real.isFile());
		Logger.debug(this, "renameFile", success, "check %s", Laxkit.canonical(real));
		
		// 删除目录
		if (success) {
			File other = new File(real.getParentFile(), name);
			success = real.renameTo(other);
			if (!success) {
				Logger.error(this, "renameFile", "%s -> %s, failed!", Laxkit.canonical(real), Laxkit.canonical(other));
				return null;
			}
			// 取虚路径
			String s1 = Laxkit.canonical(disk.getRoot());
			String s2 = Laxkit.canonical(other);
			if (s2.startsWith(s1)) {
				String suffix = s2.substring(s1.length());
				suffix = translate(suffix); // 转义分隔符
				SFile sf = new SFile(suffix);
				sf.setLastModified(new Date(other.lastModified()));
				sf.setLength(other.length());
				return sf;
			}
		}
		return null; 
	}
	
	/**
	 * 修改目录名
	 * @param issuer
	 * @param path
	 * @param name
	 * @return
	 */
	public SDirectory renameDirectory(Siger issuer, String path, String name) {
		SDisk disk = findDisk(issuer);
		if (disk == null) {
			return null;
		}

		File dir = makeFile(disk.getRoot(), path);
		// 判断目录存在
		boolean success = (dir.exists() && dir.isDirectory());
		Logger.debug(this, "renameDirectory", success, "check %s", Laxkit.canonical(dir));
		
		// 删除目录
		if (success) {
			File other = new File(dir.getParentFile(), name);
			success = dir.renameTo(other);
			if (!success) {
				Logger.error(this, "renameDirectory", "%s -> %s, failed!", Laxkit.canonical(dir), Laxkit.canonical(other));
				return null;
			}
			// 取虚路径
			String s1 = Laxkit.canonical(disk.getRoot());
			String s2 = Laxkit.canonical(other);
			if (s2.startsWith(s1)) {
				String suffix = s2.substring(s1.length());
				suffix = translate(suffix); // 转义分隔符
				SDirectory sd = new SDirectory(suffix);
				sd.setLastModified(new Date(other.lastModified()));
				return sd;
			}
		}
		return null; 
	}
}