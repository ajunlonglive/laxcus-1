/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.pool;

import java.io.*;
import java.util.*;

import com.laxcus.command.cloud.*;
import com.laxcus.command.cloud.task.*;
import com.laxcus.log.client.*;
import com.laxcus.task.archive.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;

/**
 * 软件资源池
 * 
 * @author scott.liang
 * @version 1.0 8/21/2020
 * @since laxcus 1.0
 */
public class WareOnAccountPool extends ComponentPool {

	private static WareOnAccountPool selfHandle = new WareOnAccountPool();

	/** 准备删除的元组 **/
	private ArrayList<DropPackageTuple> deletes = new ArrayList<DropPackageTuple>();
	
	/** 准备读取的元组 **/
	private ArrayList<ReadPackageTuple> readers = new ArrayList<ReadPackageTuple>();

	/** 准备写入的元组 **/
	private ArrayList<WritePackageTuple> writers = new ArrayList<WritePackageTuple>();

	/**
	 * 构造软件资源池
	 */
	private WareOnAccountPool() {
		super();
		setSleepTime(30);
	}

	/**
	 * 返回软件资源池
	 * @return
	 */
	public static WareOnAccountPool getInstance() {
		return WareOnAccountPool.selfHandle;
	}
	
	/**
	 * 产生但是不建立一个磁盘子目录
	 * @param issuer 用户签名
	 * @return 返回File实例
	 */
	private File buildSubRoot(Siger issuer) {
		// 名称，保证唯一！
		String name = "system";
		if (issuer != null) {
			// 大写的用户签名
			name = issuer.toString().toUpperCase();
		}

		return new File(getRoot(), name);
	}

