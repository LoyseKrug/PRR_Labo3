/**
 * Authors: Adrien Allemand, Loyse Krug
 */

import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class that launch and treate the different messages of an election
 * it inherits from BetterUDPReciever.Observer and therefore has an
 * onMessageRecieved methode that will be called every time a message
 * arrives
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

    //The different states that a machine can have. It is in ELECTION mode when it has recieved an announce message and
    //is still waiting for the result message.
    public enum State{ELECTION,NORMAL}

    //BetterUDPReciever and BetterUDPSender are used to send messages that recieve an acknowlgment
    //and throw an error if they don't recieve it after a given amount of time (timeout)
    private BetterUDPReciever budpr;
    private BetterUDPSender budps;

    //The Election instance has a state that can be NORMAL ou ELECTION
    private State state;
    //The id, added to a basePort in the protocol, will create the port of the machine
    private byte id;
    private Candidate chosen;
    private int bestAptitude;
    private List<Candidate> candidates = new ArrayList<Candidate>();

    //Diffenent buffers are used to send the different messages, see the content of each message in the Protocol
    // class file
    private byte[] announceBuffer;
    private byte[] resultBuffer;
    private byte[] acknowlegment;

    /**
     * Election constructor
     * @param candidates : list of all the candidats to the election
     * @param id, id of the machine
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

        budpr = new BetterUDPReciever(Protocole.basePort + id + 4);
        budpr.registerObserver(this);
        budpr.start();
    }

    /**
     * Starts an election. The announce buffer is constructed with its initials values
     * Announce: 00000000  + NOTTREATED/TREATED + aptitude of site 0  // 1 + 1 + 4 bytes
                           + NOTTREATED/TREATED + aptitude of site 1  // 1 + 4 bytes
                           + NOTTREATED/TREATED + aptitude of site 2  // 1 + 4 bytes
                           + NOTTREATED/TREATED + aptitude of site 3  // 1 + 4 bytes
                            = total 21 bytes
       Only the aptitude of the current site will be written, all the others are set at 0
     */
    public void launchElection(){
        announceBuffer[0] = Protocole.ANNOUNCE;
        byte[] aptitude;
        int apt = 0;

        //for each machine of the system, the message will contain informations wether it has already been treated
        //in the election or not (TREATED or NOTTREATED), and the value of its aptitude
        for(int i = 0; i < 4; ++ i){
            if(i == id){
                //The current machine is considered as treated, for it already enters its value in the message
                announceBuffer[(i * 5) + 1] = Protocole.TREATED;
                apt = Protocole.basePort + id;
            }else{
                announceBuffer[(i * 5) + 1] = Protocole.NOTTREATED;
            }
            //we convert the aptitude into an array of bytes to add it to the message
            aptitude = Util.intToBytes(apt);
            for(int j = 1; j <= 4 ; j++){
                announceBuffer[(i * 5) + 1 + j] = aptitude[j - 1];
            }
            //When the message is ready we can send it to the next machine
            sendToNext(announceBuffer);
        }
    }

    /**
     * Get the chosen site, according to this instance of Election
     * @return chosen candidate
     */
    public Candidate getChosen(){
        return chosen;
    }

    /**
     * Get the id of the site.
     * @return
     */
    public int getId(){
        return id;
    }

    /**
     * Processes an announce message depending on the state of the site
     * @param message, the announce message recieved from the previous machine
     */
    private synchronized  void processAnnounce(byte[] message){
        //We copy the content of the message in the announce array, in order to send the smallest array possible
        Util.copyToFillByteArray(announceBuffer, message);

        //If the site has not recieved this announce message yet
        if(announceBuffer[5 * id + 1] == Protocole.NOTTREATED){
            //If the site has not recivec any announce message from the last closed election yet
            //or if the message it recieves contains a better aptitude than the one saved yet.
            if(state == State.NORMAL || newAnnounceIsBetter(message)){
                //The site enter its own informations in the message
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

        } else { //the announce has already passed by here (Protocol.TREATED)
            //It means that the message has already been sent to all the availables sites. The announce can now
            //be treated
            analyseResults(message);

            //The resultBuffer is perpared with its initial values:
            /*
            Result:   00000001  + id of chosen          // 1 + 1 bytes
                    + NOTTREATED/TREATED    // 1 byte
                    + NOTTREATED/TREATED    // 1 byte
                    + NOTTREATED/TREATED    // 1 byte
                    + NOTTREATED/TREATED    // 1 byte
                    total 6 bytes
             */
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

    /**
     * Treats the result message. The message must be sent to all the available sites in the system
     * @param message, message containing the id of the chosen site and for each site weather it has already
     *                 recieved the message or not
     */
    private synchronized void processResult(byte[] message){
        //We copy the content of the message in the result array, in order to send the smallest array possible
        Util.copyToFillByteArray(resultBuffer, message);
        //The 2 first bytes contain the type of the message and the id of the chosen site
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
            //the message has already been sent to all the availables machines in the system
            System.out.println("The election is finished");
            //We check if the chosen machine has recieved the result message.
            //If not the site launches a new election
            if(resultBuffer[(chosen.port - Protocole.basePort) + 2] == Protocole.NOTTREATED){
                launchElection();
            }
        }
    }

    /**
     * Check if the announce contains a better aptitude than the one currenty saved by the site
     * @param announce, message containting the announce infos
     * @return, true if the message contains a better aptitude, false otherwise
     */
    private boolean newAnnounceIsBetter(byte[] announce){
        for(int i = 0; i < Protocole.NBSITES; ++i){
            int aptitude = Util.convertByteArrayToInt(Arrays.copyOfRange(announce, 5 * i + 2, 5 * i + 6));
            if(aptitude > bestAptitude){
                return true;
            }
        }
        return false;
    }

    /**
     * Extract the id of the chosen site, from the announce message
     * @param announce, message containing the aptitudes of all the available machines
     */
    private void analyseResults(byte[] announce){
        int bestAptitudeId = 0;
        int bestAptitude = 0;
        for(int i = 0; i < 4; ++i){
            //we parse the message to obtain the different aptitudes
            int aptitude = Util.convertByteArrayToInt(Arrays.copyOfRange(announce, 5 * i + 2, 5 * i + 6));
            if(aptitude > bestAptitude){
                bestAptitude = aptitude;
                bestAptitudeId = i;
            }
        }
        chosen = candidates.get(bestAptitudeId);
    }

    /**
     * Method sending the message to the next site. If an error occures beacuse the site is not available, it
     * sends the message to the next site, and so on until it finds an available machine (or finds that all the other
     * sites around are down)
     * @param message
     */
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
                    //We send a message using the BetterUDPSender, that asks for an acknowlegment
                    budps.SendMessage(message, address, Protocole.basePort + idToSendTo);
                    stopTrying = true;
                    //As the site has been able to send the message furtherer, it can be marked as in ELECTION state
                    state = State.ELECTION;

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (BetterUDPSender.CommunicationErrorException e) {
                    e.printStackTrace();
                    //it the next site gives no ack, we increase the id the attempt to send the machine to the
                    // following one
                    idToSendTo = (idToSendTo + 1) % Protocole.NBSITES;
                    state = State.NORMAL;
                }
            }
        }
    }
}
