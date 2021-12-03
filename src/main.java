import java.io.File;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.BitSet;

public class main {
    private static final String path = "./resources/";

    public static void main(String[]args){
        server server = new server(512,3 , 50);

        client alice = new client("Alice","password123 :)", server.getS(), server.getR());
        client bob = new client("Bobbert", "12345", server.getS(), server.getR());

        File f = new File(path +"test.txt");

        uploadProtocol(alice, server, f);

        searchProtocol(alice, server, "abc");
        searchProtocol(bob, server, "abc");
        searchProtocol(alice, server, "abc");
        
    }

    public static void uploadProtocol(client client, server server,File file){

        File bloomFilter = client.buildIndex(file, server.getUpperbound());
        File encrypted = client.encryptFile(file);
        server.upload(client.getName(), encrypted, bloomFilter);
    }

    public static void searchProtocol(client client, server server, String searchword){

        BigInteger[] trapdoor = client.trapdoor(searchword);
        File[] files = server.searchAllFiles(client.getName(), trapdoor);
        for(File f:files){
            client.decryptFile(f);
        }

    }
}

