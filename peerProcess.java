class peerProcess {
    public peerProcess(String peerId) {
        logger = new Logger(peerId);
    }
    
    static Logger logger;

    // this function is to take a string of binary and pad it on the left with zeroes
    // such that it is a certain length.
    static String padWithZeroes(String s, int length) {
        return s + "0".repeat(length - s.length());
    }

    static String createHandshakeMessage(int peerId) {
        //This code is probably not the best
        String result = "P2PFILESHARINGGPROJ";
        char c = 0;
        for (int i = 0; i < 10; i++) {
            result = result + c;
        }
        result = result + Integer.toBinaryString(peerId);
        return result;
    }

    // no body in message
    static String createChokeMessage() {
        String result = padWithZeroes(Integer.toBinaryString(0),32);
        result = result + padWithZeroes(Integer.toBinaryString(MessageType.CHOKE.ordinal()),8);
        return result;
    }

    //no body in message
    static String createUnchokeMessage() {
        String result = padWithZeroes(Integer.toBinaryString(0),32);
        result = result + padWithZeroes(Integer.toBinaryString(MessageType.UNCHOKE.ordinal()),8);
        return result;
    }

    //no body in message
    static String createInterestedMessage() {
        String result = padWithZeroes(Integer.toBinaryString(0),32);
        result = result + padWithZeroes(Integer.toBinaryString(MessageType.INTERESTED.ordinal()),8);
        return result;
    }

    //no body in message
    static String createNotInterestedMessage() {
        String result = padWithZeroes(Integer.toBinaryString(0),32);
        result = result + padWithZeroes(Integer.toBinaryString(MessageType.NOT_INTERESTED.ordinal()),8);
        return result;
    }

    static String createHaveMessage() {
        String result = padWithZeroes(Integer.toBinaryString(0),32);
        result = result + padWithZeroes(Integer.toBinaryString(MessageType.HAVE.ordinal()),8);
        return result;
    }

    static String createBitfieldMessage() {
        String result = padWithZeroes(Integer.toBinaryString(0),32);
        result = result + padWithZeroes(Integer.toBinaryString(MessageType.BITFIELD.ordinal()),8);
        return result;
    }

    static String createRequestMessage() {
        String result = padWithZeroes(Integer.toBinaryString(0),32);
        result = result + padWithZeroes(Integer.toBinaryString(MessageType.REQUEST.ordinal()),8);
        return result;
    }
    
    static String createPieceMessage() {
        String result = padWithZeroes(Integer.toBinaryString(0),32);
        result = result + padWithZeroes(Integer.toBinaryString(MessageType.PIECE.ordinal()),8);
        return result;
    }

    static void decodeMessage(String binary) {

    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Process number must be provided.");
            return;
        }
        String peerID = args[0];
        // if peerID is not a positive integer
        if (!peerID.matches("\\d+")) {
            System.out.println("Process number must be a positive integer.");
            return;
        }

        System.out.println("Process " + args[0]);
    }
}