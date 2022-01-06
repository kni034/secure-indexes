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

    /*
    private HashMap<Path,Path> readLookup(String userID){
        Path lookupPath = Paths.get(path + userID +".lookup");


    }

     */

    public File[] searchAllFiles(String userID, BigInteger[] trapdoor){
        String userPath = path + userID;
        File userDir = new File(userPath);
        userDir.mkdirs();
        File[] files = userDir.listFiles();

        ArrayList<File> returnFiles = new ArrayList<>();

        for(File f : files){
            if (f.getName().endsWith(".bf")){
                if(searchBF(f, trapdoor)){
                    //TODO: use hashmap (lookup)
                    returnFiles.add(new File(f.getPath().substring(0,f.getPath().length()-3))); //removes ".bf" from the file name and adds the original file to be returned
                }
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

    //not a part of the algorithm, only used to clean up serer storage
    public void deleteFile(String userName,String fileName){
        String userPath = path + userName;

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
