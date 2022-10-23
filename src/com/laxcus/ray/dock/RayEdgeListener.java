/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.dock;

/**
 * 边缘监听器
 * 
 * @author scott.liang
 * @version 1.0 12/6/2021
 * @since laxcus 1.0
 */
public interface RayEdgeListener {

	public void doRunFromEdge();

	public void doShutdownFromEdge();
}