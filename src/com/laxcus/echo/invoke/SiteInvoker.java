/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoke;

import java.io.*;
import java.util.*;

import com.laxcus.command.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.*;
import com.laxcus.remote.client.echo.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.lock.*;
import com.laxcus.visit.*;

/**
 * 站点调用器。<br><br>
 * 
 * 站点调用器为异步操作提供基本的参数和接口，包括建立异步缓存，保存异步应答数据。<br>
 * 
 * 
 * 站点调用器中包括以下基本参数：<br>
 * 1. 调用器编号，由所属节点分配，保证在所属节点中唯一。<br>
 * 2. 预定义的缓存数目。<br>
 * 3. 缓存集合。<br>
 * 4. 异步数据受理器。这是一个接口，由托管站点调用器的管理池实现。
 * 当有异步应答数据时，通过“异步数据受理器”，转发通知站点调用器，完成任务分析和计算。<br><br>
 * 
 * 当预定义的缓存数量和缓存集合数目不一致时，存在两种可能：<br>
 * 1. 分配过程已经失败。<br>
 * 2. 还在分配过程中。<br>
 * 
 * 站点调用器不行串行化、克隆、可类化!
 * 
 * @author scott.liang
 * @version 1.3 7/23/2015
 * @since laxcus 1.0
 */
public class SiteInvoker extends MutexHandler implements Comparable<SiteInvoker> {
	
	/** 线程标识符，调用器启动时设置，调用器结束后失效 **/
	private volatile long threadId;

	/** 异步数据受理器 **/
	private EchoAcceptor acceptor;
	
	/** 接收的数据流量 **/
	private long receiveFlow;
	
	/** 发送的数据流量 **/
	private long sendFlow;
	
	/** 初始化时间，构造时生成，不能修改 **/
	private long initTime;

	/** 启动时间。在构造时生成，可以重置 **/
	private long launchTime;
	
	/** 调用器进入线程时间，由InvokerTrustor设置。**/
	private long threadStartTime;
	
	/** 统计线程的运行时处理时间 **/
	private long processTime;

	/** 调用器编号，在生存期内唯一，由所在节点分配。 **/
	private long invokerId;
	
	/** 快速投递编号 **/
	private int castIndex;
	
	/** 处理次数，迭代编号 **/
	private int iterateIndex;

	/** 预分配的缓存数目。如果defaultSize == buffers.size()，表示分配完毕。**/
	private int defaultSize;

	/** 回显缓存编号 -> 回显缓存 **/	
	private Map<java.lang.Integer, EchoBuffer> buffers = new TreeMap<java.lang.Integer, EchoBuffer>();
	
	/** 执行绑定或者关联操作，默认是假 **/
	private boolean shackle;

	/**
	 * 构造一个默认的站点调用器
	 */
	protected SiteInvoker() {
		super();
		// 初始流量为0
		receiveFlow = sendFlow = 0L;
		// 启动时间
		initTime = launchTime = System.currentTimeMillis();
		// 不定义缓存成员数目
		defaultSize = -1;
		// 不定义调用器编号
		invokerId = InvokerIdentity.INVALID;
		// 异步快速投递编号
		castIndex = 0;
		// 线程编号，默认-1。
		threadId = -1;
		// 迭代编号
		iterateIndex = 0;
		// 默认不绑定或者不进行关联操作
		setShackle(false);
		
		// 线程启动时间
		setThreadStartTime(System.currentTimeMillis());
		// 迭代状态下，一个调用器运行过程消耗的时间
		processTime = 0L;
	}

	/**
	 * 构造一个站点调用器，同时指定它的基本参数
	 * @param acceptor 回显工作受理器
	 * @param invokerId 调用器编号
	 * @param defaultSize 预定的缓存成员数
	 */
	protected SiteInvoker(EchoAcceptor acceptor, long invokerId, int defaultSize) {
		this();
		setEchoAcceptor(acceptor);
		setInvokerId(invokerId);
		setDefaultSize(defaultSize);
	}

	/**
	 * 构造一个站点调用器，同时指定它的基本参数，预定缓存成员是0
	 * @param acceptor 回显工作受理器
	 * @param invokerId 调用器编号
	 * @since 1.3
	 */
	protected SiteInvoker(EchoAcceptor acceptor, long invokerId) {
		this(acceptor, invokerId, 0);
	}

	/**
	 * 设置调用器线程标识符
	 * @param who 线程标识符
	 */
	public final void setThreadId(long who) {
		threadId = who;
	}

	/**
	 * 返回调用器线程标识符
	 * @return
	 */
	public final long getThreadId() {
		return threadId;
	}
	
