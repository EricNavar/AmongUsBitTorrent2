import java.io.*;
import java.util.Vector;
import java.net.*;
import static java.lang.Math.ceil;

class peerProcess {
    int numberOfPreferredNeighbors;
    int unchokingInterval;
    int optimisticUnchokingInterval;
    String fileName;
    int fileSize;
    int pieceSize;
    int pieceCount;
    // denotes which pieces of the file this process has
    static Vector<Boolean> bitfield = new Vector<Boolean>();
    static Logger logger;
    static final int port = 5478; // random port number we will use

    public peerProcess(int peerId, int numberOfPreferredNeighbors, int unchokingInterval,
            int optimisticUnchokingInterval, String fileName, int fileSize, int pieceSize) {
        logger = new Logger(peerId);
        this.numberOfPreferredNeighbors = numberOfPreferredNeighbors;
        this.unchokingInterval = unchokingInterval;
        this.optimisticUnchokingInterval = optimisticUnchokingInterval;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.pieceSize = pieceSize;
        pieceCount = (int) ceil((double) fileSize / pieceSize);
        bitfield = new Vector<Boolean>(pieceCount);
    }

    // this function is to take a string of binary and pad it on the left with
    // zeroes
    // such that it is a certain length.
    static String padWithZeroes(String s, int length) {
        return s + "0".repeat(length - s.length());
    }

    static String createHandshakeMessage(int peerId) {
        // This code is probably not the best
        String result = "P2PFILESHARINGPROJ";
        char c = 0;
        for (int i = 0; i < 10; i++) {
            result = result + c;
        }
        result = result + Integer.toBinaryString(peerId);
        return result;
    }

    // no body in message
    static String createChokeMessage() {
        String result = padWithZeroes(Integer.toBinaryString(0), 32);
        result = result + padWithZeroes(Integer.toBinaryString(MessageType.CHOKE.ordinal()), 8);
        return result;
    }

    // no body in message
    static String createUnchokeMessage() {
        String result = padWithZeroes(Integer.toBinaryString(0), 32);
        result = result + padWithZeroes(Integer.toBinaryString(MessageType.UNCHOKE.ordinal()), 8);
        return result;
    }

    // no body in message
    static String createInterestedMessage() {
        String result = padWithZeroes(Integer.toBinaryString(0), 32);
        result = result + padWithZeroes(Integer.toBinaryString(MessageType.INTERESTED.ordinal()), 8);
        return result;
    }

    // no body in message
    static String createNotInterestedMessage() {
        String result = padWithZeroes(Integer.toBinaryString(0), 32);
        result = result + padWithZeroes(Integer.toBinaryString(MessageType.NOT_INTERESTED.ordinal()), 8);
        return result;
    }

    static String createHaveMessage(int payload) {
        String result = padWithZeroes(Integer.toBinaryString(0), 32);
        result = result + padWithZeroes(Integer.toBinaryString(MessageType.HAVE.ordinal()), 8);
        // result = result + Integer.toBinaryString(payload);

        return result;
    }

    static String createBitfieldMessage(int payload) {
        String result = padWithZeroes(Integer.toBinaryString(0), 32);
        result = result + padWithZeroes(Integer.toBinaryString(MessageType.BITFIELD.ordinal()), 8);
        return result;
    }

    static String createRequestMessage(int payload) {
        String result = padWithZeroes(Integer.toBinaryString(0), 32);
        result = result + padWithZeroes(Integer.toBinaryString(MessageType.REQUEST.ordinal()), 8);
        return result;
    }

    static String createPieceMessage(int payload) {
        String result = padWithZeroes(Integer.toBinaryString(0), 32);
        result = result + padWithZeroes(Integer.toBinaryString(MessageType.PIECE.ordinal()), 8);
        return result;
    }

    static void decodeMessage(String binary) {

    }

