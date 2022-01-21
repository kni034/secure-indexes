import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import io.github.cdimascio.dotenv.Dotenv;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.*;

public class ImageProcessor {


    public String[] readMetaData(File f) {

        String[] places = new String[0];
        String[] dates = new String[0];
        String[] both = new String[0];
        String[] fileNames = getFileNames(f);
        Metadata metadata = null;
        try {
            metadata = ImageMetadataReader.readMetadata(f);
        }
        catch(Exception e){
            e.printStackTrace();
        }

            for (Directory directory : metadata.getDirectories()) {

                try {
                    if (directory.getName().equals("GPS")) {
                        places = getLocation(directory);
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }

                try {
                    if (directory.getName().equals("Exif SubIFD")) {
                        dates = getDates(directory);
                    } else if (directory.getName().equals("MP4")) {
                        both = getDatesMP4(directory);
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }


        ArrayList<String> allWords = new ArrayList<>();
        if(places.length == 0 && both.length == 0){
            System.out.println("Error adding GPS data for file: "+ f.getName());
        }
        else {
            for (String s : places) {
                allWords.add(s);
            }
        }

        if(dates.length == 0 && both.length == 0){
            System.out.println("Error adding dates for file: "+ f.getName());
        }
        else {
            for(String s: dates){
                allWords.add(s);
            }
        }

        if(!(both.length == 0)){
            for(String s: both){
                allWords.add(s);
            }
        }

        if(fileNames.length == 0){
            System.out.println("Error adding fileNames for image: "+ f.getName());
        }
        else {
            for (String s : fileNames) {
                allWords.add(s);
            }
        }

        String[] words = allWords.toArray(new String[0]);

        return words;
    }

    private String[] getDatesMP4(Directory directory) throws UnsupportedEncodingException {
        HashMap<String,String> map = new HashMap<>();
        for (Tag tag : directory.getTags()) {
            map.put(tag.getTagName(), tag.getDescription());
        }
        String time = map.get("Creation Time");

        String[] dateElements = time.split(" ");
        String month;
        String weekday;

        switch (dateElements[0]){
            case "Mon": weekday = "MONDAY"; break;
            case "Tue": weekday = "TUESDAY"; break;
            case "Wed": weekday = "WEDNESDAY"; break;
            case "Thu": weekday = "THURSDAY"; break;
            case "Fri": weekday = "FRIDAY"; break;
            case "Sat": weekday = "SATURDAY"; break;
            default: weekday = "SUNDAY";
        }

        switch (dateElements[1]){
            case "Jan": month = "01"; break;
            case "Feb": month = "02"; break;
            case "Mar": month = "03"; break;
            case "Apr": month = "04"; break;
            case "May": month = "05"; break;
            case "Jun": month = "06"; break;
            case "Jul": month = "07"; break;
            case "Aug": month = "08"; break;
            case "Sep": month = "09"; break;
            case "Oct": month = "10"; break;
            case "Nov": month = "11"; break;
            default: month = "12";
        }

        String seasonN = "";
        String seasonE = "";
        if(month.equals("01") || month.equals("02") || month.equals("12")){
            seasonN = "Vinter";
            seasonE = "Winter";
        }
        if(month.equals("03") || month.equals("04") || month.equals("05")){
            seasonN = "Vår";
            seasonE = "Spring";
        }
        if(month.equals("06") || month.equals("07") || month.equals("08")){
            seasonN = "Sommer";
            seasonE = "Summer";
        }
        if(month.equals("09") || month.equals("10") || month.equals("11")){
            seasonN = "Høst";
            seasonE = "Autumn";
        }

        String year = "y" + dateElements[dateElements.length-1];
        month = "m" + month;
        String day = "d" + dateElements[2];

        String ymDate = month + year;
        String dmDate = day + month;
        String ymdDate = day + month + year;



        String[] places = new String[0];
        try {
            places = getLocationDataFromAPI(Double.parseDouble(map.get("Latitude")), Double.parseDouble(map.get("Longitude")));
        }
        catch (Exception e){

        }
        ArrayList<String> allElements = new ArrayList<>(Arrays.asList(places));
        allElements.add(year);
        allElements.add(month);
        allElements.add(day);
        allElements.add(weekday);
        allElements.add(ymdDate);
        allElements.add(ymDate);
        allElements.add(dmDate);
        allElements.add(seasonN);
        allElements.add(seasonE);

        return allElements.toArray(new String[0]);
    }

    private String[] getFileNames(File f){
        String name = f.getName();
        String nameWithoutExtension;

        nameWithoutExtension = name.split("\\.")[0];

        if(!name.equals(nameWithoutExtension)){
            return new String[]{name, nameWithoutExtension};
        }
        return new String[]{name};

    }

    private String[] getDates(Directory directory){
        HashMap<String,String> map = new HashMap<>();
        for (Tag tag : directory.getTags()) {
            map.put(tag.getTagName(), tag.getDescription());
            //System.out.println(tag.getTagName() +" "+ tag.getDescription());
        }

        String[] date = map.get("Date/Time Original").split(" ")[0].split(":");
        LocalDate localdate = LocalDate.of(Integer.parseInt(date[0]), Integer.parseInt(date[1]), Integer.parseInt(date[2]));
        String year = "y" + date[0];
        String month = "m"+date[1];
        String day = "d"+date[2];
        String weekday = localdate.getDayOfWeek().toString();
        String ymDate = month + year;
        String dmDate = day + month;
        String seasonN = "";
        String seasonE = "";
        if(date[1].equals("01") || date[1].equals("02") || date[1].equals("12")){
            seasonN = "Vinter";
            seasonE = "Winter";
        }
        if(date[1].equals("03") || date[1].equals("04") || date[1].equals("05")){
            seasonN = "Vår";
            seasonE = "Spring";
        }
        if(date[1].equals("06") || date[1].equals("07") || date[1].equals("08")){
            seasonN = "Sommer";
            seasonE = "Summer";
        }
        if(date[1].equals("09") || date[1].equals("10") || date[1].equals("11")){
            seasonN = "Høst";
            seasonE = "Autumn";
        }

        String ymdDate = day + month + year;

        return new String[]{year, month, day, weekday, ymdDate, ymDate, dmDate, seasonN, seasonE};
    }

    private String[] getLocation(Directory directory) throws UnsupportedEncodingException {
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

        String[] places = getLocationDataFromAPI(lat,lon);

        return  places;


    }

    private String[] getLocationDataFromAPI(double lat, double lon) throws UnsupportedEncodingException {
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
        double decimalSec = seconds / 3600.0;

        double decimal = Math.abs(degrees) + decimalMin + decimalSec;

        if(direction.equals("S") || direction.equals("W")){
            decimal *= -1.0;
        }

        return decimal;
    }

}
