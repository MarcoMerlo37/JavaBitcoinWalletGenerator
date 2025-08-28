package mainclass;

import javax.swing.*;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.math.ec.ECPoint;

import com.google.zxing.WriterException;

import data.ScreenDataHandler;
import digests.MessageDigestWrapper;
import prngs.SecureRandomWrapper;
import utils.Base58;
import utils.QRCodeUtils;
import utils.KeyGenUtils;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BitcoinWalletGenerator extends JFrame {
    
	private final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	private List<Point> points = Collections.synchronizedList(new ArrayList<>());			// -> punti toccati nello schermo
	private List<Timestamp> timestamps = Collections.synchronizedList(new ArrayList<>());	// -> timestamp di ogni punto
	private MousePanel panel;
	private JProgressBar bar = new JProgressBar(0, 100);;
	private Timer timer; // -> serve a creare punti e non linee uniche durante il movimento del mouse
	private static final long serialVersionUID = 1L;
	private byte[] entropy = new byte[32];	// -> 32 byte di entropia
	private int i = 0;			// -> contatore per controllare quante volte la chiave privata viene generata con la stessa entropia
	public BitcoinWalletGenerator() {
		
        setTitle("Generate your Bitcoin Wallet");
        setSize(screenSize.width, screenSize.height);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setVisible(true);
        
	}
	
	// -> questo metodo si occupa di generare lo schermo in cui vengono tracciati i punti a partire dai movimenti del mouse
	public void generatePoints() {
		
		panel = new MousePanel();
		
		JTextArea textArea = new JTextArea("");
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);
		textArea.setEditable(false);
		textArea.setOpaque(false);
		
		textArea.setSize(screenSize.width - 400, screenSize.height);
		textArea.setFont(new Font("Monospaced", Font.BOLD, 20));
		panel.add(textArea);
		
		JTextArea txt = new JTextArea("\n\nMove your mouse around the screen");
		txt.setFont(new Font("Monospaced", Font.BOLD, 70));
		txt.setEditable(false);
		txt.setOpaque(false);
		txt.setSize(screenSize.width - 400, screenSize.height);
		panel.add(txt);
		
		add(panel, BorderLayout.CENTER);
		ScreenDataHandler.initializeTextArea(textArea);	// -> metodo di inizializzazione dell'area di testo
        
        
        
        bar = new JProgressBar(0, 100);
        bar.setStringPainted(true);
        add(bar, BorderLayout.NORTH);
        
        timer = new Timer();
        timer.scheduleAtFixedRate(new MouseTrackingTask(), 0, 100);
        
        
	}
	
	public MousePanel getPanel() {
		return panel;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public class MousePanel extends JPanel{

		private static final long serialVersionUID = 1L;
		
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setColor(Color.GREEN);
			for (Point p : points)
				g.fillOval(p.x - 2, p.y - 2, 8, 8);
		}
    }
	
	
	class MouseTrackingTask extends TimerTask{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Point position = MouseInfo.getPointerInfo().getLocation();
			
			Point panelLoc = panel.getLocationOnScreen();
            int x = position.x - panelLoc.x;
            int y = position.y - panelLoc.y;
            Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
            
            // -> Controllo che il punto sia nei limiti dello schermo, che non sia ripetuto, 
            //	  che il timestamp non sia nullo e che punti adiacenti siano distanti
            if (x >= 0 && y >= 0 &&
                x <= panel.getWidth() && y <= panel.getHeight() &&
                !points.contains(new Point(x, y)) && timestamp != null && 
                checkDistance(x, y, points)) {
            	timestamps.add(timestamp);
            	points.add(new Point(x, y));
            	try {
            		byte[] entropyBytes = ScreenDataHandler.calculateHash(points, timestamps, ((JTextArea) panel.getComponent(0)));
            		setEntropy(entropyBytes);
					
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                bar.setValue(points.size());
                if (points.size() == 100)	{
                	timer.cancel();
                	bar.setVisible(false);
                	
                	try {
                		// -> Una volta collezionati 100 punti genero le chiavi con l'entropia ottenuta
						generateKeys(entropy);
					} catch (NoSuchAlgorithmException | WriterException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                }
                else {
                	bar.setValue(points.size());
                	panel.repaint();
                }
                
            } 
		}
		
	}

    public List<Point> getPoints(){
    	synchronized (points) {
            return new ArrayList<>(points); 
        }
    }
    
    public List<Timestamp> getTimestamps(){
    	synchronized (timestamps) {
            return new ArrayList<>(timestamps); 
        }
    }
    
    
    
    // -> metodo per verificare che due punti consecutivi siano sufficientemente distanti
    private boolean checkDistance(int x, int y, List<Point> points) {
    	if (points.size() == 0) return true;
    	
    	Point p = points.get(points.size() - 1);
    	return (
    			(x >= p.x + 7 || x <= p.x - 7) &&
    			(y >= p.y + 7 || y <= p.y -7)
    			);
    }

	public byte[] getEntropy() {
		return entropy;
	}

	public void setEntropy(byte[] entropy) {
		this.entropy = entropy;
	}
	
	// -> metodo che genera chiave pubblica, chiave privata e indirizzo Bitcoin a partire dall'entropia generata in precedenza
	public void generateKeys(byte[] entropy) throws NoSuchAlgorithmException, WriterException {
		
		// -> Utilizzo della libreria BouncyCastle per implementare ECC secp256k1 (curva ellittica)
		var curveParams = CustomNamedCurves.getByName("secp256k1");
        ECDomainParameters domain = new ECDomainParameters(
                curveParams.getCurve(),
                curveParams.getG(),
                curveParams.getN(),
                curveParams.getH()
        );

        
        // -> Hash dell'entropia per ottenere un seed di 32 byte / 256 bit
        MessageDigestWrapper sha256 = new MessageDigestWrapper("SHA-256");
        byte[] seed = sha256.computeDigest(entropy);
        
        SecureRandomWrapper srw = new SecureRandomWrapper("SHA1PRNG");
        srw.changeSeed(seed);
        
        BigInteger privateKey;
        byte[] byteKey = new byte[32]; 
        do {
        	// -> Ogni volta che si genera un nuovo indirizzo vengono caricati i 32 byte successivi
        	for (int j = 0; j <= i; j++)
        		srw.getSecureRandom().nextBytes(byteKey);             
            privateKey = new BigInteger(1, byteKey);    // -> utilizzo il costruttore con l'int 1 per far si che risulti un valore positivo
        } while (privateKey.equals(BigInteger.ZERO) || privateKey.compareTo(domain.getN()) >= 0);

        
        // -> PublicKey = PrivateKey * G (punto generatore sulla curva ellittica)
        ECPoint q = domain.getG().multiply(privateKey).normalize();
        byte[] publicKeyCompressed = q.getEncoded(true);   // -> formato compresso (33 byte)
        


        // -> Hash SHA-256 della chiave pubblica
        
        byte[] sha256Hash = sha256.computeDigest(publicKeyCompressed);

        // -> Hash RIPEMD-160 dell'hash precedente
        RIPEMD160Digest ripemd160 = new RIPEMD160Digest();
        ripemd160.update(sha256Hash, 0, sha256Hash.length);
        byte[] pubKeyHash = new byte[ripemd160.getDigestSize()];
        ripemd160.doFinal(pubKeyHash, 0);

        // -> Aggiungo version byte 0x00
        byte[] versionedPayload = new byte[1 + pubKeyHash.length];
        versionedPayload[0] = 0x00;
        System.arraycopy(pubKeyHash, 0, versionedPayload, 1, pubKeyHash.length);

        // -> Aggiungo il checksum (primi 4 bytes di SHA-256(SHA-256(x))
        byte[] checksum = sha256.computeDigest(sha256.computeDigest(versionedPayload));
        byte[] addressBytes = new byte[versionedPayload.length + 4];
        System.arraycopy(versionedPayload, 0, addressBytes, 0, versionedPayload.length);
        System.arraycopy(checksum, 0, addressBytes, versionedPayload.length, 4);

        // -> Base58 encoding
        String address = Base58.encode(addressBytes);
        
        // -> Fisso la lunghezza della chiave privata a 32 byte e la comprimo in formato WIF
        byte[] priv32 = KeyGenUtils.toFixedLength32(privateKey);
        String wifCompressed = KeyGenUtils.toWIFCompressed(priv32, sha256.getMessageDigest());
        
        System.out.println("Private key (hex): " + KeyGenUtils.bytesToHex(priv32));
        System.out.println("WIF (compressed): " + wifCompressed);
        System.out.println("Public key (compressed hex): " + KeyGenUtils.bytesToHex(publicKeyCompressed));
        System.out.println("Bitcoin address: " + address);
        
        
        
        
        
        loadKeys(
        		wifCompressed,
        		address
        		);
	}
	
	
	// -> metodo per caricare chiave e indirizzo sulla schermata
	public void loadKeys(String base58PrivKey, String base58BitcoinAddress) throws WriterException {

		
		remove(panel);
		
		
		JPanel keysPanel = new JPanel();
		keysPanel.setLayout(new BoxLayout(keysPanel, BoxLayout.Y_AXIS));
		keysPanel.setBackground(Color.white);
		JPanel panel1 = new JPanel();
		JPanel panel2 = new JPanel();
		JPanel panel3 = new JPanel();
		
		JLabel privKeyLabel = new JLabel("Private key: " + base58PrivKey);
		privKeyLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
		JLabel pubKeyComprLabel = new JLabel("Bitcoin address: " + base58BitcoinAddress);
		pubKeyComprLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
		
		privKeyLabel.setAlignmentX(0);
		pubKeyComprLabel.setAlignmentX(0);
		
		panel1.add(privKeyLabel);
		JLabel privKeyQR = new JLabel(new ImageIcon(QRCodeUtils.generateQRCode(base58PrivKey, 150))); // -> QRCode chiave privata
		privKeyQR.setAlignmentX(1);
		panel1.add(privKeyQR);
		keysPanel.add(panel1);
		
		
		panel2.add(pubKeyComprLabel);
		JLabel pubKeyComprQR = new JLabel(new ImageIcon(QRCodeUtils.generateQRCode(base58BitcoinAddress, 150))); // -> QRCode indirizzo
		pubKeyComprQR.setAlignmentX(1);
		panel2.add(pubKeyComprQR);
		keysPanel.add(panel2);
		
		
		
		add(keysPanel, BorderLayout.CENTER);
		
		// -> pulsante per la generazione di nuovi indirizzi
		JButton button = new JButton("Generate new address");
		panel3.add(button);
		button.setSize(50, 50);
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					remove(keysPanel);
					i++;
					generateKeys(entropy);
				} catch (NoSuchAlgorithmException | WriterException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
		});
		
		keysPanel.add(panel3);
		
		repaint();
		revalidate();
		
	}
	
	// -> main
	public static void main(String[] args) throws NoSuchAlgorithmException, WriterException {
			
		BitcoinWalletGenerator screen = new BitcoinWalletGenerator();
		screen.generatePoints();
		
	}

}
