/**
 * Authors: Adrien Allemand, Loyse Krug
 */

import java.net.*;

/**
 * The Applicatif class checks regularly if the chosen site answer to its requests. If it doesn't answer, it launches
 * an election
 */
public class Applicatif extends Thread{

    private Election election;

    private BetterUDPSender budps;

    private DatagramSocket socket;
    private byte[] buffer;

    /**
     * Contructor
     * @param election
     */
    public Applicatif(Election election){
        budps = new BetterUDPSender();
        this.election = election;
        buffer = new byte[2];
        try {
            socket = new DatagramSocket(election.getId() + Protocole.basePort);
            socket.setSoTimeout(Protocole.timeout);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(){
        super.run();
        try {
            //we let the Applicatif sleep for a random amout of time to avoid that all the elections are launch at the
            //same time at the beginning
            sleep((int)(Math.random() * Protocole.maxAddedWaitingTime));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while(true){
            //The Applicatif regularly asks for an ack from the chosen the Election saved.
            contactChosen();
            try {
                sleep(Protocole.baseWaitingTime + (int)(Math.random() * Protocole.maxAddedWaitingTime));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Tries the send a request message to the chosen. If the chosen sites doesn't answer (we consider the site is down)
     * then the Applicatif asks for the Election instance to launch an election
     */
    private void contactChosen(){
        Candidate chosen = election.getChosen();
        if(chosen == null){
            election.launchElection();
        } else {
            buffer[0] = Protocole.REQUEST;
            try {
                InetAddress address = InetAddress.getByName(election.getChosen().ip);
                budps.SendMessage(buffer, address, election.getChosen().port);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (BetterUDPSender.CommunicationErrorException e) {
                election.launchElection();
            }

        }
    }



}
