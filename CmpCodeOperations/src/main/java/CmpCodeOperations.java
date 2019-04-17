import com.sun.xml.internal.bind.v2.TODO;
import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

public class CmpCodeOperations {
    public static void main(String args[]) throws IOException {
        String apiKey = "XXX-XXX-XXX-XXX"; // TODO: Get Api Key from ScanLife account > Settings
        String secretKey = "sD/XuzirrBViase345reDFRT5"; // TODO: Get Secret Key from ScanLife account > Settings
        String codeName = "uniqueCodeName1"; // TODO: should be unique for all codes
        String codeURL = "http://scanlife.com"; // TODO: required for web type codes
        String createCodeUrl = createCode(codeName,codeURL,apiKey,secretKey);
        System.out.println("Create code url: "+ createCodeUrl);
    }

    /**
     *  create code api call
     * @param codeName unique name of code
     * @param codeURL url to be embedded in QR code
     * @param apiKey account api key
     * @param secretKey account secret key
     * @return shortUrl generated after the code is created
     */

    private static String createCode(String codeName, String codeURL, String apiKey, String secretKey) {
        StringBuffer buffer = new StringBuffer();
        String signature = null;
        String shortURL = null;

        SortedMap<String, String> sortedParamMap = new TreeMap<String, String>(); //parameters to be added in api call in alphabetical order
        sortedParamMap.put("codename",codeName);
        sortedParamMap.put("codetype", "web");
        sortedParamMap.put("compactQr","true");
        sortedParamMap.put("url",codeURL);


        String timeStamp = getTimeStamp(); // returns GMT time stamp in “Y-m-dTH:i:sZ” format
        sortedParamMap.put("timestamp",timeStamp);
        sortedParamMap.put("apikey",apiKey);

        try{
            Iterator<Map.Entry<String, String>> iter = sortedParamMap
                    .entrySet().iterator();
            //**** create the canonical string using the URLEncoder and utf-8 ****//
            while (iter.hasNext()) {
                Map.Entry<String, String> kvpair = iter.next();
                buffer.append(URLEncoder
                        .encode(kvpair.getKey(), "UTF-8")
                        .replace("+", "%20").replace("*", "%2A")
                        .replace("%7E", "~"));
                buffer.append("=");
                buffer.append(URLEncoder
                        .encode(kvpair.getValue(), "UTF-8")
                        .replace("+", "%20").replace("*", "%2A")
                        .replace("%7E", "~"));
                if (iter.hasNext()) {
                    buffer.append("&");
                }
            }
            //****  URLEncoder end ****//

            //**** Signature generator *****//
            byte[] data = buffer.toString().getBytes("UTF-8");
            Mac mac = Mac.getInstance("HmacSHA256");
            byte[] secretyKeyBytes = secretKey.getBytes("UTF-8");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretyKeyBytes,
                    "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] rawHmac = mac.doFinal(data);
            Base64 encoder = new Base64();
            signature = new String(encoder.encode(rawHmac));
            // finally we are generate the signature
            System.out.println("signature= " + signature);
            //**** Signature generator end *****//

            //**** Creating HTTP URL for call ****//
            String createCodeUrl = "http://app.scanlife.com/api/code/createcode?"
                    + buffer.toString() + "&signature="
                    + URLEncoder.encode(signature, "UTF-8");
            //System.out.println(createCodeUrl);
            return createCodeUrl;

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     *
     * @return GMT timestamp
     */
    private static String getTimeStamp() {
        Date today = new Date();
        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
                "Y-M-d'T'H:m:s'Z'");
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
        String timestamp = DATE_FORMAT.format(today);
        //  System.out.println("timestamp=" + timestamp);
        return  timestamp;
    }

}
