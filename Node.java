import java.io.*;
import java.math.BigInteger;
import java.util.*;


public class Node implements Runnable {
    private final int id;
    private String c_v_s;
    private String c_v_f;
    private int roundNum;
    private int componentLength;
    HashMap<Integer, Boolean> receivedUndecided;
    HashMap<Integer, BigInteger> receivedColor;
    private final HashMap<Integer, Integer> readingPort;
    private final HashMap<Integer, Integer> writingPort;
    public final HashMap<Integer, Server> servers;
    public final HashMap<Integer, Client> clients;
    private final ArrayList<Integer> neighbourList;
    private final int maxDegree;
    private BigInteger endColor;

    /**
     * constructor
     */
    public Node(int id, int[][] node_info, int numOfNodes, int maxDegree) {
        // initialization
        this.componentLength = (int) (Math.log(numOfNodes - 1) / Math.log(2)) + 2;
        this.receivedColor = new HashMap<>();
        this.receivedUndecided = new HashMap<>();
        this.maxDegree = maxDegree;
        this.roundNum = 1;
        this.id = id;
        this.c_v_s = this.CompleteColor(Integer.toBinaryString(id));
        this.c_v_f = this.c_v_s;
        this.neighbourList = new ArrayList<>();
        this.servers = new HashMap<>();
        this.clients = new HashMap<>();
        this.readingPort = new HashMap<>();
        this.writingPort = new HashMap<>();

        // extract the information from the input file
        for (int[] neighInfo : node_info) {
            this.neighbourList.add(neighInfo[0]);
            this.writingPort.put(neighInfo[0], neighInfo[1]);
            this.readingPort.put(neighInfo[0], neighInfo[2]);
        }
    }


    public void initServer() throws IOException {
        for (int currNeigh : this.neighbourList) {
            int port = this.readingPort.get(currNeigh);
            this.servers.put(currNeigh, new Server(port));
        }
    }

    public void initClient() throws IOException {
        for (int currNeigh : this.neighbourList) {
            int port = this.writingPort.get(currNeigh);
            this.clients.put(currNeigh, new Client(port));
        }
    }

    public void connectServers() throws IOException {
        for (Server server : servers.values()) {
            if (server != null) {
                server.connect();
            }
        }
    }

    public void nodeSendMess(Message mess) {
        for (int neig : neighbourList) {
            Client curr_client = clients.get(neig);
            try {
                curr_client.sendMess(mess);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String receiveColor(String neighborColor) {
        // Compare colors and find the smallest index where colors differ
        int len = this.c_v_s.length() - 1;
        int index = this.c_v_s.length() - 1;
        while (index >= 0 && this.c_v_s.charAt(index) == neighborColor.charAt(index)) {
            index--;
        }
        // Compute the new color by appending (index, this.color[index]) to the received color
        String subColor = this.CompleteColor(this.c_v_s.charAt(index) + Integer.toBinaryString(len - index));

        // Set the new color for the vertex
        return subColor;
    }


    public String CompleteColor(String component) {
        int tempComponentLength = component.length();
        int diff = this.componentLength - tempComponentLength;
        String zeros;
        if (diff > 0) {
            zeros = String.format("%0" + diff + "d", 0);
        } else {
            zeros = "";
        }
        return zeros + component;
    }

    public String GenerateComponent() {
        String first = Character.toString(this.c_v_s.charAt(this.c_v_s.length() - 1));
        return CompleteColor(first);
    }

    public BigInteger getColor() {
        return this.endColor;
    }

    public int getId() {
        return this.id;
    }

    public void TriggerRound() {
        this.c_v_f = "";

        //send c-v
        Message message = new Message(this.id, this.roundNum, this.c_v_s);
        nodeSendMess(message);
        //listen to msgs
        ArrayList<Integer> receivedList = new ArrayList<>();
        while (receivedList.size() < this.neighbourList.size()) {
            for (int neighbour : this.neighbourList) {
                if (!receivedList.contains(neighbour)) {
                    try {
                        Server curr_server = servers.get(neighbour);
//                        curr_server.getS().setSoTimeout(2000);
                        Message received = (Message) curr_server.getIn().readObject();
                        int messageRoundNum = received.getRoundNum();
                        int sourceId = received.getSourceID();
                        String received_c_v = received.getC_v();
                        if (messageRoundNum == this.roundNum) {
                            receivedList.add(sourceId);
                            this.c_v_f = receiveColor(received_c_v) + this.c_v_f;
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        //add trailing components
        for (int i = 0; i < this.maxDegree - this.neighbourList.size(); i++) {
            this.c_v_f = this.GenerateComponent() + this.c_v_f;
        }
        this.componentLength = (int) (Math.log((this.maxDegree * this.componentLength) - 1) / Math.log(2)) + 2;
        roundNum++;
    }


    public boolean TriggerReduceRound() {
        if (this.roundNum == 1) {
            for (int id : this.neighbourList) {
                this.receivedUndecided.put(id, true);
            }
            this.roundNum++;
            Message message = new Message(this.id, this.roundNum, this.endColor, true);
            nodeSendMess(message);
            return true;
        }

        //listen to messages
        int count = 0;
        int tempFreq = Collections.frequency(receivedUndecided.values(), true);
        while (count < tempFreq) {
            for (int neighbour : this.neighbourList) {
                if (receivedUndecided.get(neighbour)) {
                    try {
                        Server curr_server = servers.get(neighbour);
//                        curr_server.getS().setSoTimeout(2000);
                        Message received = (Message) curr_server.getIn().readObject();
                        int msgRoundNum = received.getRoundNum();
                        int msgId = received.getSourceID();
                        BigInteger msgColor = received.getEndColor();
                        boolean msgUndecided = received.isUndecided();

                        if (msgRoundNum == this.roundNum) {
                            receivedUndecided.put(msgId, msgUndecided);
                            receivedColor.put(msgId, msgColor);
                        }
                        count++;
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (receivedUndecided.containsValue(true)) {
            for (int i : receivedColor.keySet()) {
                if (receivedUndecided.get(i) && this.receivedColor.get(i).compareTo(this.endColor) > 0) {
                    this.roundNum++;
                    Message message = new Message(this.id, this.roundNum, this.endColor, true);
                    nodeSendMess(message);
                    return true;
                }
            }
        }

        for (int i = 0; i <= this.maxDegree; i++) {
            if (!receivedColor.containsValue(BigInteger.valueOf(i))) {
                this.endColor = BigInteger.valueOf(i);
                break;
            }
        }
        this.roundNum++;
        Message message = new Message(this.id, this.roundNum, this.endColor, false);
        nodeSendMess(message);
        return false;
    }

    @Override
    public void run() {
        do {
            this.c_v_s = this.c_v_f;
            this.TriggerRound();
        } while (this.c_v_f.length() != this.c_v_s.length());
        this.endColor = new BigInteger(this.c_v_f, 2);
        this.roundNum = 1;
        boolean flag;
        this.componentLength = this.c_v_f.length();
        do {
            flag = this.TriggerReduceRound();
        } while (flag);
        for (Server s : this.servers.values()) {
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
