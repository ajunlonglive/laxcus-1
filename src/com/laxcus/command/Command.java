/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command;

import java.io.*;

import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 命令。<br><br>
 * 
 * 命令是LAXCUS大数据操作系统实现分布处理的基本手段。节点之间的所有操作，以及节点内部的部分操作，都是通过命令完成。<br>
 * 
 * 
 * 
 * LAXCUS集群的命令包括：<br>
 * <1> CONDUCT命令（数据计算）。<br>
 * <2> ESTABLISH命令（数据构建）。<br>
 * <3> 系统管理员命令。<br>
 * <4> 用户管理命令。<br>
 * <5> 节点间检查、维护、资源调配命令（这类命令数量巨大）。<br>
 *  
 * <6> SQL数据库管理命令。<br>
 * <7> SQL数据表管理命令。<br>
 * <8> SQL账号管理命令（CREATE USER、DROP USER、ALTER USER）。<br>
 * <9> SQL操作权限管理命令（GRANT、REVOKE 授权/解除授权）。<br>
 * <10> SQL数据操纵命令（SELECT、DELETE、UPDATE、INSERT/INJECT）。<br>
 * <11> 数据管理、维护命令（区别与SQL标准命令，多且杂）。<br><br>
 * 
 * LAXCUS命令采用异步处理模式。即首先由客户机向服务器投递命令，服务器接受后，客户机关闭网络连接，等待服务器应答。服务器按照命令要求执行工作，完成之后，根据命令中提供的“命令来源地址（Cabin）”，与客户机再次连接，并发送反馈数据。在客户机关闭连接后，服务器发起连接前，它们之间不发生连接通信和数据传输。<br><br>
 *
 * 以上是LAXCUS命令的异步操作流程，相比同步操作，异步操作有以下优势：<br>
 * 1. 节约计算机和网络通信资源 <br>
 * 2. 方便调节并行计算任务数目 <br>
 * 3. 方便控制和避免超载现象 <br>
 * 4. 提高集群处理能力和承载能力 <br><br>
 * 
 * 
 * 其它说明：<br>
 * 1. 命令来源地址“source”由客户机在投递命令前设置，服务器收到后，根据这个地址判断接受或者拒绝。完成处理后，根据这个地址向客户机反馈结果。<br>
 * 2. 如果命令中的“direct”参数是“真”，表示不需要服务器反馈应答，服务器在处理结束后直接释放即可。<br>
 * 3. 如果命令中的“memory”参数是“真”，表示命令将以内存为中间存取介质进行数据处理。在实际应用中，受到计算机集群软硬资源的限制，不能确定每个命令一定可以实现“内存存取”。<br>
 * 4. 命令优先级“priority”代表命令权重，默认是0。超过0，需要优先处理。“swift”优先级是1。命令管理池会识别这个参数，走快速处理通道。优先级越高会更快处理。<br>
 * 5. 在分布任务组件中，用户可以生成命令。但是一些主要参数设置了安全管理，见“check”方法。如果确实有需要，在分布任务组件中*.policy文件中打开这项检查。格式如：permission com.laxcus.command.CommandPermission   "set.Memory"; （开放内存检查）。或者：permission com.laxcus.command.CommandPermission   "set.*"; （开放全部选项检查）
 * 
 * @author scott.liang
 * @version 1.52 8/12/2016
 * @since laxcus 1.0
 */
public abstract class Command implements Classable, Serializable, Cloneable, Comparable<Command> {

	private static final long serialVersionUID = -7016692030305942308L;
	
	/** 版本号，发送端设置，接收端判断。通过版本号，接收端决定接收发送端发送的哪些数据内容 **/
	private int version;

	/** 命令发起人签名。如果是系统命令，这个参数是空值；如果是FRONT站点发出，账号持有人必须设置自己的用户签名。 **/
	private Siger issuer;

	/** 命令原语。如果是站点间命令，这个参数是空值；如果是FRONT站点发出，设置原语。 **/
	private String primitive;

