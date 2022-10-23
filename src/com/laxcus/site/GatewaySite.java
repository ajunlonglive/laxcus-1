/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.net.*;

/**
 * 网关站点<br><br>
 * 
 * 网关站点配置两个通信地址：内网地址和公网地址。它们的IP地址不同，TCP/UDP端口必须一致。<br><br>
 * 
 * 内网地址负责集群内的通信，外网地址接入集群外的FRONT站点。<br>
 * 双模式站点在配置中指定内网和公网的IP地址，在启动时绑定通配符IP地址（0.0.0.0）。<br>
 * 
 * 网关站点有GATE/ENTRANCE/CALL三种。
 * 
 * @author scott.liang
 * @version 1.1 04/06/2015
 * @since laxcus 1.0
 */
public abstract class GatewaySite extends Site {

	private static final long serialVersionUID = -4912348145135279045L;

	/** 公网地址 **/
	private Node gateway;

	/* (non-Javadoc)
	 * @see com.laxcus.site.Site#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInstance(gateway);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.site.Site#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		gateway = reader.readInstance(Node.class);
	}

	/**
	 * 根据传入的网关站点，生成它的数据副本
	 * @param that DoubleSite实例
	 */
	protected GatewaySite(GatewaySite that) {
		super(that);
		gateway = that.gateway.duplicate();
	}

	/**
	 * 构造网关站点，指定站点类型
	 * @param family 站点类型
	 */
	protected GatewaySite(byte family) {
		super(family);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.site.Site#matches(com.laxcus.util.net.Address)
	 */
	@Override
	public boolean matches(Address address) {
		boolean success = super.matches(address);
		if (!success) {
			success = (Laxkit.compareTo(gateway.getAddress(), address) == 0);
		}
		return success;
	}

	/**
	 * 设置网关地址（对外通信地址）
	 * @param e Node实例
	 */
	public void setPublic(Node e) {
		Laxkit.nullabled(e);

		if (e.getFamily() != getFamily()) {
			throw new IllegalValueException("cannot match:%d,%d",
					e.getFamily(), getFamily());
		}
		gateway = e.duplicate();
	}

	/**
	 * 返回网关地址（对外通信地址）
	 * @return Node实例
	 */
	public Node getPublic() {
		return gateway;
	}

	/**
	 * 返回内网地址
	 * @return Node实例
	 */
	public Node getPrivate() {
		return getNode();
	}

	/**
	 * 设置内网地址
	 * @param e Node实例
	 */
	public void setPrivate(Node e) {
		setNode(e);
	}

	/**
	 * 返回网关节点地址
	 * @return DoubleNode实例
	 */
	public GatewayNode getGatewayNode() {
		return new GatewayNode(getPrivate(), gateway);
	}

	/**
	 * 设置网关映射端口
	 * @see com.laxcus.site.Site#setReflectTCPort(int)
	 */
	@Override
	public void setReflectTCPort(int port) {
		super.setReflectTCPort(port);
		if (gateway != null && gateway.getHost() != null) {
			gateway.getHost().setReflectTCPort(port);
		}
	}

	/**
	 * 设置UDP映射端口
	 * @param port 端口号
	 */
	@Override
	public void setReflectUDPort(int port) {
		super.setReflectUDPort(port);
		if (gateway != null && gateway.getHost() != null) {
			gateway.getHost().setReflectUDPort(port);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.site.Site#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s | %s", gateway, super.toString()); 
	}
}