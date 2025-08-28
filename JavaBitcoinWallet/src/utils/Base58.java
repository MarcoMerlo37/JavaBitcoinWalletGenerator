package utils;

import java.math.BigInteger;

public class Base58 {
    private static final char[] ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
    private static final BigInteger BASE = BigInteger.valueOf(58);

    // -> metodo per la codifica in Base58
    public static String encode(byte[] input) {
        BigInteger intData = new BigInteger(1, input);

        StringBuilder result = new StringBuilder();
        while (intData.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] divmod = intData.divideAndRemainder(BASE);
            intData = divmod[0];
            int digit = divmod[1].intValue();
            result.insert(0, ALPHABET[digit]);
        }

        for (int i = 0; i < input.length && input[i] == 0; i++) {
            result.insert(0, ALPHABET[0]);
        }

        return result.toString();
    }
}

