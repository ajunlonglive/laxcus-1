/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.util;

import java.io.*;
import java.util.*;

import com.laxcus.access.column.*;
import com.laxcus.access.column.attribute.*;
import com.laxcus.security.*;
import com.laxcus.util.charset.*;

/**
 * 可变长数据类型的生成器。包括RAW、CHAR、WCHAR、HCHAR诸类型。<br>
 * 
 * @author scott.liang
 * @version 1.2 3/23/2013
 * @since laxcus 1.0
 */
public class VariableGenerator {

	/**
	 * 将16进制字符串转化为字节数组描述
	 * @param input 16进制字符串
	 * @return 转义后的字节数组
	 */
	public static byte[] htob(String input) {
		int len = input.length() / 2 + (input.length() % 2);
		byte[] b = new byte[len];

		int seek = 0, index = 0;
		if (input.length() % 2 == 1) {
			String s = input.substring(0, 1);
			b[index++] = (byte) (java.lang.Integer.parseInt(s, 16) & 0xFF);
			seek++;
		}
		for (; seek < input.length(); seek += 2) {
			String s = input.substring(seek, seek + 2);
			b[index++] = (byte) (java.lang.Integer.parseInt(s, 16) & 0xFF);
		}

		return b;
	}

	/**
	 * 把输入的数据按照指定压缩算法进行压缩，并且输出。<br>
	 * 如果没有匹配的压缩算法，数据原样输出。
	 * 
	 * @param compress 压缩算法
	 * @param b 被压缩的字节数组
	 * @param off 下标
	 * @param len 长度
	 * @return 压缩后的字节数组
	 * @throws IOException
	 */
	public static byte[] compress(int compress, byte[] b, int off, int len) throws IOException {
		switch (compress) {
		case PackingTag.GZIP:
			return Inflator.gzip(b, off, len);
		case PackingTag.ZIP:
			return Inflator.zip(b, off, len);
		}
		return Arrays.copyOfRange(b, off, off + len);
	}

	/**
	 * 把输入的数据按照指定压缩算法进行解压缩，并且输出。<br>
	 * 如果没有匹配的压缩算法，数据原样输出。
	 * 
	 * @param compress 压缩算法
	 * @param b 被解压的字节数组
	 * @param off 下标
	 * @param len 长度
	 * @return 解压缩后的字节数组
	 * @throws IOException
	 */
	public static byte[] uncompress(int compress, byte[] b, int off, int len) throws IOException {
		switch (compress) {
		case PackingTag.GZIP:
			return Deflator.gzip(b, off, len);
		case PackingTag.ZIP:
			return Deflator.zip(b, off, len);
		}
		return Arrays.copyOfRange(b, off, off + len);
	}

	/**
	 * 把输入的数据按照指定加密算法进行加密，然后输出。<br>
	 * 如果没有匹配的加密算法，数据原样输出
	 * 
	 * @param encrypt 密文算法
	 * @param password 密码
	 * @param b 被加密数组
	 * @param off 数组开始下标
	 * @param len 数组字节长度
	 * @return 返回加密后的字节数组
	 * @throws SecureException
	 */
	public static byte[] encrypt(int encrypt, byte[] password, byte[] b, int off, int len) throws SecureException {
		switch (encrypt) {
		case PackingTag.DES:
			return SecureEncryptor.des(password, b, off, len);
		case PackingTag.DES3:
			return SecureEncryptor.des3(password, b, off, len);
		case PackingTag.AES:
			return SecureEncryptor.aes(password, b, off, len);
		case PackingTag.BLOWFISH:
			return SecureEncryptor.blowfish(password, b, off, len);
		}
		return Arrays.copyOfRange(b, off, off + len);
	}

	/**
	 * 把输入的数据按照指定加密算法进行解密，然后输出。<br>
	 * 如果没有匹配的加密算法，数据原样输出
	 * 
	 * @param encrypt 密文算法
	 * @param password 密码
	 * @param b 被加密数组
	 * @param off 数组开始下标
	 * @param len 数组字节长度
	 * @return 返回解密后的字节数组
	 * @throws SecureException
	 */
	public static byte[] decrypt(int encrypt, byte[] password, byte[] b, int off, int len) throws SecureException {
		switch (encrypt) {
		case PackingTag.DES:
			return SecureDecryptor.des(password, b, off, len);
		case PackingTag.DES3:
			return SecureDecryptor.des3(password, b, off, len);
		case PackingTag.AES:
			return SecureDecryptor.aes(password, b, off, len);
		case PackingTag.BLOWFISH:
			return SecureDecryptor.blowfish(password, b, off, len);
		}
		return Arrays.copyOfRange(b, off, off + len);
	}