	/**
	 * 设置调用器编号。小于0是错误
	 * @param who 调用器编号
	 */
	public final void setInvokerId(long who) {
		if (InvokerIdentity.isInvalid(who)) {
			throw new IllegalValueException("illegal invoker identity %d", who);
		}
		invokerId = who;
	}

	/**
	 * 返回调用器编号
	 * @return 调用器编号
	 */
	public final long getInvokerId() {
		return invokerId;
	}
	
	/**
	 * 生成一个快速投递序号
	 * @return 新的投递序号
	 */
	protected int doCastIndex() {
		return castIndex++;
	}
	
	/**
	 * 执行下一次迭代（launch/ending方法被调用一次）
	 */
	public void nextIterate() {
		iterateIndex++;
	}

	/**
	 * 返回迭代编号
	 * @return 整数
	 */
	public int getIterateIndex() {
		return iterateIndex;
	}

	/**
	 * 根据调用器编号，判断异步调用器处于活跃状态。<br>
	 * 调用器编号大于等于0即表示处于活跃状态，即没有被系统撤销释放。<br>
	 * 
	 * @return 返回真或者假
	 */
	public final boolean isAlive() {
		return InvokerIdentity.isValid(invokerId);
	}

	/**
	 * 设置异步数据受理器
	 * @param e 异步数据受理器实例
	 */
	public void setEchoAcceptor(EchoAcceptor e) {
		acceptor = e;
	}

	/**
	 * 返回异步数据受理器
	 * @return 异步数据受理器实例
	 */
	public EchoAcceptor getEchoAcceptor() {
		return acceptor;
	}
	
	/**
	 * 返回初始化时间
	 * @return 以毫秒为单位的时间
	 */
	public long getInitTime() {
		return initTime;
	}

	/**
	 * 返回开始时间，单位：毫秒
	 * @return 系统时间
	 */
	public long getLaunchTime() {
		return launchTime;
	}

	/**
	 * 重置启动时间
	 * @return 系统时间
	 */
	public void resetLaunchTime() {
		launchTime = System.currentTimeMillis();
	}

	/**
	 * 判断工作超时
	 * @param timeout 超时时间，单位：毫秒
	 * @return 返回真或者假
	 */
	public boolean isTimeout(long timeout) {
		return System.currentTimeMillis() - launchTime >= timeout;
	}

	/**
	 * 返回任务运行时间
	 * @return 调用器运行时间
	 */
	public long getRunTime() {
		return System.currentTimeMillis() - launchTime;
	}

	/**
	 * 设置本次调用器开始处理时间
	 * @param ms 毫秒
	 */
	public void setThreadStartTime(long ms) {
		threadStartTime = ms;
	}

	/**
	 * 返回本次调用器开始处理时间
	 * @return 毫秒
	 */
	public long getThreadStartTime() {
		return threadStartTime;
	}

	/**
	 * 返回本次调用器使用的处理时间
	 * @return 毫秒
	 */
	public long getThreadUsedTime() {
		return System.currentTimeMillis() - threadStartTime;
	}

	/**
	 * 增加处理时间
	 * @param ms 毫秒时间
	 */
	public void addProcessTime(long ms) {
		processTime += ms;
	}

	/**
	 * 返回全部处理时间
	 * @return 以毫秒为单位的时间
	 */
	public long getProcessTime() {
		return processTime;
	}

	/**
	 * 增加接收的数据流量，以字节为单位
	 * @param value 接收的数据流量
	 */
	public void addReceiveFlowSize(long value) {
		if (value < 0) {
			throw new IllegalValueException("illegal receive flow:%d", value);
		}
		receiveFlow += value;
	}

	/**
	 * 返回接收的数据流量
	 * @return 接收的数据流量
	 */
	public long getReceiveFlowSize() {
		return receiveFlow;
	}

	/**
	 * 增加发送的数据流量
	 * @param value 发送的数据流量
	 */
	public void addSendFlowSize(long value) {
		if (value < 0) {
			throw new IllegalValueException("illegal send flow:%d", value);
		}
		sendFlow += value;
	}

	/**
	 * 返回发送的数据流量，以字节计算
	 * @return 发送的数据流量
	 */
	public long getSendFlowSize() {
		return sendFlow;
	}

	/**
	 * 收集接收的数据尺寸。<br>
	 * 
	 * 注意：此处统计的数据，是正确接收数据，不是SOCKET数据。通常SOCKET数据要大于接收的数据。
	 */
	public void collectReceiveFlowSize() {
		for (EchoBuffer e : buffers.values()) {
			addReceiveFlowSize(e.getSeek());
		}
	}

