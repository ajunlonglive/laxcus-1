/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.visit;

/**
 * 远程过程调用接口（RPC接口）。<br><br>
 * 
 * Visit是远程过程调用（RPC）的基础类。<br><br>
 * 
 * Visit被定义和声明为远程过程调用的基础接口。在Visit之下，有各种类型的子接口，子接口都声明为xxxVisit（带Visit后缀）。
 * 这些Visit子接口，由属于客户端的xxxClient，和属于服务端的xxxImpl，分别去实现。<br><br>
 * 
 * Visit子接口，提供两种远程过程调用方案：同步和异步。属于哪种性质，由它的子接口来解释说明。<br><br>
 * 
 * Visit子接口的服务端实现类，在站点启动时被注册和绑定到FIXP服务器上，
 * 由“远程过程调用适配器（VisitAdapter）”根据子接口的名称形成映射关联来保存。
 * 当FIXP服务器收到RPC数据后，通过客户端提供的Visit子接口名称，
 * 找到对应的服务端接口实现类，调用它的注册方法，实现RPC操作。<br><br>
 * 
 * RPC的通信模式有两种：TCP和KEEP UDP。TCP是“流连接”模式，
 * KEEP UDP仍然属于“包连接”模式，但是融合了UDP/TCP的优点。
 * 客户端在使用时可以选择任何一种，服务端会采用同样的模式对应。<br><br>
 * 
 * Visit没有定义任何方法。它的作用是给子接口形成一个共同的标识，方便FIXP服务器绑定Visit和执行RPC操作时的确认。
 * 这类似于JAVA中的串行化接口（Serializable）。
 * 
 * @author scott.liang
 * @version 1.0 1/12/2009
 * @since laxcus 1.0
 */
public interface Visit {


}