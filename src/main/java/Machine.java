import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Authors: Adrien Allemand, Loyse Krug
 */

public class Machine {
    private int port;
    private Candidate chosen;
    private List<Candidate> candidates = new ArrayList<Candidate>();
    private boolean announceProcess;
    private int aptitude;
    private Thread askingThread;


    public Machine(List<Candidate> candidates, int aptitude, int port){
        this.candidates = candidates;
        this.aptitude = aptitude;
        this.port = port;
        chosen = null;
        announceProcess = false;


        askingThread = new Thread(){
            @Override
            public void run() {
                super.run();
                if(!contactChosen()){
                    launchElection();
                }
                try {
                    sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        askingThread.start();
    }

    private boolean contactChosen(){
        return false;
    }

    private void launchElection(){

    }

    private void sendMessage(){

    }


}
