package data;

import java.awt.Point;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.List;

import javax.swing.JTextArea;

import digests.MessageDigestWrapper;
import prngs.SecureRandomWrapper;
import utils.HexUtils;

public class ScreenDataHandler {

	// -> Metodo che inizializza l'area di testo con 512 caratteri esadecimali pseudo-casuali
	public static void initializeTextArea(JTextArea area) {
    	SecureRandomWrapper srw;
		try {
			srw = new SecureRandomWrapper("SHA1PRNG");
			byte randomString[] = new byte[256];
	    	srw.fillByteArray(randomString);
	    	String toHex = "";
	    	for (byte b : randomString) {
	    		toHex += HexUtils.toHexString(b);
	    	}
	    	area.setText(toHex);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
	
	// -> Metodo che ricalcola l'hash dell'entropia ogni volta che un nuovo punto viene registrato 
	public static byte[] calculateHash(List<Point> points, List<Timestamp> timestamps, JTextArea textArea) throws NoSuchAlgorithmException {
    	MessageDigestWrapper md = new MessageDigestWrapper("SHA-256");
    	byte[] entropy = new byte[32];
    	String text = textArea.getText();
    	StringBuilder sb = new StringBuilder();
    	sb.append(HexUtils.fromHexString(text));
    	for (int i = 0; i < points.size(); i++) {
    		if (i != 0) sb.append(entropy);
    		sb.append(points.get(i).getX() + points.get(i).getY());
    		sb.append(timestamps.get(i));
    		entropy = md.computeDigest(entropy);
    		
    	}
    	int index = (points.size() + text.length()/64) % (text.length()/64);
    	sb.setLength(0);
    	String newText = HexUtils.toHexString(entropy);
    	newText = text.substring(0, index * 64) + newText + text.substring((index + 1) * 64); // -> cambio 64 caratteri per volta
    	textArea.setText(newText);
    	
    	return entropy;
    }
}
