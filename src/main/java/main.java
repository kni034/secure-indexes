import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

public class main {
    private static final String path = "./src/main/resources/";

    public static void main(String[]args){
        File f = new File(path +"image1.jpg");
        /*
        server server = new server(512,3 , 50);

        client alice = new client("Alice","password123 :)", server.getS(), server.getR());
        client bob = new client("Bobbert", "12345", server.getS(), server.getR());

        File f = new File(path +"file1");

        uploadProtocol(alice, server, f);

        searchProtocol(alice, server, "abc");

         */

        ImageProcessor ip = new ImageProcessor();
        try {
            ip.getData(60.38246497935409, 5.329261886818714);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // ip.readMetaData(f);


        
    }

    public static void uploadProtocol(client client, server server,File file){

        File bloomFilter = client.buildIndex(file, server.getUpperbound());
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

