import java.io.File;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.BitSet;

public class main {
    private static final String path = "./resources/";

    public static void main(String[]args){

        client alice = new client();
        server server = new server();

        BitSet[] key = alice.keygen(512,3);
        System.out.println(Arrays.deepToString(key));

        BigInteger[] door = alice.trapdoor(key, "aaa");
        System.out.println(Arrays.deepToString(door));


        File f = new File(path +"test.txt");

        File bf = alice.buildIndex("navn", key, f, 50);
        System.out.println(bf.getName());

        boolean contains = server.search(bf, door);
        System.out.println(contains);

        
    }
}