	/**
	 * 把输入的数据按照指定封装规则进行数据封装（先压缩再加密），然后输出。<br>
	 * 如果没有定义封装规则，数据原样输出 
	 * 
	 * @param packing 数据封装
	 * @param b 原始字节数组
	 * @param off 数组开始下标
	 * @param len 数组字节长度
	 * @return 封装后的字节数组
	 * @throws IOException
	 */
	public static byte[] enpacking(Packing packing, byte[] b, int off, int len) throws IOException {
		int compress = packing.getCompress();
		int encrypt = packing.getEncrypt();
		if(compress != 0) {
			byte[] s = VariableGenerator.compress(compress, b, off, len);
			if (encrypt != 0) {
				return VariableGenerator.encrypt(encrypt, packing.getPassword(), s, 0, s.length);
			} else {
				return s;
			}
		} else if(encrypt != 0) {
			return VariableGenerator.encrypt(encrypt, packing.getPassword(), b, off, len);
		}
		return Arrays.copyOfRange(b, off, off + len);
	}
	
	/**
	 * 把输入的数据按照指定封装规则进行数据解封（先解密再解压），然后输出。<br>
	 * 如果没有定义封装规则，数据原样输出。
	 * 
	 * @param packing 数据封装
	 * @param b 被封装的字节数组
	 * @param off 数组开始下标
	 * @param len 数组字节长度
	 * @return 解封后的字节数组
	 * @throws IOException
	 */
	public static byte[] depacking(Packing packing, byte[] b, int off, int len) throws IOException {
		int compress = packing.getCompress();
		int encrypt = packing.getEncrypt();
		if (encrypt != 0) {
			byte[] s = VariableGenerator.decrypt(encrypt, packing.getPassword(), b, off, len);
			if (compress != 0) {
				return VariableGenerator.uncompress(compress, s, 0, s.length);
			} else {
				return s;
			}
		} else if (compress != 0) {
			return VariableGenerator.uncompress(compress, b, off, len);
		}
		return Arrays.copyOfRange(b, off, off + len);
	}

	/**
	 * 把输入的数据按照列属性定义的封装规则进行数据封装（先压缩再加密），然后输出。<br>
	 * 如果列属性没有定义封装规则，数据原样输出 
	 * 
	 * @param attribute 可变长列属性
	 * @param b 原始二进制数组
	 * @param off 数组开始下标
	 * @param len 数组有效长度
	 * @return 封装后的字节数组
	 * @throws IOException
	 */
	public static byte[] enpacking(VariableAttribute attribute, byte[] b, int off, int len) throws IOException {
		Packing packing = attribute.getPacking();
		if (packing != null && packing.isEnabled()) {
			return VariableGenerator.enpacking(packing, b, off, len);
		} else {
			return Arrays.copyOfRange(b, off, off + len);
		}
	}

	/**
	 * 把输入的数据按照列属性定义的封装规则进行数据解封，然后输出。<br>
	 * 如果列属性没有定义封装规则，数据原样输出 
	 * 
	 * @param attribute 可变长列属性
	 * @param b 被封装的二进制数组
	 * @param off 数组开始下标
	 * @param len 数组有效长度
	 * @return 解封装后的字节数组
	 * @throws IOException
	 */
	public static byte[] depacking(VariableAttribute attribute, byte[] b, int off, int len) throws IOException {
		Packing packing = attribute.getPacking();
		if (packing != null && packing.isEnabled()) {
			return VariableGenerator.depacking(packing, b, off, len);
		} else {
			return Arrays.copyOfRange(b, off, off + len);
		}
	}
	
	/**
	 * 把输入的字节数组按照可变长列属性定义的封装规则，进行数据编码（即是数据封装），然后输出。
	 * @param attribute 可变长列属性
	 * @param b 原始字节数组
	 * @param off 数组开始下标
	 * @param len 数据有效长度
	 * @return 封装后的字节数组
	 * @throws IOException
	 */
	public static byte[] encode(VariableAttribute attribute, byte[] b, int off, int len) throws IOException {
		return VariableGenerator.enpacking(attribute, b, off, len);
	}

