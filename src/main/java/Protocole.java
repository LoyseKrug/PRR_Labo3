import java.util.concurrent.atomic.AtomicLong;

public class Protocole {

    public static final byte ANNOUNCE = 00000000;
    public static final byte RESULT = 00000001;
    public static final int timeout = 2000;
    public static AtomicLong messageNumber;
}
