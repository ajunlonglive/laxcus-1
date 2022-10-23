/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.watch;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.site.Node;

/**
 * 转发站点不足命令的命令
 * 
 * @author scott.liang
 * @version 1.0 2018-6-8
 * @since laxcus 1.0
 */
public class ShiftSiteMissing extends ShiftCommand {
	
	private static final long serialVersionUID = 6288918628655616934L;

	/** WATCH站点地址 **/
	private ArrayList<Node> sites = new ArrayList<Node>();
	
	/**
	 * @param cmd
	 */
	public ShiftSiteMissing(List<Node> sites, SiteMissing cmd) {
		super(cmd);
		addSites(sites);
	}

	/**
	 * @param that
	 */
	public ShiftSiteMissing(ShiftSiteMissing that) {
		super(that);
		this.sites.addAll(that.sites);
	}
	
	public void addSites(List<Node> a) {
		sites.addAll(a);
	}

	public List<Node> getSites() {
		return new ArrayList<Node>(sites);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.ShiftCommand#getCommand()
	 */
	@Override
	public SiteMissing getCommand() {
		return (SiteMissing)super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public Command duplicate() {
		// TODO Auto-generated method stub
		return null;
	}

}
