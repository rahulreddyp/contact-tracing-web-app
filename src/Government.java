import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.sql.*;
import java.util.*;

public class Government {

    // declare db connection, preparedStatement and resultSet variables
    Connection connection;
    PreparedStatement preStatement;
    ResultSet resultSet;

    InputStream inputStream;

    public Government(String configFile){

        Map<String, String> DBConfigs;
        BufferedReader reader;


        // try block to check SQL Exceptions
        if(!configFile.isEmpty()) {
            try {
                File file = new File(configFile);

                // Check if length of file is 0 if true then proceed
                if (file.isFile() && file.length() != 0) {
                    reader = new BufferedReader(new FileReader(file));

                    DBConfigs = new HashMap<>();
                    String line;

                    while((line = reader.readLine()) != null) {
                        // If line.length is Zero then continue - skip to next Iteration
                        if(line.length() == 0){
                            continue;
                        }

                        String[] parts = line.trim().split("=");

                        if(!parts[0].isEmpty() && !parts[1].isEmpty()){
                            DBConfigs.put(parts[0], parts[1]);
                        }
                    }

                    reader.close();

//                Properties prop = new Properties();
//                inputStream = getClass().getClassLoader().getResourceAsStream(configFile);
//
//                if (inputStream != null) {
//                    prop.load(inputStream);
//
//                    DBConfigs = new HashMap<>();
//
//                    String[] configNames = {"driver", "database", "user", "password"};
//
//                    for(String name: configNames){
//                        DBConfigs.put(name, prop.getProperty(name));
//                    }

                    Class.forName(DBConfigs.get("driver")).newInstance();
                    // remove serverTimezone=UTC before pushing to remote
                    connection = DriverManager.getConnection(DBConfigs.get("database")+"?serverTimezone=UTC", DBConfigs.get("user"), DBConfigs.get("password"));
                    System.out.println("Connected");
                }
            }
            // catch block, returns false if any error occurs
            catch (Exception ex) {
                System.out.println("Error connecting to Database:\n" + ex.getMessage());
            }
        }
    }

    public boolean mobileContact(String initiator, String contactInfo){
        if(!initiator.isEmpty() && initiator.chars().allMatch(Character::isLetterOrDigit) && !contactInfo.isEmpty()){

            try {
//                preStatement = connection.prepareStatement("INSERT into Users values(?)");
//                preStatement.setString(1, initiator);

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

                // we are creating an object of builder to parse
                // the  xml file.
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(contactInfo);
                doc.getDocumentElement().normalize();
                System.out.println("Root element: " + doc.getDocumentElement().getNodeName());

                NodeList nodeList = doc.getElementsByTagName("contact_details");
                for (int i = 0; i < nodeList.getLength(); ++i) {
                    Node node = nodeList.item(i);

                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element tElement = (Element) node;
                        System.out.println("Individual: " + tElement.getElementsByTagName("individual").item(0).getTextContent());
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public void recordTestResult(String testHash, int date, boolean result){}

    int findGatherings(int date, int minSize, int minTime, float density){return 0;}
}
