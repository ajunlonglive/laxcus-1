/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.permit;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 权限命令操作报告。是“GRANT/REVOKE”命令的返回结果
 * 
 * @author scott.liang
 * @version 1.1 05/09/2015
 * @since laxcus 1.0
 */
public final class CertificateProduct extends EchoProduct {

	private static final long serialVersionUID = 1984518257192076204L;

	/** 生效账号 **/
	private ArrayList<Siger> effects = new ArrayList<Siger>();

	/** 没有生效账号 **/
	private ArrayList<Siger> ineffects = new ArrayList<Siger>();

	/**
	 * 根据传入实例，生成它的浅层数据副本
	 * @param that CertificateProduct实例
	 */
	private CertificateProduct(CertificateProduct that) {
		super(that);
		effects.addAll(that.effects);
		ineffects.addAll(that.ineffects);
	}

	/**
	 * 构造默认的权限命令操作报告
	 */
	public CertificateProduct() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析权限命令操作报告
	 * @param reader - 可类化数据读取器
	 * @since 1.1
	 */
	public CertificateProduct(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 保存一批参数
	 * @param e
	 */
	public void add(CertificateProduct e) {
		effects.addAll(e.effects);
		ineffects.addAll(e.ineffects);
	}
	
	/**
	 * 保存受理的账号
	 * @param e Siger实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean addIssuer(Siger e) {
		Laxkit.nullabled(e);

		return effects.add(e);
	}

	/**
	 * 输出受理账号列表
	 * @return 返回Siger列表
	 */
	public List<Siger> getIssuers() {
		return new ArrayList<Siger>(effects);
	}
	
	/**
	 * 被受理的数目
	 * @return 受理数目
	 */
	public int getEffectSize() {
		return effects.size();
	}

	/**
	 * 保存拒绝的账号
	 * @param e Siger实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean addIneffect(Siger e) {
		Laxkit.nullabled(e);

		return ineffects.add(e);
	}

	/**
	 * 输出拒绝的账号列表
	 * @return 返回Siger列表
	 */
	public List<Siger> getIneffects() {
		return new ArrayList<Siger>(ineffects);
	}
	
	/**
	 * 输出被拒绝的数目
	 * @return 被拒绝的数目
	 */
	public int getIneffectSize() {
		return ineffects.size();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public CertificateProduct duplicate() {
		return new CertificateProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(effects.size());
		for (Siger e : effects) {
			writer.writeObject(e);
		}
		writer.writeInt(ineffects.size());
		for (Siger e : ineffects) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Siger e = new Siger(reader);
			effects.add(e);
		}
		size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Siger e = new Siger(reader);
			ineffects.add(e);
		}
	}

}
