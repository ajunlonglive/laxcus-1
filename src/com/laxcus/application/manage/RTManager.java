/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.application.manage;

import java.io.*;
import java.util.*;

import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.hash.*;
import com.laxcus.util.lock.*;
import com.laxcus.util.naming.*;

/**
 * 运行时管理器，保存命令和匹配的调用器。<br><br>
 * 
 * 是一个静态类，保存“命令” -> “引导单元”的映射关系。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 6/29/2021
 * @since laxcus 1.0
 */
public class RTManager extends MutexHandler {

	/** 运行时管理器 静态实例 **/
	private static RTManager selfHandle = new RTManager();

	/** 判断为可用 **/
	private volatile boolean usabled = false;

	/** 已经更新或者否 **/
	private volatile boolean updated = false;

	/** 哈希码 -> 应用包 **/
	private Map<SHA256Hash, WRoot> roots = new TreeMap<SHA256Hash, WRoot>();

	/** KEY -> 应用程序 **/
	private Map<WKey, WProgram> programs = new TreeMap<WKey, WProgram>();

	/** 命令 -> 单元集合 **/
	private Map<Naming, RTVector> commands = new TreeMap<Naming, RTVector>();

	/**
	 * 构造运行时管理器
	 */
	private RTManager() {
		super();
		usabled = false;
		updated = false;
	}

	/**
	 * 判断为可用
	 * @return
	 */
	public boolean isUsabled() {
		return usabled;
	}

	/**
	 * 设置为可用或者否
	 * @param b
	 */
	public void setUsabled(boolean b) {
		usabled = b;
	}

	/**
	 * 重置为不更新
	 */
	public void resetUpdate() {
		updated = false;
	}

	/**
	 * 返回更新
	 * @return
	 */
	public boolean isUpdated(){
		return updated;
	}

	/**
	 * 返回实例
	 * @return
	 */
	public static RTManager getInstance() {
		return RTManager.selfHandle;
	}

