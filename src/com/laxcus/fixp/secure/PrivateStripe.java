/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.secure;

import java.math.*;

import com.laxcus.util.classable.*;

/**
 * RSA私钥
 * 
 * @author scott.liang
 * @version 1.0 2/10/2021
 * @since laxcus 1.0
 */
public class PrivateStripe extends SecureStripe {

	private static final long serialVersionUID = 801822883890213059L;

	/**
	 * 构造RSA私钥
	 */
	public PrivateStripe() {
		super();
	}
	
	/**
	 * 构造RSA私钥
	 * @param modulus 系数
	 * @param exponent 指数
	 */
	public PrivateStripe(BigInteger modulus, BigInteger exponent) {
		this();
		setModulus(modulus);
		setExponent(exponent);
	}

	/**
	 * 从可类化读取器生成RSA私钥
	 * @param reader 可类化读取器
	 */
	public PrivateStripe(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * @param that
	 */
	private PrivateStripe(PrivateStripe that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.fixp.safe.SKey#duplicate()
	 */
	@Override
	public PrivateStripe duplicate() {
		return new PrivateStripe(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.fixp.safe.SKey#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.laxcus.fixp.safe.SKey#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// TODO Auto-generated method stub

	}

}