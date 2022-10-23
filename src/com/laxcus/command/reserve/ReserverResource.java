/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.reserve;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 备份资源命令 <br><br>
 * 
 * 这个命令及它的子类在TOP/HOME的管理站点和监视站点之间传递，用来备份管理站点的资源文件到监视站点。<br><br>
 * 
 * 流程：<br>
 * 1. TOP/HOME管理站点产生“DispatchReserveResource”命令，命令调用器产生“CommitResrveResource”命令，投递到TOP/HOME的监视站点。<br>
 * 2. TOP/HOME监视站点得到“CommitReserveResource”命令，产生“TakeReserveResource”命令，向TOP/HOME管理站点请求下载资源数据。<br>
 * 3. TOP/HOME管理站点收到“TakeReserveResource”命令，向来源地址传输指定的资源数据文件（在文件传输过程中，这个文件被锁定）。<br>
 * 
 * 
 * @author scott.liang
 * @version 1.0 7/21/2014
 * @since laxcus 1.0
 */
public abstract class ReserverResource extends Command {

	private static final long serialVersionUID = -447301504854004150L;

	/** 资源名称。可以是任何基于字符串的定义，如文件路径，表名等 **/
	private String resource;

	/**
	 * 构造默认的备份资源命令
	 */
	protected ReserverResource() {
		super();
	}

	/**
	 * 从备份资源命令实例中，生成它的数据副本
	 * @param that ReserverResource实例
	 */
	public ReserverResource(ReserverResource that) {
		super(that);
		resource = that.resource;
	}

	/**
	 * 设置资源数据路径（在TOP/HOME管理站点的文件路径）
	 * @param e 资源数据路径
	 */
	public void setResource(String e) {
		Laxkit.nullabled(e);

		resource = e;
	}

	/**
	 * 返回资源数据路径
	 * @return 资源数据路径
	 */
	public String getResource() {
		return resource;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeString(resource);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		resource = reader.readString();
	}

}
