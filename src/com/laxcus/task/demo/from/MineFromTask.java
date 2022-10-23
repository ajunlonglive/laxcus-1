/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.demo.from;

import java.io.*;
import java.util.*;

import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.distribute.conduct.command.*;
import com.laxcus.distribute.conduct.session.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.from.*;
import com.laxcus.task.talk.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.hash.*;

/**
 * 选择GPU/CPU进行挖矿。
 * 
 * @author xiaoyang.yuan
 * @version 1.0 2015-2-12
 * @since laxcus 1.0
 */
public class MineFromTask extends FromTask {

	/**
	 * 构造默认的挖矿FROM阶段排序任务
	 */
	public MineFromTask() {
		super();
//		// 默认是检查和安装动态链接库
//		checkAndLoadLibraries0();
	}

//	/**
//	 * 检查和安装动态链接库
//	 */
//	protected void checkAndLoadLibraries0() {
//		try {
//			loadLibraries();
//		} catch (SecurityException e) {
//			e.printStackTrace();
//		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
//		} catch (NullPointerException e) {
//			e.printStackTrace();
//		} catch (UnsatisfiedLinkError e) {
//			e.printStackTrace();
//		} catch (NoSuchFieldException e) {
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//	}
	
//	/**
//	 * 加载全部动态链接库
//	 * @throws IllegalAccessException 
//	 * @throws NoSuchFieldException 
//	 * @throws IllegalArgumentException 
//	 * @throws SecurityException 
//	 */
//	protected void loadLibraries() throws SecurityException,
//			IllegalArgumentException, NoSuchFieldException,
//			IllegalAccessException, NullPointerException, UnsatisfiedLinkError {
//
//		ClassLoader c = getClass().getClassLoader();
//		// 判断是动态库类加载器
//		if (!Laxkit.isClassFrom(c, LibraryClassLoader.class)) {
//			return;
//		}
//
//		LibraryClassLoader loader = (LibraryClassLoader) c;
//		// 1. 没有预存储动态链接库，不用加载
//		if (!loader.hasLibraries()) {
//			return;
//		}
//		// 2. 如果动态库已经全部加载，忽略它！
//		if (loader.isLoadedLibraries()) {
//			return;
//		}
//		// 2. 取出动态链接库，加载它！注意：只能在用户对象中使用“System.load”加载动态链接库，而不是在LibraryClassLoader中加载！
//		String[] paths = loader.getLibraries();
//		int size = (paths == null ? 0 : paths.length);
//		System.out.printf("Hi, count libraries %d\n", size);
//		for (int i = 0; i < size; i++) {
//			String path = paths[i];
//			// 以全路径格式加载动态链接库到“LibraryClassLoader”
//			System.load(path);
//
//			System.out.printf("Hi, load library %s\n", path);
//			// Logger.debug(getIssuer(), this, "loadLibraries",
//			// "load library %s", path);
//		}
//	}

