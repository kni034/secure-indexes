package Server;

import java.io.File;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.UUID;

public class authenticator extends Thread {

    private final Server server;
    private DB database;
    private String userID;
    private UUID uuid;



    public authenticator(Server server, File dir){
        this.server = server;
        this.database = new DB(dir);
        try {
            database.createClientTable();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    //register 1
    public String createNewUser(String userID){
        String saltString = null;
        if(database.clientExists(userID)){
            System.out.println("Authenticator: user exits");
            return null;
        }else {

            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            saltString = salt.toString();
            database.addClient(userID, saltString);
            server.createUser(userID);
        }
        return saltString;
    }

    //register 2
    public UUID setPassword(String userID, String password){

        if (database.setPassword(userID, password)) {
            System.out.println("Authenticator: User Created");
        }
        else {
            System.out.println("Authenticator: Error finalizing new user");
            return null;
        }

        if(database.login(userID, password)){
            UUID uuid = UUID.randomUUID();
            this.uuid = uuid;
            this.userID = userID;

            return uuid;
        }else{
            System.out.println("(debug)Authenticator: Unknown error, wrong password when creating user?!?");
        }

        return null;
    }

    //login 1
    public String getSalt(String userID){
        if (database.clientExists(userID)){
            String salt = database.getSalt(userID);

            if(salt != null){
                return salt;
            }
            else{
                //System.out.println("(debug)Authenticator: User does not exist");
            }
        }


        return null;
    }

    //login 2
    public UUID login(String userID, String password){

        if(database.login(userID, password)){
            UUID uuid = UUID.randomUUID();
            this.uuid = uuid;
            this.userID = userID;

            System.out.println("Authenticator: Login successful");
            return uuid;
        }else{
            System.out.println("Authenticator: Wrong username or password");
        }

        return null;
    }


    public boolean upload(String userID, UUID uuid, File file, File bloomFilter){
        if(uuid == null){
            System.out.println("Authenticator: User not logged in");
            return false;
        }
        if(this.uuid.equals(uuid)){
            server.upload(userID, file, bloomFilter);
            return true;
        }

        System.out.println("Authenticator: User not logged in");
        return false;
    }


    public File[] search(String userID, UUID uuid, BigInteger[] trapdoor){
        if(this.uuid.equals(uuid)) {
            return server.searchAllFiles(userID, trapdoor);
        }
        else {
            System.out.println("Authenticator: Cannot search, user not logged in");
        }
        return null;
    }

    public int getS(){
        return server.getS();
    }

    public int getR(){
        return server.getR();
    }

    public int getUpperbound() {
        return server.getUpperbound();
    }

    //midlertidig TODO: slett
    public Server getServer(){
        return server;
    }


}
