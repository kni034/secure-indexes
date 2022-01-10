import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import io.github.cdimascio.dotenv.Dotenv;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;

public class ImageProcessor {


    public String[] readMetaData(File f) {

        try {
            Metadata metadata = ImageMetadataReader.readMetadata(f);

            /*
            for (Directory directory : metadata.getDirectories()) {

                for (Tag tag : directory.getTags()) {

                    System.out.println(tag);
                }
            }



             */



            for (Directory directory : metadata.getDirectories()) {

                if(directory.getName().equals("GPS")){
                    HashMap<String,String> coordinates = new HashMap<>();
                    for (Tag tag : directory.getTags()) {
                        coordinates.put(tag.getTagName(), tag.getDescription());
                        //System.out.println(tag);
                    }

                    //format and convert longtitude from format DMS to decimal
                    String lonString = coordinates.get("GPS Longitude");
                    lonString = lonString.replace("°", "");
                    lonString = lonString.replace("'", "");
                    lonString = lonString.replace("\"", "");
                    //lonString = lonString.replace(".", ",");
                    String[] lonDMS = lonString.split(" ");
                    String lonDirection = coordinates.get("GPS Longitude Ref");
                    double lon = DMStoDecimal(Integer.parseInt(lonDMS[0]), Integer.parseInt(lonDMS[1]), Double.parseDouble(lonDMS[2]), lonDirection);

                    //format and convert latitude from format DMS to decimal
                    String latString = coordinates.get("GPS Latitude");
                    latString = latString.replace("°", "");
                    latString = latString.replace("'", "");
                    latString = latString.replace("\"", "");
                    //latString = latString.replace(".", ",");
                    String[] latDMS = latString.split(" ");
                    String latDirection = coordinates.get("GPS Latitude Ref");
                    double lat = DMStoDecimal(Integer.parseInt(latDMS[0]), Integer.parseInt(latDMS[1]), Double.parseDouble(latDMS[2]), latDirection);

                    String[] places = getData(lat,lon);
                    return  places;

                }


            }


        } catch (Exception e) {
            //System.out.println(f.getName() + " has no location :(");
        }
        return new String[0];
    }

    private String[] getData(double lat, double lon) throws UnsupportedEncodingException {
        Dotenv dotenv = Dotenv.load();

        String host = "https://eu1.locationiq.com/v1/reverse.php";
        String charset = "UTF-8";
        String key = "?key=" + URLEncoder.encode(dotenv.get("ACCESS_KEY"), charset);

        String latLon = String.format("&lat=%s&lon=%s", URLEncoder.encode(String.valueOf(lat), charset), URLEncoder.encode(String.valueOf(lon), charset));
        String format = "&format=json";

        String url = host + key + latLon + format;
        //System.out.println(url);

        HttpResponse<JsonNode> resp = Unirest.get(url).asJson();
        //System.out.println(resp.getHeaders());
        //System.out.println(resp.getBody().toPrettyString());

        String[] result = resp.getBody().getObject().get("display_name").toString().split(",");
        return result;
    }

    private double DMStoDecimal(int degrees, int minutes, double seconds, String direction) {

        double decimalMin = minutes / 60.0;
        double decimalSec = minutes / 3600.0;

        double decimal = degrees + decimalMin + decimalSec;

        if(direction.equals("S") || direction.equals("W")){
            decimal *= -1.0;
        }

        return decimal;
    }

}