	/**
	 * 设置回显缓存数目。必须大于等于0，小于0是错误。<br>
	 * 这个方法在预分配缓存时调用；或者分配过程中发生错误，为保证也实际缓存数一致时被修改。<br>
	 * 
	 * @param size 缓存数目
	 * @throws IllegalValueException - 当小于0时弹出错误
	 */
	public void setDefaultSize(int size) {
		if (size < 0) {
			throw new IllegalValueException("cannot be %d", size);
		}
		defaultSize = size;
	}

	/**
	 * 返回回显缓存数目
	 * @return 缓存数目
	 */
	public int getDefaultSize() {
		return defaultSize;
	}

	/**
	 * 返回全部缓存索引编号
	 * @return List<Integer>
	 */
	public final List<Integer> getEchoKeys() {
		return new ArrayList<Integer>(buffers.keySet());
	}

	/**
	 * 从缓存索引编号中，找到所在下标的索引编号
	 * @param index 编号数组下标
	 * @return 返回所在下标的编号
	 */
	public int findEchoKey(int index) {
		List<Integer> keys = getEchoKeys();
		if (index >= keys.size()) {
			return -1;
		}
		return keys.get(index);
	}

	/**
	 * 根据传入的参数，建立一个回显缓存，同时绑定到异步数据代理上。
	 * @param flag 回显标识
	 * @param ondisk 数据写入磁盘。如果参数是“真”，将在磁盘上建立文件，异步数据写入磁盘。
	 * @param cmd 操作命令
	 * @param hub 服务器地址。异步数据的产生来源地址。
	 * @return 成功返回“真”，否则“假”。
	 */
	public final boolean createBuffer(EchoFlag flag, boolean ondisk, Command cmd, Node hub) {
		// 这个参数必须有
		if (acceptor == null) {
			throw new NullPointerException();
		}
		// 检查调用器编号，不一致是错误
		if (flag.getInvokerId() != invokerId) {
			throw new IllegalValueException("cannot be match:%d,%d ", invokerId , flag.getInvokerId());
		}

		int index = flag.getIndex();
		EchoBuffer buff = buffers.get(index);
		boolean success = (buff == null);
		// 建立异步缓存，在异步缓存建立的同时，它绑定到“异步应答数据代理”上;
		if (success) {
			buff = new EchoBuffer(acceptor, flag, ondisk);
			// 检查绑定，如果没有绑定是错误
			if (!buff.isAttached()) {
				return false;
			}
			buff.setCommand(cmd);
			buff.setHub(hub);
			success = (buffers.put(index, buff) == null);
		}

		return success;
	}

	/**
	 * 根据回显标识和磁盘标识，建立一个回显缓存
	 * @param flag 回显标识
	 * @param ondisk 回显数据写入磁盘
	 * @return 成功返回真，否则假
	 */
	public boolean createBuffer(EchoFlag flag, boolean ondisk) {
		return createBuffer(flag, ondisk, null, null);
	}

	/**
	 * 建立回显缓存，指定参数
	 * @param flag 回显标识
	 * @param ondisk 回显数据写入磁盘
	 * @param cmd 操作命令
	 * @return 成功返回真，否则假
	 */
	public boolean createBuffer(EchoFlag flag, boolean ondisk, Command cmd) {
		return createBuffer(flag, ondisk, cmd, null);
	}

	/**
	 * 根据回显标识和命令，建立一个回显缓存，同时绑定到异步数据代理上。
	 * @param flag 回显标识
	 * @param command 操作命令
	 * @return 成功返回真，否则假
	 */
	public boolean createBuffer(EchoFlag flag, Command command) {
		return createBuffer(flag, false, command, null);
	}

	/**
	 * 根据回显标识，建立一个回显缓存，同时绑定到异步数据代理上。
	 * @param flag 回显标识
	 * @return 成功返回真，否则假
	 */
	public boolean createBuffer(EchoFlag flag) {
		return createBuffer(flag, false, null, null);
	}

	/**
	 * 根据回显标识，删除一个回显缓存
	 * @param flag 回显标识
	 * @return 成功返回“真”，否则“假”。
	 */
	public boolean removeBuffer(EchoFlag flag) {
		if (flag.getInvokerId() != invokerId) {
			return false;
		}
		// 从队列中删除
		EchoBuffer buff = buffers.remove(flag.getIndex());
		// 如果存在，销毁它
		boolean success = (buff != null);
		if (success) {
			// "push"线程，即ReplyReceiver没有退出时，延时
			while (buff.getPushThreads() > 0) {
				delay(500);
			}
			// 销毁任务
			buff.destroy();
		}
		return success;
	}

