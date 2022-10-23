/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.archive;

import java.io.*;
import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.hash.*;
import com.laxcus.util.naming.*;

/**
 * 分布任务组件文件。是除去数字内容外的其它文件属性。
 * 
 * @author scott.liang
 * @version 1.1 05/10/2015
 * @since laxcus 1.0
 */
public final class TaskFile implements Classable, Cloneable, Serializable, Comparable<TaskFile> {

	private static final long serialVersionUID = 1654312869115201806L;

	/** 阶段部件 **/
	private TaskPart part;

	/** 内容签名 **/
	private MD5Hash sign;

	/** 文件键，包含磁盘文件全路径名称、文件长度、文件最后修改时间。分布任务组件文件名称以“.dtc”后缀结尾 **/
	private FileKey key;

	/** 组件分段 **/
	private TreeSet<Tock> tocks = new TreeSet<Tock>();

	/**
	 * 根据分布任务组件文件，生成它的数据副本
	 * @param that
	 */
	private TaskFile(TaskFile that) {
		this();
		part = that.part;
		sign = that.sign;
		key = that.key;
		tocks.addAll(that.tocks);
	}

	/**
	 * 构造默认的组件文件
	 */
	protected TaskFile() {
		super();
		key = new FileKey();
	}

	/**
	 * 构造分布任务组件文件，指定全部参数
	 * @param filename 磁盘文件全路径
	 * @param length 文件长度
	 * @param time 最后修改时间
	 */
	public TaskFile(String filename, long length, long time) {
		this();
		key = new FileKey(filename, length, time);
	}

	/**
	 * 构造分布任务组件文件，指定参数
	 * @param file 磁盘文件
	 */
	public TaskFile(File file) {
		this();
		key = new FileKey(file);
	}

	/**
	 * 返回文件键
	 * @return FileKey实例
	 */
	public FileKey getKey() {
		return key;
	}

	/**
	 * 从可类化数据读取器中解析任务文件参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public TaskFile(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置阶段部件
	 * @param e TaskPart实例
	 */
	public void setPart(TaskPart e) {
		part = e;
	}

	/**
	 * 返回阶段部件
	 * @return TaskPart实例
	 */
	public TaskPart getPart() {
		return part;
	}

	/**
	 * 返回组件阶段类型，见PhaseTag定义。
	 * @return 组件阶段类型
	 */
	public int getFamily() {
		return part.getFamily();
	}

	/**
	 * 返回组件的用户签名
	 * @return Siger实例
	 */
	public Siger getIssuer() {
		return part.getIssuer();
	}

	/**
	 * 设置内容签名
	 * @param e MD5Hash实例
	 */
	public void setSign(MD5Hash e) {
		sign = e;
	}

	/**
	 * 返回内容签名
	 * @return MD5Hash实例
	 */
	public MD5Hash getSign() {
		return sign;
	}

	/**
	 * 返回分布任务组件标识
	 * @return TaskTag实例
	 */
	public TaskTag getTag() {
		return new TaskTag(part, sign);
	}

	/**
	 * 返回文件路径
	 * @return 文件路径
	 */
	public String getPath() {
		return key.getPath();
	}

	/**
	 * 设置文件路径
	 * @param e 文件路径
	 */
	public void setPath(String e) {
		key.setPath(e);
	}
	
	/**
	 * 返回File实例
	 * @return File实例
	 */
	public File getFile() {
		return key.getFile();
	}

	/**
	 * 返回文件长度
	 * @return 文件长度
	 */
	public long getLength() {
		return key.getLength();
	}

	/**
	 * 设置文件长度
	 * @param len 文件长度
	 */
	public void setLength(long len) {
		key.setLength(len);
	}

	/**
	 * 返回文件最后修改时间
	 * @return 文件最后修改时间
	 */
	public long getModified() {
		return key.getModified();
	}

	/**
	 * 设置文件最后修改时间
	 * @param modified 文件最后修改时间
	 */
	public void setModified(long modified) {
		key.setModified(modified);
	}
	
	/**
	 * 清除全部单元
	 */
	public void clearTocks() {
		tocks.clear();
	}
	
	/**
	 * 保存分布任务组件分段
	 * @param e 实例
	 * @return 返回真或者假
	 */
	public boolean addTock(Tock e){
		Laxkit.nullabled(e);
		return tocks.add(e);
	}

	/**
	 * 保存一批分布任务组件分段
	 * @param e 数组
	 * @return 返回新增成员数目
	 */
	public int addTocks(Collection<Tock> a) {
		Laxkit.nullabled(a);
		int size = tocks.size();
		for (Tock e : a) {
			tocks.add(e);
		}
		return tocks.size() - size;
	}

	/**
	 * 返回全部组件名称
	 * @return Tock列表
	 */
	public List<Tock> getTocks() {
		return new ArrayList<Tock>(tocks);
	}

