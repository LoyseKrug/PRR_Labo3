/**
 * Authors: Adrien Allemand, Loyse Krug
 *
 * sources: https://www.techyourchance.com/thread-safe-observer-design-pattern-in-java/
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

/**
 * Listen on a port from its create and Answer with an ACK to every messages it recieves, before forwarding the content
 * of the message to all its Observers
 */
public class BetterUDPReciever extends Thread {

    /**
     * Implements a unique method that treat the recieved message
     */
    public interface Observer {
        void onMessageRecieved(byte[] message);
    }

    // this is the object we will be synchronizing on ("the monitor")
    private final Object MONITOR = new Object();

    private Set<Observer> mObservers;

    // Connection related variables

    private DatagramSocket socket;
    private boolean isRunning;

    //buffer contains type of the message, for each site
    private byte[] buffer;

    //Contains, type of the message, + for all of the four sites, the aptitude + a byte indicating the apritude is given
    private byte[] acknowlegment;

    /**
     * Constructor
     * @param port listening port
     */
    public BetterUDPReciever(int port){
        buffer = new byte[256];
        try {
            socket = new DatagramSocket(port);
            acknowlegment = new byte[1];
            acknowlegment[0] = Protocole.ACKNOWLEGMENT;

        } catch (SocketException e) {
            throw new RuntimeException("error opening datagramm socket");
        }
    }

    /**
     * This method adds a new Observer - it will be notified when Observable changes
     */
    public void registerObserver(Observer observer) {
        if (observer == null) return;

        synchronized(MONITOR) {
            if (mObservers == null) {
                mObservers = new HashSet<>(1);
            }
            if (mObservers.add(observer) && mObservers.size() == 1) {
                //performInit(); // some initialization when first observer added
            }
        }
    }

    /**
     * This method removes an Observer - it will no longer be notified when Observable changes
     */
    public void unregisterObserver(Observer observer) {
        if (observer == null) return;

        synchronized(MONITOR) {
            if (mObservers != null && mObservers.remove(observer) && mObservers.isEmpty()) {
                //performCleanup(); // some cleanup when last observer removed
            }
        }
    }

    /**
     * This method notifies currently registered observers about Observable's change
     */
    private void notifyObservers(byte[] message) {
        Set<Observer> observersCopy;

        synchronized(MONITOR) {
            if (mObservers == null) return;
            observersCopy = new HashSet<>(mObservers);
        }

        for (Observer observer : observersCopy) {
            observer.onMessageRecieved(message);
        }
    }

    @Override
    public void run() {
        isRunning = true;
        while(isRunning){
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                byte messageType = buffer[0];

                // extract all data from packet
                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                byte[] message = packet.getData();

                //Send acknowlegment back to the sender
                packet = new DatagramPacket(acknowlegment, acknowlegment.length, address, port);
                socket.send(packet);

                // notify we recieved a message
                notifyObservers(message);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
