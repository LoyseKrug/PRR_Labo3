import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Authors: Adrien Allemand, Loyse Krug
 */

public class Election implements BetterUDPReciever.Observer{

    @Override
    public void onMessageRecieved(byte[] message) {
        switch (message[0]){
            case Protocole.ANNOUNCE:
                processAnnounce(message);
                break;
            case Protocole.RESULT:
                processResult(message);
                break;
            default: //if the message is REQUEST type the site doesn't have to send the message furtherer
                break;
        }
    }

    public enum State{ELECTION,NORMAL}

    private BetterUDPReciever budpr;
    private BetterUDPSender budps;

    private State state;
    private byte id;
    private Candidate chosen;
    private int bestAptitude;
    private List<Candidate> candidates = new ArrayList<Candidate>();

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
        bestAptitude = Protocole.basePort + id;
        announceBuffer = new byte[21];
        resultBuffer = new byte[6];
        acknowlegment = new byte[1];

        budps = new BetterUDPSender();

        budpr = new BetterUDPReciever(Protocole.basePort + id);
        budpr.registerObserver(this);
        budpr.start();
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
            bestAptitude = id + Protocole.basePort;
            sendToNext(announceBuffer);
        }
    }

    /*
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
    */


    private synchronized  void processAnnounce(byte[] message){
        Util.copyToFillByteArray(announceBuffer, message);
        if(announceBuffer[5 * id + 1] == Protocole.NOTTREATED){
            if(state == State.NORMAL || newAnnounceIsBetter(message)){
                announceBuffer[5 * id + 1] = Protocole.TREATED;
                byte[] apt = Util.intToBytes(Protocole.basePort + id);
                for(int i = 1; i <= 4 ;++i){
                    announceBuffer[5 * id + 1 + i] = apt[i - 1];
                }

                //We check if the message contains a better aptitude than the one registered until now
                for(int i = 0; i < 4; ++i){
                    int aptitude = Util.convertByteArrayToInt(Arrays.copyOfRange(message, 5 * i + 2, 5 * i + 6));
                    if(aptitude > bestAptitude){
                        bestAptitude = aptitude;
                    }
                }
                sendToNext(announceBuffer);
            }else{
                System.out.println(id + ": the last recieved announce has been cancelled");
            }

        } else { //the announce has already passed by here
            analyseResults(message);
            byte chosenId = (byte)(chosen.port - Protocole.basePort);
            resultBuffer[0] = Protocole.RESULT;
            resultBuffer[1] = chosenId;
            for(int i = 0; i < 4; ++i){
                if(i == id){
                    resultBuffer[2 + i] = Protocole.TREATED;
                }else{
                    resultBuffer[2 + i] = Protocole.NOTTREATED;
                }
            }
            sendToNext(resultBuffer);
        }
    }

    private synchronized void processResult(byte[] message){
        Util.copyToFillByteArray(resultBuffer, message);
        if(resultBuffer[id + 2] == Protocole.NOTTREATED){
            if(state == State.ELECTION){
                resultBuffer[id + 2] = Protocole.TREATED;
                chosen = candidates.get(resultBuffer[1]);
                //we change back the best aptitude registered for the next election
                bestAptitude = Protocole.basePort + id;
                state = State.NORMAL;
                sendToNext(resultBuffer);
            }else{
                System.out.println("Error");
            }
        }else{
            System.out.println("The election is finished");
            if(resultBuffer[(chosen.port - Protocole.basePort) + 2] == Protocole.NOTTREATED){
                launchElection();
            }
        }
    }

    public Candidate getChosen(){
        return chosen;
    }

    public int getId(){
        return id;
    }

    private boolean newAnnounceIsBetter(byte[] announce){
        for(int i = 0; i < Protocole.NBSITES; ++i){
            int aptitude = Util.convertByteArrayToInt(Arrays.copyOfRange(announce, 5 * i + 2, 5 * i + 6));
            if(aptitude > bestAptitude){
                return true;
            }
        }
        return false;
    }

    private void analyseResults(byte[] announce){
        int bestAptitudeId = 0;
        int bestAptitude = 0;
        for(int i = 0; i < 4; ++i){
            int aptitude = Util.convertByteArrayToInt(Arrays.copyOfRange(announce, 5 * i + 2, 5 * i + 6));
            if(aptitude > bestAptitude){
                bestAptitude = aptitude;
                bestAptitudeId = i;
            }
        }
        chosen = candidates.get(bestAptitudeId);
    }

    private void sendToNext(byte[] message){
        boolean stopTrying = false;
        int idToSendTo = (id + 1) % Protocole.NBSITES;
        while(stopTrying == false){

            if(idToSendTo != id) {
                chosen = candidates.get(id);
                System.out.println(id + ":  All others sites around are down");
                stopTrying = true;
            }else{
                try {
                    InetAddress address = InetAddress.getByName(Protocole.ipAdresses[idToSendTo]);
                    budps.SendMessage(announceBuffer, address, Protocole.basePort + idToSendTo);
                    stopTrying = true;
                    state = State.ELECTION;

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (BetterUDPSender.CommunicationErrorException e) {
                    e.printStackTrace();
                    idToSendTo = (idToSendTo + 1) % Protocole.NBSITES;
                    state = State.NORMAL;
                }
            }
        }
    }
}
