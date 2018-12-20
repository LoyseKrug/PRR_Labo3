import java.io.IOException;
import java.net.*;

public class Applicatif extends Thread{

    private Election election;

    private DatagramSocket socket;
    private byte[] buffer;

    public Applicatif(Election election){
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
        while(true){
            contactChosen();
            try {
                sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void contactChosen(){
        Candidate chosen = election.getChosen();
        if(chosen == null){
            election.launchElection();
        } else {
            buffer[0] = Protocole.REQUEST;
            try {
                InetAddress address = InetAddress.getByName(election.getChosen().ip);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, election.getChosen().port);
                socket.send(packet);
                packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
            } catch(SocketTimeoutException e){
                election.launchElection();
            } catch (UnknownHostException e) {
                throw new RuntimeException("error getting the InetAddress");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}
