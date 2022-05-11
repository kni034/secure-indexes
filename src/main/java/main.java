import Client.Client;
import Client.Gui;
import Server.authenticator;
import Server.Server;

import java.io.File;

public class main {
    private static final String path = "./src/main/resources/";

    public static void main(String[]args){
        File dir = new File(path + "/allImages/");

        Server server = new Server(512,3 , 351);
        authenticator auth = new authenticator(server);

        //new Gui(auth);




        Client alice = new Client("Alice","password", auth, server.getS(), server.getR());

        long startTime = System.currentTimeMillis();

        for(int i=0;i<10;i++){
            uploadTxtTest(alice);
        }


        //searchTxtTest(alice,"to");
        long endTime = System.currentTimeMillis();
        System.out.println("That took " + (endTime - startTime)/10 + " milliseconds");


        //alice.setRecompute(true);
        //alice.loginToServer();


        //Client bob = new Client("Bob", "12345",auth, server.getS(), server.getR());
        //Client.client connor = new Client.client("Connor", "Super secret password", Server.server.getS(), Server.server.getR());
        //Client.client daisy = new Client.client("Daisy", "kjempebra", Server.server.getS(), Server.server.getR());

        //File f = new File(dir + "/IMG_8598.JPG");
        //File f = new File(dir + "/20210728_153031.jpg");
        //alice.registerToServer();
        //bob.registerToServer();
        //alice.loginToServer();

        //uploadProtocol(alice, f);


        //searchProtocol(alice,"pond");


        //uploadAll(alice);

        //alice.rangeSearch(Server.server, "d01m01y2013", "d17m07y2014");

        //searchProtocol(alice, "pond");

    }

    public static void uploadProtocol(Client client, File file){

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
    public static void searchProtocol(Client client, String searchWord){
        if(!client.loginToServer()){
            return;
        }
        client.search(searchWord);
    }

    public static void uploadAll(Client client){
        if(!client.loginToServer()){
            return;
        }
        File dir = new File(path + "/allImages/");
        int i = 0;
        //long startTime = System.currentTimeMillis();
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
        //long endTime = System.currentTimeMillis();
        //System.out.println("That took " + (endTime - startTime) + " milliseconds");
    }

    public static void uploadTxtTest(Client client){
        File dir = new File(path + "/allTexts/");
        int i = 0;

        for(File f: dir.listFiles()){
            i++;

            client.uploadTxt(f);
            //System.out.println(f.getName() + Arrays.toString(places));
            System.out.println(i + "/"+ dir.listFiles().length);


        }

    }

    public static void searchTxtTest(Client client, String word){

        int number_of_files = client.searchTxt(word);
        System.out.println("downloaded " + number_of_files + " files");

    }

}

