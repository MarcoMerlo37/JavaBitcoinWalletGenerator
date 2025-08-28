package utils;

import java.math.BigInteger;
import java.security.MessageDigest;

public class KeyGenUtils {

	// -> metodo per fissare la lunghezza a 32 byte 
	public static byte[] toFixedLength32(BigInteger k) {
	    byte[] tmp = k.toByteArray();
	    if (tmp.length == 32) {
	        return tmp;
	    } else if (tmp.length == 33 && tmp[0] == 0x00) {
	        byte[] out = new byte[32];
	        System.arraycopy(tmp, 1, out, 0, 32);
	        return out;
	    } else if (tmp.length < 32) {
	        byte[] out = new byte[32];
	        System.arraycopy(tmp, 0, out, 32 - tmp.length, tmp.length);
	        return out;
	    } else {
	        byte[] out = new byte[32];
	        System.arraycopy(tmp, tmp.length - 32, out, 0, 32);
	        return out;
	    }
	}

	// -> metodo per costruire chiavi WIF compresse
	public static String toWIFCompressed(byte[] priv32, MessageDigest sha256) {
	    // extended payload: 0x80 + priv32 + 0x01
	    byte[] ext = new byte[1 + priv32.length + 1];
	    ext[0] = (byte) 0x80;
	    System.arraycopy(priv32, 0, ext, 1, priv32.length);
	    ext[ext.length - 1] = 0x01; // indica chiave pubblica compressa

	    // checksum
	    byte[] chk = sha256.digest(sha256.digest(ext));
	    byte[] finalBytes = new byte[ext.length + 4];
	    System.arraycopy(ext, 0, finalBytes, 0, ext.length);
	    System.arraycopy(chk, 0, finalBytes, ext.length, 4);

	    return Base58.encode(finalBytes);
	}

	public static String bytesToHex(byte[] bytes) {
	    StringBuilder sb = new StringBuilder();
	    for (byte b : bytes) sb.append(String.format("%02x", b & 0xff));
	    return sb.toString();
	}
}