	/**
	 * 把输入的字符串按照字符列属性定义的字符集和封装规则，进行数据编码（先用字符串编码，然后数据封装），然后输出。
	 * @param attribute 字符列属性
	 * @param line 输入字符串
	 * @return 封装后的字节数组
	 * @throws IOException
	 */
	public static byte[] encode(WordAttribute attribute, String line) throws IOException {
		// 取出字符集进行编码
		Charset charset = attribute.getCharset();
		byte[] b = charset.encode(line);
		// 执行压缩/加密处理
		return VariableGenerator.enpacking(attribute, b, 0, b.length);
	}

	/**
	 * 把输入的字符串按照字符列属性定义的字符集和封装规则，进行数据解码（先是数据解封，然后用字符集解码），然后输出。
	 * @param attribute 字符列属性
	 * @param b 封装后字节数组
	 * @param off 字节数组下标开始位置
	 * @param len 字节数组的转换长度
	 * @return 解封后的字符串
	 * @throws IOException
	 */
	public static String decode(WordAttribute attribute, byte[] b, int off, int len) throws IOException {
		Charset charset = attribute.getCharset();
		Packing packing = attribute.getPacking();
		// 如果有压缩，先解压再解码。否则直接解码
		if (packing != null && packing.isEnabled()) {
			byte[] bs = VariableGenerator.depacking(packing, b, off, len);
			return charset.decode(bs, 0, bs.length);
		} else {
			return charset.decode(b, off, len);
		}
	}

	/**
	 * 根据文本内容，生成字符列的数值、索引、模糊检索关键字列（LIKE列），保存到字符列中。
	 * 
	 * @param dsm 列存储模型
	 * @param attribute 字符列属性
	 * @param text 原始文本
	 * @param word 字符列，包括CHAR、WCHAR、HCHAR
	 * @throws IOException
	 */
	private static void createWord(boolean dsm, WordAttribute attribute, String text, Word word) throws IOException {
		word.setId(attribute.getColumnId());

		// 1. 如果是字符串不包含数据，设为"EMPTY"状态，用于"IS EMPTY, NOT EMPTY"检索
		if (text.isEmpty()) {
			word.setValue(new byte[0]);
			word.setIndex(new byte[0]);
			return;
		}

		// 生成编码和打包后的数据值
		byte[] b = VariableGenerator.encode(attribute, text);
		word.setValue(b);

		// 如果是索引键(主键或者从键)，生成索引值，在索引基础上，生成模糊检索
		if (attribute.isKey()) {
			// 生成索引，可能是NULL
			b = VariableGenerator.toIndex(dsm, attribute, text);
			word.setIndex(b);
			// 在索引基础上，分割字符串，保存“LIKE”查询的字符串(默认最大限制16字符)
			List<RWord> array = VariableGenerator.createRWords(attribute, text);
			if (array != null) {
				word.addRWords(array);
			}
		}
	}

	/**
	 * 根据文本内容和单字符列属性，生成单字符列的数值、索引、单字符模糊检索列（LIKE CHAR列），保存到单字符列中。
	 * 
	 * @param dsm 列存储模型
	 * @param attribute 字符列属性
	 * @param text 原始文本
	 * @return 返回单字符列实例
	 */
	public static Char createChar(boolean dsm, CharAttribute attribute, String text) throws IOException {
		Char utf8 = new Char();
		VariableGenerator.createWord(dsm, attribute, text, utf8);
		return utf8;
	}

	/**
	 * 根据文本内容和宽字符列属性，生成宽字符列的数值、索引、宽字符模糊检索列（LIKE WCHAR列），保存到宽字符列中。
	 * 
	 * @param dsm 列存储模型
	 * @param attribute 字符列属性
	 * @param text 原始文本
	 * @return 返回宽字符列实例
	 */
	public static WChar createWChar(boolean dsm, WCharAttribute attribute, String text) throws IOException {
		WChar wchar = new WChar();
		VariableGenerator.createWord(dsm, attribute, text, wchar);
		return wchar;
	}

	/**
	 * 根据文本内容和大字符列属性，生成大字符列的数值、索引、大字符模糊检索列（LIKE WCHAR列），保存到大字符列中。
	 * 
	 * @param dsm 列存储模型
	 * @param attribute 字符列属性
	 * @param text 原始文本
	 * @return 返回大字符列实例
	 */
	public static HChar createHChar(boolean dsm, HCharAttribute attribute, String text) throws IOException {
		HChar hchar = new HChar();
		VariableGenerator.createWord(dsm, attribute, text, hchar);
		return hchar;
	}

