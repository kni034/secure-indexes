import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class server {
    private static final String path = "./src/main/resources/serverStorage/";
    private int upperbound;
    private int s;
    private int r;

    public server(int s,int r, int upperbound){
        this.s = s;
        this.r = r;
        this.upperbound = upperbound;


        Path userPath = Paths.get(path);
        try {
            Files.createDirectories(userPath);
        }
        catch (Exception e){
            e.printStackTrace();
        }


    }

    public int getUpperbound() {
        return upperbound;
    }

    public int getS(){
        return s;
    }

    public int getR(){
        return r;
    }

    public HashSet<BigInteger> readBloomFilter(File BloomFilter){

        ObjectInputStream inputStream = null;
        HashSet<BigInteger> bf = new HashSet<>();
        try {
            inputStream = new ObjectInputStream(new FileInputStream(BloomFilter));
            bf = (HashSet<BigInteger>)inputStream.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return bf;
    }

    public void upload(String userID, File file, File bloomFilter){

        Path userPath = Paths.get(path + userID);
        Path originalPath = Paths.get(file.getPath());
        Path bfPath = Paths.get(bloomFilter.getPath());

        String newBfPath = userPath + "/" + bloomFilter.getName();
        String newImagePath = userPath + "/" + file.getName();

        try {
            Files.createDirectories(userPath);

            Files.move(originalPath, userPath.resolve(originalPath.getFileName()),
                    StandardCopyOption.REPLACE_EXISTING);

            Files.move(bfPath, userPath.resolve(bfPath.getFileName()),
                    StandardCopyOption.REPLACE_EXISTING);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        updateLookup(userID, newBfPath, newImagePath);
    }



    public File[] searchAllFiles(String userID, BigInteger[] trapdoor){
        String userPath = path + userID;
        File userDir = new File(userPath);
        userDir.mkdirs();
        File[] files = userDir.listFiles();

        ArrayList<File> returnFiles = new ArrayList<>();
        HashMap<String, String> lookup = readLookup(userID);

        for(String s: lookup.keySet()){
            String bfPath = lookup.get(s);
            if(searchBF(new File(bfPath), trapdoor)){
                returnFiles.add(new File(s));
            }
        }

        File[] returnArray = returnFiles.toArray(new File[0]);
        return returnArray;
    }

    private boolean searchBF(File bloomFilter, BigInteger[] trapdoor){
        HashSet<BigInteger> bf = readBloomFilter(bloomFilter);
        for(BigInteger xi : trapdoor){
            if(!bf.contains(xi)){
                return false;
            }
        }
        return true;
    }

    public void updateLookup(String userID, String bloomFilter, String imageFile){

        HashMap<String, String> lookup = readLookup(userID);
        lookup.put(imageFile, bloomFilter);
        writeLookup(userID, lookup);
    }

    public HashMap<String,String> readLookup(String userID) {

        File toFile = new File(path + userID + "/" + ".lookup");
        HashMap<String, String> lookup;
        try {

            File toRead = toFile;

            FileInputStream fis = new FileInputStream(toRead);
            ObjectInputStream ois = new ObjectInputStream(fis);

            HashMap<String,String> mapInFile=(HashMap<String,String>)ois.readObject();

            ois.close();
            fis.close();

            lookup = mapInFile;
            return lookup;

        } catch(Exception e) {
            System.out.println("user has no lookup file, creating new");
        }
        return new HashMap<String, String>();
    }



    public void writeLookup(String userID, HashMap<String,String> lookup) {

        File lookupFile = new File(path + userID + "/" + ".lookup");

        try {
            FileOutputStream fos = new FileOutputStream(lookupFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(lookup);
            oos.flush();
            oos.close();
            fos.close();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }



    //not a part of the algorithm, only used to clean up serer storage
    public void deleteFile(String userID,String fileName){
        String userPath = path + userID;

        try{
            File f = new File(userPath + fileName);
            File bf = new File(userPath + fileName+".bf");

            f.delete();
            bf.delete();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
