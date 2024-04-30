import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;


public class Manager {
    private final ArrayList<int[][]> nodeInfoList;
    private final HashMap<Integer, Node> nodesDict;
    private HashMap<Integer, BigInteger> resDict;

    public Manager() {
        this.nodeInfoList = new ArrayList<>();
        this.nodesDict = new HashMap<>();
    }

    public void readInput(String path) {
        try {
            Scanner scannerInput = new Scanner(new File(path));
            int numNodes = Integer.parseInt(scannerInput.next());
            int maxDegree = Integer.parseInt(scannerInput.next());
            while (scannerInput.hasNext()) {
                Integer neighbourId = Integer.parseInt(scannerInput.next());
                String neighbourAttributes = scannerInput.nextLine();
                ArrayList<List<Integer>> resultList = new ArrayList<>();
                // Remove the square brackets at the beginning and end of the string
                String cleanedInput = neighbourAttributes.substring(1, neighbourAttributes.length() - 1);
                // Split the string into individual list elements
                String[] listElements = cleanedInput.split("\\], \\[");
                int[][] neighbourArray = new int[listElements.length][3];
                for (int i = 0; i < listElements.length; i++) {
                    // Remove any remaining square brackets
                    String cleanedListElement = listElements[i].replace("[", "").replace("]", "");
                    // Split the list element into individual values
                    String[] values = cleanedListElement.split(", ");
                    for (int j = 0; j < 3; j++) {
                        // Parse each value as an Integer and add it to the list
                        neighbourArray[i][j] = Integer.parseInt(values[j]);
                    }
                }
                this.nodeInfoList.add(neighbourArray);

            }
            for (int i = 0; i < this.nodeInfoList.size(); i++) {
                int[][] nodeInfo = this.nodeInfoList.get(i);
                nodesDict.put(i, new Node(i, nodeInfo, numNodes, maxDegree));
            }
            this.initial_servers_clients();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String start() {
        ArrayList<Thread> threadList = new ArrayList<>();
        for (Node curr_node : this.nodesDict.values()) {
            Thread curr_thread = new Thread(curr_node);
            threadList.add(curr_thread);
            curr_thread.start();
        }

        for (Thread thread : threadList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.resDict = new HashMap<>();
        for (Node curr_node : this.nodesDict.values()) {
            resDict.put(curr_node.getId(), curr_node.getColor());
        }
        for (Node curr_node : this.nodesDict.values()){
            for (Client c : curr_node.clients.values()) {
                try {
                    c.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        for (Node curr_node : this.nodesDict.values()){
            for (Server s : curr_node.servers.values()) {
                try {
                    s.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }


        //assemble output string
        String out = "";
        for (Integer i : resDict.keySet()) {
            out = out + i + "," + resDict.get(i) + "\n";
        }


        return out;
    }

    /**
     * initialize the servers and clients
     * connect between them
     */
    public void initial_servers_clients() throws IOException {
        // init servers
        for (Node curr_node : nodesDict.values()) {
            curr_node.initServer();
        }

        // init clients
        for (Node curr_node : nodesDict.values()) {
            curr_node.initClient();
        }

        // connect
        for (Node curr_node : nodesDict.values()) {
            curr_node.connectServers();
        }
    }

    public String terminate() {
        StringBuilder out = new StringBuilder();
        for (Integer i : resDict.keySet()) {
            out.append(i).append(",").append(resDict.get(i)).append("\n");
        }
        return out.toString();
    }

}
