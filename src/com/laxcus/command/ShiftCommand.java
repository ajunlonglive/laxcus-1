/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command;

import com.laxcus.echo.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 转发命令。<br><br>
 * 
 * 转发命令是一个本地产生和执行的命令，不在网络里传输。<br>
 * 在转发命令里，包含着另一个命令，这个命令才是需要通过网络发送到目标地址的命令，它是必选项。<br>
 * 所有带“Shift”前缀的命令都是“ShiftCommand”子类，如“ShiftFindIndexZone”。<br>
 * 转发命令中有一个命令钩子，钩子的作用是给调用方提供在方法中等待，EchoInvoker子类处理完成后，把处理结果转交给命令钩子，
 * 调用方得到处理结果。命令钩子是一个可选项。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 04/09/2012
 * @since laxcus 1.0
 */
public abstract class ShiftCommand extends Command {

	private static final long serialVersionUID = -6635034443286808681L;

	/** 用于发送的真实命令 **/
	private Command command;

	/** 命令钩子 **/
	private CommandHook hook;

	/**
	 * 构造默认的转发命令
	 */
	protected ShiftCommand() {
		super();
	}

	/**
	 * 构造转发命令，指定要发送的命令。
	 * @param cmd 被发送的命令
	 */
	protected ShiftCommand(Command cmd) {
		this();
		setCommand(cmd);
	}

	/**
	 * 构造转发命令，指定要发送的命令和命令钩子
	 * @param cmd 被发送的命令
	 * @param hook 命令钩子
	 */
	protected ShiftCommand(Command cmd, CommandHook hook) {
		this(cmd);
		setHook(hook);
	}

	/**
	 * 根据传入的转发命令实例，生成它的数据副本
	 * @param that 转发命令
	 */
	protected ShiftCommand(ShiftCommand that) {
		super(that);
		command = that.command;
		hook = that.hook;
	}

	/**
	 * 设置真实命令
	 * @param e 命令实例
	 */
	public void setCommand(Command e) {
		command = e;

		// 命令钩子如果没有定义超时时间，以命令的超时时间为准！
		if (hook != null && command != null) {
			if (hook.isInfinite()) {
				hook.setTimeout(command.getTimeout());
			}
		}
	}

	/**
	 * 返回真实命令
	 * @return 命令实例
	 */
	public Command getCommand() {
		return command;
	}

	/**
	 * 设置命令钩子
	 * @param e 命令钩子实例
	 */
	public final void setHook(CommandHook e) {
		hook = e;

		// 命令钩子触发时间
		if (hook != null && command != null) {
			if (hook.isInfinite()) {
				hook.setTimeout(command.getTimeout());
			}
		}
	}

	/**
	 * 返回命令钩子
	 * @return 命令钩子实例
	 */
	public CommandHook getHook() {
		return hook;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#setMemory(boolean)
	 */
	@Override
	public void setMemory(boolean b) {
		super.setMemory(b);
		if (command != null) {
			command.setMemory(b);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#setDisk(boolean)
	 */
	@Override
	public void setDisk(boolean b) {
		super.setDisk(b);
		if (command != null) {
			command.setDisk(b);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#setDirect(boolean)
	 */
	@Override
	public void setDirect(boolean b) {
		super.setDirect(b);
		if (command != null) {
			command.setDirect(b);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#setReply(boolean)
	 */
	@Override
	public void setReply(boolean b) {
		super.setReply(b);
		if (command != null) {
			command.setReply(b);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#setIssuer(com.laxcus.util.Siger)
	 */
	@Override
	public void setIssuer(Siger e) {
		super.setIssuer(e);
		if (command != null) {
			command.setIssuer(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#setPriority(byte)
	 */
	@Override
	public void setPriority(byte no) {
		super.setPriority(no);
		if (command != null) {
			command.setPriority(no);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#setQuick(boolean)
	 */
	@Override
	public void setQuick(boolean b) {
		super.setQuick(b);
		if (command != null) {
			command.setQuick(b);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#setFast(boolean)
	 */
	@Override
	public void setFast(boolean b) {
		super.setFast(b);
		if (command != null) {
			command.setFast(b);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#setLocalId(long)
	 */
	@Override
	public void setLocalId(long invokerId) {
		super.setLocalId(invokerId);
		if (command != null) {
			command.setLocalId(invokerId);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#setRelateId(long)
	 */
	@Override
	public void setRelateId(long invokerId) {
		super.setRelateId(invokerId);
		if (command != null) {
			command.setRelateId(invokerId);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#setSource(com.laxcus.echo.Cabin)
	 */
	@Override
	public void setSource(Cabin e) {
		super.setSource(e);
		if (command != null) {
			command.setSource(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#setTimeout(long)
	 */
	@Override
	public void setTimeout(long ms) {
		super.setTimeout(ms);
		// 当前命令超时时间
		if (command != null) {
			command.setTimeout(ms);
		}
		// 设置钩子超时时间
		if (hook != null) {
			hook.setTimeout(ms);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#setTigger(boolean)
	 */
	@Override
	public void setTigger(boolean b) {
		super.setTigger(b);
		if (command != null) {
			command.setTigger(b);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#setSound(boolean)
	 */
	@Override
	public void setSound(boolean b) {
		super.setSound(b);
		if (command != null) {
			command.setSound(b);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {

	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {

	}
}