	/**
	 * 根据索引编号，使用默认调用器编号，删除一个回显缓存。
	 * @param index 回显标识里的索引编号
	 * @return 成功返回“真”，否则“假”。
	 */
	public boolean removeBuffer(int index) {
		return removeBuffer(new EchoFlag(invokerId, index));
	}

	/**
	 * 根据回显标识，找到它匹配的回显缓存
	 * @param flag 回显标识
	 * @return EchoBuffer实例，或者空指针
	 */
	public EchoBuffer findBuffer(EchoFlag flag) {
		if (flag.getInvokerId() != invokerId) {
			return null;
		}
		return buffers.get(flag.getIndex());
	}

	/**
	 * 根据索引编号，使用默认调用器编号，查找回显缓存
	 * @param index 回显标识里的索引编号
	 * @return 回显缓存实例
	 */
	public EchoBuffer findBuffer(int index) {
		return findBuffer(new EchoFlag(invokerId, index));
	}

	/**
	 * 根据索引编号，查找异步缓存的异步应答报头
	 * @param index 索引编号
	 * @return EchoHead实例
	 */
	public EchoHead findBufferHead(int index) {
		EchoBuffer buf = findBuffer(index);
		if (buf != null) {
			return buf.getHead();
		}
		return null;
	}

	/**
	 * 根据索引编号，查找异步缓存的异步应用报尾
	 * @param index 索引编号
	 * @return EchoTail实例
	 */
	public EchoTail findBufferTail(int index) {
		EchoBuffer buf = findBuffer(index);
		if (buf != null) {
			return buf.getTail();
		}
		return null;
	}

	/**
	 * 根据下标，返回缓存指向的服务器地址
	 * @param index 缓存索引编号
	 * @return Node实例
	 */
	public Node getBufferHub(int index) {
		EchoBuffer buf = findBuffer(index);
		if (buf != null) {
			return buf.getHub();
		}
		return null;
	}

	/**
	 * 返回全部回显缓存指向的服务器地址
	 * @return Node实例列表
	 */
	public List<Node> getBufferHubs() {
		ArrayList<Node> nodes = new ArrayList<Node>();
		for (EchoBuffer buf : buffers.values()) {
			nodes.add(buf.getHub());
		}
		return nodes;
	}

	/**
	 * 返回回显缓存数目
	 * @return 缓存数目
	 */
	public int getBufferSize() {
		return buffers.size();
	}

	/**
	 * 根据回显标识，找到对应的命令
	 * @param flag 回显标识
	 * @return 返回Command实例，没有返回空指针
	 */
	public Command findCommand(EchoFlag flag) {
		EchoBuffer buf = findBuffer(flag);
		if (buf != null) {
			return buf.getCommand();
		}
		return null;
	}

	/**
	 * 根据索引号，找到对应的命令
	 * @param index 索引号
	 * @return 返回Command实例，没有返回空指针
	 */
	public Command findCommand(int index) {
		return findCommand(new EchoFlag(invokerId, index));
	}
	
	/**
	 * 根据回显标识，找到对应的服务器地址
	 * @param flag 回显标识
	 * @return Node实例
	 */
	public Node findHub(EchoFlag flag) {
		EchoBuffer buf = findBuffer(flag);
		if (buf != null) {
			return buf.getHub();
		}
		return null;
	}

	/**
	 * 根据索引号，找到对应的命令
	 * @param index 索引号
	 * @return Node实例
	 */
	public Node findHub(int index) {
		return findHub(new EchoFlag(invokerId, index));
	}

	/**
	 * 根据回显标识，找到对应的磁盘文件名称
	 * @param flag 回显标识
	 * @return 成功返回文件名称，没有返回空指针
	 */
	public String findFilename(EchoFlag flag) {
		EchoBuffer buf = findBuffer(flag);
		if (buf != null) {
			return buf.getFilename();
		}
		return null;
	}

	/**
	 * 根据索引号和已经定义的调用器编号，找到对应的磁盘文件名称
	 * @param index 索引号
	 * @return 成功返回文件句柄，失败返回空指针
	 */
	public String findFilename(int index) {
		return findFilename(new EchoFlag(invokerId, index));
	}

	/**
	 * 根据回显标识，找到对应的磁盘文件
	 * @param flag 回显标识
	 * @return 成功返回文件句柄，失败返回空指针
	 */
	public File findFile(EchoFlag flag) {
		EchoBuffer buf = findBuffer(flag);
		if (buf != null) {
			return buf.getFile();
		}
		return null;
	}

