import com.drew.metadata.Directory;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;

public class main {
    private static final String path = "./src/main/resources/";

    public static void main(String[]args){
        ImageProcessor ip = new ImageProcessor();


        File dir = new File(path + "/allImages/");


        for(File f: dir.listFiles()){
            String[] places = ip.readMetaData(f);

            System.out.println(f.getName() + Arrays.toString(places));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }






        /*

        File f = new File(path + "/allImages/"+ "IMG_8346.JPG");

        String[] places = ip.readMetaData(f);

        for(String s: places){
            System.out.println(s);
        }


         */





        /*
        server server = new server(512,3 , 50);

        client alice = new client("Alice","password123 :)", server.getS(), server.getR());
        client bob = new client("Bobbert", "12345", server.getS(), server.getR());

        File f = new File(path +"file1");

        uploadProtocol(alice, server, f);

        searchProtocol(alice, server, "abc");

         */


    }

    public static void uploadProtocol(client client, server server,File file){

        File bloomFilter = client.buildIndex(file, server.getUpperbound(), true);
        File encrypted = client.encryptFile(file);
        server.upload(client.getName(), encrypted, bloomFilter);
    }

    public static void searchProtocol(client client, server server, String searchword){

        BigInteger[] trapdoor = client.trapdoor(searchword);
        File[] files = server.searchAllFiles(client.getName(), trapdoor);
        System.out.println(files.length);

        for(File f:files){
            File dec = client.decryptFile(f);

            if(!client.checkError(searchword, dec)){
                dec.delete();
            }
        }
    }

    /*
    public static void test(client client,client adv, server server, String searchword){
        BigInteger[] trapdoor = adv.trapdoor(searchword);

        File[] files = server.searchAllFiles(client.getName(), trapdoor);
        System.out.println(files.length);
    }

     */

}

