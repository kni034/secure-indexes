import io.github.cdimascio.dotenv.Dotenv;

import java.io.File;

public class main {
    private static final String path = "./src/main/resources/";

    public static void main(String[]args){
        File dir = new File(path + "/allImages/");

        server server = new server(512,3 , 50);

        new Gui(server);




        //client alice = new client("Alice","password", server.getS(), server.getR());
        //alice.setRecompute(true);


        //client bob = new client("Bob", "12345", server.getS(), server.getR());
        //client connor = new client("Connor", "Super secret password", server.getS(), server.getR());
        //client daisy = new client("Daisy", "kjempebra", server.getS(), server.getR());

        //File f = new File(dir + "/IMG_9677.MOV");

        //uploadProtocol(alice, server, f);


        //uploadAll(alice, server);

        //alice.rangeSearch(server, "d01m01y2013", "d17m07y2014");

        //searchProtocol(alice, server, "pond");

    }

    public static void uploadProtocol(client client, server server,File file){

        client.upload(server, file);
    }


    //TODO: vision AI
    //TODO: finpusse gui (Brukerveiledning, logout)


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
    marineholmenp-sone, thormøhlensgate, møhlenpris, bergenhus, bergen, vestland, 5058, norway, y2017, m09, d25, monday, d25m09y2017, m09y2017, d25m09, høst, autumn, pond.jpg, pond
     */
    public static void searchProtocol(client client, server server, String searchWord){

        client.search(server, searchWord);
    }

    public static void uploadAll(client client, server server){
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
            uploadProtocol(client, server, f);
            //System.out.println(f.getName() + Arrays.toString(places));
            System.out.println(i + "/"+ dir.listFiles().length);


        }
    }

}

