/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.launch.licence;

/**
 * 冗余，无用！
 * 
 * @author scott.liang
 * @version 1.0 2020-7-19
 * @since laxcus 1.0
 */
public class LicenceMirror {

	private static final int BASELENGTH = 255;
	private static final int LOOKUPLENGTH = 64;

	private static final int TWENTYFOURBITGROUP = 24;
	private static final int EIGHTBIT = 8;
	private static final int SIXTEENBIT = 16;
	private static final int SIXBIT = 6;

	private static final byte PAD = 61;

	private static byte[] base64Alphabet = new byte[LicenceMirror.BASELENGTH];
	private static byte[] lookUpLicenceMirrorAlphabet = new byte[LicenceMirror.LOOKUPLENGTH];

	static {
		for (int i = 0; i < LicenceMirror.base64Alphabet.length; i++)
			LicenceMirror.base64Alphabet[i] = -1;

		for (int i = 90; i >= 65; i--) {
			LicenceMirror.base64Alphabet[i] = (byte) (i - 65);
		}

		for (int i = 122; i >= 97; i--) {
			LicenceMirror.base64Alphabet[i] = (byte) ((i - 97) + 26);
		}

		for (int i = 57; i >= 48; i--) {
			LicenceMirror.base64Alphabet[i] = (byte) ((i - 48) + 52);
		}

		LicenceMirror.base64Alphabet[43] = 62;
		LicenceMirror.base64Alphabet[47] = 63;

		for (int i = 0; i <= 25; i++) {
			LicenceMirror.lookUpLicenceMirrorAlphabet[i] = (byte) (65 + i);
		}

		int i = 26;
		for (int j = 0; i <= 51; j++) {
			LicenceMirror.lookUpLicenceMirrorAlphabet[i] = (byte) (97 + j);
			i++;
		}

		i = 52;
		for (int j = 0; i <= PAD; j++) {
			LicenceMirror.lookUpLicenceMirrorAlphabet[i] = (byte) (48 + j);
			i++;
		}

		LicenceMirror.lookUpLicenceMirrorAlphabet[62] = 43;
		LicenceMirror.lookUpLicenceMirrorAlphabet[63] = 47;
	}

	public static byte[] decode(byte base64Data[]) {
		if (base64Data.length == 0) return new byte[0];
		int numberQuadruple = base64Data.length / 4;
		byte decodedData[] = null;
		byte b1 = 0;
		byte b2 = 0;
		byte b3 = 0;
		byte b4 = 0;
		byte marker0 = 0;
		byte marker1 = 0;
		int encodedIndex = 0;
		int dataIndex = 0;
		int lastData;
		for (lastData = base64Data.length; base64Data[lastData - 1] == PAD;)
			if (--lastData == 0)
				return new byte[0];

		decodedData = new byte[lastData - numberQuadruple];
		for (int i = 0; i < numberQuadruple; i++) {
			dataIndex = i * 4;
			marker0 = base64Data[dataIndex + 2];
			marker1 = base64Data[dataIndex + 3];
			b1 = base64Alphabet[base64Data[dataIndex]];
			b2 = base64Alphabet[base64Data[dataIndex + 1]];
			if (marker0 != PAD && marker1 != PAD) {
				b3 = base64Alphabet[marker0];
				b4 = base64Alphabet[marker1];
				decodedData[encodedIndex] = (byte) (b1 << 2 | b2 >> 4);
				decodedData[encodedIndex + 1] = (byte) ((b2 & 0xf) << 4 | b3 >> 2 & 0xf);
				decodedData[encodedIndex + 2] = (byte) (b3 << SIXBIT | b4);
			} else if (marker0 == PAD) {
				decodedData[encodedIndex] = (byte) (b1 << 2 | b2 >> 4);
			}
			else if (marker1 == PAD) {
				b3 = base64Alphabet[marker0];
				decodedData[encodedIndex] = (byte) (b1 << 2 | b2 >> 4);
				decodedData[encodedIndex + 1] = (byte) ((b2 & 0xf) << 4 | b3 >> 2 & 0xf);
			}
			encodedIndex += 3;
		}
		return decodedData;
	}

