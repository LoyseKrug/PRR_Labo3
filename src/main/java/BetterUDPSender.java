/**
 * Authors: Adrien Allemand, Loyse Krug
 *
 * Source: https://www.techyourchance.com/thread-safe-observer-design-pattern-in-java/
 */

import java.io.IOException;
import java.net.*;

public class BetterUDPSender {

    public class CommunicationErrorException extends Exception {
        public CommunicationErrorException(String s){
            super(s);
        }
    }

    private DatagramSocket socket;

    /**
     * Send a message and throw an error if no acknowlwgement is recieved from the reciever
     * @param content
     * @param address
     * @param port
     * @throws CommunicationErrorException
     */
    public void SendMessage(byte[] content, InetAddress address, int port) throws CommunicationErrorException {

        try {

            socket = new DatagramSocket();

            DatagramPacket packet = new DatagramPacket(content, content.length, address, port);

            socket.send(packet);

            packet = null;

            socket.setSoTimeout(Protocole.timeout);
            socket.receive(packet);

            if(packet.getLength() != 1 || packet.getData()[0] != Protocole.ACKNOWLEGMENT){
                throw new CommunicationErrorException("Not an ACK");
            }

        } catch (SocketTimeoutException e){
            throw new CommunicationErrorException("Timeout after sending packet");
        } catch (IOException e) {
            throw new CommunicationErrorException("Connection failed while sending packet");
        }
    }
}