	/**
	 * 根据用户SHA256签名，建立专属他的子目录，如果是系统组件，目录是“system”。
	 * @param issuer 用户签名，或者空指针
	 * @return 成功返回真的目录名，否则是空指针
	 */
	private File buildFile(Siger issuer, Naming ware, String suffix) {
		File dir = buildSubRoot(issuer);
		// 判断目录存在且是“目录”属性
		boolean success = (dir.exists() && dir.isDirectory());
		// 不存在，建立一个新的目录
		if (!success) {
			success = dir.mkdirs();
		}

		// 成功，生成文件
		if (success) {
			String software = ware.toString().toLowerCase();
			String name = String.format("%s%s", software, suffix);
			return new File(dir, name);
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		// 判断目录有效
		boolean success = (getRoot() != null);

		Logger.debug(this, "init", success, "init ware directory");

		// 返回结果
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info(this, "process", "into ...");

		// 循环检查
		while (!isInterrupted()) {
			int count = 0;
			// 判断有写入元组
			if (hasWriter()) {
				WritePackageTuple tuple = popupWriteTuple();
				write(tuple);
				count++;
			}
			// 判断有读元组
			if (hasRead()) {
				ReadPackageTuple tuple = popupReadTuple();
				read(tuple);
				count++;
			}
			if(hasDrop()) {
				DropPackageTuple tuple = popupDropTuple();
				delete(tuple);
				count++;
			}
			// 空值，进入延时
			if (count == 0) {
				sleep();
			}
		}

		Logger.info(this, "process", "exit");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		readers.clear();
		writers.clear();
	}
	
	/**
	 * 弹出一个元组
	 * @return DropPackageTuple
	 */
	private DropPackageTuple popupDropTuple() {
		// 锁定
		super.lockSingle();
		try {
			if (deletes.size() > 0) {
				return deletes.remove(0);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return null;
	}
	
	/**
	 * 判断有删除元组
	 * @return 返回真或者假
	 */
	private boolean hasDrop() {
		super.lockMulti();
		try {
			return deletes.size() > 0;
		} finally {
			super.unlockMulti();
		}
	}
	
	/**
	 * 保存删除元组
	 * @param tuple
	 * @return 成功返回真，否则假
	 */
	private boolean add(DropPackageTuple tuple) {
		boolean success = false;
		// 锁定保存!
		super.lockSingle();
		try {
			deletes.add(tuple);
			success = true;
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}


	/**
	 * 弹出一个元组
	 * @return ReadPackageTuple
	 */
	private ReadPackageTuple popupReadTuple() {
		// 锁定
		super.lockSingle();
		try {
			if (readers.size() > 0) {
				return readers.remove(0);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return null;
	}
	
	/**
	 * 弹出一个元组
	 * @return WritePackageTuple
	 */
	private WritePackageTuple popupWriteTuple() {
		// 锁定
		super.lockSingle();
		try {
			if (writers.size() > 0) {
				return writers.remove(0);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return null;
	}
	
	/**
	 * 判断有写入元组
	 * @return 返回真或者假
	 */
	private boolean hasWriter() {
		super.lockMulti();
		try {
			return writers.size() > 0;
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 判断有读取元组
	 * @return 返回真或者假
	 */
	private boolean hasRead() {
		super.lockMulti();
		try {
			return readers.size() > 0;
		} finally {
			super.unlockMulti();
		}
	}
	
	/**
	 * 保存写入元组
	 * @param tuple
	 * @return 成功返回真，否则假
	 */
	private boolean add(WritePackageTuple tuple) {
		boolean success = false;
		// 锁定保存!
		super.lockSingle();
		try {
			writers.add(tuple);
			success = true;
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 保存写入元组
	 * @param tuple
	 * @return 成功返回真，否则假
	 */
	private boolean add(ReadPackageTuple tuple) {
		boolean success = false;
		// 锁定保存!
		super.lockSingle();
		try {
			readers.add(tuple);
			success = true;
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 分布式应用软件包写入磁盘
	 * @param issuer 发布者
	 * @param component 组件
	 * @return 成功返回真，否则假
	 */
	public boolean write(Siger issuer, CloudPackageComponent component) {
		// 生成实例
		WritePackageTuple tuple = new WritePackageTuple(issuer, component);
		boolean success = add(tuple);
		// 保存成功，唤醒线程，等待写入完成
		if (success) {
			wakeup();
			tuple.await();
		}

		Logger.debug(this, "write", tuple.isSuccessful(), "write %s#%s",
				issuer, component.getName());

		return tuple.isSuccessful();
	}
	
	/**
	 * 读取元组
	 * @param part 工作部件
	 * @return 成功ReadPackageTuple实例，否则空指针
	 */
	public ReadPackageTuple read(TaskPart part) {
		// 生成实例
		ReadPackageTuple tuple = new ReadPackageTuple(part);
		boolean success = add(tuple);
		// 保存成功，唤醒线程，等待写入完成
		if (success) {
			wakeup();
			tuple.await();
		}

		Logger.debug(this, "read", tuple.isSuccessful(), "read %s", part);
		return (tuple.isSuccessful() ? tuple : null);
	}
	
	
	/**
	 * 删除磁盘上的软件包
	 * @param part 工作部件
	 * @param ware 软件名
	 * @return 成功返回真，否则假
	 */
	public boolean delete(TaskPart part, Naming ware) {
		// 生成实例
		DropPackageTuple tuple = new DropPackageTuple(part, ware);
		boolean success = add(tuple);
		// 保存成功，唤醒线程，等待写入完成
		if (success) {
			wakeup();
			tuple.await();
		}

		Logger.debug(this, "delete", tuple.isSuccessful(), "delete %s#%s",
				part.getIssuer(), ware);

		return tuple.isSuccessful();
	}
	
	/**
	 * 判断包类型，返回文件名后缀
	 * @param reader
	 * @return 有效是字符串，否则空指针
	 */
	private String doSuffix(CloudPackageReader reader) {
		if (reader.isConduct()) {
			return BuildConductPackage.SUFFIX;
		}
		if (reader.isEstablish()) {
			return BuildEstablishPackage.SUFFIX;
		}
		if (reader.isContact()) {
			return BuildContactPackage.SUFFIX;
		}
		return null;
	}
	
	/**
	 * 判断包类型，返回文件名后缀
	 * @param family
	 * @return 有效是字符串，否则空指针
	 */
	private String doSuffix(int family) {
		if (PhaseTag.isConduct(family)) {
			return BuildConductPackage.SUFFIX;
		}
		if (PhaseTag.isEstablish(family)) {
			return BuildEstablishPackage.SUFFIX;
		}
		if (PhaseTag.isContact(family)) {
			return BuildContactPackage.SUFFIX;
		}
		return null;
	}
	
	/**
	 * 软件包写入磁盘
	 * @param tuple
	 * @return 成功返回真，否则假
	 * @throws IOException
	 */
	private boolean doWrite(WritePackageTuple tuple) throws IOException {
		CloudPackageComponent component = tuple.getComponent();
		byte[] content = component.getContent();

		CloudPackageReader reader = new CloudPackageReader(content);
		CloudPackageItem item = reader.readGTC();
		if (item == null) {
			Logger.error(this, "doWrite", "cannot git gtc!");
			return false;
		}
		String suffix = doSuffix(reader);
		if (suffix == null) {
			Logger.error(this, "doWrite", "cannot git suffix!");
			return false;
		}
		GuideComponentReader sub = new GuideComponentReader(item.getContent());
		WareTag tag = sub.readWareTag();
		if (tag == null) {
			Logger.error(this, "doWrite", "cannot git ware-tag!");
			return false;
		}
		File file = buildFile(tuple.getIssuer(), tag.getNaming(), suffix);
		if (file == null) {
			Logger.error(this, "doWrite", "cannot build filename!");
			return false;
		}
		
		// 数据写入磁盘
		return writeContent(file, content);
	}

	/**
	 * 写入软件包
	 * @param tuple
	 */
	private void write(WritePackageTuple tuple) {
		if (tuple == null) {
			return;
		}

		// 写入磁盘
		boolean success = false;
		try {
			success = doWrite(tuple);
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}

		tuple.setSuccessful(success);
	}
	
	private void doReadPackage(File file, ReadPackageTuple tuple)
			throws IOException {
		TaskPart part = tuple.getPart();

		CloudPackageReader reader = new CloudPackageReader(file);
		CloudPackageItem item = reader.readGTC();
		if (item == null) {
			return;
		}
		GuideComponentReader sub = new GuideComponentReader(item.getContent());
		WareTag tag = sub.readWareTag();
		if (tag == null) {
			return;
		}

		// 附件
		List<CloudPackageItem> list = reader.readAssists(part.getFamily());
		if (list != null) {
			for (CloudPackageItem e : list) {
				e.setWare(tag.getNaming()); // 设置软件名称
				tuple.addJar(e);
			}
		}
		// 动态链接库
		list = reader.readLibraries(part.getFamily());
		if (list != null) {
			for (CloudPackageItem e : list) {
				e.setWare(tag.getNaming()); // 软件名称
				tuple.addLibrary(e);
			}
		}
	}
	
	/**
	 * 读取文件
	 * @param tuple
	 * @return 成功返回真，否则假
	 */
	private boolean doRead(ReadPackageTuple tuple) {
		TaskPart part = tuple.getPart();
		File dir = buildSubRoot(part.getIssuer());
		boolean success = (dir.exists() && dir.isDirectory());
		if (!success) {
			return false;
		}

		File[] files = dir.listFiles();
		for (int i = 0; files != null && i < files.length; i++) {
			File file = files[i];
			success = (file.exists() && file.isFile());
			if (!success) {
				continue;
			}

			// 读单个文件
			try {
				doReadPackage(file, tuple);
			} catch (IOException e) {
				Logger.error(e);
			} catch (Throwable e) {
				Logger.fatal(e);
			}
		}

		return true;
	}

	/**
	 * 读出组件
	 * @param tuple
	 */
	private void read(ReadPackageTuple tuple) {
		if (tuple == null) {
			return;
		}

		// 读软件包
		boolean success = doRead(tuple);
		tuple.setSuccessful(success);
	}

	/**
	 * 删除元组
	 * @param tuple
	 * @return 成功返回真，否则假
	 */
	private boolean doDelete(DropPackageTuple tuple) {
		TaskPart part = tuple.getPart();
		String suffix = doSuffix(part.getFamily());
		// 生成这个文件
		File file = buildFile(part.getIssuer(), tuple.getWare(), suffix);
		boolean success = (file.exists() && file.isFile());
		if (!success) {
			Logger.error(this, "doDelete", "not found file %s", file);
			return false;
		}
		
		// 删除磁盘上的文件
		boolean delete = file.delete();
		Logger.note(this, "doDelete", delete, "delete file %s", file);

		// 以上成功，取它的目录名，如果是空目录，删除这个目录
		if (delete) {
			File path = file.getParentFile();
			// 判断是目录
			success = (path != null && path.exists() && path.isDirectory());
			// 取目录中的文件名或者目录名
			if (success) {
				File[] subs = path.listFiles();
				success = (subs == null || subs.length == 0);
				if (success) {
					success = path.delete();
					Logger.note(this, "doDelete", success, "delete directory %s", path);
				}
			}
		}
		
		return delete;
	}

	/**
	 * 删除组件
	 * @param tuple
	 */
	private void delete(DropPackageTuple tuple) {
		if (tuple == null) {
			return;
		}

		// 读软件包
		boolean success = doDelete(tuple);
		tuple.setSuccessful(success);
	}
}