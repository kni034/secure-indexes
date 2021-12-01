import java.io.File;
import java.util.Arrays;
import java.util.BitSet;

public class main {

    public static void main(String[]args){

        client alice = new client();

        BitSet[] key = alice.keygen(512,3);
        System.out.println(Arrays.deepToString(key));

        BitSet[] door = alice.trapdoor(key, "aaaaa");
        System.out.println(Arrays.deepToString(door));

        String[] a = {"abc", "asd"};

        File f = alice.buildIndex("navn", key, a, 10);
        System.out.println(f.getName());
        
    }
}