    static peerProcess ReadCommongConfig(int peerId) {
        String st = "";
        int numberOfPreferredNeighbors = 0;
        int unchokingInterval = 0;
        int optimisticUnchokingInterval = 0;
        String fileName = "";
        int fileSize = 0;
        int pieceSize = 0;

        try {
            BufferedReader in = new BufferedReader(new FileReader("Common.cfg"));

            st = in.readLine();
            String[] tokens = st.split("\\s+");
            numberOfPreferredNeighbors = Integer.parseInt(tokens[1]);

            st = in.readLine();
            tokens = st.split("\\s+");
            unchokingInterval = Integer.parseInt(tokens[1]);

            st = in.readLine();
            tokens = st.split("\\s+");
            optimisticUnchokingInterval = Integer.parseInt(tokens[1]);

            st = in.readLine();
            tokens = st.split("\\s+");
            fileName = tokens[1];

            st = in.readLine();
            tokens = st.split("\\s+");
            fileSize = Integer.parseInt(tokens[1]);

            st = in.readLine();
            tokens = st.split("\\s+");
            pieceSize = Integer.parseInt(tokens[1]);

            in.close();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }

        return new peerProcess(peerId, numberOfPreferredNeighbors, unchokingInterval, optimisticUnchokingInterval,
                fileName, fileSize, pieceSize);
    }

    private static int GetProcessId(String[] args) {
        if (args.length == 0) {
            System.out.println("Process number must be provided.");
            return -1;
        }
        String peerIdString = args[0];
        // if peerIdString is not a positive integer
        int peerId = 0;
        try {
            peerId = Integer.parseInt(peerIdString);
            assert peerId >= 0;
        } catch (Exception e) {
            System.out.println("The process ID must be a positive integer");
        }

        System.out.println("Process " + args[0]);

        return peerId;
    }

    public static void startTCPConnection(StartRemotePeers srp, int peerId) throws Exception {
        // start server
        System.out.println("Attempting to create server socket.");
        int clientNum = 1;
        if (!srp.hasFile) {
            System.out.print("This process does not have the file. ");
            System.out.println(" Attempting to connect as a client to the port...");
            Client client = new Client();
            client.run();
        } else {
            System.out.println("This process has the file. ");
            System.out.println("Starting a listener at the post and try to handshake with other processes...");
            Server server = new Server();
            server.startServer();

            try {
                // make list of peerIds that have the file
                Vector<Integer> haveFile = new Vector<Integer>();
                for (RemotePeerInfo rpi : srp.peerInfoVector) {
                    if (rpi.hasFile) {
                        haveFile.addElement(rpi.peerId);
                    }
                }

                // try to handshake with processes that have the file
                for (Integer i : haveFile) {
                    String messageToSend = createHandshakeMessage(peerId);
                    // new Handler(listener.accept(), peerId).sendMessage(messageToSend);
                }

            } catch (Exception e) {

            }
        }
    }

    public static void main(String[] args) {
        int peerId = GetProcessId(args);
        if (peerId == -1) {
            return;
        }
        peerProcess pp = ReadCommongConfig(peerId);
        StartRemotePeers srp = new StartRemotePeers(peerId);
        // srp.Start(peerId);
        // if PeerInfo.cfg lists the current peerId as having the file
        for (int i = 0; i < bitfield.size(); i++) {
            bitfield.set(i, srp.hasFile);
        }

        /*
         * TODO: Suppose that peer A tries to make a TCP connection to peer B. Here we
         * describe the behavior of peer A, but peer B should also follow the same
         * procedure as peer A. After the TCP connection is established, peer A sends a
         * handshake message to peer B. It also receives a handshake message from peer B
         * and checks whether peer B is the right neighbor. The only thing to do is to
         * check whether the handshake header is right and the peer ID is the expected
         * one. After handshaking, peer A sends a ‘bitfield’ message to let peer B know
         * which file pieces it has. Peer B will also send its ‘bitfield’ message to
         * peer A, unless it has no pieces. If peer A receives a ‘bitfield message from
         * peer B and it finds out that peer B has pieces that it doesn doesn’t have,
         * peer A sends ‘interestedinterested’ message to peer B. Otherwise, it sends
         * ‘not interested interested’ message.
         */
        try {
            startTCPConnection(srp, peerId);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}