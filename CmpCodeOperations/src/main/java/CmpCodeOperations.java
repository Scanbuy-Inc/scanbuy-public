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
        String shortURL = createCode(codeName,codeURL,apiKey,secretKey);
        System.out.println("Code Created: "+ shortURL);
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
            System.out.println("signature=" + signature);
            //**** Signature generator end *****//

            //**** Creating HTTP URL for call ****//
            String createCodeUrl = "http://app.scanlife.com/api/code/createcode?"
                    + buffer.toString() + "&signature="
                    + URLEncoder.encode(signature, "UTF-8");
            System.out.println(createCodeUrl);
            String responseString = callURL(createCodeUrl); // This function makes Http GET call
            //**** Creating HTTP URL for call end ****//

            //**** Handling XML response of create code call ****//
            if(responseString != null){
                DocumentBuilderFactory dbf =
                        DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                InputSource is = new InputSource();
                is.setCharacterStream(new StringReader(responseString));

                Document doc = db.parse(is); // xml response of create code
                NodeList nodes = doc.getElementsByTagName("createcoderesponse");
                Element element = (Element) nodes.item(0);
                String message = getElementString(element,"message");
                System.out.println("Create Code "+ message);
                if(message.equals("Success!")){
                    shortURL = getElementString(element,"shorturl");
                    //System.out.println("COdeIDDD "+ codeID);
                    //  System.out.println("SHORTURL "+ shortURL);

                }
                else{
                    String errorMessage = getElementString(element,"errormsg");
                    //System.out.println("ERRDSF "+ errorMessage);

                }
                return shortURL;
            }else{
                System.out.println("URL CALL FAILED!! CHECK NETWORK!!");
                return null;
            }
            //**** Handling XML response of create code call **** ends//

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

    /**
     *
     * @param url Http get request url
     * @return Http response
     * @throws Exception
     */

    private static String callURL(String url) throws Exception{

        try {

            URL obj = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
            conn.setReadTimeout(6000);
            conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
            conn.addRequestProperty("User-Agent", "Mozilla");
            //   conn.addRequestProperty("Referer", "google.com");

            //  System.out.println("Request URL ... " + url);

            boolean redirect = false;

            // normally, 3xx is redirect
            int status = conn.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                if (status == HttpURLConnection.HTTP_MOVED_TEMP
                        || status == HttpURLConnection.HTTP_MOVED_PERM
                        || status == HttpURLConnection.HTTP_SEE_OTHER)
                    redirect = true;
            }

            //   System.out.println("Response Code ... " + status);

            if (redirect) {

                // get redirect url from "location" header field
                String newUrl = conn.getHeaderField("Location");

                // get the cookie if need, for login
                String cookies = conn.getHeaderField("Set-Cookie");

                // open the new connnection again
                conn = (HttpURLConnection) new URL(newUrl).openConnection();
                conn.setRequestProperty("Cookie", cookies);
                conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
                conn.addRequestProperty("User-Agent", "Mozilla");
                //   conn.addRequestProperty("Referer", "google.com");

                //      System.out.println("Redirect to URL : " + newUrl);

            }

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer html = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                html.append(inputLine);
            }
            in.close();

            //   System.out.println("URL Content... \n" + html.toString());
            //   System.out.println("Done");

            return  html.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     *
     * @param element
     * @param tagName
     * @return
     */
    private static String getElementString(Element element,String tagName) {
        NodeList name = element.getElementsByTagName(tagName);
        Element line = (Element) name.item(0);
        return  getCharacterDataFromElement(line);
    }

    /**
     *
     * @param e
     * @return
     */
    private static String getCharacterDataFromElement(Element e) {
        Node child = e.getFirstChild();
        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return "?";
    }

}
