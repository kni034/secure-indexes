package Client;

import Server.authenticator;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class Client {
    private final String name;
    private final String password;
    private static final String tmpFolder = "./src/main/resources/";
    private final BitSet[] Kpriv;
    private final int s;
    private final int r;
    private final String masterKey;
    private IvParameterSpec iv;
    private final SecretKeySpec secretKeySpec;
    private boolean recompute = false;
    private UUID uuid;
    private authenticator auth;


    public Client(String name, String password, authenticator auth, int s, int r){
        this.auth = auth;
        this.name = name;
        this.password = password;
        this.s = s;
        this.r = r;
        this.masterKey = CryptoHelper.sha512Hash(name + password);
        secretKeySpec = new SecretKeySpec(masterKey.substring(0,16).getBytes(), "AES");

        Path up = Paths.get(tmpFolder + "/clientStorage/" + name);
        try {
            Files.createDirectories(up);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        iv = new IvParameterSpec(masterKey.substring(16,32).getBytes(StandardCharsets.UTF_8));

        this.Kpriv = keygen(s,r,masterKey);
    }

    public boolean loginToServer(){

        String salt = auth.getSalt(getUid());
        if (salt == null){
            System.out.println("Client: Wrong username or password");
        }

        String hashedPass = CryptoHelper.hashPassword(password.toCharArray(), salt.getBytes(), 10000, 512);
        System.out.println(hashedPass);

        try {
            this.uuid = auth.login(getUid(), hashedPass);
            if(uuid != null) {
                return true;
            }
        }
        catch (Exception e){
            System.out.println("Client: Wrong username or password");
        }
        return false;
    }

    public boolean registerToServer(){

        String salt = auth.createNewUser(getUid());

        if(salt == null){
            return false;
        }

        String hashedPass = CryptoHelper.hashPassword(password.toCharArray(), salt.getBytes(), 10000, 512);
        System.out.println(hashedPass);

        try {
            this.uuid = auth.setPassword(getUid(), hashedPass);
            if(uuid != null) {
                return true;
            }
        }
        catch (Exception e){
            System.out.println("Client: wrong username or password");
        }
        return false;
    }

    protected String getName(){
        return name;
    }
//return new String[]{
//                Client.CryptoHelper.sha512Hash(name),
//                Client.CryptoHelper.sha512Hash(password)
//        };
    //user identifier
    public String getUid(){
        return CryptoHelper.sha512Hash(name);

    }

    public void setRecompute(boolean value){
        this.recompute = value;
    }


    public void searchRange(String from, String to){
        from = formatSearchWord(from);
        to = formatSearchWord(to);

        String[] separatedFrom = from.split(":");
        String[] separatedTo = to.split(":");

        for(int i=0;i<separatedFrom.length;i++){
            if(separatedFrom[i].isBlank() && !separatedTo[i].isBlank()){
                System.out.println("Client: Dates need to have the same format");
                return;
            }
            if(separatedTo[i].isBlank() && !separatedFrom[i].isBlank()){
                System.out.println("Client: Dates need to have the same format");
                return;
            }
        }


        boolean containsDay = !separatedFrom[0].equals("");
        boolean containsMonth = !separatedFrom[1].equals("");
        boolean containsYear = !separatedFrom[2].equals("");

        int fromYear = containsYear ? Integer.parseInt(separatedFrom[2]) : 1;
        int fromMonth = containsMonth ? Integer.parseInt(separatedFrom[1]) : 1;
        int fromDay = containsDay ? Integer.parseInt(separatedFrom[0]) : 1;

        int toYear = containsYear ? Integer.parseInt(separatedTo[2]) : 1;
        int toMonth = containsMonth ? Integer.parseInt(separatedTo[1]) : 1;
        int toDay = containsDay ? Integer.parseInt(separatedTo[0]) : 1;

        LocalDate fromDate = LocalDate.of(fromYear, fromMonth, fromDay);
        LocalDate toDate = LocalDate.of(toYear, toMonth, toDay);

        if(toDate.isBefore(fromDate)){
            LocalDate temp = fromDate;
            fromDate = toDate;
            toDate = temp;
        }

        List<LocalDate> datesBetween = fromDate.datesUntil(toDate).collect(Collectors.toList());
        HashSet<String> datesBetweenString = new HashSet<>();

        for(LocalDate date : datesBetween){

            String yearString = containsYear ? "y"+ date.getYear() : "";

            String monthString = containsMonth ? "m"+ String.format("%02d", date.getMonthValue()) : "";
            String dayString = containsDay ? "d"+ String.format("%02d", date.getDayOfMonth()) : "";

            datesBetweenString.add(dayString + monthString + yearString);
        }
        int filesDownloaded = 0;
        System.out.println(datesBetweenString.size() + " searches");
        for (String value : datesBetweenString) {
            filesDownloaded += searchForWord(value);
        }

        System.out.println("Client: Downloaded " + filesDownloaded + " files from " + getName());
    }

    public void search(String searchWord){
        int filesDownloaded = searchForWord(searchWord);
        System.out.println("Client: Downloaded " + filesDownloaded + " files from " + getName());
    }

    private int searchForWord(String searchWord){

        BigInteger[] trapdoor = trapdoor(searchWord);
        File[] files = auth.search(getUid(), uuid, trapdoor);

        for(File f:files){
            File dec = decryptFile(f);



            //TODO: find solution?
            //Error check does not work for extra words
            /*
            if(!checkError(searchWord, dec)){
                System.out.println(dec.getName() + " was downloaded from a hash collision, it will be deleted");
                dec.delete();
                continue;
            }

             */

            Path userPath = Paths.get(tmpFolder + "/clientStorage/" + getName());
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
        return files.length;
        //System.out.println("Downloaded " + files.length + " files from " + getName());
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


    private boolean checkError(String w, File file){
        String[] words = getWords(file);

        for (String word : words) {
            word = formatSearchWord(word);
            if (word.equals(w)) {
                return true;
            }
        }

        return false;
    }

    private String[] getWords(File file) {
        String[] words;

        if (recompute) {
            ImageProcessor ip = new ImageProcessor();
            words = ip.readMetaData(file);

            HashMap<String, String[]> preComp = readPreComp();
            preComp.put(file.getName(), words);
            writePreComp(preComp);
            waitForAPI();
        } else {
            HashMap<String, String[]> preComp = readPreComp();
            if (preComp.containsKey(file.getName())) {
                words = preComp.get(file.getName());
            } else {
                ImageProcessor ip = new ImageProcessor();
                words = ip.readMetaData(file);
                preComp.put(file.getName(), words);
                writePreComp(preComp);
                waitForAPI();
            }
        }
        return words;
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

    public BigInteger[] codeWord(BigInteger[] tw, String Did){
        BigInteger[] cw = new BigInteger[tw.length];

        for (int i = 0; i < tw.length; i++) {
            byte[] yiByte = CryptoHelper.calculateHMAC(Did.getBytes(), tw[i].toByteArray());
            BigInteger yi = new BigInteger(yiByte);
            cw[i] = yi;
        }

        return cw;
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

    public void upload(File file, String[] extraWords){
        File bloomFilter = buildIndex(file, extraWords, auth.getUpperbound());

        File encrypted = encryptFile(file);

        /*
        File bloomFilterNewName = new File(tmpFolder + encrypted.getName() + ".bf");
        try{
            Files.move(bloomFilter.toPath(), bloomFilterNewName.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

         */
        auth.upload(getUid(), uuid, encrypted, bloomFilter);

    }

    /* Support for txt format
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
     */

    public String formatSearchWord(String word){
        word = word.replaceAll(" ", "");
        word = word.toLowerCase();
        return word;
    }

    public File buildIndex(File file, String[] extraWords, int u){
        ArrayList<String> allWords = new ArrayList<>();
        String[] words = getWords(file);

        if(words.length != 0){
            for(String s: words){
                allWords.add(s);
            }
        }

        if(extraWords.length != 0){
            for(String s: extraWords){
                allWords.add(s);
            }
        }
        //System.out.println(Arrays.toString(words));

        return buildIndexWordsProvided(file, u, allWords.toArray(new String[0]));
    }

    public File buildIndex(File file, int u, String[] words){
        return buildIndexWordsProvided(file, u, words);
    }

    public File buildIndexWordsProvided(File file, int u, String[] words){
        String newFileName = file.getName();
        try {
            newFileName = CryptoHelper.encryptString(file.getName(), secretKeySpec, iv);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        String Did = newFileName + ".bf";
        Set<BigInteger> bloomFilter = new HashSet<>();

        for(String word : words){
            BigInteger[] Tw = trapdoor(word);
            BigInteger[] Cw = codeWord(Tw, newFileName);
            bloomFilter.addAll(Arrays.asList(Cw));
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
        ObjectOutputStream outputStream;
        try {
            outputStream = new ObjectOutputStream(new FileOutputStream(f));
            outputStream.writeObject(bloomFilter);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return f;
    }



    public HashMap<String,String[]> readPreComp() {
        Path preCompPath = Paths.get(tmpFolder + "preComp/");
        try {
            Files.createDirectories(preCompPath);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        File toFile = new File(preCompPath + "/words");
        HashMap<String, String[]> preComputed;
        try {

            File toRead = toFile;

            FileInputStream fis = new FileInputStream(toRead);
            ObjectInputStream ois = new ObjectInputStream(fis);

            HashMap<String,String[]> mapInFile=(HashMap<String,String[]>)ois.readObject();

            ois.close();
            fis.close();

            preComputed = mapInFile;
            return preComputed;

        } catch(Exception e) {

        }
        return new HashMap<String, String[]>();
    }



    public void writePreComp(HashMap<String,String[]> preComp) {

        File preComputed = new File(tmpFolder + "preComp/words");

        try {
            FileOutputStream fos = new FileOutputStream(preComputed);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(preComp);
            oos.flush();
            oos.close();
            fos.close();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void waitForAPI(){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
