import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.plaf.multi.MultiSeparatorUI;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class client {
    private String name;
    private static final String tmpFolder = "./src/main/resources/";
    private String userPath;
    private BitSet[] Kpriv;
    private int s;
    private int r;
    private String masterKey;
    private IvParameterSpec iv;
    private SecretKeySpec secretKeySpec;


    public client(String name, String password, int s, int r){
        this.name = name;
        this.s = s;
        this.r = r;
        this.masterKey = CryptoHelper.sha512Hash(name + password);    //salt?
        secretKeySpec = new SecretKeySpec(masterKey.substring(0,16).getBytes(), "AES");

        Path up = Paths.get(tmpFolder + "/clientStorage/" + name);
        try {
            Files.createDirectories(up);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        try {
            iv = new IvParameterSpec(masterKey.substring(16,32).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        this.Kpriv = keygen(s,r,masterKey);
    }
    public void rangeSearch(server server, String from, String to){
        from = formatSearchWord(from);
        to = formatSearchWord(to);


        //splits the date search word and removes an empty string element at position 0 f the array, for both inputs
        String[] fromNumbersOnly = Arrays.asList(from.split("y|m|d")).subList(1, from.split("y|m|d").length).toArray(new String[0]);
        String[] toNumbersOnly = Arrays.asList(to.split("y|m|d")).subList(1, to.split("y|m|d").length).toArray(new String[0]);

        //remove numbers from the strings
        String fromFormatString = from.replaceAll("\\d", "");
        String toFormatString = to.replaceAll("\\d", "");

        if(!fromFormatString.equals(toFormatString)){
            System.out.println("Date formats must be the same for both ends of the range");
            return;
        }


        boolean containsYear = fromFormatString.contains("y");
        boolean containsMonth = fromFormatString.contains("m");
        boolean containsDay = fromFormatString.contains("d");

        int fromYear = containsYear ? Integer.parseInt(fromNumbersOnly[2]) : 1;
        int fromMonth = containsMonth ? Integer.parseInt(fromNumbersOnly[1]) : 1;
        int fromDay = containsDay ? Integer.parseInt(fromNumbersOnly[0]) : 1;

        int toYear = containsYear ? Integer.parseInt(toNumbersOnly[2]) : 1;
        int toMonth = containsMonth ? Integer.parseInt(toNumbersOnly[1]) : 1;
        int toDay = containsDay ? Integer.parseInt(toNumbersOnly[0]) : 1;

        LocalDate fromDate = LocalDate.of(fromYear, fromMonth, fromDay);
        LocalDate toDate = LocalDate.of(toYear, toMonth, toDay);

        if(toDate.isBefore(fromDate)){
            LocalDate temp = fromDate;
            fromDate = toDate;
            toDate = temp;
        }

        List<LocalDate> datesBetween = fromDate.datesUntil(toDate).collect(Collectors.toList());
        HashSet<String> datesBetweenString = new HashSet<String>();

        for(LocalDate date : datesBetween){

            String yearString = containsYear ? "y"+ date.getYear() : "";

            String monthString = containsMonth ? "m"+ String.format("%02d", date.getMonthValue()) : "";
            String dayString = containsDay ? "d"+ String.format("%02d", date.getDayOfMonth()) : "";

            datesBetweenString.add(dayString + monthString + yearString);
        }
        Iterator<String> dateIter = datesBetweenString.iterator();
        while(dateIter.hasNext()){
            search(server, dateIter.next());
        }


    }

    public void search(server server, String searchWord){

        BigInteger[] trapdoor = trapdoor(searchWord);
        File[] files = server.searchAllFiles(getName(), trapdoor);

        for(File f:files){
            File dec = decryptFile(f);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(!checkError(searchWord, dec)){
                dec.delete();
            }

            Path userPath = Paths.get(tmpFolder + "/clientStorage/" + name);
            Path originalPath = Paths.get(dec.getPath());

            try {
                Files.createDirectories(userPath);

                Files.move(originalPath, userPath.resolve(originalPath.getFileName()),
                        StandardCopyOption.REPLACE_EXISTING);

            }
            catch (Exception e){
                e.printStackTrace();
            }


        }

    }


    public String getName(){
        return name;
    }

    public BitSet[] keygen(int s, int r, String masterKey){
        Random random = new Random(masterKey.hashCode());
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
        ImageProcessor ip = new ImageProcessor();
        String[] words = ip.readMetaData(file);

        for (String word : words) {
            word = formatSearchWord(word);
            if(word.equals(w)){
                return true;
            }
        }

        return false;
    }


    public BigInteger[] trapdoor(String w){
        BigInteger[] Tw = new BigInteger[Kpriv.length];

        String formattedWord = formatSearchWord(w);
        //System.out.println(formattedWord);

        for (int i = 0; i < Kpriv.length; i++) {
            byte[] xiByte = CryptoHelper.calculateHMAC(formattedWord.getBytes(), Kpriv[i].toByteArray());
            BigInteger xi = new BigInteger(xiByte);
            Tw[i] = xi;
        }
        //System.out.println(Arrays.toString(Tw));

        return Tw;
    }

    public File encryptFile(File file){
        String newFileName = file.getName();
        try {
            newFileName = CryptoHelper.encryptString(file.getName(), secretKeySpec, iv);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        File encrypted = new File(tmpFolder + newFileName);
        CryptoHelper.encryptFile(file, encrypted, iv, secretKeySpec);

        return encrypted;
    }

    public File decryptFile(File file) {

        String originalFileName = file.getName();
        try{
            originalFileName = CryptoHelper.decryptString(file.getName(), secretKeySpec, iv);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        File clear = new File(tmpFolder + originalFileName);
        CryptoHelper.decryptFile(file, clear, iv, secretKeySpec);

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

    public String formatSearchWord(String word){
        word = word.replaceAll(" ", "");
        word = word.toLowerCase();
        return word;
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
        String Did = file.getName() + ".bf";
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
                byte[] randomHash = CryptoHelper.calculateHash(bytes);
                bloomFilter.add(new BigInteger(randomHash));

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        //System.out.println(Arrays.toString(bloomFilter.toArray()));

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
