import com.drew.metadata.Directory;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;

public class main {
    private static final String path = "./src/main/resources/";

    public static void main(String[]args){
        ImageProcessor ip = new ImageProcessor();
        File dir = new File(path + "/allImages/");

        server server = new server(512,3 , 50);
        client alice = new client("Alice","password123 :)", server.getS(), server.getR());
        client bob = new client("Bobbert", "12345", server.getS(), server.getR());

        File f = new File(dir + "/pond.jpg");
        uploadProtocol(alice, server, f);

        //uploadAll(alice, server);


        //searchProtocol(alice, server, "norway");


    }

    public static void uploadProtocol(client client, server server,File file){


        File bloomFilter = client.buildIndex(file, server.getUpperbound(), true);

        File encrypted = client.encryptFile(file);

        File bloomFilterNewName = new File(path + encrypted.getName() + ".bf");
        try{
            Files.move(bloomFilter.toPath(), bloomFilterNewName.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.upload(client.getName(), encrypted, bloomFilterNewName);
    }

    /*
    Current supported searchWord formats:
    place names: "Bergen" (disregarding capital letters and spaces),
    year: "y2019"(year 2019),
    month:"m03"(march),
    day:"d12"(12'th day of the month),
    weekday:"sunday",
    full date:"d12m03y2019" (12-03-2019),
    filename: "profilePicture.jpg",
    filename without extension: "profilePicture"

    Example searchWords for picture taken in bergen:
    marineholmenp-sone, thormøhlensgate, møhlenpris, bergenhus, bergen, vestland, 5058, norway, y2017, m09, d25, monday, d25m09y2017, pond.jpg, pond
     */
    public static void searchProtocol(client client, server server, String searchWord){

        BigInteger[] trapdoor = client.trapdoor(searchWord);
        File[] files = server.searchAllFiles(client.getName(), trapdoor);
        System.out.println(files.length);

        for(File f:files){
            File dec = client.decryptFile(f);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(!client.checkError(searchWord, dec)){
                dec.delete();

            }

        }
    }

    public static void uploadAll(client client, server server){
        File dir = new File(path + "/allImages/");
        int i = 0;
        for(File f: dir.listFiles()){
            //
            uploadProtocol(client, server, f);
            //System.out.println(f.getName() + Arrays.toString(places));
            System.out.println(i + "/"+ dir.listFiles().length);
            i++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}

