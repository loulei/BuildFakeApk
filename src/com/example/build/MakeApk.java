package com.example.build;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class MakeApk {
	
	static final String PATH_APK = "file/SignApkDemo-resigned.apk";
	static final String PATH_DEX = "file/classes.dex";
	static final String PATH_FAKE_APK = "file/SignApkDemo-fake.apk";
	
	static final byte[] CENTERAL_DIRECTORY_FLAG = new byte[]{0x50, 0x4b, 0x01, 0x02};
	static final byte[] END_CENTERAL_DIRECTORY_FLAG = new byte[]{0x50, 0x4b, 0x05, 0x06};
	
	public static void main(String[] args) throws Exception {
		byte[] u4 = new byte[4];
		byte[] u2 = new byte[2];
		
		byte[] apkBytes = Files.readAllBytes(Paths.get(PATH_APK));
		byte[] dexBytes = Files.readAllBytes(Paths.get(PATH_DEX));
		
		System.out.println("apk size="+apkBytes.length);
		System.out.println("dex size="+dexBytes.length);
		
		byte[] fakeApkBytes = new byte[dexBytes.length+apkBytes.length];
		System.arraycopy(dexBytes, 0, fakeApkBytes, 0, dexBytes.length);
		System.arraycopy(apkBytes, 0, fakeApkBytes, dexBytes.length, apkBytes.length);
		
		//fix dex
		System.arraycopy(dexBytes, 32, u4, 0, 4);
		int dexFileSize = Utils.byteToInt(u4);
		System.out.println("dexFileSize="+dexFileSize);
		byte[] fakeDexSizeBytes = Utils.intToByte(fakeApkBytes.length);
		System.arraycopy(fakeDexSizeBytes, 0, fakeApkBytes, 32, 4);
		
		
		//fix zip
		for(int i=0; i<apkBytes.length-4; i++){
			System.arraycopy(apkBytes, i, u4, 0, 4);
			if(Arrays.equals(CENTERAL_DIRECTORY_FLAG, u4)){
				System.out.println("find cd = "+i);
				System.arraycopy(apkBytes, i+42, u4, 0, 4);
				int headOffset = Utils.byteToInt(u4);
				
				byte[] fixOffset = Utils.intToByte(headOffset+dexFileSize);
				System.arraycopy(fixOffset, 0, fakeApkBytes, i+42+dexFileSize, 4);
				
				System.out.println("head offset="+headOffset);
				System.arraycopy(apkBytes, headOffset+26, u2, 0, 2);
				short filenameLen = Utils.byteToShort(u2);
				byte[] filenameBytes = new byte[filenameLen];
				System.arraycopy(apkBytes, headOffset+30, filenameBytes, 0, filenameLen);
				System.out.println("filename="+new String(filenameBytes));
			}else if(Arrays.equals(END_CENTERAL_DIRECTORY_FLAG, u4)){
				System.out.println("find eocd = " + i);
				
				System.arraycopy(apkBytes, i+16, u4, 0, 4);
				int cdoffset = Utils.byteToInt(u4);
				System.out.println("cd offset="+cdoffset);
				
				byte[] fixOffset = Utils.intToByte(cdoffset+dexFileSize);
				System.arraycopy(fixOffset, 0, fakeApkBytes, dexFileSize+i+16, 4);
			}
		}
		
		
		
		//fix dex signature
		byte[] signatureBytes = Utils.doCheckSha1(fakeApkBytes, 32);
		System.out.println(Utils.bytes2HexStr(signatureBytes));
		System.arraycopy(signatureBytes, 0, fakeApkBytes, 12, signatureBytes.length);
		
		//fix dex checksum
		byte[] checkSumBytes = Utils.doCheckSumAlder32(fakeApkBytes, 12);
		System.out.println(Utils.bytes2HexStr(checkSumBytes));
		System.arraycopy(checkSumBytes, 0, fakeApkBytes, 8, checkSumBytes.length);
		
		
		Files.write(Paths.get(PATH_FAKE_APK), fakeApkBytes, StandardOpenOption.CREATE);
	}

}
