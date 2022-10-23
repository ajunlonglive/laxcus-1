/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub.transfer;

import com.laxcus.access.schema.*;
import com.laxcus.access.stub.*;
import com.laxcus.util.classable.*;

/**
 * 更新数据块命令 <br><br>
 * 
 * 这是一个通知命令，由DATA/BUILD站点发出，目标是数据块关联的DATA站点，通知它更新一个本地的数据块，这个数据块可以在目标DATA站点存在或者不存在。<br><br>
 * 
 * 数据更新通知流程：<br>
 * 1. 源站点发出更新数据块命令（DATA/BUILD节点）<br>
 * 2. 到达目标站点（其它DATA/BUILD站点）<br>
 * 3. 目标站点执行下载 <br>
 * 4. 源站点收到下载命令，执行上传 <br>
 * 5. 上传完成，源站点接收通知（可选）<br><br>
 * 
 * @author scott.liang
 * @version 1.1 5/17/2015
 * @since laxcus 1.0
 */
public class UpdateMass extends TransferMass {

	private static final long serialVersionUID = 473723819280142311L;

	/**
	 * 构造默认和私有的更新数据块命令
	 */
	private UpdateMass() {
		super();
	}

	/**
	 * 根据传入实例，生成更新数据块命令的浅层数据副本
	 * @param that UpdateMass实例
	 */
	private UpdateMass(UpdateMass that) {
		super(that);
	}

	/**
	 * 构造更新数据块命令，指定数据块标识
	 * @param flag 数据块标识
	 */
	public UpdateMass(StubFlag flag) {
		this();
		setFlag(flag);
	}

	/**
	 * 构造更新数据块命令，指定数据表名和数据块编号
	 * @param space 数据表名
	 * @param stub 数据块编号
	 */
	public UpdateMass(Space space, long stub) {
		this();
		setFlag(space, stub);		
	}

	/**
	 * 从可类化数据读取器中解析更新数据块命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public UpdateMass(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public UpdateMass duplicate() {
		return new UpdateMass(this);
	}

}