	/**
	 * 根据索引号和已经定义的调用器编号，找到对应的磁盘文件
	 * @param index 索引号
	 * @return 成功返回文件句柄，失败返回空指针
	 */
	public File findFile(int index) {
		return findFile(new EchoFlag(invokerId, index));
	}

	/**
	 * 返回全部缓存文件
	 * @return File实例数目
	 */
	public File[] getAllFiles() {
		File[] files = new File[buffers.size()];
		int index = 0;
		for (EchoBuffer buf : buffers.values()) {
			files[index++] = buf.getFile();
		}
		return files;
	}

	/**
	 * 重置回显缓存空间。<br><br>
	 * 
	 * 首先销毁旧的回显缓存，然后确定新的缓存数目。<br>
	 * 注意：这里只预定义后续需要的缓存数目，不分配缓存，实际的回显缓存通过"createBuffer"方法建立。
	 * 
	 * @param size 新的缓存数目。不能小于1
	 * @throws IllegalValueException 
	 */
	public void resetAllBuffers(int size) {
		if (size < 1) {
			throw new IllegalValueException("illegal buffer size:%d", size);
		}
		// 销毁全部资源，比如磁盘文件。防止再分配的时候发生冲突。
		for(EchoBuffer buff : buffers.values()) {
			// "push"线程，即ReplyReceiver没有退出时，延时
			while (buff.getPushThreads() > 0) {
				delay(500);
			}
			buff.destroy();
		}
		// 清除缓存
		buffers.clear();
		// 预定义新缓存
		setDefaultSize(size);
	}

	/**
	 * 清除全部回显缓存，预定义缓存数目是0
	 */
	public void clearAllBuffers() {
		// 销毁全部缓存
		for (EchoBuffer buff : buffers.values()) {
			// "push"线程，即ReplyReceiver没有退出时，延时
			while (buff.getPushThreads() > 0) {
				delay(500);
			}
			// 销毁
			buff.destroy();
		}
		// 清除缓存
		buffers.clear();
		// 预定义缓存数目是0
		setDefaultSize(0);
	}

//	/**
//	 * 重置回显缓存空间。<br><br>
//	 * 
//	 * 首先销毁旧的回显缓存，然后确定新的缓存数目。<br>
//	 * 注意：这里只预定义后续需要的缓存数目，不分配缓存，实际的回显缓存通过"createBuffer"方法建立。
//	 * 
//	 * @param size 新的缓存数目。不能小于1
//	 * @throws IllegalValueException 
//	 */
//	public void resetAllBuffers(int size) {
//		if (size < 1) {
//			throw new IllegalValueException("illegal buffer size:%d", size);
//		}
//		// 清除缓存
//		buffers.clear();
//		// 预定义新缓存
//		setDefaultSize(size);
//	}
//
//	/**
//	 * 清除全部回显缓存，预定义缓存数目是0
//	 */
//	public void clearAllBuffers() {
//		// 清除缓存
//		buffers.clear();
//		// 预定义缓存数目是0
//		setDefaultSize(0);
//	}
	
	/**
	 * 判断全部异步应答数据接收完成。
	 * @return 如果是返回“真”，否则“假”。
	 */
	public final boolean isCompleted() {
		// 预分配参数和缓存数不一致时，不是正常状态的完成
		if (defaultSize != buffers.size()) {
			return false;
		}
		int count = 0;
		for (EchoBuffer e : buffers.values()) {
			if (e.isCompleted()) {
				count++;
			}
		}
		return count == defaultSize;
	}

	/**
	 * 判断全部异步应答数据接收完成，并且“存在错误”。
	 * @return 如果是返回“真”，否则“假”。
	 */
	public final boolean isFaultCompleted() {
		// 预分配数和缓存数目一致时，不是故障状态的完成。
		if (defaultSize != buffers.size()) {
			return false;
		}
		int count = 0;
		for (EchoBuffer e : buffers.values()) {
			if (e.isFaultCompleted()) {
				count++;
			}
		}
		return count == defaultSize;
	}

	/**
	 * 判断某一个异步应答接收完成，并且是错误
	 * @param index 缓存下标
	 * @return 如果是返回“真”，否则“假”。
	 */
	public final boolean isFaultCompleted(int index) {
		EchoBuffer buf = findBuffer(index);
		return buf != null && buf.isFaultCompleted();
	}

	/**
	 * 判断全部异步应答数据接收完成，并且“全部成功”。
	 * @return 如果是返回“真”，否则“假”。
	 */
	public final boolean isSuccessCompleted() {
		// 预分配参数和实际值必须一致
		if (defaultSize != buffers.size()) {
			return false;
		}
		int count = 0;
		for (EchoBuffer buf : buffers.values()) {
			if (buf.isSuccessCompleted()) {
				count++;
			}
		}
		return count == defaultSize;
	}

