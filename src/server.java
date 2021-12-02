import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class server {
    private static final String path = "./resources/serverStorage";
    //private HashMap<String, ArrayList<Object>> table;

    public server(){


    }

    /*
    public void writeTable(HashMap<String, ArrayList<Object>> table){
        File tableFile = new File(path + "table");

        try {
            FileOutputStream fos = new FileOutputStream(tableFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(table);
            oos.flush();
            oos.close();
            fos.close();

        } catch(Exception e) {
            e.printStackTrace();
        }

    }

     */

    /*
    public void setTable(File table){


        try {
            File toRead = table;

            FileInputStream fis = new FileInputStream(toRead);
            ObjectInputStream ois = new ObjectInputStream(fis);

            HashMap<String,ArrayList<Object>> mapInFile=(HashMap<String,ArrayList<Object>>)ois.readObject();

            ois.close();
            fis.close();

            //this.table = mapInFile;

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

     */

    public HashSet<BigInteger> readBloomFilter(File BloomFilter){
        //String name = BloomFilter.getName();

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

        try {
            Files.createDirectories(userPath);

            Files.move(originalPath, userPath.resolve(originalPath.getFileName()),
                    StandardCopyOption.REPLACE_EXISTING);

            Files.move(bfPath, userPath.resolve(originalPath.getFileName()+".bf"),
                    StandardCopyOption.REPLACE_EXISTING);
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    public boolean search(File bloomFilter, BigInteger[] trapdoor){
        HashSet<BigInteger> bf = readBloomFilter(bloomFilter);
        for(BigInteger xi : trapdoor){
            if(!bf.contains(xi)){
                return false;
            }
        }
        return true;
    }


}