	/** 命令来源回显地址。说明命令来源，由客户机设置，被服务器使用。服务器根据这个地址向客户机反馈结果。 **/
	private Cabin source;

	/** 超时时间。单位：毫秒，默认是-1，无超时限制。由客户端设置，与命令来源地址配合，被服务端使用。达到超时时间，服务端将删除命令，并向客户端发出一个超时的应答。 **/
	private long timeout;

	/** 单向处理命令。单向处理命令被客户端投递给服务端后，不需要服务端反馈应答，默认是“假”。 **/
	private boolean direct;

	/** 内存处理模式，默认是“假”。此参数为“真”后，分布数据处理工作将以内存为介质实施数据存取。**/
	private boolean memory;

	/** 命令优先级（权重）。大于0时，系统需要优先处理。默认优先级是0。**/
	private byte priority;

	/** 记录命令，用在Tigger.command方法中，默认是true **/
	private boolean tigger;

	/** 命令建立时间，是本地参数，不参与串行化和可类化。**/
	private transient long createTime;

	/** 关联编号。当前命令做为一个新命令，需要与另一个调用器实现关联，这个编号是它们关联扭带。只在本地使用，不参与串行化和可类化 **/
	private transient long relateId;

	/** 操作这个命令的调用器编号，在分配调用器时设置。只在本地存在且有效，不参与串行化和可类化 **/
	private transient long localId;

	/** 播放声音，这个只对图形界面有效，字符界面和云端无效，不做可类化处理 **/
	private transient boolean sound;

	/**
	 * 将命令参数写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 * @since 1.1
	 */
	@Override
	public int build(ClassWriter w) {
		final int size = w.size();

		// 命令版本号
		ClassWriter writer = new ClassWriter();
		writer.writeInt(version);
		// 命令发起人签名
		writer.writeInstance(issuer);
		// 命令来源地址，命令绑定的调用器监听地址
		writer.writeInstance(source);
		// 命令原语
		writer.writeString(primitive);
		// 超时时间
		writer.writeLong(timeout);
		// 单向/双向命令
		writer.writeBoolean(direct);
		// 内存处理模式
		writer.writeBoolean(memory);
		// 优先级
		writer.write(priority);
		// 记录命令
		writer.writeBoolean(tigger);
		// 调用子类接口，将子类信息写入可类化存储器
		buildSuffix(writer);

		// Command字节流写入可类化存储器
		byte[] b = writer.effuse();
		w.writeInt(b.length);
		w.write(b);

		// 返回写入的数据长度
		return w.size() - size;
	}

	/**
	 * 从可类化读取器中解析命令参数。
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 * @since 1.1
	 */
	@Override
	public int resolve(ClassReader r) {
		final int seek = r.getSeek();

		// 从可类化读取器中读取Command字节流
		int len = r.readInt();
		byte[] b = r.read(len);

		ClassReader reader = new ClassReader(b);
		// 命令版本号
		version = reader.readInt();
		// 命令发起人签名
		issuer = reader.readInstance(Siger.class);
		// 命令来源地址
		source = reader.readInstance(Cabin.class);
		// 命令原语
		primitive = reader.readString();
		// 超时时间
		timeout = reader.readLong();
		// 单向/双向命令
		direct = reader.readBoolean();
		// 内存处理模式
		memory = reader.readBoolean();
		// 优先级
		priority = reader.read();
		// 记录命令
		tigger = reader.readBoolean();
		// 从可类化读取器中解析子类信息
		resolveSuffix(reader);

		// 返回读取的数据长度
		return r.getSeek() - seek;
	}