	/**
	 * 判断某一个缓存异步应答数据接收完成，并且“成功”时。
	 * @param index 索引编号
	 * @return 返回真或者假
	 */
	public final boolean isSuccessCompleted(int index) {
		EchoBuffer e = findBuffer(index);
		return e != null && e.isSuccessCompleted();
	}

	/**
	 * 判断全部异步应答数据是可以“对象化”的数据流
	 * @return 如果是返回“真”，否则“假”。
	 */
	public final boolean isObjectable() {
		// 预分配参数和实际值必须一致
		if (defaultSize != buffers.size()) {
			return false;
		}
		int count = 0;
		for (EchoBuffer e : buffers.values()) {
			if (e.isObjectable()) {
				count++;
			}
		}
		return count == defaultSize;
	}

	/**
	 * 判断对象是对象
	 * @param index 索引编号
	 * @return 返回真或者假
	 */
	public final boolean isObjectable(int index) {
		EchoBuffer e = findBuffer(index);
		return e != null && e.isObjectable();
	}

	/**
	 * 异步应答数据是“成功”和“可对象化”的。
	 * @return 如果是返回“真”，否则“假”。
	 */
	public final boolean isSuccessObjectable() {
		return isObjectable() && isSuccessCompleted();
	}

	/**
	 * 判断某一个子实例的必须应答是成功的“可对象化”实例。
	 * @param index 索引编号
	 * @return 返回真或者假
	 */
	public final boolean isSuccessObjectable(int index) {
		EchoBuffer e = findBuffer(index);
		return e != null && e.isSuccessObjectable();
	}

	/**
	 * 判断全部异步应答数据在磁盘文件中。这个操作在全部异步应答数据完成后执行。
	 * @return 返回真或者假
	 */
	public boolean isEchoFiles() {
		// 没有完成返回“假”。
		if (!isCompleted()) {
			return false;
		}

		List<Integer> keys = getEchoKeys();
		int count = 0;
		for (int index : keys) {
			EchoBuffer buf = findBuffer(index);
			if (buf.isDisk()) count++;
		}

		return (keys.size() > 0 && keys.size() == count);
	}

	/**
	 * 返回异步应答数据容量尺寸
	 * @return long
	 */
	public long getEchoCapacity() {
		long length = 0;

		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			EchoBuffer buf = findBuffer(index);
			if (buf.isDisk()) {
				File file = buf.getFile();
				length += file.length();
			} else {
				length += buf.getMemorySize();
			}
		}

