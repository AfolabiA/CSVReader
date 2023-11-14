import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class CSVReader {

    public static void main(String[] args) {
        String pathToCsv = "service-names-port-numbers.csv"; // replace with your CSV file path
        String line;

        // Step 2: Parse the CSV raw data using the delimiter character and the columns needed
        Map<Integer, String> portMap = new HashMap<>(); // Create a new map for Redis
        TreeSet<Integer> sortedKeys = new TreeSet<>(); // Create a set to store sorted keys

        try (BufferedReader br = new BufferedReader(new FileReader(pathToCsv))) {
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    // Skip empty lines
                    continue;
                }

                // Use comma as separator
                String[] columns = line.split(",");

                // Step 3: Map data structure collection and place all the proper columns
                // port numbers and port descriptions sorted numerically by the key (the port number)
                if (columns.length >= 4) {
                    String keyString = columns[1];
                    String value = columns[3];

                    try {
                        int key = Integer.parseInt(keyString);

                        if (!portMap.containsKey(key)) {
                            portMap.put(key, value);
                            sortedKeys.add(key); // Add key to the set for sorting
                        }
                    } catch (NumberFormatException e) {
                        // Handle the case when keyString is not a valid integer
                    }
                } else {
                    // Handle the case when the array doesn't have enough elements
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Jedis jedis = null;
        try {
            // Step 4: Using your localhost, scan your localhost networking system for all the open ports.
            // Assuming you want to check open ports from 1 to 65535
            for (int port = 1; port <= 65535; port++) {
                try (Socket socket = new Socket("localhost", port)) {
                    // Step 5: Look up and match the port number to get the port description in the Map collection
                    if (socket.isConnected()) {
                        String value = portMap.get(port);
                        if (value != null) {
                            // Step 6: Write out the Map key-value pairs for the localhost open ports
                            // found to the Redis database port number and description.
                            jedis = new Jedis("localhost");
                            jedis.set(String.valueOf(port), value);
                            jedis.close(); // Always close the connection
                        }
                    }
                } catch (IOException e) {
                    // Port is closed or filtered
                }
            }

            // Read and print the Redis database contents in order of keys
            for (Integer key : sortedKeys) {
                jedis = new Jedis("localhost");
                String value = jedis.get(key.toString());
                System.out.println("Port: " + key + " " + value);
                jedis.close(); // Always close the connection
            }
        } catch (JedisConnectionException e) {
            System.out.println("Could not connect to Redis: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Issue: " + e.getMessage());
        }
    }
}

