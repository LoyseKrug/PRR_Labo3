import java.util.ArrayList;
import java.util.List;

public class Main {


    public static void main(String[] args){

        List<Candidate> candidats = new ArrayList<Candidate>();
        for(int i = 0; i < 4; ++i){
            candidats.add(new Candidate(Protocole.ipAdresses[i], Protocole.basePort + i));
        }

        if(args.length == 2){
            new Applicatif(new Election(candidats, Byte.parseByte(args[1])));
        }else{
            System.out.println("Wrong number of arguments");
        }
    }
}
