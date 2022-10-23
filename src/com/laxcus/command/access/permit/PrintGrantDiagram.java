/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.permit;

import com.laxcus.command.access.user.*;
import com.laxcus.util.classable.*;

/**
 * 显示用户授权状态命令。<br><br>
 * 
 * 这个命令由集群管理员和注册用户共同使用。集群管理员可以检查全部注册用户状态，注册用户只能检查自己。<br>
 * 
 * 命令格式1：PRINT GRANT DIAGRAM 用户签名, ... <br>
 * 命令格式2: PRINT GRANT DIAGRAM ME  <br>
 * 
 * @author scott.liang
 * @version 1.0 02/12/2018
 * @since laxcus 1.0
 */
public class PrintGrantDiagram extends MultiUser {
	
	private static final long serialVersionUID = -39691031531263439L;

	/**
	 * 构造默认的显示用户授权状态命令。
	 */
	public PrintGrantDiagram() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析显示用户授权状态
	 * @param reader 可类化数据读取器
	 */
	public PrintGrantDiagram(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 根据传入的显示用户授权状态命令，生成它的数据副本
	 * @param that PrintGrantDiagram实例
	 */
	private PrintGrantDiagram(PrintGrantDiagram that) {
		super(that);
	}
	
	/**
	 * 判断显示自己
	 * @return 返回真或者假
	 */
	public boolean isMe() {
		return getUserSize() == 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public PrintGrantDiagram duplicate() {
		return new PrintGrantDiagram(this);
	}

}