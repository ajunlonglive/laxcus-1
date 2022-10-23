/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cross;

import com.laxcus.command.*;
import com.laxcus.law.cross.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 强制授权分享资源 <br>
 * 这个命令由TOP站点发出，分发给GATE/HOME站点，再由HOME发送给CALL/WORK/BUILD站点。
 * 
 * @author scott.liang
 * @version 1.0 7/29/2017
 * @since laxcus 1.0
 */
public abstract class AwardShareCross extends Command {
	
	private static final long serialVersionUID = -2117087668348342722L;
	
	/** 分享单元 **/
	private CrossField field;

	/**
	 * 构造默认的强制授权分享资源
	 */
	protected AwardShareCross() {
		super();
	}

	/**
	 * 生成强制授权分享资源的数据副本
	 * @param that AwardShareCross实例
	 */
	protected AwardShareCross(AwardShareCross that) {
		super(that);
		field = that.field;
	}

	/**
	 * 设置分享单元
	 * @param e CrossField实例
	 */
	public void setField(CrossField e) {
		Laxkit.nullabled(e);

		field = e;
	}

	/**
	 * 返回分享单元
	 * @return CrossField实例
	 */
	public CrossField getField() {
		return field;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(field);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		field = new CrossField(reader);
	}

}