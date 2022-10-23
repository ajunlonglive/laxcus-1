/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.relate;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 获得授权人的CALL站点。<br>
 * 
 * 命令由FRONT站点发给GATE站点。GATE站点在本地检索匹配的参数，返回给FRONT站点。
 * 
 * @author scott.liang
 * @version 1.0 8/6/2018
 * @since laxcus 1.1
 */
public class TakeAuthorizerCall extends Command {

	private static final long serialVersionUID = 1789946789750406423L;

	/** 授权人的用户签名 **/
	private Siger authorizer;
	
	/** 被授权人的用户签名**/
	private Siger conferrer;

	/**
	 * 根据传入的获得授权人的CALL站点，生成它的浅层数据副本
	 * @param that 获得授权人的CALL站点实例
	 */
	private TakeAuthorizerCall(TakeAuthorizerCall that) {
		super(that);
		authorizer = that.authorizer;
		conferrer = that.conferrer;
	}

	/**
	 * 构造默认的获得授权人的CALL站点命令
	 */
	private TakeAuthorizerCall() {
		super();
	}

	/**
	 * 构造获得授权人的CALL站点，指定授权人的用户签名
	 * @param authorizer 授权人签名
	 * @param conferrer 被授权人签名
	 */
	public TakeAuthorizerCall(Siger authorizer, Siger conferrer) {
		this();
		setAuthorizer(authorizer);
		setConferrer(conferrer);
	}

	/**
	 * 从可类化读取器中解析SCALING命令
	 * @param reader 可类化数据读取器
	 */
	public TakeAuthorizerCall(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置授权人的用户签名
	 * @param e Siger实例
	 */
	public void setAuthorizer(Siger e) {
		Laxkit.nullabled(e);

		authorizer = e;
	}

	/**
	 * 返回授权人的用户签名
	 * @return Siger实例
	 */
	public Siger getAuthorizer() {
		return authorizer;
	}

	/**
	 * 设置被授权人的用户签名
	 * @param e Siger实例
	 */
	public void setConferrer(Siger e) {
		Laxkit.nullabled(e);

		conferrer = e;
	}

	/**
	 * 返回被授权人的用户签名
	 * @return Siger实例
	 */
	public Siger getConferrer() {
		return conferrer;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public TakeAuthorizerCall duplicate() {
		return new TakeAuthorizerCall(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(authorizer);
		writer.writeObject(conferrer);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		authorizer = new Siger(reader);
		conferrer = new Siger(reader);
	}

}