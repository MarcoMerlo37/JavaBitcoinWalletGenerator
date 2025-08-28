package prngs;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class SecureRandomWrapper {

    private final SecureRandom sr;

    /*
     * Given a PRNG algorithm, sets the sr variable to be an instance of SecureRandom.
     * Total suggested lines: 1.
     */
    public SecureRandomWrapper(String algorithm) throws NoSuchAlgorithmException {

    	sr = SecureRandom.getInstance(algorithm);

    }

    /*
     * Given a seed, this method changes the seed of the sr variable.
     * Total suggested lines: 1.
     */
    public void changeSeed(int seed) {
    	
    	sr.setSeed(seed);

    }
    
    public void changeSeed(byte[] seed) {
    	sr.setSeed(seed);
    }

    /*
     * Retrieves a random integer from the nested SecureRandom variable.
     * Total suggested lines: 1.
     */
    public int getRandomInt() {

    	return sr.nextInt();

    }
    

    // To be used in ciphers
    /*
     * Given a byte array in input, it fills it with random values.
     * Total suggested lines: 1.
     */
    public void fillByteArray(byte[] input) {

    	sr.nextBytes(input);

    }

    /*
     * Getter method to retrieve the nested SecureRandom variable.
     * Total suggested lines: 1.
     */
    public SecureRandom getSecureRandom() {

    	return sr;

    }
}
