

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import spec.CalculatorSpec;
import util.StringUtil;
import util.Type;

import org.apache.commons.lang3.ArrayUtils;

class Aes {
	static String start(CalculatorSpec spec) throws Exception{
		Path inputPath = Paths.get(spec.getInputFile().getAbsolutePath());
		Path keyPath = Paths.get(spec.getKeyFile().getAbsolutePath());
		
		String keyHex = Files.readAllLines(keyPath).get(0);
		byte[] keyBytes = StringUtil.hexStringToByteArray(keyHex);
		byte[] input = Files.readAllBytes(inputPath);
		
		//if(spec.getType().equals(Type.ENCRYPT)){
			return encrypt(spec.getInputFile().getName(), input, keyBytes);
		//} else{
			//return decrypt(input, keyBytes);
		//}
				
		//return null;
	}
	
	static String startD(CalculatorSpec spec) throws Exception{
		Path inputPath = Paths.get(spec.getInputFile().getAbsolutePath());
		Path keyPath = Paths.get(spec.getKeyFile().getAbsolutePath());
		
		String keyHex = Files.readAllLines(keyPath).get(0);
		byte[] keyBytes = StringUtil.hexStringToByteArray(keyHex);
		byte[] input = Files.readAllBytes(inputPath);
		
		//if(spec.getType().equals(Type.ENCRYPT)){
		//	return encrypt(spec.getInputFile().getName(), input, keyBytes);
		//} else{
			return decrypt(input, keyBytes);
		//}
				
		//return null;
	}
	
	private static String encrypt(String name, byte[] input, byte[] keyBytes) throws Exception{
		// TODO Auto-generated method stub
		SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
		Cipher cipher = Cipher.getInstance("AES/CTR/PKCS5PADDING");
		
		String hexFileName = StringUtil.toHexString(name + ";");
		byte[] byteFileName = StringUtil.hexStringToByteArray(hexFileName);
		input = ArrayUtils.addAll(byteFileName, input);
		byte[] iv = StringUtil.generateIV();
		
		cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(input);
		CipherInputStream cipherInputStream = new CipherInputStream(byteArrayInputStream, cipher);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		
		int cis;
		while((cis = cipherInputStream.read()) >= 0){
			byteArrayOutputStream.write(cis);
		} 
		
		byte[] cipherText = byteArrayOutputStream.toByteArray();
		
		Path cipherPath = Paths.get("cipher.inc");
		Files.write(cipherPath, ArrayUtils.addAll(iv, cipherText));
		
		return cipherPath.toAbsolutePath().toString();
	}
	
	private static String decrypt(byte[] input, byte[] keyBytes) throws Exception {
		// TODO Auto-generated method stub
		SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
		Cipher cipher = Cipher.getInstance("AES/CTR/PKCS5PADDING");
		
		ByteArrayOutputStream byteArrayOutputStream;
		
		byte[] ivByte = new byte[16];
		System.arraycopy(input, 0, ivByte, 0, 16);
		input = Arrays.copyOfRange(input, 16, input.length);
		
		cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(ivByte));
		byteArrayOutputStream = new ByteArrayOutputStream();
		
		CipherOutputStream cipherOutputStream = new CipherOutputStream(byteArrayOutputStream, cipher);
		cipherOutputStream.write(input);
		cipherOutputStream.close();
		
		byte[] decryptedByte = byteArrayOutputStream.toByteArray();
		int nameDelimiterPos =  StringUtil.findNameDelimiter(decryptedByte);
		decryptedByte = ArrayUtils.removeElement(decryptedByte, (byte)0x3B); // removing ";"
		
		byte[] byteFileName = new byte[nameDelimiterPos];
		System.arraycopy(decryptedByte, 0, byteFileName, 0, nameDelimiterPos);
		String fileName = StringUtil.fromHexString(StringUtil.toHexString(byteFileName));
		byte[] fileBytes = Arrays.copyOfRange(decryptedByte, nameDelimiterPos, decryptedByte.length);
		
		Path decryptedPath = Paths.get("[decrypted] " + fileName);
		Files.write(decryptedPath, fileBytes);
		
		return decryptedPath.toAbsolutePath().toString();
	}

	
}
