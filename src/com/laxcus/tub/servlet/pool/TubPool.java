/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub.servlet.pool;

import java.io.*;
import java.util.*;
import java.util.jar.*;

import org.w3c.dom.*;

import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.tub.servlet.*;
import com.laxcus.util.*;
import com.laxcus.util.loader.*;
import com.laxcus.util.naming.*;
import com.laxcus.xml.*;

/**
 * 边缘容器管理池。 <br>
 * 包括容器组件，运行中的容器都在这里接受管理。
 * 
 * @author scott.liang
 * @version 1.0 6/19/2019
 * @since laxcus 1.0
 */
public class TubPool extends DiskPool implements TubTrustor {

	/** 边缘容器管理池静态句柄 **/
	private static TubPool selfHandle = new TubPool();

	/** 序列号生成器，在一段时间内唯一 **/
	private SerialGenerator serial = new SerialGenerator();

	/** 边缘容器的类加载器  **/
	private HotClassLoader loader = new HotClassLoader();

	/** 保存的JAR包记录 **/
	private TreeSet<FileKey> records = new TreeSet<FileKey>();

	/** 命名 -> 类实例 **/
	private TreeMap<Naming, TubTag> tags = new TreeMap<Naming, TubTag>();

	/** 运行中的边缘计算服务组件。 编号 -> 边缘计算服务组件**/
	private TreeMap<Long, TubServlet> runTubs = new TreeMap<Long, TubServlet>();

	/** 推送器 **/
	private TubResourceHelper pusher;

	/**
	 * 构造默认的边缘容器管理池
	 */
	private TubPool() {
		super();
		setSleepTime(60);
	}

	/**
	 * 返回边缘容器管理池静态句柄
	 * @return 边缘容器管理池句柄
	 */
	public static TubPool getInstance() {
		return TubPool.selfHandle;
	}

	/**
	 * 设置容器管理器助手
	 * @param e 助手实例 
	 */
	public void setHelper(TubResourceHelper e) {
		pusher = e;
	}

	/**
	 * 返回容器管理器助手
	 * @return 助手实例
	 */
	public TubResourceHelper getHelper() {
		return pusher;
	}

