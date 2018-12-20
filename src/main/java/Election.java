import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Authors: Adrien Allemand, Loyse Krug
 */

public class Election{
    public enum State{ELECTION,NORMAL, RESULT}

    private State state;
    private byte id;
    private Candidate chosen;
    private List<Candidate> candidates = new ArrayList<Candidate>();

    //private Thread askingThread;
    private Thread messageProcessor;

    private DatagramSocket socket;
    private DatagramSocket socketWithTimeout;
    private boolean isRunning;

    //buffer contains type of the message, for each site
    private byte[] buffer;

    //Contains, type of the message, + for all of the four sites, the aptitude + a byte indicating the apritude is given
    private byte[] announceBuffer;
    private byte[] resultBuffer;
    private byte[] acknowlegment;

    /**
     * Election constructor
     * @param candidates : list of all the candidats to the election
     * @param id, id of the
     */
    public Election(List<Candidate> candidates, byte id){
        this.candidates = candidates;
        this.id = id;
        this.state = State.NORMAL;
        chosen = null;
        buffer = new byte[256];
        announceBuffer = new byte[21];
        resultBuffer = new byte[6];
        acknowlegment = new byte[2];


        try {
            socket = new DatagramSocket(Protocole.basePort + id);
            socketWithTimeout = new DatagramSocket(Protocole.basePort + id);
            socketWithTimeout.setSoTimeout(Protocole.timeout);
        } catch (SocketException e) {
            throw new RuntimeException("error opening datagramm socket");
        }

        messageProcessor = new Thread(){
            @Override
            public void run(){
                isRunning = true;
                while(isRunning){
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    try {
                        socket.receive(packet);
                        byte messageType = buffer[0];
                        //Send acknowlegment to the sender
                        InetAddress address = packet.getAddress();
                        int port = packet.getPort();
                        acknowlegment[0] = Protocole.ACKNOWLEGMENT;
                        acknowlegment[1] = messageType;
                        packet = new DatagramPacket(acknowlegment, acknowlegment.length, address, port);
                        socket.send(packet);

                        switch (messageType){
                            case Protocole.ANNOUNCE:
                                processAnnounce();
                                break;
                            case Protocole.RESULT:
                                processResult();
                                break;
                            default: //if the message is REQUEST type the site doesn't have to send the message furtherer
                                break;
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        };
        messageProcessor.start();

    }

    public void launchElection(){
        announceBuffer[0] = Protocole.ANNOUNCE;
        byte[] aptitude;
        int apt = 0;
        for(int i = 0; i < 4; ++ i){

            if(i == id){
                //set the values in the
                announceBuffer[(i * 5) + 1] = Protocole.TREATED;
            }else{
                announceBuffer[(i * 5) + 1] = Protocole.NOTTREATED;
                apt = Protocole.basePort + id;
            }
            //we convert the aptitude into an array of bytes to add it to the message
            aptitude = Util.intToBytes(apt);
            for(int j = 1; j <= 4 ; j++){
                announceBuffer[(i * 5) + 1 + j] = aptitude[j - 1];
            }
            try {
                sendMessage(socketWithTimeout, announceBuffer, id);
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
            }
        }




    }

    private void sendMessage(DatagramSocket socket, byte[] buffer, byte id) throws SocketTimeoutException {
        InetAddress address = null;
        try {
            address = InetAddress.getByName(Protocole.ipAdresses[id]);
        } catch (UnknownHostException e) {
            throw new RuntimeException("Error getting the ip address");
        }
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, Protocole.basePort + id);
        try {
            socket.send(packet);
            packet = new DatagramPacket(acknowlegment, acknowlegment.length);
            socket.receive(packet);
        } catch (IOException e) {
            throw new RuntimeException("Error sending the packet");
        }
    }

    private void processAnnounce(){

        //set the status on ANNOUNCE mode
        //prepare the message to send
        //while not getting an answer, wait try to send to each of the machines
    }

    private void processResult(){
        //set the status on NORMAL mode
        //set the chosen
        //prepare the message to send
        //while not getting an answer, wait try to send to each of the machines
    }

    public Candidate getChosen(){
        return chosen;
    }

    public int getId(){
        return id;
    }



}
