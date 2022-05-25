import Client.Client;
import Client.Gui;
import Server.authenticator;
import Server.Server;

import javax.swing.*;
import java.io.File;
import java.security.SecureRandom;
import java.util.Scanner;

public class main {
    private static final String path = "./src/main/resources/";
    private static int s = 512;
    private static int r = 3;
    private static int u = 100;
    public static void main(String[]args){

        Scanner sc = new Scanner(System.in);
        System.out.println("Before we start, you have to choose a working directory.");
        System.out.println("This is where the server 'stores' files and downloads them to.");
        System.out.println("It is highly recommended that you choose an empty directory, or create a new one");
        System.out.println("You can open the directory while the program runs to see what is going on");
        System.out.println("Press 'Enter' to continue, this opens a window to choose directory");

        sc.nextLine();

        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setMultiSelectionEnabled(false);
        fc.showOpenDialog(null);
        File dir = fc.getSelectedFile();
        String path = dir.getAbsolutePath() + "/";

        Server server = new Server(s,r , u, dir);
        authenticator auth = new authenticator(server, dir);

        new Gui(auth, dir);

    }

}

