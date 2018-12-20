/**
 * Authors: Adrien Allemand, Loyse Krug
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Through this class it is possible to start a site with its Applicatif component and its Election component
 * To do so:
 * Run the main.java file and enter the id of the site (the id must be unique and its value can be 0, 1, 2 or 3)
 *
 * The main will then create the Election instance of the site, giving it the id and port (class Candidate) of all
 * the other machines of the system, and tje port of the site (composed of the id + a basePort (Protocol class))
 */
public class Main {

    public static void main(String[] args){

        // to be able to read the user input
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);
        String line = "";
        System.out.println("Enter a unique id for the site (0-3)");

        try {
            line = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Candidate> candidats = new ArrayList<Candidate>();
        for(int i = 0; i < 4; ++i){
            candidats.add(new Candidate(Protocole.ipAdresses[i], Protocole.basePort + i));
        }

        new Applicatif(new Election(candidats, Byte.parseByte(line)));

    }
}
