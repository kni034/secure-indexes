import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
import java.util.stream.Stream;

public class client {

    private static final String tmpFolder = "./resources/";

    public client(){

    }
    /*
    public static BitSet toBinary(int x, int len) {
        final BitSet buff = new BitSet(len);

        for (int i = len - 1; i >= 0 ; i--)
        {
            int mask = 1 << i;
            buff.set(len - 1 - i,len - 1 - i, (x & mask) != 0 ? true : false);
        }
        return buff;
    }

     */

    public BitSet[] keygen(int s, int r){
        SecureRandom random = new SecureRandom();
        BitSet[] masterKey = new BitSet[r];
        //int upperBound = (int) Math.pow(2,s);

        for(int i=0;i<r;i++){
            BitSet k = new BitSet(s);
            for(int j=0;j<s;j++){
                boolean bit = random.nextBoolean();
                k.set(j,bit);
            }


            masterKey[i] = k;
        }
        return masterKey;
    }

    /*
    private static String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

     */

    /*
    public static byte[] calculateHMAC(byte[] data, byte[] key)
    {
        String HMAC_SHA512 = "HmacSHA512";
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, HMAC_SHA512);
        Mac mac = null;
        try {
            mac = Mac.getInstance(HMAC_SHA512);
            mac.init(secretKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }

        return mac.doFinal(data);
    }

     */
    /*
    public static byte[] calculateHash(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte[] messageDigest = md.digest(data);
        return messageDigest;
    }

     */

    public BigInteger[] trapdoor(BitSet[] Kpriv, String w){
        BigInteger[] Tw = new BigInteger[Kpriv.length];

        for (int i = 0; i < Kpriv.length; i++) {
            byte[] xiByte = CryptoHelper.calculateHMAC(w.getBytes(), Kpriv[i].toByteArray());
            BigInteger xi = new BigInteger(xiByte);
            Tw[i] = xi;
        }

        return Tw;
    }

    public File buildIndex(String Did, BitSet[] key, File file, int u){
        Set bloomFilter = new HashSet<BigInteger>();
        int s = key[0].size();

        ArrayList<String> allWords = new ArrayList<>();

        try {
            Scanner fileReader = new Scanner(file);
            fileReader.hasNextLine();

            while (fileReader.hasNextLine()) {
                String data = fileReader.nextLine();
                String[] words = data.split(" ");

                for (String word : words) {
                    allWords.add(word);
                }
            }

            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] words = new String[allWords.size()];
        words = allWords.toArray(words);


        for(String word : words){
            BigInteger[] Tw = trapdoor(key, word);
            for(BigInteger Twi : Tw){
                bloomFilter.add(Twi);
            }
        }

        while(bloomFilter.size() < u){
            byte[] bytes = new byte[s/8];
            SecureRandom random = new SecureRandom();
            try {
                random.nextBytes(bytes);
                byte[] randomHash = CryptoHelper.calculateHash(bytes);
                bloomFilter.add(new BigInteger(randomHash));

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        //BigInteger[] bf = bloomFilter.toArray(BigInteger);


        System.out.println(Arrays.toString(bloomFilter.toArray()));

        File f = new File(tmpFolder + Did);
        ObjectOutputStream outputStream = null;
        try {
            outputStream = new ObjectOutputStream(new FileOutputStream(f));
            outputStream.writeObject(bloomFilter);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return f;
    }



}
