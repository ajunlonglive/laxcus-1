/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.front;

import java.io.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 单元实例
 * 
 * @author scott.liang
 * @version 1.0 3/21/2022
 * @since laxcus 1.0
 */
public class CheckRemoteSiteItem implements Classable, Cloneable, Serializable, Comparable<CheckRemoteSiteItem> {
	
	private static final long serialVersionUID = 2572095681846464597L;

	public static final int ENTRANCE = 1;
	
	public static final int GATE = 2;
	
	public static final int AUTHORIZER_GATE = 3; // authorizer
	
	public static final int CLOUD_STORE = 4;
	
	public static final int CALL = 5;
	
	/** 类型定义 **/
	private int family;
	
	/** 节点地址 **/
	private Node site;

	/**
	 * 
	 */
	public CheckRemoteSiteItem() {
		super();
	}
	
	public CheckRemoteSiteItem(int family, Node site) {
		this();
		this.setFamily(family);
		this.setSite(site);
	}
	
	public CheckRemoteSiteItem(ClassReader reader) {
		this();
		this.resolve(reader);
	}
	
	private CheckRemoteSiteItem(CheckRemoteSiteItem that) {
		this();
		this.family = that.family;
		this.site = that.site;
	}
	
	public void setFamily(int who) {
		this.family = who;
	}
	
	public int getFamily(){
		return this.family;
	}
	
	public void setSite(Node e){
		this.site = e;
	}
	
	public Node getSite(){
		return this.site;
	}
	
	
	public CheckRemoteSiteItem duplicate() {
		return new CheckRemoteSiteItem(this);
	}
	
	public Object clone() {
		return this.duplicate();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null || o.getClass() != this.getClass()) {
			return false;
		} else if (o == this) {
			return true;
		}
		return this.compareTo((CheckRemoteSiteItem) o) == 0;
	}

	@Override
	public int hashCode() {
		return family ^ site.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CheckRemoteSiteItem that) {
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(family, that.family);
		if (ret == 0) {
			ret = Laxkit.compareTo(site, that.site);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeInt(family);
		writer.writeObject(site);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int off = reader.getSeek();
		family = reader.readInt();
		site = new Node(reader);
		return reader.getSeek() - off;
	}

}