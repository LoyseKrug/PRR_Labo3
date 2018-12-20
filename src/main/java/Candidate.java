/**
 * Authors: Adrien Allemand, Loyse Krug
 */

/**
 * The Candidate class is used to stock the port and the ip of a candidate to the Election
 */
public class Candidate {

    String ip;
    int port;

    /**
     * Constructor
     * @param ip, string: name of the ip address
     * @param port, int, port number
     */
    public Candidate(String ip, int port){
        this.ip = ip;
        this.port = port;

    }
}