	public static byte[] encode(byte binaryData[]) {
		int lengthDataBits = binaryData.length * EIGHTBIT;
		int fewerThan24bits = lengthDataBits % TWENTYFOURBITGROUP;
		int numberTriplets = lengthDataBits / TWENTYFOURBITGROUP;
		byte encodedData[] = null;
		if (fewerThan24bits != 0)
			encodedData = new byte[(numberTriplets + 1) * 4];
		else
			encodedData = new byte[numberTriplets * 4];
		byte k = 0;
		byte l = 0;
		byte b1 = 0;
		byte b2 = 0;
		byte b3 = 0;
		int encodedIndex = 0;
		int dataIndex = 0;
		int i = 0;
		for (i = 0; i < numberTriplets; i++) {
			dataIndex = i * 3;
			b1 = binaryData[dataIndex];
			b2 = binaryData[dataIndex + 1];
			b3 = binaryData[dataIndex + 2];
			l = (byte) (b2 & 0xf);
			k = (byte) (b1 & 0x3);
			encodedIndex = i * 4;
			byte val1 = (b1 & 0xffffff80) != 0 ? (byte) (b1 >> 2 ^ 0xc0)
					: (byte) (b1 >> 2);
			byte val2 = (b2 & 0xffffff80) != 0 ? (byte) (b2 >> 4 ^ 0xf0)
					: (byte) (b2 >> 4);
			byte val3 = (b3 & 0xffffff80) != 0 ? (byte) (b3 >> SIXBIT ^ 0xfc)
					: (byte) (b3 >> SIXBIT);
			encodedData[encodedIndex] = lookUpLicenceMirrorAlphabet[val1];
			encodedData[encodedIndex + 1] = lookUpLicenceMirrorAlphabet[val2 | k << 4];
			encodedData[encodedIndex + 2] = lookUpLicenceMirrorAlphabet[l << 2 | val3];
			encodedData[encodedIndex + 3] = lookUpLicenceMirrorAlphabet[b3 & 0x3f];
		}

		dataIndex = i * 3;
		encodedIndex = i * 4;
		if (fewerThan24bits == EIGHTBIT) {
			b1 = binaryData[dataIndex];
			k = (byte) (b1 & 0x3);
			byte val1 = (b1 & 0xffffff80) != 0 ? (byte) (b1 >> 2 ^ 0xc0)
					: (byte) (b1 >> 2);
			encodedData[encodedIndex] = lookUpLicenceMirrorAlphabet[val1];
			encodedData[encodedIndex + 1] = lookUpLicenceMirrorAlphabet[k << 4];
			encodedData[encodedIndex + 2] = PAD;
			encodedData[encodedIndex + 3] = PAD;
		} else if (fewerThan24bits == SIXTEENBIT) {
			b1 = binaryData[dataIndex];
			b2 = binaryData[dataIndex + 1];
			l = (byte) (b2 & 0xf);
			k = (byte) (b1 & 0x3);
			byte val1 = (b1 & 0xffffff80) != 0 ? (byte) (b1 >> 2 ^ 0xc0)
					: (byte) (b1 >> 2);
			byte val2 = (b2 & 0xffffff80) != 0 ? (byte) (b2 >> 4 ^ 0xf0)
					: (byte) (b2 >> 4);
			encodedData[encodedIndex] = lookUpLicenceMirrorAlphabet[val1];
			encodedData[encodedIndex + 1] = lookUpLicenceMirrorAlphabet[val2 | k << 4];
			encodedData[encodedIndex + 2] = lookUpLicenceMirrorAlphabet[l << 2];
			encodedData[encodedIndex + 3] = PAD;
		}
		return encodedData;
	}

	public static boolean isArrayByteLicenceMirror(byte arrayOctect[]) {
		if(arrayOctect==null || arrayOctect.length==0) return false;
		int length = arrayOctect.length;
		for(int i = 0; i < length; i++) {
			if(!isLicenceMirror(arrayOctect[i])) return false;
		}
		return true;
	}

	public static boolean isLicenceMirror(byte octect) {
		return octect == PAD || base64Alphabet[octect] != -1;
	}

	public static boolean isLicenceMirror(String isValidString) {
		return isArrayByteLicenceMirror(isValidString.getBytes());
	}

}
