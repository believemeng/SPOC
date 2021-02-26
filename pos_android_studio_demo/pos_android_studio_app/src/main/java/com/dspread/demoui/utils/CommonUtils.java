package com.dspread.demoui.utils;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.dspread.demoui.R;
import com.dspread.demoui.net.retrofitUtil.RetrofitAuthUtil;
import com.dspread.xpos.utils.AESUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

public class CommonUtils {
	public static final String SHA_256 = "SHA-256";
	private static final String TAG = CommonUtils.class.getSimpleName();
	private static String radioCheckValue = "Verify";

	// check Service is runing
	public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
		try {
			ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			if (manager != null) {
				for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
					if (serviceClass.getName().equals(service.service.getClassName())) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	public static String getSignature(String s) {
		String signature = null;
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(s.getBytes(), 0, s.length());
			signature = new BigInteger(1, md5.digest()).toString(16);
			while (signature.length() < 32) {
				signature = "0" + signature;

			}
			System.out.println("Signature: " + signature);

		} catch (final NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return signature;
	}

	/**
	 * Created SHA256 of input
	 * @param input (assumes UTF-8 string)
	 * @return
	 */
	public static byte[] hash(String input){
		if(!TextUtils.isEmpty(input)) {
			try {
				byte[] inputBytes = input.getBytes("UTF-8");
				return hash(inputBytes);
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "problem hashing \"" + input + "\" " + e.getMessage(), e);
			}
		}
		return null;
	}

	/**
	 * Created SHA256 of input
	 * @param input
	 * @return
	 */
	public static byte[] hash(byte[] input){
		if(input!=null) {
			final MessageDigest digest;
			try {
				digest = MessageDigest.getInstance(SHA_256);
				byte[] hashedBytes = input;
				digest.update(hashedBytes, 0, hashedBytes.length);
				return hashedBytes;
			} catch (NoSuchAlgorithmException e) {
				Log.e(TAG, "problem hashing \"" + input + "\" " + e.getMessage(), e);
			}
		}else{
			Log.w(TAG, "hash called with null input byte[]");
		}
		return null;
	}

	public static String getSigningKeyFingerprint(Context ctx) {
		String result = null;
		try {
			byte[] certEncoded = getSigningKeyCertificate(ctx);
			MessageDigest md = MessageDigest.getInstance("SHA1");
			byte[] publicKey = md.digest(certEncoded);
			result = byte2HexFormatted(publicKey);
		} catch (Exception e) {
			Log.w(TAG, e);
		}
		return result;
	}



	/**
	 * Gets the encoded representation of the first signing cerificated used to sign current APK
	 * @param ctx
	 * @return
	 */
	private static byte[] getSigningKeyCertificate(Context ctx) {
		try {
			PackageManager pm = ctx.getPackageManager();
			String packageName = ctx.getPackageName();
			int flags = PackageManager.GET_SIGNATURES;
			PackageInfo packageInfo = pm.getPackageInfo(packageName, flags);
			Signature[] signatures = packageInfo.signatures;

			if(signatures!=null && signatures.length>=1) {
				//takes just the first signature, TODO: handle multi signed apks
				byte[] cert = signatures[0].toByteArray();
				InputStream input = new ByteArrayInputStream(cert);
				CertificateFactory cf = CertificateFactory.getInstance("X509");
				X509Certificate c = (X509Certificate) cf.generateCertificate(input);
				return c.getEncoded();

			}
		} catch (Exception e) {
			Log.w(TAG, e);
		}
		return null;
	}

	private static String byte2HexFormatted(byte[] arr) {
		StringBuilder str = new StringBuilder(arr.length * 2);
		for (int i = 0; i < arr.length; i++) {
			String h = Integer.toHexString(arr[i]);
			int l = h.length();
			if (l == 1) h = "0" + h;
			if (l > 2) h = h.substring(l - 2, l);
			str.append(h.toUpperCase());
			if (i < (arr.length - 1)) str.append(':');
		}
		return str.toString();
	}

	//将hex值转为ascii码
	public static  String convertHexToString(String hex){

		StringBuilder sb = new StringBuilder();
		StringBuilder temp = new StringBuilder();

		//49204c6f7665204a617661 split into two characters 49, 20, 4c...
		for( int i=0; i<hex.length()-1; i+=2 ){
			//grab the hex in pairs
			String output = hex.substring(i, (i + 2));
			//convert hex to decimal
			int decimal = Integer.parseInt(output, 16);
			//convert the decimal to character
			sb.append((char)decimal);

			temp.append(decimal);
		}
		return sb.toString();
	}

	public static List<String> calcApkCertificateDigests(Context context, String packageName) {
		List<String> encodedSignatures = new ArrayList<String>();

		// Get signatures from package manager
		PackageManager pm = context.getPackageManager();
		PackageInfo packageInfo;
		try {
			packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			return encodedSignatures;
		}
		Signature[] signatures = packageInfo.signatures;

		// Calculate b64 encoded sha256 hash of signatures
		for (Signature signature : signatures) {
			try {
				MessageDigest md = MessageDigest.getInstance(SHA_256);
				md.update(signature.toByteArray());
				byte[] digest = md.digest();
				encodedSignatures.add(Base64.encodeToString(digest, Base64.NO_WRAP));
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}
		return encodedSignatures;
	}

	public static String calcApkDigest(final Context context) {
		byte[] hashed2 = getApkFileDigest(context);
		String encoded2 = Base64.encodeToString(hashed2, Base64.NO_WRAP);
		return encoded2;
	}

	private static long getApkFileChecksum(Context context) {
		String apkPath = context.getPackageCodePath();
		Long chksum = null;
		try {
			// Open the file and build a CRC32 checksum.
			FileInputStream fis = new FileInputStream(new File(apkPath));
			CRC32 chk = new CRC32();
			CheckedInputStream cis = new CheckedInputStream(fis, chk);
			byte[] buff = new byte[80];
			while (cis.read(buff) >= 0) ;
			chksum = chk.getValue();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return chksum;
	}


	private static byte[] getApkFileDigest(Context context) {
		String apkPath = context.getPackageCodePath();
		try {
			return getDigest(new FileInputStream(apkPath), SHA_256);
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		}
		return null;
	}

	public static final int BUFFER_SIZE = 2048;

	public static byte[] getDigest(InputStream in, String algorithm) throws Throwable {
		MessageDigest md = MessageDigest.getInstance(algorithm);
		try {
			DigestInputStream dis = new DigestInputStream(in, md);
			byte[] buffer = new byte[BUFFER_SIZE];
			while (dis.read(buffer) != -1) {
				//
			}
			dis.close();
		} finally {
			in.close();
		}
		return md.digest();
	}

	static final String HEXES = "0123456789ABCDEF";
	public static String byteArray2Hex(byte[] raw) {
		if (raw == null) {
			return null;
		}
		final StringBuilder hex = new StringBuilder(2 * raw.length);
		for (final byte b : raw) {
			hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
		}
		return hex.toString();
	}

	public static String buildCvmPinBlock(String randomData,String panToken,String aesKey,String pin){
		//iso-format4 pinblock
		int pinLen = pin.length();
		pin = "4"+pinLen+pin;
		for(int i = 0 ; i < 14 - pinLen ; i++){
			pin = pin + "A";
		}
		pin+=randomData.substring(0,16);
		String panBlock = "";
		panToken = CommonUtils.convertHexToString(panToken);
		int panLen = panToken.length();
		int m = 0;
		if(panLen < 12){
			panBlock = "0";
			for(int i = 0 ; i <12- panLen; i++){
				panBlock += "0";
			}
			panBlock = panBlock + panToken+ "0000000000000000000";
		}else{
			m = panToken.length() - 12;
			panBlock = m + panToken ;
			for(int i = 0 ; i < 31 - panLen ; i ++){
				panBlock += "0";
			}
		}
		TRACE.i("aeskey = "+aesKey+" pin ="+pin);
		String pinBlock1 = AESUtil.encrypt(aesKey,pin);
		TRACE.i("pinBlock1 = "+pinBlock1);
		pin = xor16(HexStringToByteArray(pinBlock1),HexStringToByteArray(panBlock));
		String pinBlock2 = AESUtil.encrypt(aesKey,pin);
		return pinBlock2;
	}

	//16 byte xor
	public static String xor16(byte[] src1, byte[] src2){
		byte[] results = new byte[16];
		for (int i = 0; i < results.length; i++){
			results[i] = (byte)(src1[i] ^ src2[i]);
		}
		return byteArray2Hex(results);
	}

	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	/**
	 * 16进制格式的字符串转成16进制byte 44 --> byte 0x44
	 *
	 * @param hexString
	 * @return
	 */
	public static byte[] HexStringToByteArray(String hexString) {//
		if (hexString == null || hexString.equals("")) {
			return new byte[]{};
		}
		if (hexString.length() == 1 || hexString.length() % 2 != 0) {
			hexString = "0" + hexString;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	public static byte[] whiteBoxAESEncrypt(String str){
		byte[] a = new byte[10];
		return a;
	}

	public static Dialog dialog;
	public static void showMyErrorDialog(final Context context, String title, String content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(content);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CheckCvmAppStateUtil.killAppProcess(context);
                dialog.dismiss();
            }
        });
        dialog = builder.create();
        dialog.setCancelable(false);
        if(!dialog.isShowing()) {
			dialog.show();
		}
	}

	public static void showMyDialog(final Context context, String title, String content) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
		builder.setMessage(content);

		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		Dialog dialog = builder.create();
		dialog.setCancelable(false);
		dialog.show();
	}

	public static View getRadioView(Context context, List<String> list){
		LinearLayout view = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.dialog_lin,null,false);
		RadioGroup group = view.findViewById(R.id.group);
		for(int i = 0 ;i<list.size() ; i++){
			final RadioButton radiobtnLin = (RadioButton) LayoutInflater.from(context).inflate(R.layout.item_radiobutton,null,false);
			radiobtnLin.setText(list.get(i));
			radiobtnLin.setId(i);
			if(radioCheckValue != null && !radioCheckValue.equals("")){
				if(radioCheckValue.equals(radiobtnLin.getText().toString())){
					radiobtnLin.setChecked(true);
				}
			}else{
				if(i == 0 ){
					radioCheckValue = radiobtnLin.getText().toString();
					radiobtnLin.setChecked(true);
				}
			}
			radiobtnLin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
					if(b){
						radioCheckValue = radiobtnLin.getText().toString();
					}
				}
			});
			group.addView(radiobtnLin);
		}
		return view;
	}

	public static void showRadioButtonDialog(final Context context, String title,  List<String> list){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
		builder.setView(getRadioView(context,list));
		builder.setPositiveButton(context.getString(R.string.all_ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(radioCheckValue != null && !radioCheckValue.equals("")){
					dialog.dismiss();
					if(radioCheckValue.equals("Verify")){
						mlistener.onVerifyStatusListener(true);
					}else if(radioCheckValue.equals("No Verify")){
						mlistener.onVerifyStatusListener(false);
					}
				}
			}
		});
		Dialog dialog = builder.create();
		dialog.setCancelable(false);
		dialog.show();
	}

	public interface ChooseVerifyStatusInterface{
		void onVerifyStatusListener(boolean isVerify);
	}

	private static ChooseVerifyStatusInterface mlistener;
	public static void setVerifyStatusListener(ChooseVerifyStatusInterface listener){
		mlistener = listener;
	}

}