	/**
	 * 返回匹配的组件名称
	 * @param root 命名
	 * @return Tock列表
	 */
	public List<Tock> findTocks(Naming root) {
		ArrayList<Tock> array = new ArrayList<Tock>();
		for (Tock e : tocks) {
			if (Laxkit.compareTo(e.getRoot(), root) == 0) {
				array.add(e);
			}
		}
		return array;
	}

//	/**
//	 * 分析一个磁盘文件
//	 * @return 成功返回真，否则假
//	 */
//	public boolean analyse() {
//		File file = new File(getPath());
//		// 取得阶段部件
//		try {
//			byte[] content = new byte[(int) file.length()];
//			// 读内容
//			FileInputStream fin = new FileInputStream(file);
//			fin.read(content);
//			fin.close();
//
//			return analyse(content);
//		} catch (IOException e) {
//			Logger.error(e);
//		}
//
//		return false;
//	}
//
//	/**
//	 * 分析一个“.dtc”后缀的文件。
//	 * 包括生成MD5散列码和取得分布组件的部件命名
//	 * 
//	 * @param content DCT文件
//	 * @return 成功返回真，否则假
//	 */
//	public boolean analyse(byte[] content) {
//		part = null;
//		// 建立签名
//		sign = Laxkit.doMD5Hash(content);
//
//		// 取得阶段部件
//		try {
//			// 解析tasks.xml
//			ByteArrayInputStream bin = new ByteArrayInputStream(content, 0, content.length);
//			JarInputStream jin = new JarInputStream(bin);
//			while (true) {
//				JarEntry entry = jin.getNextJarEntry();
//				if (entry == null) {
//					break;
//				}
//
//				String name = entry.getName();
//
//				ByteArrayOutputStream out = new ByteArrayOutputStream();
//				byte[] b = new byte[1024];
//				while(true) {
//					int len = jin.read(b, 0, b.length);
//					if(len == -1) break;
//					out.write(b, 0, len);
//				}
//				b = out.toByteArray();
//				if(b == null || b.length == 0) {
//					continue;
//				}
//
//				if (TaskArchive.TAG.equals(name)) { // tag file (tasks.xml)
//					part = split(b);
//					break;
//				}
//			}
//			jin.close();
//		} catch (IOException e) {
//			Logger.error(e);
//		}
//
//		return part != null && sign != null;
//	}

//	/**
//	 * 从tasks.xml文件中解析全部参数
//	 */
//	private TaskPart split(byte[] data) {
//		XMLocal xml = new XMLocal();
//		Document document = xml.loadXMLSource(data);
//		if (document == null) {
//			return null;
//		}
//
//		// 任务阶段
//		String phase = xml.getXMLValue(document.getElementsByTagName("phase"));
//		int family = PhaseTag.translate(phase);
//		// 存在错误，忽略它
//		if (family < 1) {
//			Logger.error(this, "doPart", "illegal phase:%s", phase);
//			return null;
//		}
//
//		// 用户签名（16进制的SHA256字符串）
//		String username = xml.getXMLValue(document.getElementsByTagName("sign"));
//		// 生成签名，不论是空指针、16进制字符串，或者明文
//		Siger siger = SHAUser.doSiger(username);
//
//		// 解析命名（分为系统命名和用户命名）
//		NodeList list = document.getElementsByTagName("task");
//		int size = list.getLength();
//		for (int i = 0; i < size; i++) {
//			Element element = (Element) list.item(i);
//
//			// 任务命名，包括根命名和子命名两部分，中间由逗号隔开
//			String naming = xml.getValue(element, "naming");
//			Tock name = new Tock(naming);
//			segments.add(name);
//		}
//
//		// 设置部件
//		return new TaskPart(siger, family);
//	}

	/**
	 * 生成当前实例的数据副本
	 * @return TaskFile实例
	 */
	public TaskFile duplicate() {
		return new TaskFile(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (part != null) {
			return part.hashCode();
		} else {
			return key.hashCode();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (part != null && sign != null) {
			return String.format("%s %s", part, sign);
		} else {
			return key.toString();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != TaskFile.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((TaskFile) that) == 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(TaskFile that) {
		if (that == null) {
			return 1;
		}
		// 比较部件、文件、内容签名
		int ret = Laxkit.compareTo(part, that.part);
		if (ret == 0) {
			ret = Laxkit.compareTo(key, that.key);
		}
		if (ret == 0) {
			ret = Laxkit.compareTo(sign, that.sign);
		}

		return ret;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		// 阶段部件和MD5签名
		writer.writeInstance(part);
		writer.writeInstance(sign);
		// 文件键，包括文件路径、长度、最后修改时间
		writer.writeInstance(key);
		// 组件分段
		writer.writeInt(tocks.size());
		for (Tock e : tocks) {
			writer.writeObject(e);
		}
		// 生成数据长度
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		// 阶段部件和MD5签名
		part = reader.readInstance(TaskPart.class);
		sign = reader.readInstance(MD5Hash.class);
		// 文件键，包括文件路径、长度、最后修改时间
		key = reader.readInstance(FileKey.class);
		// 组件分段
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Tock e = new Tock(reader);
			tocks.add(e);
		}
		// 返回解析尺寸
		return reader.getSeek() - seek;
	}

}