	/**
	 * 区分字符列属性，使用输入的文本，生成关联的字符列实例
	 * @param dsm 列存储模型
	 * @param attribute 字符列属性
	 * @param text 原始文本
	 * @return 返回匹配的字符列实例（CHAR、WCHAR、HCHAR的一种）
	 * @throws IOException
	 */
	public static Word createWord(boolean dsm, WordAttribute attribute, String text) throws IOException {
		if (attribute.isChar()) {
			return VariableGenerator.createChar(dsm, (CharAttribute) attribute, text);
		} else if (attribute.isWChar()) {
			return VariableGenerator.createWChar(dsm, (WCharAttribute) attribute, text);
		} else if (attribute.isHChar()) {
			return VariableGenerator.createHChar(dsm, (HCharAttribute) attribute, text);
		}
		throw new IOException("illegal family");
	}

	/**
	 * 根据二进制数组列属性定义，生成二进制数组列实例
	 * @param dsm 列存储模型
	 * @param attribute 二进制数组列属性
	 * @param value 原始数据
	 * @return 返回字节数组列实例
	 * @throws IOException
	 */
	public static Raw createRaw(boolean dsm, RawAttribute attribute, byte[] value) throws IOException {
		Raw raw = new Raw(attribute.getColumnId());

		// 设置"EMPTY"状态, 用于"IS EMPTY, IS NOT EMPTY"检索
		if (value != null && value.length == 0) {
			raw.setValue(new byte[0]);
			raw.setIndex(new byte[0]);
			return raw;
		}

		// 生成打包后的数组
		byte[] b = VariableGenerator.encode(attribute, value, 0, value.length);
		raw.setValue(b);

		// 如果是索引键，截取一段数据，生成索引
		if(attribute.isKey()) {
			b = VariableGenerator.toIndex(dsm, attribute, value);
			raw.setIndex(b);
		}

		return raw;
	}
	
	/**
	 * 根据字节数组列属性，把字符串转换为字节数组，生成二进制数组列实例
	 * @param dsm 列存储模型
	 * @param attribute 二进制数组列属性
	 * @param value 字符串
	 * @return 返回字节数组列实例
	 * @throws IOException
	 */
	public static Raw createRaw(boolean dsm, RawAttribute attribute, String value) throws IOException {
		byte[] b = VariableGenerator.htob(value);
		return VariableGenerator.createRaw(dsm, attribute, b);
	}
	
	/**
	 * 根据字符列属性和模糊检索参数，生成模糊检索字符列的数据、索引，并保存到模糊检索列中。<br>
	 * 
	 * @param attribute 字符列属性
	 * @param left 模糊检索字左侧留空
	 * @param right 模糊检索字右侧留空
	 * @param text 输入文本
	 * @param word 模糊检索字
	 * @throws IOException
	 */
	private static void toRWord(WordAttribute attribute, short left, short right, String text, RWord word) throws IOException {
		// 如果大小写不敏感，转换为小字字母
		if(!attribute.isSentient()) {
			text = text.toLowerCase(); 
		}

		// 取出对应的字符集
		Charset charset = attribute.getCharset();

		byte[] b = charset.encode(text);
		byte[] index = VariableGenerator.enpacking(attribute, b, 0, b.length);

		// 使用模糊关键字的列标识号
		short columnId = attribute.getColumnId();
		short likeId = (short)(columnId | 0x8000);

		word.setId(likeId);
		word.setRange(left, right);
		word.setIndex(index);
	}

	/**
	 * 根据单字符列属性和模糊检索参数，生成一个单字符模糊检索列，并且输出
	 * 
	 * @param attribute 字符列属性
	 * @param left 模糊检索字左侧留空
	 * @param right 模糊检索字右侧留空
	 * @param text 输入文本
	 * @return 返回单字符模糊检索列
	 */
	public static RChar createRChar(CharAttribute attribute, 
			short left, short right, String text) throws IOException {
		RChar column = new RChar();
		VariableGenerator.toRWord(attribute, left, right, text, column);
		return column;
	}

	/**
	 * 根据宽字符列属性和模糊检索参数，生成一个宽字符模糊检索列，并且输出
	 * 
	 * @param attribute 字符列属性
	 * @param left 模糊检索字左侧留空
	 * @param right 模糊检索字右侧留空
	 * @param text 输入文本
	 * @return 返回宽字符模糊检索列
	 */
	public static RWChar createRWChar(WCharAttribute attribute, 
			short left, short right, String text) throws IOException {
		RWChar column = new RWChar();
		VariableGenerator.toRWord(attribute, left, right, text, column);
		return column;
	}

