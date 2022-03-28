package Server;

import com.google.api.gax.core.ExecutorAsBackgroundResource;

import java.sql.*;
import java.util.concurrent.ExecutionException;

public class DB {

    private static final String dbLocation = "jdbc:sqlite:./src/main/resources/serverStorage/users.db";

    public DB(){
        try (Connection con = connect()) {
            if (con != null) {
                DatabaseMetaData meta = con.getMetaData();
                con.close();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    private static Connection connect(){
        String url = dbLocation;
        Connection con = null;
        try{
            con = DriverManager.getConnection(url);
        } catch(SQLException e){
            e.printStackTrace();
        }
        return con;
    }

    public static void createClientTable() throws SQLException {
        String sql =
                "CREATE TABLE IF NOT EXISTS clients (\n"
                        + "     id integer PRIMARY KEY AUTOINCREMENT, \n"
                        + "     userID varchar(512) UNIQUE NOT NULL,\n"
                        + "     password varchar(512),\n"
                        + "     salt varchar(512),\n"
                        + ");";
        Connection con = connect();
        Statement stmt = con.createStatement();
        stmt.execute(sql);
        con.close();
    }

    public static boolean clientExists(String userID) throws SQLException{
        String query = "SELECT *"
                + "FROM clients "
                + "WHERE userID = ?";
        Connection con = connect();
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, userID);
        ResultSet rs = ps.executeQuery(query);
        con.close();
        return rs.next();
    }

    public static void addClient(String userID, String salt){
        String dir = "/"+userID+"/";
        try {
            String query = "INSERT INTO clients(userID,password,directory)"
                    + "VALUES (?,?,?)";
            Connection con = connect();
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, userID);
            ps.setString(2, null);
            ps.setString(3, salt);
            ps.executeUpdate();
            con.close();
        }
        catch (SQLException e){
            e.printStackTrace();
        }

    }

    public static boolean setPassword(String userID, String password){
        String query =
                "UPDATE clients "
                + "Set password = ? " +
                        "WHERE userid = ?";
        Connection con = connect();
        try(PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1,userID);
            ps.setString(2,password);
            ResultSet rs = ps.executeQuery();
            con.close();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getSalt(String userID){
        String query =
                "SELECT salt "
                        + "FROM clients  WHERE userID = ?";
        Connection con = connect();
        try(PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1,userID);
            ResultSet rs = ps.executeQuery();
            con.close();
            return rs.getString(0);

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static boolean login(String userID, String password){
        String query =
                "SELECT * "
                        + "FROM clients  WHERE userID = ?"
                        + "AND password = ?";
        Connection con = connect();
        try(PreparedStatement ps = con.prepareStatement(query)){
            ps.setString(1,userID);
            ps.setString(2,password);
            ResultSet rs = ps.executeQuery();
            con.close();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
