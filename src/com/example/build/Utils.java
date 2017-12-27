package com.example.build;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;

public class Utils {
	public static String bytes2HexStr(byte[] bs){
		char[] chars = "0123456789ABCDEF".toCharArray();
		StringBuilder sb = new StringBuilder("");
		int bit;
		for (int i = 0; i < bs.length; i++) {
			bit = (bs[i] & 0x0f0) >> 4;
			sb.append(chars[bit]);
			bit = bs[i] & 0x0f;
			sb.append(chars[bit]);
		}
		return sb.toString();
	}

	public static String str2HexStr(String str) {
		char[] chars = "0123456789ABCDEF".toCharArray();
		StringBuilder sb = new StringBuilder("");
		byte[] bs = str.getBytes();
		int bit;
		for (int i = 0; i < bs.length; i++) {
			bit = (bs[i] & 0x0f0) >> 4;
			sb.append(chars[bit]);
			bit = bs[i] & 0x0f;
			sb.append(chars[bit]);
		}
		return sb.toString();
	}

	public static byte[] hexStringToByte(String hex) {
		int len = (hex.length() / 2);
		byte[] result = new byte[len];
		char[] achar = hex.toCharArray();
		for (int i = 0; i < len; i++) {
			int pos = i * 2;
			result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
		}
		return result;
	}

	private static int toByte(char c) {
		byte b = (byte) "0123456789ABCDEF".indexOf(c);
		return b;
	}

	public static String toStringHex(String s) {
		byte[] baKeyword = new byte[s.length() / 2];
		for (int i = 0; i < baKeyword.length; i++) {
			try {
				baKeyword[i] = (byte) (0xff & Integer.parseInt(
						s.substring(i * 2, i * 2 + 2), 16));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			s = new String(baKeyword, "utf-8");// UTF-16le:Not
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return s;
	}
	
	public static byte[] doCheckSha1(byte[] data, int offset){
		if(data == null || data.length <= 0 || data.length < offset){
			return null;
		}
		byte[] d = new byte[data.length - offset];
		System.arraycopy(data, offset, d, 0, d.length);
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA1");
			digest.update(d);
			return digest.digest();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static byte[] doCheckSumAlder32(byte[] data, int offset){
		if(data == null || data.length <= 0 || data.length < offset){
			return null;
		}
		byte[] d = new byte[data.length - offset];
		System.arraycopy(data, offset, d, 0, d.length);
		Adler32 adler32 = new Adler32();
		adler32.update(d);
		long checksum = adler32.getValue();
		int lchecksum = (int) (0x00000000ffffffff & checksum);
		byte[] checksumBytes = Utils.intToByte(lchecksum);
		return checksumBytes;
	}

	public static long doCheckSumAlder32(String filename, long offset) {
		try {
			File file = new File(filename);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int len = 0;
			byte[] b = new byte[1024];
			FileInputStream fis = new FileInputStream(file);
			fis.skip(offset);
			while ((len = fis.read(b)) != -1) {
				bos.write(b, 0, len);
			}
			byte[] fileBytes = bos.toByteArray();
			fis.close();
			bos.close();
			Adler32 adler32 = new Adler32();
			adler32.update(fileBytes);
			long checksum = adler32.getValue();
			return checksum;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	

	public static byte[] longToByte(long number) {
		long temp = number;
		byte[] b = new byte[8];
		for (int i = 0; i < b.length; i++) {
			b[i] = new Long(temp & 0xff).byteValue();
			temp = temp >> 8;
		}
		return b;
	}

	public static long byteToLong(byte[] b) {
		long s = 0;
		long s0 = b[0] & 0xff;// 最低位
		long s1 = b[1] & 0xff;
		long s2 = b[2] & 0xff;
		long s3 = b[3] & 0xff;
		long s4 = b[4] & 0xff;// 最低位
		long s5 = b[5] & 0xff;
		long s6 = b[6] & 0xff;
		long s7 = b[7] & 0xff;

		// s0不变
		s1 <<= 8;
		s2 <<= 16;
		s3 <<= 24;
		s4 <<= 8 * 4;
		s5 <<= 8 * 5;
		s6 <<= 8 * 6;
		s7 <<= 8 * 7;
		s = s0 | s1 | s2 | s3 | s4 | s5 | s6 | s7;
		return s;
	}

	public static byte[] intToByte(int number) {
		int temp = number;
		byte[] b = new byte[4];
		for (int i = 0; i < b.length; i++) {
			b[i] = new Integer(temp & 0xff).byteValue();// 将最低位保存在最低位
			temp = temp >> 8; // 向右移8位
		}
		return b;
	}

	public static int byteToInt(byte[] b) {
		int s = 0;
		int s0 = b[0] & 0xff;// 最低位
		int s1 = b[1] & 0xff;
		int s2 = b[2] & 0xff;
		int s3 = b[3] & 0xff;
		s3 <<= 24;
		s2 <<= 16;
		s1 <<= 8;
		s = s0 | s1 | s2 | s3;
		return s;
	}
	
	public static byte[] shortToByte(short number) {
		int temp = number;
		byte[] b = new byte[2];
		for (int i = 0; i < b.length; i++) {
			b[i] = new Integer(temp & 0xff).byteValue();//
			temp = temp >> 8; // 向右移8位
		}
		return b;
	}

	public static short byteToShort(byte[] b) {
		short s = 0;
		short s0 = (short) (b[0] & 0xff);// 最低位
		short s1 = (short) (b[1] & 0xff);
		s1 <<= 8;
		s = (short) (s0 | s1);
		return s;
	}
	
	public static int decodeULEB128(byte[] data){
		int pos = 0;
		int offset = 0;
		int result = 0;
		
		while(data[pos] != 0){
			result |= ((data[pos] & 0x7f) << offset);
			offset += 7;
			if((data[pos] & 0x80) == 0){
				break;
			}
			pos += 1;
		}
		byte[] remaind = new byte[data.length - pos - 1];
		System.arraycopy(data, pos+1, remaind, 0, remaind.length);
		System.out.println("remaind: " + Utils.bytes2HexStr(remaind));
		return result;
	}
	
	public static int decodeULEB128(InputStream is) throws IOException{
		int offset = 0;
		int result = 0;
		
		byte tmp;
		while((tmp = (byte) is.read()) != -1){
			result |= ((tmp & 0x7f) << offset);
			offset += 7;
			if((tmp & 0x80) == 0){
				break;
			}
		}
		return result;
	}
	
	public static InputStream byte2InputStream(byte[] data){
		InputStream inputStream = new ByteArrayInputStream(data);
		return inputStream;
	}
}