	/**
	 * 根据大字符列属性和模糊检索参数，生成一个大字符模糊检索列，并且输出
	 * 
	 * @param attribute 字符列属性
	 * @param left 模糊检索字左侧留空
	 * @param right 模糊检索字右侧留空
	 * @param text 输入文本
	 * @return 返回大字符模糊检索列
	 */
	public static RHChar createRHChar(HCharAttribute attribute, 
			short left, short right, String text) throws IOException {
		RHChar column = new RHChar();
		VariableGenerator.toRWord(attribute, left, right, text, column);
		return column;
	}
	
	/**
	 * 区分字符列属性，使用输入模糊检索参数，生成关联的模糊检索字符列实例
	 * @param attribute 字符列属性
	 * @param left 模糊检索字左侧留空
	 * @param right 模糊检索字右侧留空
	 * @param text 输入文本
	 * @return 返回匹配的模糊检索列实例（LIKE CHAR、LIKE WCHAR、LIKE HCHAR的一种）
	 * @throws IOException
	 */
	public static RWord createRWord(WordAttribute attribute, 
			short left, short right, String text) throws IOException {
		if (attribute.isChar()) {
			return VariableGenerator.createRChar((CharAttribute) attribute, left, right, text);
		} else if (attribute.isWChar()) {
			return VariableGenerator.createRWChar((WCharAttribute) attribute, left, right, text);
		} else if (attribute.isHChar()) {
			return VariableGenerator.createRHChar((HCharAttribute) attribute, left, right, text);
		}
		throw new IOException("illegal like word");
	}

	/**
	 * 根据字符列属性的定义，截取传入文本的一段数据，进行封装(压缩+加密)，生成索引
	 * @param dsm 列存储模型
	 * @param attribute 字符列属性
	 * @param text 字符文本
	 * @return 封装后的二进制索引值
	 * @throws IOException
	 */
	public static byte[] toIndex(boolean dsm, WordAttribute attribute, String text) throws IOException {
		Charset charset = attribute.getCharset();

		// 截取字符值的前面一段做为索引
		String index = charset.subCodePoints(0, attribute.getIndexSize(), text);

		// 大小写不敏感(NOT CASE)，转为小写
		if (!attribute.isSentient()) {
			index = index.toLowerCase();
		}

		// 如果是列存储模式，并且索引和数值一样，不需要保留索引
		if (dsm && index.equals(text)) {
			return null;
		}

		// 编码
		byte[] encodes = charset.encode(index);
		// 打包(压缩、加密)
		return VariableGenerator.enpacking(attribute, encodes, 0, encodes.length);
	}

	/**
	 * 根据字符列属性和传入的字符串，生成一批模糊检索列
	 * 
	 * @param attribute 字符列属性
	 * @param text 输入文本
	 * @return 糊模检索列列表
	 */
	public static List<RWord> createRWords(WordAttribute attribute, String text) throws IOException {
		// 如果不支持模糊检索，不需要以下处理
		if (!attribute.isLike()) {
			return null;
		}

		Charset charset = attribute.getCharset();

		// 截取字符值的前面一段做为索引
		String index = charset.subCodePoints(0, attribute.getIndexSize(), text);

		// 大小写不敏感(NOT CASE)，转为小写
		if (!attribute.isSentient()) index = index.toLowerCase();

		// 分割字符串
		List<RWord> array = new ArrayList<RWord>();
		// 以代码位为单位，统计实际字符数
		int codePints = charset.codePointCount(index);
		for (int begin = 0; begin < codePints; begin++) {
			for (int end = codePints; begin < end; end--) {
				String sub = charset.subCodePoints(begin, end - begin, index);
				short left = (short) begin;
				short right = (short)(end - begin);

				byte[] encodes = charset.encode(sub);
				byte[] b = VariableGenerator.enpacking(attribute, encodes, 0, encodes.length);

				//like id区别与 column id
				short likeId = (short)(attribute.getColumnId() | 0x8000);

				if (attribute.isChar()) {
					array.add(new RChar(likeId, left, right, b));
				} else if (attribute.isWChar()) {
					array.add(new RWChar(likeId, left, right, b));
				} else if (attribute.isHChar()) {
					array.add(new RHChar(likeId, left, right, b));
				}
			}
		}

		return array;
	}

