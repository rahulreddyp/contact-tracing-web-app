import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class MobileDevice {

    private Map<String, String> configMap = new HashMap<>();
    private Map<String, List<Integer>> contactDetails = null;

    private List<List<UserContacts>> userContacts = new ArrayList<>();
    private Government government;
    private String[] userTestHash;
    private String deviceHash;

    private class UserContacts {
        public String individual;
        public int date;
        public int duration;

        public UserContacts(String individual, int date, int duration) {
            this.individual = individual;
            this.date = date;
            this.duration = duration;
        }
    }

    public MobileDevice(String configFile, Government contactTracer) {

        BufferedReader reader;
        try {
            File file = new File(configFile);

            // Check if length of file is 0 if true then proceed
            if (file.isFile() && file.length() != 0) {
                reader = new BufferedReader(new FileReader(file));
                StringBuilder values = new StringBuilder();
                String line;

                while((line = reader.readLine()) != null){
                    // If line.length is Zero then continue - skip to next Iteration
                    if(line.length() == 0){
                        continue;
                    }

                    String[] parts = line.trim().split("=");


                    if(!parts[0].isEmpty() && !parts[1].isEmpty()){
                        values.append(parts[1]);
                    }
                }

                deviceHash = encryptData(values.toString());

                contactDetails = new HashMap<>();

                userTestHash = new String[1];
                // use contactTracer to record values
                userContacts.add(new ArrayList<>());
                government = contactTracer;

                // Close the reader stream
                reader.close();
            }else {
                System.out.println("Error Occurred: Please enter mobiledevice config filename with path \n");
            }
        } catch (Exception e) {
            System.out.println("Error Occurred:" + e.getMessage());
        }

    }

    public void recordContact(String individual, int date, int duration){

        if(isValidContact(individual, date, duration)){

            List<UserContacts> newContact = new ArrayList<>();
            newContact.add(new UserContacts(individual, date, duration));
            userContacts.add(newContact);


//            if(contactDetails.containsKey(individual)){
//
//                // loop through each individual entry set
//                for(Map.Entry<String, List<Integer>> entry : contactDetails.entrySet())
//                {
//                    // if current key matches to categoryName then add all productList as values
//                    if(entry.getKey().equals(individual)) {
//                        entry.getValue().add(date);
//                        entry.getValue().add(duration);
//                    }
//                }
//            }else {
//                contactDetails.put(individual, new ArrayList<>());
//                contactDetails.get(individual).add(date);
//                contactDetails.get(individual).add(duration);
//            }
        }
    }

    private boolean isValidContact(String individual, int date, int duration) {
        if(!individual.isEmpty() && individual.chars().allMatch(Character::isLetterOrDigit)){
            return (date > 0 && duration > 0);
        }
        return false;
    }

    public void positiveTest(String testHash) {
        if(!testHash.isEmpty() && testHash.chars().allMatch(Character::isLetterOrDigit)){
            userTestHash[0] = testHash;
        }
    }

    public boolean synchronizeData(){
        try {
//            String text = configMap.get("address") + configMap.get("deviceName");
            if(!deviceHash.isEmpty()) {
                government.mobileContact(deviceHash, createXMLFormattedString(deviceHash));
            }

        }
        catch (Exception e){
            return false;
        }
        return true;
    }

    private String encryptData(String text) {
        MessageDigest md;
        String hashValue;
        try {
            md = MessageDigest.getInstance("SHA-256");
            byte[] digestBytes = md.digest(text.getBytes(StandardCharsets.UTF_8));

            hashValue = String.format("%064x", new BigInteger(1, digestBytes));
        }
        catch (NoSuchAlgorithmException e) {
            System.out.println("Exception:" + e.getMessage());
            return null;
        }
        return hashValue;
    }

    private String createXMLFormattedString(String deviceHash) {

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document document = dBuilder.newDocument();

            Element rootElement = document.createElement("remote_contacts");
            document.appendChild(rootElement);

            Element deviceHashRoot = createRootElement(document, rootElement, "device_hash");
            deviceHashRoot.appendChild(document.createTextNode(deviceHash));

            List<UserContacts> contacts = userContacts.get(0);
            Iterator<UserContacts> iterator = contacts.iterator();

            Element remoteContactsRoot = createRootElement(document, rootElement, "remote_contacts");

            while(iterator.hasNext()){
                Element contactDetails = document.createElement("contact_details");

                remoteContactsRoot.appendChild(getCurrentElement(document, "individual", String.valueOf(iterator.next())));
                remoteContactsRoot.appendChild(getCurrentElement(document, "contact_date", iterator.next().toString()));
                remoteContactsRoot.appendChild(getCurrentElement(document, "contact_duration", iterator.next().toString()));

                remoteContactsRoot.appendChild(contactDetails);
            }

            Element positiveTestRoot = createRootElement(document, rootElement, "positive_test");
            positiveTestRoot.appendChild(document.createTextNode(userTestHash[0]));

            Transformer tf = TransformerFactory.newInstance().newTransformer();
            tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            tf.setOutputProperty(OutputKeys.INDENT, "no");
            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(document);
            tf.transform(source, result);

            return result.getWriter().toString();
        }
        catch (Exception e){
            System.out.println("Could not create XML"+ e.getMessage());
            return null;
        }
    }

    private Element createRootElement(Document doc, Element root, String tagName) {
        if(root != null && !tagName.isEmpty()){
            Element element = doc.createElement(tagName);
            root.appendChild(element);
            return element;
        }
        return null;
    }

    /**
     * getCurrentElement  - utility method to create text node for given element name and value
     */
    private Node getCurrentElement(Document doc,String name, String value) {
        Element node = doc.createElement(name);
        node.appendChild(doc.createTextNode(value));
        return node;
    }

}
