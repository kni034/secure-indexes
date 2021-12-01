import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

public class server {
    private static final String path = "./resources/serverStorage";
    private HashMap<String, ArrayList<Object>> table;

    public server(){


    }

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

    public void setTable(File table){


        try {
            File toRead = table;

            FileInputStream fis = new FileInputStream(toRead);
            ObjectInputStream ois = new ObjectInputStream(fis);

            HashMap<String,ArrayList<Object>> mapInFile=(HashMap<String,ArrayList<Object>>)ois.readObject();

            ois.close();
            fis.close();

            this.table = mapInFile;

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public BigInteger[] readBloomFilter(File BloomFilter){
        String name = BloomFilter.getName();
        ObjectInputStream inputStream = null;
        BigInteger[] bf = null;
        try {
            inputStream = new ObjectInputStream(new FileInputStream(BloomFilter));
            bf = (BigInteger[])inputStream.readObject();
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

        }
        catch (Exception e){
            e.printStackTrace();
        }

        ArrayList<Object> info = new ArrayList<>();
        info.add(readBloomFilter(bloomFilter));
        info.add(userPath);

        table.put(file.getName(),info);

    }
}
