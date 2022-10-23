/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.invoke;

import java.io.*;
import com.laxcus.fixp.*;

/**
 * 数据流适配器。继承“StreamInvoker”接口。提供数据流服务。
 * 
 * @author scott.liang
 * @version 1.1 4/12/2013
 * @since laxcus 1.0
 */
public abstract class StreamAdapter implements StreamInvoker {
	
	/**
	 * 构造默认的数据流适配器
	 */
	protected StreamAdapter() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.invoke.StreamInvoker#invoke(com.laxcus.fixp.Stream, java.io.OutputStream)
	 */
	@Override
	public void invoke(Stream stream, OutputStream resp) throws IOException {
		Mark cmd = stream.getMark();
		if (cmd.isAsk()) {
			apply(stream, resp);
		} else if (cmd.isAnswer()) {
			reply(stream, resp);
		} else {
			throw new IOException("illegal stream!");
		}
	}

	/**
	 * 输出数据流
	 * @param reply FIXP应答流
	 * @param output 数据发送接口
	 * @throws IOException
	 */
	protected void flush(Stream reply, OutputStream output) throws IOException {
		if (reply == null) return;
		byte[] b = reply.build();
		output.write(b, 0, b.length);
		output.flush();
	}

	/**
	 * 处理数据流的请求
	 * @param request FIXP请求流
	 * @param output 数据输出接口 
	 * @throws IOException
	 */
	protected abstract void apply(Stream request, OutputStream output) throws IOException;

	/**
	 * 处理数据流的应答
	 * @param resp FIXP应答流
	 * @param output 数据输出接口
	 * @throws IOException
	 */
	protected abstract void reply(Stream resp, OutputStream output) throws IOException;
}