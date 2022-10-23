/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.application.manage;

import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 *
 * @author scott.liang
 * @version 1.0 2021-8-3
 * @since laxcus 1.0
 */
public class WDirectory extends WElement {

	/** 数组单元 **/
	private ArrayList<WElement> tokens = new ArrayList<WElement>();
	
	/**
	 * 
	 */
	public WDirectory() {
		super();
	}

	/**
	 * @param that
	 */
	private WDirectory(WDirectory that) {
		super(that);
		tokens.addAll(that.tokens);
	}

	/**
	 * 保存
	 * @param e
	 */
	public void addToken(WElement e) {
		Laxkit.nullabled(e);
		tokens.add(e);
	}
	
	/**
	 * 返回...
	 * @return
	 */
	public List<WElement> getTokens() {
		return new ArrayList<WElement>(tokens);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.application.manage.WToken#duplicate()
	 */
	@Override
	public WDirectory duplicate() {
		return new WDirectory(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.application.manage.WElement#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 子单元
		writer.writeInt(tokens.size());
		for (WElement token : tokens) {
			writer.writeDefault(token);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.application.manage.WElement#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 子成员
		int elements = reader.readInt();
		for (int i = 0; i < elements; i++) {
			WElement token = (WElement) reader.readDefault();
			tokens.add(token);
		}
	}

}