	//	/**
	//	 * 将命令参数写入可类化存储器
	//	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	//	 * @since 1.1
	//	 */
	//	@Override
	//	public int build(ClassWriter writer) {
	//		final int size = writer.size();
	//		
	//		// 命令版本号
	//		writer.writeShort(version);
	//		// 命令发起人签名
	//		writer.writeInstance(issuer);
	//		// 命令来源地址，命令绑定的调用器监听地址
	//		writer.writeInstance(source);
	//		// 命令原语
	//		writer.writeString(primitive);
	//		// 超时时间
	//		writer.writeLong(timeout);
	//		// 单向/双向命令
	//		writer.writeBoolean(direct);
	//		// 内存处理模式
	//		writer.writeBoolean(memory);
	//		// 优先级
	//		writer.write(priority);
	//		// 记录命令
	//		writer.writeBoolean(tigger);
	//		// 调用子类接口，将子类信息写入可类化存储器
	//		buildSuffix(writer);
	//		// 返回写入的数据长度
	//		return writer.size() - size;
	//	}
	//
	//	/**
	//	 * 从可类化读取器中解析命令参数。
	//	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	//	 * @since 1.1
	//	 */
	//	@Override
	//	public int resolve(ClassReader reader) {
	//		final int seek = reader.getSeek();
	//		// 命令版本号
	//		version = reader.readShort();
	//		// 命令发起人签名
	//		issuer = reader.readInstance(Siger.class);
	//		// 命令来源地址
	//		source = reader.readInstance(Cabin.class);
	//		// 命令原语
	//		primitive = reader.readString();
	//		// 超时时间
	//		timeout = reader.readLong();
	//		// 单向/双向命令
	//		direct = reader.readBoolean();
	//		// 内存处理模式
	//		memory = reader.readBoolean();
	//		// 优先级
	//		priority = reader.read();
	//		// 记录命令
	//		tigger = reader.readBoolean();
	//		// 从可类化读取器中解析子类信息
	//		resolveSuffix(reader);
	//		// 返回读取的数据长度
	//		return reader.getSeek() - seek;
	//	}

	/**
	 * 构造一个默认的操作命令。
	 */
	protected Command() {
		super();
		version = 0; // 版本号默认是0
		timeout = -1L; 		// 默认无超时限制
		direct = false;		// 默认需要回馈应答
		memory = true;		// 为加快处理速度，命令默认采用内存处理。FRONT/WATCH站点在发送前根据用户需求调整。
		priority = CommandPriority.NONE;	// 默认无优先级
		tigger = true; // 默认是记录命令
		createTime = System.currentTimeMillis();
		// 默认是无效编号
		relateId = InvokerIdentity.INVALID;
		// 默认是无效编号
		localId = InvokerIdentity.INVALID;
		// 播放声音，默认是真
		sound = true;
	}

	/**
	 * 用传入对象给当前参数赋值，生成新的实例
	 * @param that 命令对象
	 */
	protected Command(Command that) {
		this();
		// 版本号
		version = that.version;
		// 用户签名
		if (that.issuer != null) {
			issuer = that.issuer.duplicate();
		}
		// 命令回显地址
		if (that.source != null) {
			source = that.source.duplicate();
		}
		primitive = that.primitive;
		timeout = that.timeout;
		direct = that.direct;
		memory = that.memory;
		priority = that.priority;
		tigger = that.tigger;
		createTime = that.createTime;
		relateId = that.relateId;
		localId = that.localId;
		sound = that.sound;
	}


	/**
	 * 设置版本号，短整型。子类的"buildSuffix/resolveSuffix"方法根据版本号决定生成或者解析的数字内容
	 * @param v 版本号
	 */
	public void setVersion(int v) {
		version = v;
	}