		return length;
	}

	/**
	 * 收集全部异步应答数据并且输出。此操作在判断收到全部应答数据后进行。
	 * @return byte数组
	 */
	public byte[] collect() {
		long size = getEchoCapacity();
		// 不能超过最大限值
		if (size >= 0x7FFFFFFF) {
			throw new OutOfMemoryError();
		}

		byte[] array = new byte[(int) size];
		int seek = 0;

		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			EchoBuffer buf = findBuffer(index);
			// 判断数据在磁盘文件
			if (buf.isDisk()) {
				File file = buf.getFile();
				int length = (int) file.length();
				byte[] b = new byte[1024];
				try {
					FileInputStream reader = new FileInputStream(file);
					// 文件逐段读取
					for (int off = 0; off < length;) {
						int limit = Laxkit.limit(off, length, b.length);
						int len = reader.read(b, 0, limit);
						// 出错
						if (limit != len) {
							throw new IOException(limit + " != " + len);
						}
						System.arraycopy(b, 0, array, seek, len);
						seek += len;
						off += len;
					}
					reader.close();
				} catch (IOException e) {
					Logger.error(e);
					throw new EchoException(e);
				}
			} else {
				byte[] b = buf.getMemory();
				System.arraycopy(b, 0, array, seek, b.length);
				seek += b.length;
			}
		}

		Logger.debug(this, "collect", "byte array size is %d", array.length);

		return array;
	}

	/**
	 * 取出指定回显标识的异步缓存，生成它的类对象。<br><br>
	 * 
	 * 操作成功要满足两个条件：<br>
	 * 1. 缓存数据必须是“类对象”属性<br>
	 * 2. 必须收到全部应答数据。<br><br>
	 * 
	 * 特别注意：返回结果是一个“Object”，但是它可能属于对象数组，
	 * 如用“(Integer[])object”强制解开。这种处理由通信双方来判定<br>
	 * 
	 * 
	 * @param flag 回显标识
	 * @return 类对象（存在对象数组的可能）
	 * @throws VisitException
	 */
	public Object getObject(EchoFlag flag) throws VisitException {
		EchoBuffer buff = findBuffer(flag);
		if (buff == null) {
			throw new VisitException("not found buffer! %s", flag);
		} else if (!buff.isSuccessCompleted()) {
			throw new VisitException("not completed!");
		} else if (!buff.isObjectable()) {
			throw new VisitException("cannot be object");
		}

		byte[] data = null;

		// 从磁盘或者内存中读取数据
		if (buff.isDisk()) {
			File file = buff.getFile();
			try {
				data = new byte[(int) file.length()];
				FileInputStream in = new FileInputStream(file);
				int len = in.read(data);
				in.close();
				if (len != data.length) {
					throw new VisitException("%d != %d", data.length, len);
				}
			} catch (IOException e) {
				throw new VisitException(e);
			} catch (Throwable e) {
				throw new VisitException(e);
			}
		} else {
			data = buff.getMemory();
		}

		// 解析数据
		PatternExtractor reply = null;
		try {
			reply = PatternExtractor.resolve(data);
		} catch (Throwable e) {
			Logger.fatal(e);
			throw new VisitException(e);
		}
		if (reply == null) {
			throw new VisitException("reply is null pointer!");
		}
		// 如果有错误，弹出异常。
		if (reply.getThrowable() != null) {
			throw new VisitException(reply.getThrowText());
		}

		// 返回对象
		return reply.getObject();
	}

	/**
	 * 根据索引号，找到对象的类对象。
	 * 
	 * @param index 指定下标处的异步缓存
	 * @return 返回对应对象，没有是空指针
	 * @throws VisitException
	 */
	public Object getObject(int index) throws VisitException {
		EchoFlag flag = new EchoFlag(invokerId, index);
		return getObject(flag);
	}

	/**
	 * 解析全部异步缓存，返回它们全部的类对象数组。
	 * 成功返回的前提是异步应答全部成功，并且全部是类对象。
	 * 返回所有异步缓存的类对象。异步缓存数和类对象数组是一致的。
	 * 
	 * @return 对象数组
	 * @throws VisitException
	 */
	public Object[] getObjects() throws VisitException {
		// 在全部成功的情况才允许解析
		if (!(isObjectable() && isSuccessCompleted())) {
			return null;
		}

		List<Integer> keys = getEchoKeys();
		int size = keys.size();
		// 生成数据
		Object[] array = new Object[size];
		// 取对象
		for (int n = 0; n < size; n++) {
			int index = keys.get(n);
			array[n] = getObject(index);
		}
		return array;
	}

	/**
	 * 根据类型返回对应的类实例
	 * @param <T> 泛式类型
	 * @param clazz 指定类
	 * @param flag 回显标识
	 * @return 实际类实例
	 * @throws VisitException
	 */
	@SuppressWarnings("unchecked")
	public <T> T getObject(Class<T> clazz, EchoFlag flag) throws VisitException {
		Object obj = getObject(flag);
		if (obj == null) {
			return null;
		}
		return (T) obj;
	}

	/**
	 * 返回对应的类实例
	 * @param <T> 泛式类型
	 * @param clazz 指定类
	 * @param index 索引编号
	 * @return 实例类实例
	 * @throws VisitException
	 */
	public <T> T getObject(Class<T> clazz, int index) throws VisitException {
		EchoFlag flag = new EchoFlag(invokerId, index);
		return getObject(clazz, flag);
	}

	/**
	 * 比较两个站点调用器一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != getClass()) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((SiteInvoker) that) == 0;
	}

	/**
	 * 站点调用器的散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) (invokerId >>> 32 ^ invokerId);
	}

	/**
	 * 根据调用器编号比较它们排列顺序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SiteInvoker that) {
		// 空值在前
		if(that == null) {
			return 1;
		}
		return Laxkit.compareTo(invokerId, that.invokerId);
	}
	
	/**
	 * 要求执行绑定或者关联操作。
	 * 这个方法由子类定义。
	 * @param b
	 */
	protected void setShackle(boolean b) {
		shackle = b;
	}
	
	/**
	 * 判断要求执行绑定或者关联操作
	 * @return 返回真或者假
	 */
	public boolean isShackle() {
		return shackle;
	}
	
	/**
	 * 执行某此绑定或者关联操作。<br><br>
	 * 在“launch/ending”方法前执行，被InvokerTrustor调用。<br>
	 * 本处是空方法，子类如果有需求，继承这个方法实现个性业务逻辑。<br>
	 * @since 1.3
	 */
	public void shackle() {

	}

	/**
	 * 解除绑定或者关联操作。<br>
	 * <br>
	 * 在“launch/ending”方法结束后执行，被InvokerTrustor调用。<br>
	 * 本处是空方法，子类如果有需求，继承这个方法实现私有业务逻辑。<br>
	 * 通常“shackle”和“unshackle”是匹配设置。<br>
	 * @since 1.3
	 */
	public void unshackle() {

	}
	
	/**
	 * 返回当前仍然处于运行状态，执行“push”方法的线程，即ReplyReceiver线程统计数。
	 * @return 整数，没有是0
	 */
	public int getPushThreads() {
		int count = 0;
		for (EchoBuffer buff : buffers.values()) {
			count += buff.getPushThreads();
		}
		return count;
	}

	/**
	 * 销毁全部资源数据 <br><br>
	 * 
	 * 特别注意事项：<br>
	 * 1. “destroy”方法在InvokerPool中，被“InvokerPool”以锁定资源的方式销毁，所以“destroy”方法中不能调用“InvokerPool”与锁定相关的方法，否则会造成死锁。<br>
	 * 2. 子类实现自己的“destroy”方法时，必须加入“super.destroy();”，否则上级资源数据不能被释放。<br>
	 * 3. 子类“destroy”释放数据的流程是：子类先释放自己的资源，再调用“super.destroy();”，去释放父类资源数据。<br>
	 */
	public void destroy() {
		// 激活状态
		if (isAlive()) {
			Logger.debug(this, "destroy", "invoker id:%d, runtime:%d ms, receive flow:%d, send flow:%d",
					invokerId, getRunTime(), getReceiveFlowSize(), getSendFlowSize());
		}

		// 施放资源
		if (buffers.size() > 0) {
			for (EchoBuffer buff : buffers.values()) {
				// "push"线程，即ReplyReceiver没有退出时，延时
				while (buff.getPushThreads() > 0) {
					delay(500);
				}
				// 销毁
				buff.destroy();
			}
			buffers.clear();
		}
		// 重置标记
		invokerId = InvokerIdentity.INVALID;
		defaultSize = -1;
		if (acceptor != null) {
			acceptor = null;
		}
	}

	/**
	 * 给全部异步缓存设置超时参数
	 */
	public void doTimeoutFault() {
		for (EchoBuffer buf : buffers.values()) {
			EchoHead head = new EchoHead(new EchoCode(Major.FAULTED, Minor.INVOKER_TIMEOUT));
			EchoTail tail = new EchoTail(false, -1, 0);
			buf.start(head);
			buf.stop(tail);
		}
	}
	
	/**
	 * 读出磁盘文件数据
	 * @param file 磁盘文件
	 * @return 返回字节数组，失败返回空指针
	 */
	protected byte[] readContent(File file) {
		// 判断有效
		boolean success = (file.exists() && file.isFile());
		if (!success) {
			return null;
		}
		// 判断文件长度
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
	protected final boolean writeContent(File file, boolean append, byte[] b) {
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
	 * 数据写入磁盘，如果是旧文件，覆盖它！
	 * 
	 * @param file 磁盘文件
	 * @param b 字节数组
	 * @return 成功返回真，否则假
	 */
	protected final boolean writeContent(File file,byte[] b) {
		return writeContent(file, false, b);
	}

	/**
	 * 移动磁盘文件
	 * @param source 源文件
	 * @param target 临时目标文件
	 * @return 成功返回真，否则假
	 */
	protected boolean moveTo(File source, File target) {
		// 如果是空指针，忽略它
		if (source == null) {
			Logger.error(this, "moveTo", "source file is null pointer!");
			return false;
		}
		
		long seek = 0L;
		long length = source.length();
		byte[] b = new byte[10240];

		try {
			FileInputStream in = new FileInputStream(source);
			FileOutputStream out = new FileOutputStream(target);
			while (seek < length) {
				// 从文件中读数据
				int len = in.read(b, 0, b.length);
				if (len < 1) break;
				// 写数据到目录文件
				out.write(b, 0, len);
				seek += len;
			}
			in.close();
			out.close();
		} catch (IOException e) {
			Logger.error(e);
		}

		// 判断成功
		boolean success = (seek == length);
		
		Logger.note(this, "moveTo", success, "%s # %d MoveTo %s # %d",
				source, seek, target, length);

		// 如果写入不成功，尝试删除这个文件
		if (!success) {
			target.delete();
		}

		return success;
	}
	
	/**
	 * 释放资源
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		destroy();
	}

}