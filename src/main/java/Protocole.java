
/*
Structure of the messages:
Announce: 00000000  + NOTTREATED/TREATED + aptitude of site 0  // 1 + 1 + 4 bytes
                    + NOTTREATED/TREATED + aptitude of site 1  // 1 + 4 bytes
                    + NOTTREATED/TREATED + aptitude of site 2  // 1 + 4 bytes
                    + NOTTREATED/TREATED + aptitude of site 3  // 1 + 4 bytes
                    total 21 bytes

Result:   00000001  + id of chosen          // 1 + 1 bytes
                    + NOTTREATED/TREATED    // 1 byte
                    + NOTTREATED/TREATED    // 1 byte
                    + NOTTREATED/TREATED    // 1 byte
                    + NOTTREATED/TREATED    // 1 byte
                    total 6 bytes

Request:  00000010  total 1 byte

Acknowlegement of announce:
          00000011  +  00000000   total 2 bytes

Acknowlegement of result:
          00000011  +  00000001   total 2 bytes

Acknowlegement of request:
          00000011  +  00000010   total 2 bytes



*/

public class Protocole {

    public static final byte ANNOUNCE = 00000000;
    public static final byte RESULT = 00000001;
    public static final byte REQUEST = 00000010;
    public static final byte ACKNOWLEGMENT = 00000011;
    //public static final byte CANCELLATION = 00000100;

    public static final byte NOTTREATED = 00000000;
    public static final byte TREATED = 00000001;
    public static final int timeout = 2000;
    public static final int basePort = 1992;

    //Insert your addresses here
    public static String[] ipAdresses = {"localhost", "localhost", "localhost", "localhost"};


}