	/**
	 * 返回版本号。
	 * @return 版本号
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * 进行安全许可检查
	 * @param method 被调用的命令方法名
	 */
	private void check(String method) {
		// 安全检查
		SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
			String name = String.format("using.%s", method);
			sm.checkPermission(new CommandPermission(name));
		}
	}

	/**
	 * 设置命令的语句描述。原语允许空值。
	 * @param e 命令原语文本
	 */
	public void setPrimitive(String e) {
		check("Primitive");
		primitive = e;
	}

	/**
	 * 返回命令的语句描述
	 * @return 命令原语文本
	 */
	public String getPrimitive() {
		return primitive;
	}

	/**
	 * 设置为单向处理命令。<br>
	 * 单向处理命令不需要服务反馈应答，通常的命令都要求服务器返回处理结果。
	 * 
	 * @param b 单向处理
	 */
	public void setDirect(boolean b) {
		direct = b;
	}

	/**
	 * 要求命令反馈结果
	 * 
	 * @param b 是或者否
	 */
	public void setReply(boolean b) {
		setDirect(!b);
	}

	/**
	 * 判断是单向处理命令
	 * @return 返回真或者假。
	 */
	public boolean isDirect() {
		return direct;
	}

	/**
	 * 判断不是单向处理命令
	 * @return 返回真或者假。
	 */
	public boolean isReply() {
		return !direct;
	}

	/**
	 * 设置命令为内存存取模式。<br><br>
	 * 
	 * 用户设置命令为内存存取模式后，在实施过程中，仍然需要根据当时系统资源状况来判断和决定。也就是说，虽然用户命令是内存存取模式，也不一定全部保证分布处理都是内存存取模式。<br><br>
	 * 
	 * 2.0版本的内存分布计算取消了预约操作，统一采用直接请求方式。主要原因有3点：1.当系统总体资源达不到用户请求要求时，内存计算将永远无法实施，最后达到超时后撤销，若不设置超时，则永远滞留在集群中。2.当系统资源不足时，预约要长时间等待，可能比直接请求效率还低。3.简化设计。<br><br>
	 * 
	 * <br>
	 * @param b 内存存取模式
	 */
	public void setMemory(boolean b) {
		check("Memory");
		// 保存参数
		memory = b;
	}

	/**
	 * 设置命令为硬盘模式
	 * @param b 硬盘存取模式
	 */
	public void setDisk(boolean b){
		setMemory(!b);
	}

	/**
	 * 判断命令选用内存做为中间存取介质。<br>
	 * @return 返回真或者假。
	 */
	public boolean isMemory() {
		return memory;
	}

	/**
	 * 判断命令选用磁盘做为中间存取介质
	 * @return 返回真或者假
	 */
	public boolean isDisk() {
		return !memory;
	}

	/**
	 * 设置命令优先级
	 * 这个标识只允许系统命令使用，普通用户禁止使用。当这个参数为真时，服务端在分配异步调用器时，将跳过资源检查，直接分配交给异步调用器处理。<br>
	 * <b>特别注意：</b>此参数只为应对加急情况，或者能够快速得到结果的命令。若这类命令太多将影响节点运行稳定性。
	 * @param no 优先级编号
	 */
	public void setPriority(byte no) {
		check("Priority");
		// 判断合法
		if (!CommandPriority.isPriority(no)) {
			throw new IllegalValueException("illegal priority:%d", no);
		}
		priority = no;
	}

	/**
	 * 返回命令优先级。见CommandPriority定义
	 * @return 优先级编号
	 */
	public byte getPriority(){
		return priority;
	}

	/**
	 * 设置为快速处理 <br><br>
	 * 
	 * 当这个参数为真时，服务器将优先处理它。<br><br>
	 * 
	 * <b>特别注意：</b>此参数只为加急情况，或者能够快速得到结果的命令。
	 * 
	 * @param b 快速处理标记
	 */
	public void setQuick(boolean b) {
		setPriority(b ? CommandPriority.QUICK
				: CommandPriority.NONE);
	}

	/**
	 * 判断是快速处理。优先级大于0即是快速处理。
	 * @return 返回真或者假。
	 */
	public boolean isQuick() {
		return CommandPriority.isQuick(priority);
	}

	/**
	 * 设置为极速处理。<br>
	 * 针对不允许延时处理的命令，在CommandPool/InvokerPool，将跳过资源检查，立即处理。<br>
	 * 这种命令只限系统内部使用。
	 * 
	 * @param b 极速处理
	 */
	public void setFast(boolean b) {
		setPriority(b ? CommandPriority.FAST
				: CommandPriority.NONE);
	}

	/**
	 * 判断是极速处理命令
	 * @return 返回真或者假
	 */
	public boolean isFast() {
		return CommandPriority.isFast(priority);
	}

	/**
	 * 判断是系统级别的极速处理命令，条件：<br>
	 * 1. 是极速处理 <br>
	 * 2. 来自非FRONT节点<br>
	 * @return 返回真或者假
	 */
	public boolean isSystemFast() {
		Node e = getSourceSite();
		return (e != null && !e.isFront() && isFast());
	}

	/**
	 * 设置记录命令
	 * @param b 真或者假
	 */
	public void setTigger(boolean b) {
		tigger = b;
	}

	/**
	 * 判断记录命令
	 * @return 返回真或者假
	 */
	public boolean isTigger() {
		return tigger;
	}

	/**
	 * 设置命令来源地址。<br>
	 * 命令来源地址用于异步命令中，指示命令发起的源头。命令来源地址由命令的请求端设置，被服务端调用。服务器根据这个地址，将异步应答数据返回给请求端。<br>
	 * 命令来源地址被设置后，表示将执行异步处理操作。
	 * @param e 命令来源地址
	 */
	public void setSource(Cabin e) {
		check("Source");
		source = e;
	}

	/**
	 * 返回命令来源地址
	 * @return 命令来源地址实例
	 */
	public final Cabin getSource() {
		return source;
	}

	/**
	 * 返回命令所在站点地址
	 * @return Node实例，或者空值
	 */
	public final Node getSourceSite() {
		if (source == null) {
			return null;
		}
		return source.getNode();
	}

	/**
	 * 设置命令发起人签名<br>
	 * 如果命令由FRONT站点发出，是账号持有人的用户签名；如果是站点间的交互命令，这是空值。
	 * 通过发起人签名，可以判断命令的来源，实现诸如云计算“计费”这样的需求。
	 * @param e Siger实例
	 */
	public void setIssuer(Siger e) {
		check("Issuer");
		issuer = e;
	}

	/**
	 * 返回命令发起人签名
	 * @return Siger实例
	 */
	public final Siger getIssuer() {
		return issuer;
	}

	/**
	 * 判断有命令发起人签名
	 * @return 返回真或者假
	 */
	public boolean hasIssuer() {
		return issuer != null;
	}

	/**
	 * 判断是系统命令。系统命令没有用户签名
	 * @return 返回真或者假
	 */
	public boolean isSystem() {
		return issuer == null;
	}

	/**
	 * 判断是用户命令。用户命令有用户签名
	 * @return 返回真或者假
	 */
	public boolean isUser() {
		return issuer != null;
	}

	/**
	 * 设置超时时间，单位：毫秒。
	 * 超时时间是决定一个命令在管理池上的租赁时间，超过这个时间后，命令和任务将被清除，并且由服务器向客户端返回一个错误提示。
	 * @param ms 毫秒
	 */
	public void setTimeout(long ms) {
		check("Timeout");
		timeout = ms;
	}

	/**
	 * 返回超时时间
	 * @return 时间（长整型）
	 */
	public long getTimeout() {
		return timeout;
	}

	/**
	 * 判断没有超时限制
	 * @return 返回真或者假
	 */
	public boolean isInfinite() {
		return timeout <= 0L;
	}

	/**
	 * 返回以毫秒为单位的命令建立时间
	 * @return 建立时间
	 */
	public long getCreateTime() {
		return createTime;
	}

	/**
	 * 判断命令建立时间超时
	 * @return 返回真或者假
	 */
	public boolean isCreateTimeout() {
		return isCreateTimeout(timeout);
	}

	/**
	 * 根据传入的超时时间，判断命令存在时间超时
	 * @param ms 以毫秒为单位的时间
	 * @return 返回真或者假
	 */
	public boolean isCreateTimeout(long ms) {
		return ms > 0 && getRunTime() >= ms;
	}

	/**
	 * 返回以毫秒为单位的运行时间
	 * @return 当前运行时间
	 */
	public long getRunTime() {
		return System.currentTimeMillis() - createTime;
	}

	/**
	 * 设置当前调用器编号，实现“命令/调用器”的绑定。<br>
	 * 
	 * @param who 当前调用器编号
	 */
	public void setLocalId(long who) {
		// 如果发生这个错误，是系统编程的问题，此外无它！
		if (InvokerIdentity.isInvalid(who)) {
			throw new IllegalValueException("illegal invoker identity %d", who);
		}
		localId = who;
	}

	/**
	 * 返回当前调用器编号
	 * @return 长整数
	 */
	public long getLocalId() {
		return localId;
	}

	/**
	 * 设置关联编号，用于安全检查和找到这个命令的关联调用器。<br>
	 * 关联编号：当前命令做为一个新命令，需要与另一个调用器实现关联时，这个编号是它们关联扭带。<br>
	 * <b>“分布任务组件”和“快捷组件”投递命令前，使用这个方法，供管理池进行安全检查和参数配置。</b><br><br>
	 * 
	 * 关联编号是SiteInvoker.invokeId的副本。<br><br>
	 * 
	 * 主要是CALL/DATA/WORK/BUILD节点，分配任务组件和快捷组件的辅助命令时设置。
	 * 
	 * @param who 长整数
	 */
	public void setRelateId(long who) {
		// 如果发生这个错误，是系统编程的问题，此外无它！
		if (InvokerIdentity.isInvalid(who)) {
			throw new IllegalValueException("illegal invoker identity %d", who);
		}
		relateId = who;
	}

	/**
	 * 设置关联编号
	 * @return 另一个调用器的编号（长整数）
	 */
	public long getRelateId() {
		return relateId;
	}

	/**
	 * 判断关联编号有效。<br>
	 * 如果有效，可能是分布任务组件/快捷组件发出。
	 * 
	 * @return 返回真或者假
	 */
	public boolean hasRelateId() {
		return InvokerIdentity.isValid(relateId);
	}
	
	/**
	 * 设置播放声音
	 * @param b 真或者假
	 */
	public void setSound(boolean b) {
		sound = b;
	}

	/**
	 * 判断播放声音
	 * @return 返回真或者假
	 */
	public boolean isSound() {
		return sound;
	}

	/**
	 * 复制Command子类对象的浅层数据副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 比较两个对象是否一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != getClass()) {
			return false;
		} else if (this == that) {
			return true;
		}
		return compareTo((Command) that) == 0;
	}

	/**
	 * 返回底层散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode();
	}

	/**
	 * 比较两个对象的排列顺序。<br><br>
	 * 
	 * 比较条件：<br>
	 * 1. 命令优先级，优先级高在前。<br>
	 * 2. 建立时间，先用升序（时间小者在前） <br>
	 * 3. 哈希码（对象地址，低地址在前）<br>
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Command that) {
		// 空值在前
		if (that == null) {
			return 1;
		}

		// 首先比较优先级，高的在前面，低的在后面
		int ret = that.priority - priority;
		// 比较建立时间
		if (ret == 0) {
			ret = Laxkit.compareTo(createTime, that.createTime);
		}
		// 比较哈希码
		if (ret == 0) {
			ret = hashCode() - that.hashCode();
		}
		return ret;
	}

	/**
	 * 返回操作命令的语句描述原语
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (primitive != null) {
			return primitive;
		}
		return getClass().getSimpleName();
	}

	/**
	 * 将命令转换为数据流和输出
	 * @return 字节数组
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter();
		build(writer);
		return writer.effuse();
	}

	/**
	 * 解析数据流，转换为命令参数
	 * @param b 字节数组
	 * @param off 字节开始下标
	 * @param len 有效长度
	 * @return 返回解析的字节长度
	 */
	public int resolve(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		return resolve(reader);
	}

	/**
	 * Command子类对象生成自己的浅层数据副本。<br>
	 * 浅层数据副本和标准数据副本的区别在于：浅层数据副本只赋值对象，而不是复制对象内容本身。
	 * @return Command子类实例
	 */
	public abstract Command duplicate();

	/**
	 * 将操作命令参数信息写入可类化数据存储器
	 * @param writer 可类化数据存储器
	 * @since 1.1
	 */
	protected abstract void buildSuffix(ClassWriter writer);

	/**
	 * 从可类化数据读取器中解析操作命令参数信息
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	protected abstract void resolveSuffix(ClassReader reader);

}