package sample;

import javafx.fxml.FXML;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller {

    public static final String PORT = "5555";

    public static final String ADB_LIST_DEVICES = "adb devices -l";
    //e.g. adb -s 9B051FFAZ00EGF shell ip -f inet addr show
    public static final String ADB_GET_DEVICE_IP_TEMPLATE_STRING = "adb -s %s shell ip -f inet addr show";

    //e.g. adb -s 9B051FFAZ00EGF tcpip 5555
    public static final String ADB_SET_CONNECTION_PORT_TEMPLATE_STRING = "adb -s %s tcpip %s";
    //e.g. adb connect 192.168.1.121:5555
    public static final String ADB_CONNECT_TO_IP_PORT_TEMPLATE_STRING = "adb connect %s:%s";

    //e.g. adb disconnect 192.168.1.121:5555 && adb usb
    public static final String ADB_DISCONNECT_DEVICE_IP_TEMPLATE_STRING = "adb disconnect %s:%s && adb usb";

    public static final String IFCONFIG = "ifconfig";
    public static final Pattern IP_REGEX_PATTERN = Pattern.compile("\\b(?:[0-9]{1,3}\\.){3}[0-9]{1,3}\\b");


    @FXML
    private void initialize()
    {
       goWireless();
    }

    private static void goWireless(){
        try {
            //Get Device List (and device Map of attributes)
            HashMap<String, HashMap<String, String>> deviceMap = getDeviceMap();

            //Get Computer IP Address
            ArrayList<String> computerIPAddresses = getComputerIPAddress();

            //Array of Device IDs
            Object[] devicesIDs = deviceMap.keySet().toArray();
            if(devicesIDs.length > 0) {
                //Get Device IP from ID
                ArrayList<String> listOfDeviceIPAddress = getDeviceIPAddresses(devicesIDs[0].toString());

                System.out.println(listOfDeviceIPAddress.toString());
            }

            //Compare and get the IP address of the device that is on the same subnet as the computer

            //Run the SET CONNECTION PORT command using the deviceID and PORT

            //Run the ADB CONNECT command using the IP address and PORT number


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void disconnectDeviceWirelessADB(String ipAddress, String portNumber){
        //Run the ADB Disconnect ipAddress:portNumber

    }

    private static ArrayList<String> getDeviceIPAddresses(String devicesID) throws IOException {
        Process getDeviceIPCommand = Runtime.getRuntime().exec(String.format(ADB_GET_DEVICE_IP_TEMPLATE_STRING, devicesID));
        String getDeviceIPCommandResult = resultToString(getDeviceIPCommand).toString();
        return getIPAddressesFromString(getDeviceIPCommandResult);
    }

    private static ArrayList<String> getComputerIPAddress() throws IOException {
        Process ifConfigCommand = Runtime.getRuntime().exec(IFCONFIG);
        String ifConfigCommandResult = resultToString(ifConfigCommand).toString();
        ArrayList<String> listOfIPAddress = getIPAddressesFromString(ifConfigCommandResult);
        return listOfIPAddress;
    }

    /**
     * Gets deviceMap of devices plugged in (key is device id and attributes are key value pairs hashmap)
     * @return
     * @throws IOException
     */
    private static HashMap<String, HashMap<String, String>> getDeviceMap() throws IOException {
        Process deviceListCommand = Runtime.getRuntime().exec(ADB_LIST_DEVICES);
        String deviceListCommandResult = resultToString(deviceListCommand).toString();
        HashMap<String, HashMap<String, String>> deviceMap = convertResultToDeviceMap(deviceListCommandResult);
        System.out.println(deviceMap.toString());
        return deviceMap;
    }


    /**
     * Extracts ArrayList of IPAddress Strings from a String
     * @param ifConfigCommandResult
     * @return
     */
    private static ArrayList<String> getIPAddressesFromString(String ifConfigCommandResult) {
        Matcher ipAddressMatcher = IP_REGEX_PATTERN.matcher(ifConfigCommandResult);
        ArrayList<String> listOfIPAddress = new ArrayList<>();
        while(ipAddressMatcher.find()){
            String ipAddress = ipAddressMatcher.group();
            if(!ipAddress.startsWith("127.0.0.1") && !ipAddress.startsWith("169.254") && !ipAddress.endsWith(".255")) {
                listOfIPAddress.add(ipAddress);
            }
        }
        return listOfIPAddress;
    }

    /**
     * Print result of command to console
     * @param process
     * @throws IOException
     */
    private static void printResults(Process process) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = "";
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
    }

    /**
     * Return String of result of command executed
     * @param process
     * @return
     * @throws IOException
     */
    private static StringBuffer resultToString(Process process) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuffer stringBuffer = new StringBuffer();
        String line = "";
        while ((line = reader.readLine()) != null) {
            stringBuffer.append(line);
            stringBuffer.append("\n");
        }
        return stringBuffer;
    }

    /**
     * Converts devicelist result string into Device HashMap
     * @param result
     * @return
     */
    private static HashMap<String, HashMap<String, String>> convertResultToDeviceMap(String result){
        String[] lines = result.replaceAll("List of devices attached\n", "").split("\n");

        HashMap<String, HashMap<String, String>> deviceMap = new HashMap<>();

        for (int l = 0; l < lines.length; l++) {
            //Per line
            String[] values = lines[l].split("[ ]+");
            //On each line the first value is always the id:
            String identifier = values[0];

            HashMap<String, String> attributeMap = new HashMap<>();
            //For each value:
            for (int i = 0; i < values.length; i++) {
                if(values[i].contains(":")){
                    String[] pair = values[i].split(":");
                    attributeMap.put(pair[0], pair[1]);
                } else {
                    //attributeMap.put(values[i],values[i]);
                }
            }

            deviceMap.put(identifier, attributeMap);

        }
        return deviceMap;
    }

}
