import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

class GossipData implements Serializable{ // Must be serializable to send 1 bit after another over the network.
    int nodeNumber;
    int average;
    int highValue;
    int lowValue;
    String commandl;
    String userString;
    // Whatever else you might want to send.
    public  GossipData(){

    }

    public int getAverage() {
        return average;
    }

    public String getCommandl() {
        return commandl;
    }

    public void setCommandl(String commandl) {
        this.commandl = commandl;
    }
}

class GossipWorker extends Thread {    // Class definition. Many of these worker threads may run simultaneously.
    GossipData gossipObj;                         // Class member, gossipObj, local to GossipWorker.
    GossipWorker (GossipData c) {gossipObj = c;}  // Constructor, assign arg c to local object

    public void run(){ // Do whatever you like here:
        System.out.println("\nGW: In Gossip worker: " + gossipObj.userString + "\n");
    }
}

 class GossipStarter {
    public static int serverPort = 45565; // Port number as a class variable
    public static int NodeNumber = 0;     // Will have to get this from the fist argument passed.

    public static void main(String[] args) throws Exception {
        System.out.println
                ("Clark Elliott's Gossip Server 1.0 starting up, listening at port " + GossipStarter.serverPort + ".\n");

        ConsoleLooper CL = new ConsoleLooper(); // create a DIFFERENT thread
        Thread t = new Thread(CL);
        t.start();  // ...and start it, waiting for console input

        boolean loopControl = true; // Keeps the datagram listener running.

        try{
            DatagramSocket DGSocket = new DatagramSocket(GossipStarter.serverPort);
            // Careful: you may have so many datagrams flying around you loose some for lack of buffer size.
            System.out.println("SERVER: Receive Buffer size: " + DGSocket.getReceiveBufferSize() + "\n");
            byte[] incomingData = new byte[1024];
            InetAddress IPAddress = InetAddress.getByName("localhost");

            while (loopControl) { // Use control-C to manually terminate the server.
                DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
                DGSocket.receive(incomingPacket);
                byte[] data = incomingPacket.getData();
                ByteArrayInputStream in = new ByteArrayInputStream(data);
                ObjectInputStream is = new ObjectInputStream(in);
                try {
                    GossipData gossipObj = (GossipData) is.readObject();
                    if (gossipObj.userString.indexOf("stopserver") > -1){
                        System.out.println("SERVER: Stopping UDP listener now.\n");
                        loopControl = false;
                    }

                    System.out.println("\nSERVER: Gossip object received = " + gossipObj.userString + "\n");
                    new GossipWorker(gossipObj).start(); // Spawn a worker thread to handle it. Listen for more.
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }
}

class ConsoleLooper implements Runnable {

    public void run(){ // RUNning the Console listen loop
        System.out.println("CL: In the Console Looper Thread");

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        try {
            String someString;
            do {
                System.out.print
                        ("CL: Enter a string to send to the gossipServer, (or, quit/stopserver): ");
                System.out.flush ();
                someString = in.readLine ();

                if (someString.indexOf("quit") > -1){
                    System.out.println("CL: Exiting now by user request.\n");
                    System.exit(0); // Ugly way to stop. You can fix with a more elegant throw.
                }

                // Trigger some action by sending a datagram packet to ourself:
                try{
                    System.out.println("CL: Preparing the datagram packet now...");
                    DatagramSocket DGSocket = new DatagramSocket();
                    InetAddress IPAddress = InetAddress.getByName("localhost");

                    GossipData gossipObj = new GossipData();
                    gossipObj.userString = someString;

                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    ObjectOutputStream os = new ObjectOutputStream(outputStream);
                    os.writeObject(gossipObj);
                    byte[] data = outputStream.toByteArray();
                    DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, GossipStarter.serverPort);
                    DGSocket.send(sendPacket);
                    System.out.println("CL: Datagram has been sent.");
                } catch (UnknownHostException UH){
                    System.out.println("\nCL: Unknown Host problem.\n"); // Test by commenting out / uncommenting out above.
                    UH.printStackTrace();
                }
            } while (true);
        } catch (IOException x) {x.printStackTrace ();}
    }
}


public class Gossip{
    public static final int BasePort=48100;
    private static final int Base_N=20;

    private int gossip_id;
    private int gossip_size;
    private int Number_of_gossip_cycle;
    private int  max_allowed_gossip;
    private Random random;
    private String gossipcommands;
    GossipData gossipObJ;
    private double average_gossip;
    public static String neighbourgossip;
    private static int local_gossip_value;
    private Set<InetAddress> neighborNode;
    public Gossip(int id_gossip){
        this.gossip_id=id_gossip;
        this.local_gossip_value=new Random().nextInt(99);
        this.average_gossip =this.local_gossip_value;
        this.gossip_size=1;
        this.Number_of_gossip_cycle=0;
        this.neighborNode = new HashSet<>();
        gossipObJ=new GossipData();
       this.gossipObJ.commandl=gossipcommands;
       this.gossipObJ.nodeNumber=gossip_id;


    }
     private int getPort(){
        return BasePort+gossip_id;
    }
   public void startGossiping() {
        DatagramSocket socket=null;
        try{
            socket=new DatagramSocket(getPort());
            System.out.println("Node "+gossip_id+"Starting at port"+getPort());
            while(true) {

                System.out.println(" Node " + gossip_id);
                System.out.println("Please enter t command in order to see list of commands");
                BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
                gossipcommands = bf.readLine();
                GossipListnerThread GLT;
                new GossipListnerThread(getPort()).start();


                if (gossipcommands.equalsIgnoreCase("t")) {
                    System.out.println("Commands:");
                    System.out.println("  t - List all commands");
                    System.out.println("  l - Display local values ");
                    System.out.println("  p - Ping neighbor nodes");
                    System.out.println("  m - Display min/max values in the network");
                    System.out.println("  a - Calculate the average of all the local values in the [sub-]network");
                    System.out.println("  z - Calculate the current [sub-]network size");
                    System.out.println("  v - Recreate random values throughout the network");
                    System.out.println("  d - Delete the current node");
                    System.out.println("  k - Kill the entire network");
                    System.out.println("  y - Display cycle number on all nodes");
                    System.out.println("  N - Set maximum messages per neighbor");
                    System.out.println("  remove - enter remove ID for removeNeighbor");
                    System.out.println("  cycle - startCycle");

                } else if (gossipcommands.equalsIgnoreCase("m") || gossipcommands.equalsIgnoreCase("a")
                        || gossipcommands.equalsIgnoreCase("z") || gossipcommands.equalsIgnoreCase("v") || gossipcommands.equalsIgnoreCase("k")
                        || gossipcommands.equalsIgnoreCase("y")) {
                    gossipObJ.setCommandl(gossipcommands);
                    new GossipWorkerThread(gossipObJ).start();
                    if (gossipcommands.equalsIgnoreCase("l") || neighbourgossip.equalsIgnoreCase("l")) {
                        printLocalValues();
                    }
                    if (gossipcommands.equalsIgnoreCase("m")) {
                        minMaxValuesNetwork();
                    }
                    if (gossipcommands.equalsIgnoreCase("a")) {
                        calculateAverage();
                    }
                    if (gossipcommands.equalsIgnoreCase("z")) {
                        calculateSizeOfNetwork();
                    }
                    if (gossipcommands.equalsIgnoreCase("k")) {
                        killGossipNetwork();
                    }
                    if (gossipcommands.equalsIgnoreCase("y")) {
                        viewNoOfGossipCycle();
                    }

                } else if (gossipcommands.equalsIgnoreCase("p")) {
                    pingNeighbours();
                } else if (gossipcommands.equalsIgnoreCase("d")) {
                    deleteGossipNode(socket);


                } else {
                    System.out.println("Really Sorry, No such commands");
                }


            }
        } catch (IOException exe) {
            throw new RuntimeException(exe);
        }

   }

    private void deleteGossipNode(DatagramSocket socket) {
    }

    private void pingNeighbours() {
    }

    private void viewNoOfGossipCycle() {
    }

    private void killGossipNetwork() {
    }


    private void calculateSizeOfNetwork() throws SocketException {
        System.out.println(gossip_id + ":");
        int networkSize = 1; // Include the local node
        for (InetAddress neighbor : neighborNode) {
            try {
                DatagramSocket gossipSocket = new DatagramSocket();
                byte[] buffer = ("gossip " + gossip_id).getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, neighbor, getPort());
                gossipSocket.send(packet);

                buffer = new byte[256];
                packet = new DatagramPacket(buffer, buffer.length);
                gossipSocket.receive(packet);

                String response = new String(packet.getData(), 0, packet.getLength());
                int neighborSize = Integer.parseInt(response);

                networkSize += neighborSize;

                gossipSocket.close();
                } catch (IOException e) {
                throw new RuntimeException(e);
            }}}
    private void calculateAverage() {

    }

    private void minMaxValuesNetwork() {
    }

    private static void printLocalValues() {
        System.out.println("Local value :"+local_gossip_value);

    }


    public static void main(String[] args) {
        int getId;
        getId = args.length == 0 ? 1 : Integer.parseInt(args[0]);
        int idGossip = getId;//Integer.parseInt(args[0].equals(""):getId?args[0]);
        Gossip gnode = new Gossip(idGossip);
        gnode.startGossiping();
    }



}

class GossipWorkerThread extends Thread{
    private static String command;
    GossipData gossipOBJ;

    public GossipWorkerThread(GossipData gD){
        this.gossipOBJ=gD;
       // this.command=command;
    }


    @Override
    public void run() {
        try{
            int sending_node= Gossip.BasePort+gossipOBJ.nodeNumber;
            InetAddress IPAddress = InetAddress.getByName("localhost");

            DatagramSocket DGSocket = new DatagramSocket();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(outputStream);
        os.writeObject(gossipOBJ);
        byte[] data = outputStream.toByteArray();
            DatagramPacket sendrightNodePACKET = new DatagramPacket(data, data.length, IPAddress, sending_node+1);
            DGSocket.send(sendrightNodePACKET);
            DatagramPacket sendleftNodePACKET = new DatagramPacket(data, data.length, IPAddress, sending_node-1);
            DGSocket.send(sendleftNodePACKET);
        System.out.println("Packets sends to neighbours");

    } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}

class GossipListnerThread extends Thread{
    private static String command;
    int port;

    public GossipListnerThread(int port){
        this.port=port;
        // this.command=command;
    }


    @Override
    public void run() {
        try{
           // int sending_node= Gossip.BasePort+gossipOBJ.nodeNumber;
            InetAddress IPAddress = InetAddress.getByName("localhost");
            byte[] incomingData = new byte[1024];
            DatagramSocket DGSocket = new DatagramSocket();
            DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
            DGSocket.receive(incomingPacket);
            byte[] data = incomingPacket.getData();
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            ObjectInputStream is = new ObjectInputStream(in);
            GossipData returnobj = (GossipData) is.readObject();
            Gossip.neighbourgossip=returnobj.commandl;
            System.out.println("Packets received neighbours");

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


}


class NodesSpecification{
    private int node_id;
    private InetAddress inet;
    private int portAddress;
    private double avg;

    public NodesSpecification(int id,InetAddress address,int port){
        this.node_id=id;
        this.inet=address;
        this.portAddress=port;
        this.avg=0.00;
    }

    public double getAvg() {
        return avg;
    }

    public int getNode_id() {
        return node_id;
    }

    public InetAddress getInet() {
        return inet;
    }

    public int getPortAddress() {
        return portAddress;
    }

    public void setAvg(double avg) {
        this.avg = avg;
    }

    public void setNode_id(int node_id) {
        this.node_id = node_id;
    }

    public void setInet(InetAddress inet) {
        this.inet = inet;
    }

    public void setPortAddress(int portAddress) {
        this.portAddress = portAddress;
    }

}


