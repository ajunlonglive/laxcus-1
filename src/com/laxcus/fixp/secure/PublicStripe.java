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
 * RSA公钥
 * 
 * @author scott.liang
 * @version 1.0 2/10/2021
 * @since laxcus 1.0
 */
public final class PublicStripe extends SecureStripe {

	private static final long serialVersionUID = 6692573612244592148L;

	/**
	 * 构造RSA公钥
	 */
	public PublicStripe() {
		super();
	}

	/**
	 * 构造RSA公钥
	 * @param modulus 系数
	 * @param exponent 指数
	 */
	public PublicStripe(BigInteger modulus, BigInteger exponent) {
		this();
		setModulus(modulus);
		setExponent(exponent);
	}
	
	/**
	 * 从可类化读取器生成RSA公钥
	 * @param reader 可类化读取器
	 */
	public PublicStripe(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * @param that
	 */
	private PublicStripe(PublicStripe that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.fixp.safe.SKey#duplicate()
	 */
	@Override
	public PublicStripe duplicate() {
		return new PublicStripe(this);
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