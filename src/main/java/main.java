import Client.Gui;
import Client.client;
import Server.authenticator;
import Server.server;

import java.io.File;
import java.util.UUID;

public class main {
    private static final String path = "./src/main/resources/";

    public static void main(String[]args){
        File dir = new File(path + "/allImages/");

        server server = new server(512,3 , 50);
        authenticator auth = new authenticator(server);

        //new Gui(auth);




        Client.client alice = new Client.client("Alice","password", auth, server.getS(), server.getR());
        //alice.setRecompute(true);


        //Client.client bob = new Client.client("Bob", "12345", Server.server.getS(), Server.server.getR());
        //Client.client connor = new Client.client("Connor", "Super secret password", Server.server.getS(), Server.server.getR());
        //Client.client daisy = new Client.client("Daisy", "kjempebra", Server.server.getS(), Server.server.getR());

        //File f = new File(dir + "/IMG_2099.HEIC");
        alice.registerToServer();
        alice.loginToServer();

        //uploadProtocol(alice, f);


        //searchProtocol(alice,"pond");


        uploadAll(alice);

        //alice.rangeSearch(Server.server, "d01m01y2013", "d17m07y2014");

        //searchProtocol(alice, Server.server, "pond");

    }

    public static void uploadProtocol(client client, File file){

        client.upload(file, new String[0]);
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
    Objects in the picture: "dog" , "sunset", "water"

    Example searchWords for picture taken in bergen:
    marineholmenp-sone, thormøhlensgate, møhlenpris, bergenhus, bergen, vestland, 5058, norway, y2017, m09, d25, monday, d25m09y2017, m09y2017, d25m09, høst, autumn, pond.jpg, pond
     */
    public static void searchProtocol(client client, String searchWord){

        client.search(searchWord);
    }

    public static void uploadAll(client client){
        File dir = new File(path + "/allImages/");
        int i = 0;
        for(File f: dir.listFiles()){
            i++;
            /*
            if(i <= 20){
                continue;
            }
            if(i >= 60){
                break;
            }

             */
            uploadProtocol(client, f);
            //System.out.println(f.getName() + Arrays.toString(places));
            System.out.println(i + "/"+ dir.listFiles().length);


        }
    }

}

