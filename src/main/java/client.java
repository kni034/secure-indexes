import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.util.*;

public class client {
    private String name;
    private static final String tmpFolder = "./src/main/resources/";
    private BitSet[] Kpriv;
    private int s;
    private int r;
    private CryptoHelper ch = new CryptoHelper();
    private String masterKey;
    private IvParameterSpec iv;
    private SecretKeySpec secretKeySpec;


    public client(String name, String password, int s, int r){
        this.name = name;
        this.s = s;
        this.r = r;
        this.masterKey = ch.sha512Hash(name + password);    //salt?
        secretKeySpec = new SecretKeySpec(masterKey.substring(0,16).getBytes(), "AES");

        try {
            iv = new IvParameterSpec(masterKey.substring(16,32).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        this.Kpriv = keygen(s,r,masterKey);
    }

    public String getName(){
        return name;
    }

    public BitSet[] keygen(int s, int r, String masterKey){
        SecureRandom random = new SecureRandom(masterKey.getBytes());
        BitSet[] Kpriv = new BitSet[r];

        for(int i=0;i<r;i++){
            BitSet k = new BitSet(s);
            for(int j=0;j<s;j++){
                boolean bit = random.nextBoolean();
                k.set(j,bit);
            }


            Kpriv[i] = k;
        }
        return Kpriv;
    }

    public boolean checkError(String w, File file){
        try {
            Scanner fileReader = new Scanner(file);
            fileReader.hasNextLine();

            while (fileReader.hasNextLine()) {
                String data = fileReader.nextLine();
                String[] words = data.split(" ");

                for (String word : words) {
                    if(word.equals(w)){
                        return true;
                    }
                }
            }

            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }



    public BigInteger[] trapdoor(String w){
        BigInteger[] Tw = new BigInteger[Kpriv.length];

        for (int i = 0; i < Kpriv.length; i++) {
            byte[] xiByte = ch.calculateHMAC(w.getBytes(), Kpriv[i].toByteArray());
            BigInteger xi = new BigInteger(xiByte);
            Tw[i] = xi;
        }

        return Tw;
    }

    public File encryptFile(File file){

        File encrypted = new File(tmpFolder + file.getName()+".enc");
        ch.encryptFile(file, encrypted, iv, secretKeySpec);

        return encrypted;
    }

    public File decryptFile(File file){
        File clear = new File(tmpFolder + file.getName().substring(0,file.getName().length()-4) + ".dec"); //adds .dec instead of removing extension, used for testing
        //File clear = new File(tmpFolder + file.getName().substring(0,file.getName().length()-4));
        ch.decryptFile(file, clear, iv, secretKeySpec);

        return clear;
    }



    private String[] readWords(File file){
        ArrayList<String> allWords = new ArrayList<>(); //remove duplicates

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
        return words;
    }

    public File buildIndex(File file, int u, boolean image){
        String[] words;
        if(image){
            ImageProcessor ip = new ImageProcessor();
            words = ip.readMetaData(file);
        }
        else {
            words = readWords(file);
        }
        return buildIndexWordsProvided(file, u, words);
    }

    public File buildIndex(File file, int u, String[] words){
        return buildIndexWordsProvided(file, u, words);
    }

    public File buildIndexWordsProvided(File file, int u, String[] words){
        String Did = getName();
        Set bloomFilter = new HashSet<BigInteger>();

        for(String word : words){
            BigInteger[] Tw = trapdoor(word);
            for(BigInteger Twi : Tw){
                bloomFilter.add(Twi);
            }
        }

        //(upperbound u - unique words v) * number of hashes r
        //u = number of trapdoors(words), not entries in bloomfilter. (number of entries = u*r)
        while(bloomFilter.size() < u*r){
            byte[] bytes = new byte[s/8];
            SecureRandom random = new SecureRandom();
            try {
                random.nextBytes(bytes);
                byte[] randomHash = ch.calculateHash(bytes);
                bloomFilter.add(new BigInteger(randomHash));

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

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