	/**
	 * 检查调用器
	 */
	private void check() {
		super.delay(2000);

		long invokerId = getInvokerId();
		Siger siger = getIssuer();
		FromSession session = getSession();

		List<Node> buddies = session.getBuddies();
		Logger.debug(siger, this, "check", "同级远程节点数目是:%d", buddies.size());

		if (buddies.size() <1) {
			return;
		}
		// 调用
		Node remote = buddies.get(0);

		try {
			FromTrustor trustor = getFromTrustor();
			Node local = trustor.getLocal(invokerId);

			TalkFalg flag = new TalkFalg(siger, local, session.getMaster());
			TaskMoment status = check(remote, flag);
			Logger.debug(siger, this, "check", "查询：%s 状态是：%s", remote,
					TaskStatus.translate(status.getStatus()));

			// 查询请求
			TalkQuest quest = new TalkQuest(flag, new byte[10240]);
			TalkReply reply = ask(remote, quest);
			Logger.debug(siger, this, "check", "查询请求结果是：%s", (reply != null ? "有效" : "失败"));
			if (reply != null ) {
				Logger.debug(siger, this, "check", "反馈信息长度：%d !", reply.getPrimitiveLength() );
			}
		} catch (TaskException e) {
			Logger.error(siger, e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.FluxTask#talk(com.laxcus.task.talk.TalkQuest)
	 */
	@Override
	public TalkReply talk(TalkQuest quest) {
		Siger siger = getIssuer();

		Logger.debug(siger, this, "talk", "请求信息长度：%d",
				quest.getPrimitiveLength());

		// 反馈本地节点
		long invokerId = getInvokerId();
		FromTrustor trustor = getFromTrustor();
		try {
			Node local = trustor.getLocal(invokerId);
			return new TalkReply(siger, local, new byte[102400]);
		} catch (TaskException e) {
			Logger.error(e);
		}
		return null;
	}

	/**
	 * 判断SHA256散列码前面N个字符是0
	 * 
	 * @param hash SHA256散列码
	 * @return 符合要求返回真，否则假
	 */
	private boolean allow(SHA256Hash hash, int zero) {
		// 输出SHA256散列码的字节数组
		byte[] b = hash.get();
		// 从0下标开始
		for (int i = 0; i < zero; i++) {
			if (b[i] != 0) return false;
		}
		return true;
	}

	//	/* (non-Javadoc)
	//	 * @see com.laxcus.task.conduct.from.FromTask#divide()
	//	 */
	//	@Override
	//	public long divide() throws TaskException {
	//		// 测试，检查交互调用
	//		this.check();
	//
	//		FromStep cmd = getCommand();
	//		FromSession session = cmd.getSession();
	//
	//		boolean gpu = findBoolean(session, "GPU");
	//		int zeros = findInteger(session, "zeros");
	//		int index = findInteger(session, "index"); // 索引，唯一，做为模值
	//		long begin = findLong(session, "begin");
	//		long end = findLong(session, "end");
	//		String prefix = findString(session, "PREFIX");
	//
	//		Logger.debug(getIssuer(), this, "divide", "本机矿码范围：%s / [%d - %d]", prefix, begin, end);
	//
	//		// 中间数据写入器和临时缓存
	//		FluxWriter writer = fetchWriter();
	//		ClassWriter buf = new ClassWriter();
	//		Node local = writer.getLocal();
	//
	//		// 被挖出的矿码数目
	//		int count = 0;
	//		// 判断支持CPU挖矿
	//		gpu = (gpu && GPUMiner.hasGPU());
	//		// 调用GPU挖矿，如果没有仍然采用CPU挖矿
	//		if (gpu) {
	//			// 调用JNI接口进行挖码
	//			byte[] b = GPUMiner.enumerate(prefix, begin, end, zeros, false);
	//			// 解析挖出的矿码和它的明文
	//			if (b != null) {
	//				ClassReader reader = new ClassReader(b);
	//				while(reader.hasLeft()){
	//					SHA256Hash hash = new SHA256Hash(reader); // 矿码
	//					String text = reader.readString(); // 明文
	//					buf.writeObject(local);
	//					buf.writeObject(hash);
	//					buf.writeString(text);
	//					count++;
	//				}
	//			}
	//		} else {
	//			// 生成散列码，判断首字符是0
	//			for (long seek = begin; seek <= end; seek++) {
	//				String text = String.format("%s%d", prefix, seek);
	//				SHA256Hash hash = Laxkit.doSHA256Hash(text.getBytes());
	//				if (allow(hash, zeros)) {
	//					buf.writeObject(local);
	//					buf.writeObject(hash);
	//					buf.writeString(text);
	//					count++;
	//				}
	//			}
	//		}
	//
	//		//		// 生成散列码，判断首字符是0
	//		//		for (long seek = begin; seek <= end; seek++) {
	//		//			String text = String.format("%s%d", prefix, seek);
	//		//			SHA256Hash hash = Laxkit.doSHA256Hash(text.getBytes());
	//		//			if (allow(hash, zeros)) {
	//		//				buf.writeObject(local);
	//		//				buf.writeObject(hash);
	//		//				buf.writeString(text);
	//		//				count++;
	//		//			}
	//		//		}
	//
	//		Logger.debug(getIssuer(), this, "divide", "挖出的矿码统计是：%d", count);
	//
	//		// 建立数据写入器，存取模式根据命令的“内存/磁盘”选定
	//		if (buf.size() > 0) {
	//			// 以添加方式写入缓存
	//			byte[] b = buf.effuse();
	//			FluxField field = writer.append(index, count, b, 0, b.length);
	//			// 如果为null，即写入失败
	//			if (field == null) {
	//				throw new FromTaskException("数据写入失败！");
	//			}
	//		}
	//
	//		// 返回元数组字节长度
	//		return assemble();
	//	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.conduct.from.FromTask#divide()
	 */
	@Override
	public long divide() throws TaskException {
		// 测试，检查交互调用
		this.check();

		FromStep cmd = getCommand();
		FromSession session = cmd.getSession();

		boolean gpu = findBoolean(session, "GPU");
		int zeros = findInteger(session, "zeros");
		int index = findInteger(session, "index"); // 索引，唯一，做为模值
		long begin = findLong(session, "begin");
		long end = findLong(session, "end");
		String prefix = findString(session, "PREFIX");

		Logger.debug(getIssuer(), this, "divide", "本机矿码范围：%s / [%d - %d]", prefix, begin, end);

		// 中间数据写入器和临时缓存
		FluxWriter writer = fetchWriter();
		ClassWriter buf = new ClassWriter();
		Node local = writer.getLocal();

		// 被挖出的矿码数目
		int count = 0;
		// 判断支持CPU挖矿
		gpu = (gpu && GPUMiner.hasGPU());
		// 调用GPU挖矿，如果没有仍然采用CPU挖矿
		if (gpu) {
			// 调用JNI接口进行挖码
			byte[] b = GPUMiner.enumerate(prefix, begin, end, zeros, false);
			// 解析挖出的矿码和它的明文
			if (b != null) {
				ClassReader reader = new ClassReader(b);
				while(reader.hasLeft()){
					SHA256Hash hash = new SHA256Hash(reader); // 矿码
					String text = reader.readString(); // 明文
					buf.writeObject(local);
					buf.writeObject(hash);
					buf.writeString(text);
					count++;
				}
			}
		} else {
			// 生成散列码，判断首字符是0
			for (long seek = begin; seek <= end; seek++) {
				String text = String.format("%s%d", prefix, seek);
				SHA256Hash hash = Laxkit.doSHA256Hash(text.getBytes());
				if (allow(hash, zeros)) {
					buf.writeObject(local);
					buf.writeObject(hash);
					buf.writeString(text);
					count++;
				}
			}
		}

		//		// 生成散列码，判断首字符是0
		//		for (long seek = begin; seek <= end; seek++) {
		//			String text = String.format("%s%d", prefix, seek);
		//			SHA256Hash hash = Laxkit.doSHA256Hash(text.getBytes());
		//			if (allow(hash, zeros)) {
		//				buf.writeObject(local);
		//				buf.writeObject(hash);
		//				buf.writeString(text);
		//				count++;
		//			}
		//		}

		Logger.debug(getIssuer(), this, "divide", "挖出的矿码统计是：%d", count);

		// 建立数据写入器，存取模式根据命令的“内存/磁盘”选定
		if (buf.size() > 0) {
			// 以添加方式写入缓存
			byte[] b = buf.effuse();
			FluxField field = writer.append(index, count, b, 0, b.length);
			// 如果为null，即写入失败
			if (field == null) {
				throw new FromTaskException("数据写入失败！");
			}
		}

		// 返回元数组字节长度
		return assemble();
	}

	/**
	 * 计算元数据字节数组长度
	 * @return 长整数
	 * @throws TaskException
	 */
	private long assemble() throws TaskException {
		// 生成元数据字节数组，返回字节长度
		byte[] b = effuse();
		return b.length;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.conduct.from.FromTask#effuse()
	 */
	@Override
	public byte[] effuse() throws TaskException {
		FluxArea area = createFluxArea();
		return area.build();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.conduct.from.FromTask#flushTo(java.io.File)
	 */
	@Override
	public long flushTo(File file) throws TaskException {
		byte[] b = effuse();
		return 	writeTo(file, false, b, 0, b.length);
	}

}