	/**
	 * 根据可变长列属性的定义，截取传入字节数组的一段数据，进行封装(压缩+加密)，生成索引
	 * @param dsm 列存储模型
	 * @param attribute 可变长列属性
	 * @param value 可变长二进制数组
	 * @return 封装后的二进制数组
	 * @throws IOException
	 */
	public static byte[] toIndex(boolean dsm, VariableAttribute attribute, byte[] value) throws IOException {
		int size = (attribute.getIndexSize() < value.length ? attribute.getIndexSize() : value.length);
		byte[] index = Arrays.copyOfRange(value, 0, size);
		// 如果是列存储模式，并且索引和数值完全一致，不生成索引
		if (dsm && Arrays.equals(value, index)) {
			return null;
		}
		return VariableGenerator.enpacking(attribute, index, 0, index.length);
	}

	/**
	 * 根据图像列属性定义，生成图像列实例
	 * @param dsm 列存储模型
	 * @param attribute 图像列属性
	 * @param value 图像原始数据
	 * @return 返回图像列实例
	 */
	public static Image createImage(boolean dsm, ImageAttribute attribute, byte[] value) throws IOException {
		Image image = new Image(attribute.getColumnId());

		// 设置"EMPTY"状态, 用于"IS EMPTY, IS NOT EMPTY"检索
		if (value != null && value.length == 0) {
			image.setValue(new byte[0]);
			image.setIndex(new byte[0]);
			return image;
		}

		// 生成打包后的数组
		byte[] b = VariableGenerator.encode(attribute, value, 0, value.length);
		image.setValue(b);

		// 如果是索引键，截取一段数据，生成索引
		if(attribute.isKey()) {
			b = VariableGenerator.toIndex(dsm, attribute, value);
			image.setIndex(b);
		}

		return image;
	}
	
	/**
	 * 根据文档列属性定义，生成文档列实例
	 * @param dsm 列存储模型
	 * @param attribute 文档列属性
	 * @param value 文档原始数据
	 * @return 返回文档列实例
	 */
	public static Document createDocument(boolean dsm, DocumentAttribute attribute, byte[] value) throws IOException {
		Document document = new Document(attribute.getColumnId());

		// 设置"EMPTY"状态, 用于"IS EMPTY, IS NOT EMPTY"检索
		if (value != null && value.length == 0) {
			document.setValue(new byte[0]);
			document.setIndex(new byte[0]);
			return document;
		}

		// 生成打包后的数组
		byte[] b = VariableGenerator.encode(attribute, value, 0, value.length);
		document.setValue(b);

		// 如果是索引键，截取一段数据，生成索引
		if(attribute.isKey()) {
			b = VariableGenerator.toIndex(dsm, attribute, value);
			document.setIndex(b);
		}

		return document;
	}
	
	/**
	 * 根据音频列属性定义，生成音频列实例
	 * @param dsm 列存储模型
	 * @param attribute 音频列属性
	 * @param value 音频原始数据
	 * @return 返回音频列实例
	 */
	public static Audio createAudio(boolean dsm, AudioAttribute attribute, byte[] value) throws IOException {
		Audio audio = new Audio(attribute.getColumnId());

		// 设置"EMPTY"状态, 用于"IS EMPTY, IS NOT EMPTY"检索
		if (value != null && value.length == 0) {
			audio.setValue(new byte[0]);
			audio.setIndex(new byte[0]);
			return audio;
		}

		// 生成打包后的数组
		byte[] b = VariableGenerator.encode(attribute, value, 0, value.length);
		audio.setValue(b);

		// 如果是索引键，截取一段数据，生成索引
		if(attribute.isKey()) {
			b = VariableGenerator.toIndex(dsm, attribute, value);
			audio.setIndex(b);
		}

		return audio;
	}
	
	/**
	 * 根据视频列属性定义，生成视频列实例
	 * @param dsm 列存储模型
	 * @param attribute 视频列属性
	 * @param value 视频原始数据
	 * @return 返回视频列实例
	 */
	public static Video createVideo(boolean dsm, VideoAttribute attribute, byte[] value) throws IOException {
		Video video = new Video(attribute.getColumnId());

		// 设置"EMPTY"状态, 用于"IS EMPTY, IS NOT EMPTY"检索
		if (value != null && value.length == 0) {
			video.setValue(new byte[0]);
			video.setIndex(new byte[0]);
			return video;
		}

		// 生成打包后的数组
		byte[] b = VariableGenerator.encode(attribute, value, 0, value.length);
		video.setValue(b);

		// 如果是索引键，截取一段数据，生成索引
		if (attribute.isKey()) {
			b = VariableGenerator.toIndex(dsm, attribute, value);
			video.setIndex(b);
		}

		return video;
	}
}