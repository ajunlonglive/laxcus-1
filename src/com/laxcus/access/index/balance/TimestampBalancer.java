/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.index.balance;

import com.laxcus.access.index.section.*;

/**
 * 时间戳平衡分割器。<br>
 * 
 * @author scott.liang
 * @version 1.0 8/15/2009
 * @since laxcus 1.0
 */
public final class TimestampBalancer extends Bit64Balancer {

	/**
	 * 构造时间戳平衡分割器
	 */
	public TimestampBalancer() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.index.balance.Bit64Balancer#getSector()
	 */
	@Override
	public Bit64Sector getSector() {
		return new TimestampSector();
	}

	//	public static void main(String[] args) {
	////		LongZone zone = new LongZone(0, 0, 1222);
	//		LongZone zone = new LongZone(SimpleTimestamp.MIN_VALUE, SimpleTimestamp.MAX_VALUE, 1);
	////		LongRange[] ranges = zone.getRange().split(2);
	////		for(LongRange e : ranges) {
	////			System.out.println(e);
	////		}
	//		
	//		TimestampBalancer e = new TimestampBalancer();
	//		e.add(zone);
	//		IndexSector is = e.balance(2);
	//		System.out.println("OKAY!");
	//	}

}