	/**
	 * 输出全部
	 * @return
	 */
	public List<WRoot> list() {
		ArrayList<WRoot> array = new ArrayList<WRoot>();
		// 锁定...
		super.lockMulti();
		try {
			array.addAll(roots.values());
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return array;
	}

	/**
	 * 注入参数
	 * @param element
	 */
	private void inject(WElement element) {
		if (Laxkit.isClassFrom(element, WProgram.class)) {
			WProgram wp = (WProgram) element;
			programs.put(wp.getKey(), wp);
			String command = wp.getCommand();

			// 保存命名
			Naming name = new Naming(command);
			RTVector vector = commands.get(name);
			if (vector == null) {
				vector = new RTVector(name);
				commands.put(vector.getCommand(), vector);
			}
			vector.add(wp.getKey());
			//			commands.put(new Naming(command), wp.getKey());
		} else if (Laxkit.isClassFrom(element, WDocument.class)) {

		} else if (Laxkit.isClassFrom(element, WDirectory.class)) {
			WDirectory dir = (WDirectory) element;
			for (WElement sub : dir.getTokens()) {
				inject(sub);
			}
		}
	}

	/**
	 * 加入应用
	 * @param root
	 */
	private void inject(WRoot root) {
		// 生成映射
		roots.put(root.getHash(), root);

		WElement element = root.getElement();

		if (Laxkit.isClassFrom(element, WProgram.class)) {
			WProgram wp = (WProgram) element;
			programs.put(wp.getKey(), wp);
			String command = wp.getCommand();
			// 保存命名
			Naming name = new Naming(command);
			RTVector v = commands.get(name);
			if (v == null) {
				v = new RTVector(name);
				commands.put(v.getCommand(), v);
			}
			v.add(wp.getKey());
			//			commands.put(new Naming(command), wp.getKey());
		} else if (Laxkit.isClassFrom(element, WDocument.class)) {

		} else if (Laxkit.isClassFrom(element, WDirectory.class)) {
			WDirectory dir = (WDirectory)element;
			for(WElement sub : dir.getTokens()) {
				inject(sub);
			}
		}
	}

	private boolean writeShiftout(WKey key, boolean shiftout, WElement element) {
		if (Laxkit.isClassFrom(element, WProgram.class)) {
			WProgram wp = (WProgram) element;
			if (Laxkit.compareTo(wp.getKey(), key) == 0) {
				wp.setShiftout(shiftout);
				return true;
			}
		}
		if (Laxkit.isClassFrom(element, WDirectory.class)) {
			WDirectory dir = (WDirectory) element;
			for (WElement sub : dir.getTokens()) {
				if (writeShiftout(key, shiftout, sub)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 设置从桌面移出标记
	 * @param key WKey
	 * @param shiftout 标记值
	 * @return 成功返回真，否则假
	 */
	public boolean setShiftout(WKey key, boolean shiftout) {
		boolean success = false;
		// 锁定
		super.lockSingle();
		try {
			WProgram item = programs.get(key);
			if (item != null) {
				item.setShiftout(shiftout);
			}

			WRoot root = roots.get(key.getHash());
			if (root != null) {
				WElement element = root.getElement();
				success = writeShiftout(key, shiftout, element);
			}
		} catch (Throwable e) {

		} finally {
			super.unlockSingle();
		}

		// 更新
		if (success) {
			updated = true;
		}

		return success;
	}

	/**
	 * 写入单元
	 * @param root
	 * @return 成功返回真，否则真
	 */
	public boolean add(WRoot root) {
		boolean success = false;
		// 锁定...
		super.lockSingle();
		try {
			inject(root);
			success = true;
			updated = true; // 更新...
		} catch(Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 删除...
	 * @param element
	 */
	private void effuse(WElement element) {
		if (Laxkit.isClassFrom(element, WProgram.class)) {
			WProgram wp = (WProgram) element;
			String command = wp.getCommand();
			commands.remove(new Naming(command));
		} else if (Laxkit.isClassFrom(element, WDocument.class)) {

		} else if (Laxkit.isClassFrom(element, WDirectory.class)) {
			WDirectory dir = (WDirectory)element;
			for(WElement sub : dir.getTokens()) {
				effuse(sub);
			}
		}
	}

	/**
	 * 删除
	 * @param root
	 * @return
	 */
	public boolean remove(WRoot root) {
		boolean success = false;
		// 锁定
		super.lockSingle();
		try {
			success = (roots.remove(root.getHash()) != null);
			if (success) {
				effuse(root.getElement());
			}
			updated = true;
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	//	/**
	//	 * 找到匹配的单元
	//	 * @param command
	//	 * @return
	//	 */
	//	public WKey findFromCommand(Naming command) {
	//		// 查找匹配的
	//		super.lockMulti();
	//		try {
	//			return commands.get(command);
	//		} catch (Throwable e) {
	//			Logger.fatal(e);
	//		} finally {
	//			super.unlockMulti();
	//		}
	//		return null;
	//	}

	/**
	 * 找到匹配的单元
	 * @param command
	 * @return 返回数组
	 */
	public WKey[] findFromCommand(Naming command) {
		// 查找匹配的
		super.lockMulti();
		try {
			RTVector v = commands.get(command);
			if (v != null) {
				return v.toArray();
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 找到匹配的单元
	 * @param command
	 * @return
	 */
	public WKey[] findFromCommand(String command) {
		// 查找匹配的
		return findFromCommand(new Naming(command));
	}

	/**
	 * 返回应用包
	 * @param key
	 * @return
	 */
	public WProgram findProgram(WKey key) {
		// 锁定
		super.lockMulti();
		try {
			return programs.get(key);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 判断应用存在
	 * @param key 启动键
	 * @return 返回真或者假
	 */
	public boolean hasProgram(WKey key) {
		return findProgram(key) != null;
	}

	/**
	 * 返回ROOT
	 * @param hash
	 * @return
	 */
	public WRoot findRoot(SHA256Hash hash) {
		super.lockMulti();
		try {
			return roots.get(hash);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 判断某个哈希码对应的应用存在
	 * @param hash 散列码
	 * @return 返回真或者假
	 */
	public boolean hasRoot(SHA256Hash hash) {
		return findRoot(hash) != null;
	}

	/**
	 * 写应用参数
	 * @param file
	 * @param excludeSystem
	 * @return 返回成员数
	 * @throws IOException
	 */
	private int __writeRoots0(File file, boolean excludeSystem) throws IOException {
		ClassWriter writer = new ClassWriter();

		// 1. 先做筛选
		ArrayList<WRoot> array = new ArrayList<WRoot>();
		Iterator<Map.Entry<SHA256Hash, WRoot>> iterator = roots.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<SHA256Hash, WRoot> entry = iterator.next();
			WRoot rt = entry.getValue();
			// 忽略系统参数
			if (excludeSystem && rt.isSystem()) {
				continue;
			}
			array.add(rt);
		}
		// 2. 保存...
		writer.writeInt(array.size());
		for (WRoot rt : array) {
			writer.writeObject(rt);
		}
		// 3. 写入磁盘
		FileOutputStream os = new FileOutputStream(file);
		os.write(writer.effuse());
		os.flush();
		os.close();

		// 写入的成员数
		return array.size();
	}

	/**
	 * 写入到文件
	 * @param file
	 * @param excludeSystem 排斥系统应用
	 * @return 写入成员数
	 */
	public int writeRoots(File file, boolean excludeSystem) {
		// 锁定写入
		super.lockSingle();
		try {
			return __writeRoots0(file, excludeSystem);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return -1;
	}

	/**
	 * 读单元
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private WRoot[] __readRoots0(File file) throws IOException {
		ArrayList<WRoot> array = new ArrayList<WRoot>();
		// 判断存在
		boolean success = (file.exists() && file.isFile());
		if (success) {
			ClassReader reader = new ClassReader(file);
			int size = reader.readInt();
			for (int i = 0; i < size; i++) {
				WRoot root = new WRoot(reader);
				array.add(root);
			}
		}
		// 输出结果
		WRoot[] roots = new WRoot[array.size()];
		return array.toArray(roots);
	}

	/**
	 * 读单元
	 * @param file
	 * @return
	 */
	public WRoot[] readRoots(File file) {
		// 锁定读取
		super.lockSingle();
		try {
			return  __readRoots0(file);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return null;
	}

}


//private BasketBuffer readBasket(File file) {
//	// 加载软件包
//	try {
//		BasketBuffer buffer = new BasketBuffer();
//		buffer.load(file);
//		return buffer;
//	} catch (Throwable e) {
//		Logger.fatal(e);
//	}
//	return null;
//}
//
//private BasketBuffer readBasket(byte[] content) {
//	// 加载软件包
//	try {
//		BasketBuffer buffer = new BasketBuffer();
//		buffer.load(content);
//		return buffer;
//	} catch (Throwable e) {
//		Logger.fatal(e);
//	}
//	return null;
//}
//
///**
// * 读取图标
// * @param buffer
// * @param boot
// * @return
// */
//private ImageIcon readIcon(BasketBuffer buffer, BootItem boot) {
//	BootLocation bootLocation = boot.getIcon();
//	if (bootLocation == null) {
//		return null;
//	}
//	// 读取图标
//	try {
//		if (bootLocation.getURI() != null) {
//			byte[] bytes = buffer.getURI(bootLocation.getURI());
//			return ImageUtil.scale(bytes, 16, 16);
//		} else if (bootLocation.getJURI() != null) {
//			byte[] bytes = buffer.getJURI(bootLocation.getJURI());
//			return ImageUtil.scale(bytes, 16, 16);
//		}
//	} catch (Throwable e) {
//		Logger.fatal(e);
//	}
//	return null;
//}
//
////private void createElement(BasketBuffer buffer, ArrayList<RTElement> array, BootItem item) {
////	BootApplicationItem ba = item.getApplication();
////	if (ba == null) {
////		return ;
////	}
////
////	// 读取图标
////	ImageIcon icon = readIcon(buffer, item);
////	if (icon == null) {
////		return;
////	}
////
////	RTElement element = new RTElement(ba.getCommand());
////	element.setBootstrap(ba.getBootClass());
////	// 设置图标
////	element.setIcon(icon);
////
////	array.add(element);
////
////	// 找到子类，保存!
////	for (BootItem sub : item.list()) {
////		createElement(buffer, array, sub);
////	}
////}
//
//private void createElement(BasketBuffer buffer, ArrayList<RTElement> array, BootItem item) {
//	BootApplicationItem ba = item.getApplication();
//	if (ba != null) {
//		// 读取图标
//		ImageIcon icon = readIcon(buffer, item);
//		if (icon != null) {
//			RTElement element = new RTElement(ba.getCommand());
//			element.setBootstrap(ba.getBootClass());
//			// 设置图标
//			element.setIcon(icon);
//			// 保存!
//			array.add(element);
//		}
//	}
//
//	// 找到子类，保存!
//	for (BootItem sub : item.list()) {
//		createElement(buffer, array, sub);
//	}
//}
//
///**
// * 以锁定状态保存
// * @param element
// */
//private void addElement(RTElement element) {
//	// 锁定
//	super.lockSingle();
//	try {
//		Naming command = element.getCommand();
//		RTVector vector = commands.get(command);
//		if (vector == null) {
//			vector = new RTVector(command);
//			commands.put(vector.getCommand(), vector);
//		}
//		vector.add(element);
//	} catch (Throwable e) {
//		Logger.fatal(e);
//	} finally {
//		super.unlockSingle();
//	}
//}
//
///**
// * 增加成员
// * @param file
// */
//public boolean add(File file) {
//	BasketBuffer buffer = readBasket(file);
//	if (buffer == null) {
//		return false;
//	}
//
//	byte[] b = buffer.readBootstrap();
//	BootSplitter bs = new BootSplitter();
//	BootItem boot = bs.split(b);
//
//	ArrayList<RTElement> array = new ArrayList<RTElement>();
//
//	createElement(buffer, array, boot);
//
//	// 保存
//	for (RTElement element : array) {
//		element.setFile(file);
//		element.setNo(generator.nextSerial());
//		element.setSystem(boot.isSystem());
//
//		// 注入到集合中
//		addElement(element);
//	}
//
//	return true;
//}
//
///**
// * 增加成员
// * @param content
// * @return
// */
//public boolean add(byte[] content) {
//	BasketBuffer buffer = readBasket(content);
//	if (buffer == null) {
//		return false;
//	}
//
//	byte[] b = buffer.readBootstrap();
//	BootSplitter bs = new BootSplitter();
//	BootItem boot = bs.split(b);
//
//	ArrayList<RTElement> array = new ArrayList<RTElement>();
//
//	createElement(buffer, array, boot);
//
//	// 保存
//	for (RTElement element : array) {
//		element.setContent(content);
//		element.setNo(generator.nextSerial());
//		element.setSystem(boot.isSystem());
//
//		// 注入到集合中
//		addElement(element);
//	}
//
//	return true;
//}
//
///**
// * 找到匹配的单元
// * @param command
// * @return
// */
//public RTVector findFromCommand(Naming command) {
//	// 查找匹配的
//	super.lockMulti();
//	try {
//		return commands.get(command);
//	} catch (Throwable e) {
//		Logger.fatal(e);
//	} finally {
//		super.unlockMulti();
//	}
//	return null;
//}
//
///**
// * 找到匹配的单元
// * @param command
// * @return
// */
//public RTVector findFromCommand(String command) {
//	// 查找匹配的
//	return findFromCommand(new Naming(command));
//}
//
////	/**
////	 * 迭代保存
////	 * @param item
////	 */
////	private void put(BootItem item) {
////		BootApplicationItem application = item.getApplication();
////		// 当有效时...
////		if (application != null) {
////			String cmd = application.getCommand();
////			commands.put(cmd.toLowerCase(), item);
////		}
////		
////		for (BootItem sub : item.list()) {
////			put(sub);
////		}
////	}
//
////	/**
////	 * 保存
////	 * @param item
////	 */
////	public void set(BootItem item) {
////		// 锁定
////		super.lockSingle();
////		try {
////			put(item);
////		} catch (Throwable e) {
////			Logger.fatal(e);
////		} finally {
////			super.unlockSingle();
////		}
////	}
//
////	/**
////	 * 根据命令名称，查找匹配的单元
////	 * @param command
////	 * @return
////	 */
////	public BootItem findItem(String command) {
////		super.lockMulti();
////		try {
////			return commands.get(command.toLowerCase());
////		} catch (Throwable e) {
////			Logger.fatal(e);
////		} finally {
////			super.unlockMulti();
////		}
////		return null;
////	}
//
////	/**
////	 * 设置命令和类的关联
////	 * @param command
////	 * @param submain
////	 */
////	public void putCommand(String command, String submain) {
////		// 锁定
////		super.lockSingle();
////		try {
////			commands.put(command.toUpperCase(), submain);
////		} catch (Throwable e) {
////			Logger.fatal(e);
////		} finally {
////			super.unlockSingle();
////		}
////	}
//
////	/**
////	 * 返回命令关联类
////	 * @param command
////	 * @return 返回类实例
////	 */
////	public String findCommandClass(String command) {
////		super.lockMulti();
////		try {
////			return commands.get(command.toUpperCase());
////		} catch (Throwable e) {
////			Logger.fatal(e);
////		} finally {
////			super.unlockMulti();
////		}
////		return null;
////	}
////	
////	/**
////	 * 保存一个应用软件包
////	 * @param submain
////	 * @param file
////	 */
////	public void putBasket(String submain, File file) {
////		super.lockSingle();
////		try {
////			baskets.put(submain, file);
////		} catch (Throwable e) {
////			Logger.fatal(e);
////		} finally {
////			super.unlockSingle();
////		}
////	}
////	
////	/**
////	 * 找到包所在目录
////	 * @param submain 启动类
////	 * @return 返回类所在目录
////	 */
////	public File findBasketFile(String submain) {
////		super.lockMulti();
////		try {
////			return baskets.get(submain);
////		} catch (Throwable e) {
////			Logger.fatal(e);
////		} finally {
////			super.unlockMulti();
////		}
////		return null;
////	}


//	/** 命令 -> 类定义 **/
//	private TreeMap<String, BootItem> commands = new TreeMap<String, BootItem>();

///** 编号生成器 **/
//private SerialGenerator generator = new SerialGenerator(1, Long.MAX_VALUE - 1);


//public void printFault() {
//	for(Naming e : commands.keySet()) {
//		System.out.printf("[%s]\n", e);
//	}
//}

//	/** 类定义 -> 类包 **/
//	private TreeMap<String, File> baskets = new TreeMap<String, File>();