	/**
	 * 输出全部命名
	 * @return 全部命名
	 */
	public List<Naming> getTubNames() {
		super.lockMulti();
		try {
			return new ArrayList<Naming>(tags.keySet());
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 输出全部容器组件
	 * @return TubTag列表
	 */
	public List<TubTag> getTags() {
		super.lockMulti();
		try {
			return new ArrayList<TubTag>(tags.values());
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 根据边缘容器名称，查找一个异步边缘容器
	 * @param naming 边缘容器名称
	 * @return 返回异步边缘容器句柄。没有找到返回空指针。
	 */
	public TubTag findTag(Naming naming) {
		super.lockMulti();
		try {
			return tags.get(naming);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 根据边缘容器名称，判断异步边缘容器存在
	 * @param naming 边缘容器名称
	 * @return 返回真或者假
	 */
	public boolean hasTag(Naming naming) {
		TubTag tag = findTag(naming);
		return tag != null;
	}

	/**
	 * 产生一个边缘容器编号。边缘容器编号一定当前集合中没有，并且在有效范围（0-Long.MAX_VALUE）内的长整型。
	 * @return 长整型数值
	 */
	private long createProcessId() {
		do {
			long invokerId = serial.nextSerial();
			// 根据边缘容器编号，判断异步边缘容器存在；不存在即有效
			if (!hasTub(invokerId)) {
				return invokerId;
			}
		} while (true);
	}

	/**
	 * 根据边缘容器编号，判断异步边缘容器存在
	 * @param processId 边缘容器编号
	 * @return 返回真或者假
	 */
	public boolean hasTub(long processId) {
		TubServlet tubServlet = findTub(processId);
		return tubServlet != null;
	}
	
	/**
	 * 返回运行中的边缘容器
	 * @return 数字
	 */
	public int getRunTubs() {
		super.lockMulti();
		try {
			return runTubs.size();
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 根据边缘容器编号，查找一个异步边缘容器
	 * @param processId 边缘容器编号
	 * @return 返回异步边缘容器句柄。没有找到返回空指针。
	 */
	public TubServlet findTub(long processId) {
		super.lockMulti();
		try {
			return runTubs.get(processId);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 根据命名，查找运行中的边缘容器。如果不指定命名，输出返回
	 * @param names 命名
	 * @return 返回集合
	 */
	public List<TubToken> findTubs(Naming[] names) {
		ArrayList<TubToken> a = new ArrayList<TubToken>();
		// 锁定
		super.lockMulti();
		try {
			Iterator<Map.Entry<Long, TubServlet>> iterator = runTubs.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Long, TubServlet> entry = iterator.next();
				TubServlet e = entry.getValue();
				if (names == null || names.length == 0) {
					a.add(e.getToken());
				} else {
					for (Naming name : names) {
						if (Laxkit.compareTo(e.getNaming(), name) == 0) {
							a.add(e.getToken());
							break;
						}
					}
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return a;
	}

	/**
	 * 根据命名，查找运行中的边缘容器
	 * @param naming
	 * @return
	 */
	public List<TubToken> findTubs(Naming naming) {
		return findTubs(new Naming[] { naming });
	}

	/**
	 * 检查运行中的边缘窗口数量
	 * @param names 命名
	 * @return 返回运行数量
	 */
	public int checkTubs(Naming[] names) {
		List<TubToken> tokens = findTubs(names);
		return (tokens == null ? 0 : tokens.size());
	}

	/**
	 * 检查运行中的边缘窗口数量
	 * @param names 命名
	 * @return 返回运行数量
	 */
	public int checkTubs(Naming name) {
		return checkTubs(new Naming[] { name });
	}

	/**
	 * 保存实例 
	 * @param tubServlet 边缘计算服务组件
	 */
	private void add(TubServlet tubServlet) {
		super.lockSingle();
		try {
			runTubs.put(tubServlet.getId(), tubServlet);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 停止边缘容器运行
	 * @param processId 进程号
	 * @param args 参数
	 * @return 返回结果
	 * @throws TubException
	 */
	public TubStopResult stop(long processId, String args) throws TubException {
		TubServlet tubServlet = findTub(processId);
		if (tubServlet == null) {
			return new TubStopResult(TubProcessResult.NOTFOUND);
		}
		TubStopResult result = null;
		try {
			result = tubServlet.stop(args);
			// 解除关联
			detach(tubServlet);
		} catch (Throwable e) {
			// 解除关联
			detach(tubServlet);
			// 弹出错误
			throw new TubException(e.getCause() != null ? e.getCause() : e);
		}
		// 返回结果
		return result;
	}

	/**
	 * 启动实例 
	 * @param naming 命名
	 * @param args 参数
	 * @return 返回结果
	 * @throws TubException
	 */
	public TubStartResult launch(Naming naming, String args) throws TubException {
		// 建立实例
		TubServlet servlet = create(naming);
		// 启动
		if (servlet == null) {
			return null;
		}
		// 启动
		try {
			return servlet.launch(args);
		} catch (TubException e) {
			throw e;
		} catch (Throwable e) {
			if (e.getCause() != null) {
				throw new TubException(e.getCause());
			} else {
				throw new TubException(e);
			}
		}
	}

	/**
	 * 根据类路径名查找类对象。类路径名是以“.”号为分隔符，如“org.suxbit.util.xxx”。
	 * @param clazzName 类的全路径名称。
	 * @return 返回类实例
	 */
	public Class<?> findClass(String clazzName) {
		super.lockMulti();
		try {
			return Class.forName(clazzName, true, loader);
		} catch (ClassNotFoundException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 根据名称，生成实例 !
	 * @param naming 工作命名
	 * @return 返回命名实例
	 */
	public TubServlet create(Naming naming) {
		// 找到标签
		TubTag tag = findTag(naming);
		if (tag == null) {
			return null;
		}
		// 找到类实例
		Class<?> clazz = findClass(tag.getClassName());
		if (clazz == null) {
			return null;
		}

		// 生成实例
		TubServlet tubServlet = null;
		try {
			tubServlet = (TubServlet) clazz.newInstance();
		} catch (InstantiationException e) {
			Logger.error(e);
		} catch (IllegalAccessException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}

		// 赋值，保存它
		if (tubServlet != null) {
			long processId = createProcessId();
			tubServlet.setId(processId);
			tubServlet.setNaming(naming);
			tubServlet.setTrustor(this);
			add(tubServlet);
		}
		return tubServlet;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		//		// 加载配置
		//		boolean success = update();
		//		Logger.debug(this, "init", success, "load custom jar and split command/invoker");
		//		return success;

		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.debug(this, "process", "into...");
		// 循环判断直到退出
		while (!isInterrupted()) {
			// 如果允许自动检查更新时，定时检查更新
			boolean success = isUpdate();
			if (success) {
				update();
			}

			// 延时...
			sleep();
		}
		Logger.debug(this, "process", "exit!");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		records.clear();
		tags.clear();
		runTubs.clear();
		loader = null;
	}

	/** JAR文件后缀 **/
	private final String suffix = ".jar";

	/**
	 * 转换参数
	 * @param file 磁盘文件
	 * @return CruxKey实例
	 */
	private FileKey convert(File file) {
		return new FileKey(file.getAbsolutePath(), file.length(),
				file.lastModified());
	}

	/**
	 * 热发布和更新自定义JAR文件和配置。如果没有定义自定义配置参数，或者参数不正确，返回“真”。<br><br>
	 * 
	 * 热发布和更新操作流程：<br>
	 * 1. 把自定义目录中的JAR读出来，保存到本地类加载器（注意，不是系统的类加载），以实现热发布和更新。<br>
	 * 2. 重新解析COMMAND/INVOKER配置对。<br>
	 * 这个顺序不能乱！！！
	 * 
	 * @return 成功返回真，否则假
	 */
	private boolean update() {
		// // 如果参数无效，忽略它
		// if (!CustomConfig.isValidate()) {
		// Logger.warning(this, "update", "invalid custom configure!");
		// return true;
		// }

		// 先加载JAR文件，再解析配置文件
		boolean success = false;
		// 锁定的
		super.lockSingle();
		try {
			success = loadJar();
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// if (success) {
		// // success = splitStatement();
		// }
		// Logger.note(this, "update", success, "load custom jar resource");

		return success;
	}

	/**
	 * 判断发生更新
	 * @return 发生更新返回真，否则假
	 */
	private boolean isUpdate() {
		File dir = getRoot();
		// 没有，返回假
		if(dir == null || !dir.exists()) {
			return false;
		}
		// 扫描本地文件
		ArrayList<File> files = new ArrayList<File>();
		scanDisk(files, dir);

		// 转换成自定义单元
		ArrayList<FileKey> array = new ArrayList<FileKey>();
		for (File file : files) {
			FileKey e = convert(file);
			array.add(e);
		}
		// 如果数目不匹配，存在更新
		if (array.size() != records.size()) {
			return true;
		}
		// 统计匹配的数目
		int count = 0;
		for (FileKey e : array) {
			if (records.contains(e)) {
				count++;
			}
		}
		// 如果不匹配，是发生更新
		return (count != records.size());
	}

	/**
	 * 扫描磁盘文件
	 * @param array 文件数组
	 * @param dir 磁盘目录
	 */
	private void scanDisk(List<File> array, File dir) {
		File[] subs = dir.listFiles();
		for (File file : subs) {
			String name = file.getAbsolutePath();
			if (file.isDirectory()) {
				scanDisk(array, file);
			} else if (name.toLowerCase().endsWith(suffix)) {
				array.add(file);
			}
		}
	}

	//	/**
	//	 * 读出JAR内容
	//	 * @param file 磁盘文件
	//	 * @return 返回JAR内容，错误返回空值
	//	 */
	//	private byte[] readContent(File file) {
	//		int len = (int) file.length();
	//		if (len < 1) {
	//			return null;
	//		}
	//
	//		try {
	//			byte[] b = new byte[len];
	//			FileInputStream in = new FileInputStream(file);
	//			in.read(b);
	//			in.close();
	//			return b;
	//		} catch (IOException e) {
	//			Logger.error(e);
	//		}
	//		return null;
	//	}

	/**
	 * 加载指定目录下的JAR文件
	 * @return 成功返回真，否则假
	 */
	private boolean loadJar() throws IOException {
		File dir = getRoot();
		if (dir == null || !dir.exists()) {
			return false;
		}

		// 扫描本地文件
		ArrayList<File> files = new ArrayList<File>();
		scanDisk(files, dir);

		// JAR档案条目数目
		ArrayList<HotClassEntry> a1 = new ArrayList<HotClassEntry>();

		ArrayList<TubTag> a2 = new ArrayList<TubTag>();

		// 加载JAR文件
		for (File file : files) {
			byte[] content = readContent(file);
			// 如果是空值，忽略它
			if(Laxkit.isEmpty(content)) {
				continue;
			}
			// 生成JAR档案条目
			HotClassEntry entry = new HotClassEntry(file.getAbsolutePath(), content);
			a1.add(entry);

			// 解析全部命名 -> 类实例
			List<TubTag> tag = scanItems(content);
			if (tag != null) {
				a2.addAll(tag);
			}

			Logger.info(this, "loadJar", "reload %s", entry.getPath());
		}

		// 生成新的类加载器
		// 执行下面这段代码后，旧的类加载器被抛弃，调用Class.forName方法时，系统将重新调用ClassLoader.findClass方法，去找指定类
		loader = new HotClassLoader(a1);

		// 删除在窗口上的显示
		for (TubTag e : tags.values()) {
			pusher.removeTubTag(e);
		}
		// 清除旧的
		tags.clear();
		// 保存新的
		for (TubTag e : a2) {
			tags.put(e.getNaming(), e);
			pusher.addTubTag(e);
		}

		// 重新保存参数
		records.clear();
		for(File file : files) {
			FileKey e = convert(file);
			records.add(e);
		}

		Logger.info(this, "loadJar", "jar files: %d", records.size());

		// 操作成功
		return true;
	}

	/** 配置文件，命名 ->类名 **/
	public final static String TAG = "TUB-INF/tubs.xml";

	/**
	 * 从JAR流中读类文件
	 * @param reader JAR读取流
	 * @return 返回解码后的数据流
	 * @throws IOException
	 */
	private byte[] readContent(JarInputStream reader) throws IOException {
		// 读数据流
		ByteArrayOutputStream writer = new ByteArrayOutputStream();
		byte[] b = new byte[1024];
		while(true) {
			int len = reader.read(b, 0, b.length);
			if(len == -1) break;
			writer.write(b, 0, len);
		}
		return writer.toByteArray();
	}

	/**
	 * 从tubs.xml文件中解析命名参数
	 * @param config 配置数据
	 * @return 返回解析的边缘容器标签列表
	 */
	private List<TubTag> split(byte[] config, byte[] content) {
		Document document = XMLocal.loadXMLSource(config);
		if (document == null) {
			return null;
		}

		HotClassLoader as = new HotClassLoader();
		as.add("tub", content);

		TreeSet<TubTag> array = new TreeSet<TubTag>();

		NodeList list = document.getElementsByTagName("tub");
		int size = list.getLength();
		for (int i = 0; i < size; i++) {
			Element element = (Element) list.item(i);

			// 命名
			String naming = XMLocal.getXMLValue(element.getElementsByTagName("naming"));
			// 类名
			String clazz = XMLocal.getXMLValue(element.getElementsByTagName("boot-class"));
			// 资源配置
			String resource = XMLocal.getXMLValue(element.getElementsByTagName("resource"));

			// 标题
			String caption = XMLocal.getXMLValue(element.getElementsByTagName("caption"));
			// 图标
			String icon = XMLocal.getXMLValue(element.getElementsByTagName("icon"));
			// 工具提示
			String tooltip = XMLocal.getXMLValue(element.getElementsByTagName("tooltip"));
			
			String startParamTip = XMLocal.getXMLValue(element.getElementsByTagName("start-argument-tooltip"));
			
			String stopParamTip = XMLocal.getXMLValue(element.getElementsByTagName("stop-argument-tooltip"));

			// 标签
			TubTag tag = new TubTag(naming, clazz);
			tag.setResource(resource);

			tag.setCaption(caption);
			tag.setTooltip(tooltip);
			tag.setStartArgumentTooltip(startParamTip);
			tag.setStopArgumentTooltip(stopParamTip);
			
			// 图标
			if (icon != null && icon.length() > 0) {
				try {
					byte[] stream = as.readResource(icon);
					tag.setIcon(stream);
				} catch (IOException e) {
					Logger.error(e);
				}
			}

			Logger.debug(this, "split", "this is %s",tag);

			array.add(tag);
		}

		// 设置部件
		return new ArrayList<TubTag>(array);
	}

	/**
	 * 解析参数
	 * @param content
	 * @return
	 * @throws IOException
	 */
	private List<TubTag> scanItems(byte[] content) throws IOException {
		byte[] inf = null;
		// 找到和解析tasks.xml
		ByteArrayInputStream bin = new ByteArrayInputStream(content, 0, content.length);
		JarInputStream jin = new JarInputStream(bin);
		while (true) {
			JarEntry entry = jin.getNextJarEntry();
			if (entry == null) {
				break;
			}
			// 忽略目录
			if (entry.isDirectory()) {
				continue;
			}

			String name = entry.getName();
			// 查找 tasks.xml 文件，这个标签文件解释DTC文件的全部配置信息，其它文件忽略
			if (!TAG.equals(name)) {
				continue;
			}

			// 读JAR条目，保存和解析参数
			inf = readContent(jin);
			break;
		}
		jin.close();

		// 有标记配置，解析它
		if (inf != null) {
			return split(inf, content);
		}
		// 找到后退出

		return null;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.tub.TubTrustor#detach(com.laxcus.tub.TubTub)
	 */
	@Override
	public boolean detach(TubServlet that) {
		long processId = that.getId();

		boolean success = false;
		// 锁定
		super.lockSingle();
		try {
			TubServlet tubServlet = runTubs.remove(processId);
			success = (tubServlet != null);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

}
