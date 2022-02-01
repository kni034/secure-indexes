package Server;

import java.io.File;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.UUID;

public class authenticator extends Thread {

    private final server server;
    private DB database;
    private String userID;
    private UUID uuid;

    public authenticator(server server){
        this.server = server;
        this.database = new DB();
        try {
            database.createClientTable();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    public UUID login(String userID, String password){
        try {
            if (!DB.clientExists(userID)) {
                register(userID, password);
            }
        }
        catch (SQLException e){
            e.printStackTrace();
        }

        if(DB.login(userID, password)){
            UUID uuid = UUID.randomUUID();
            this.uuid = uuid;
            this.userID = userID;

            System.out.println("login godkjent");
            return uuid;
        }else{
            System.out.println("login ikke godkjent");
        }

        return null;
    }

    public void register(String userID, String password){
        try {
            if(DB.clientExists(userID)){
                System.out.println("user exits");
            }else {
                DB.addClient(userID, password);
                server.createUser(userID);
                System.out.println("user created");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void upload(String userID, UUID uuid, File file, File bloomFilter){
        if(this.uuid.equals(uuid)){
            server.upload(userID, file, bloomFilter);
        }
        else{
            System.out.println("User not logged in");
        }
    }


    public File[] search(String userID, UUID uuid, BigInteger[] trapdoor){
        if(this.uuid.equals(uuid)) {
            return server.searchAllFiles(userID, trapdoor);
        }
        else {
            System.out.println("no");
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

    //midlertidig
    public server getServer(){
        return server;
    }


}
