/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.sound;

import java.io.*;

import javax.sound.sampled.*;

import com.laxcus.log.client.*;

/**
 * 音频单元
 * 
 * @author scott.liang
 * @version 1.0 9/1/2019
 * @since laxcus 1.0
 */
public class SoundItem {
	
	/** 播放过程中发生错误 **/
	private boolean playFault;
	
	/** 编号，运行过程中唯一 **/
	private int id;

	/** 音频格式 **/
	private AudioFormat format ;
	
	/** 音频数据流 **/
	private byte[] stream;
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	public void finalize() {
		id = 0;
		format = null;
		stream = null;
	}
	
	/**
	 * 构造默认的音频单元
	 * @param id 单元编号
	 */
	public SoundItem(int who) {
		super();
		playFault = false; // 默认是假
		setID(who);
	}

	/**
	 * 构造音频单元，指定参数
	 * @param id 单元编号
	 * @param format 音频格式
	 * @param stream 音频数据流
	 */
	public SoundItem(int id, AudioFormat format, byte[] stream) {
		this(id);
		setFormat(format);
		setStream(stream);
	}

	/**
	 * 设置编号
	 * @param who 编号
	 */
	public void setID(int who) {
		id = who;
	}

	/**
	 * 返回编号
	 * @return 编号
	 */
	public int getID() {
		return id;
	}

	/**
	 * 设置音频格式
	 * @param e 音频格式
	 */
	public void setFormat(AudioFormat e) {
		format = e;
	}

	/**
	 * 返回音频格式
	 * @return 音频格式
	 */
	public AudioFormat getFormat() {
		return format;
	}

	/**
	 * 设置音频数据流
	 * @param b 音频数据流
	 */
	public void setStream(byte[] b) {
		stream = b;
	}

	/**
	 * 返回音频数据流
	 * @return 音频数据流
	 */
	public byte[] getStream() {
		return stream;
	}
	
	/**
	 * 播放音频
	 * @return 播放成功返回真，否则假
	 */
	public boolean play() {
		// 已经发生错误，返回假
		if (playFault) {
			return false;
		}

		// 执行音频播放
		boolean success = false;
		try {
			// 生成实例
			DataLine.Info datalineInfo = new DataLine.Info(SourceDataLine.class, format);
			SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(datalineInfo);
			// 打开
			sourceDataLine.open(format);
			// 启动
			sourceDataLine.start();
			// 写入音频流
			sourceDataLine.write(stream, 0, stream.length);
			// 关闭
			sourceDataLine.close();
			success = true;
		} catch (LineUnavailableException e) {
			playFault = true;
			Logger.error(e);
		}

		return success;
	}
	
	/**
	 * 生成一个音频单元实例
	 * @param id 编号
	 * @param stream 音频数据流
	 * @return 成功返回实例，否则是空指针
	 */
	public static SoundItem create(int id, byte[] stream) {
		// 编号无效， 数组无效，返回空指针
		if (id < 0) {
			return null;
		} else if (stream == null || stream.length == 0) {
			return null;
		}

		// 生成数据流
		ByteArrayInputStream soundStream = new ByteArrayInputStream(stream);

		// 生成音频格式
		AudioFormat audioFormat = null;
		try {
			AudioInputStream audioInputStream = AudioSystem
					.getAudioInputStream(soundStream);
			audioFormat = audioInputStream.getFormat();
		} catch (UnsupportedAudioFileException e) {

		} catch (IOException e) {

		}

		// 有效
		if (audioFormat != null) {
			return new SoundItem(id, audioFormat, stream);
		}

		return null;
	}

}