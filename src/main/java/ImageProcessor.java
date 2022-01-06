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

public class ImageProcessor {


    public void readMetaData(File f) {

        try {


            System.out.println("nå!");
            Metadata metadata = ImageMetadataReader.readMetadata(f);

            for (Directory directory : metadata.getDirectories()) {


                for (Tag tag : directory.getTags()) {

                    System.out.println(tag);
                    System.out.println(tag.getDescription());
                }


            }
        } catch (ImageProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getData(double lat, double lon) throws UnsupportedEncodingException {
        Dotenv dotenv = Dotenv.load();

        String host = "https://eu1.locationiq.com/v1/reverse.php";
        String charset = "UTF-8";
        String key = "?key=" + URLEncoder.encode(dotenv.get("ACCESS_KEY"), charset);

        String latLon = String.format("&lat=%s&lon=%s", URLEncoder.encode(String.valueOf(lat), charset), URLEncoder.encode(String.valueOf(lon), charset));
        String format = "&format=json";

        String url = host + key + latLon + format;
        System.out.println(url);

        HttpResponse<JsonNode> resp = Unirest.get(url).asJson();
        System.out.println(resp.getHeaders());
        System.out.println(resp.getBody().toPrettyString());

        System.out.println(resp.getBody().getObject().get("display_name"));
    }
    /*
    public void getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(HomeActivity.mContext, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            String add = obj.getAddressLine(0);
            add = add + "\n" + obj.getCountryName();
            add = add + "\n" + obj.getCountryCode();
            add = add + "\n" + obj.getAdminArea();
            add = add + "\n" + obj.getPostalCode();
            add = add + "\n" + obj.getSubAdminArea();
            add = add + "\n" + obj.getLocality();
            add = add + "\n" + obj.getSubThoroughfare();

            Log.v("IGA", "Address" + add);
            // Toast.makeText(this, "Address=>" + add,
            // Toast.LENGTH_SHORT).show();

            // TennisAppActivity.showDialog(add);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

     */
}
