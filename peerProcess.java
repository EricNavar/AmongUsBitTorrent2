import java.nio.file.Files;
import java.util.Vector;

import static java.lang.Math.ceil;

import java.util.Collections;
import java.util.Random;
import java.util.List;
import java.nio.file.Paths;
import java.nio.file.Path;

import java.nio.*;

//TODO: 1002 is receiving too much data. It goes on forever and peer_1002 gets bigger than peer_1001/thefile
//TODO: question: Do we have to consider the case where 1002 runs before 1001? The graders may expect it.

class peerProcess {
    protected int unchokingInterval;
    protected int optimisticUnchokingInterval;
    protected String fileName;
    // fileSize in bytes
    protected int fileSize;
    // how many bytes a piece is
    protected int pieceSize;
    // how many pieces there are in a file
    protected int totalPieces;
    protected int collectedPieces;
    protected int peerId;
    // if this process has the entire file
    protected boolean hasFile;
    // random port number we will use
    final protected int port = 12602;
    protected Vector<RemotePeerInfo> peerInfoVector;
    // denotes which pieces of the file this process has
    Vector<Boolean> bitfield = new Vector<Boolean>(0);
    Vector<Integer> preferredNeighbors;
    Vector<Integer> interested = new Vector<Integer>(0);
    Vector<ByteBuffer> messagesToSend = new Vector<ByteBuffer>(0);
    Vector<ByteBuffer> pieceMessages = new Vector<ByteBuffer>(0);
    Logger logger;
    Client client;
    Server server;
    Messages message;
    FileHandling FileObject;
    int optimisticallyUnchokedPeer;

    public void incrementCollectedPieces() {
        collectedPieces++;
        if (collectedPieces == totalPieces) {
            hasFile = true;
        }
    }

    public int getPortNumber() {
        return port;
    }

    public int getTotalPieces() {
        return totalPieces;
    }

    public int getPeerId() {
        return peerId;
    }

    public Vector<Boolean> getCurrBitfield() {
        return bitfield;
    }

    public Vector<Integer> getInterested() {
        return interested;
    }

    public void setInterested(Vector<Integer> interest) {
        interested = interest;
    }

    public FileHandling getFileObject() {
        return FileObject;
    }

    public int getCollectedPieces() {
        return collectedPieces;
    }

    public peerProcess(int peerId) {
        this.peerId = peerId;
        logger = new Logger(peerId);
        int numberOfPreferredNeighbors = 5; // set 5 as default

        try {
            // https://www.educative.io/edpresso/reading-the-nth-line-from-a-file-in-java
            Path tempFile = Paths.get(RemotePeerInfo.configName);
            List<String> fileLines = Files.readAllLines(tempFile);

            numberOfPreferredNeighbors = Integer.valueOf(fileLines.get(0).split(" ")[1]);
            unchokingInterval = Integer.valueOf(fileLines.get(1).split(" ")[1]);
            optimisticUnchokingInterval = Integer.valueOf(fileLines.get(2).split(" ")[1]);
            fileName = fileLines.get(3).split(" ")[1];            
            fileSize = Integer.valueOf(fileLines.get(4).split(" ")[1]);
            pieceSize = Integer.valueOf(fileLines.get(5).split(" ")[1]);
        } catch (Exception e) {

        }
        totalPieces = (int) ceil((double) fileSize / pieceSize);
        bitfield.setSize(totalPieces);
        FileObject = new FileHandling(this.peerId, totalPieces, pieceSize, fileName);
        hasFile = false;
        preferredNeighbors = new Vector<Integer>(numberOfPreferredNeighbors);
    }

    public boolean hasFile() {
        return hasFile;
    }

    public void setHasFile(boolean hasFile) {
        this.hasFile = hasFile;
    }

    // finds the peer's handle to be able to get that peer's detailed information
    public RemotePeerInfo getRemotePeerInfo(int peerId) { // searches through a list of peers to find the one with the
                                                          // matching ID
        for (RemotePeerInfo rfi : peerInfoVector) { // iterates over the set of rfi peers
            if (rfi.getPeerId() == peerId) { // if the rfi's peerID matches, then return the rfi handle
                return rfi;
            }
        }
        return null;
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

    public void startTCPConnection(StartRemotePeers srp, int peerId) throws Exception {
        // start server
        System.out.println("Attempting to create server socket."); // debug message
        if (peerId != 1001) { // if client
            System.out.println("Attempting to connect as a client to the port...");
            client = new Client(this);
            client.setPeerID(peerId);
            client.run();
        } else { // if server
            System.out.println("Starting a listener at the port and try to handshake with other processes...");
            server = new Server(this);
            server.startServer();
        }
    }

    // Calculate the peers sending the most data. The optimistically unchoked neighbor is calculated at a different interval in Common.cfg
    public void calculatePreferredNeighbors() {
        preferredNeighbors.clear();
        // Sort the vector of peers
        sortPeerInfoVector();
        // The first 4 peers are the peers that have transmitted the most.
        // Add their peerId to the list of preferred vectors
        for (int i = 0; i < peerInfoVector.size(); i++) {
            // if tie, randomly choose among tied processes
            if (interested.size() > 0) {
                for (int j = 0; j < interested.size(); j++) {
                    if (peerInfoVector.get(i).getPeerId() == interested.get(j))
                        preferredNeighbors.add(peerInfoVector.get(i).getPeerId());
                }
            }
        }

        // after recalculating the preferred neighbors, reset the value of the
        // transmitted data of all remote peers
        resetPeerInfoPiecesTransmitted();

        logger.onChangeOfPreferredNeighbors(preferredNeighbors);
    }

    // this chooses which peer to optimisically unchoke. The peerInfoVector is
    // sorted by pieces transmitted, so choose any peer other than the first 4
    // https://www.educative.io/edpresso/how-to-generate-random-numbers-in-java
    public int chooseOptimisticallyUnchokedPeer() {
        int min = 4;
        int max = interested.size();
        int randomPeerIndex = (int) Math.floor(Math.random() * (max - min + 1) + min);
        if (randomPeerIndex > interested.size() - 1)
            randomPeerIndex = interested.size() - 1;
        optimisticallyUnchokedPeer = interested.get(randomPeerIndex);
        logger.onChangeOfOptimisticallyUnchokedNeighbor(optimisticallyUnchokedPeer);
        return optimisticallyUnchokedPeer;
    }

    // returns true if the given id belongs to either a preffered peer or an optimistically unchoked peer
    public boolean isNeighbor(int id) {
        if (id == optimisticUnchokingInterval) {
            return true;
        }
        for (Integer i : preferredNeighbors) {
            if (i.equals(id)) {
                return true;
            }
        }
        return false;
    }

    private void sortPeerInfoVector() {
        Collections.sort(peerInfoVector, (o1, o2) -> {
            // We want the Vector to be in decreasing order, so we're comparing it backwards
            Integer o2Value = o2.getPiecesTransmitted();
            // need to break ties - 2 or more?
            // https://stackoverflow.com/questions/22968012/how-to-randomly-choose-between-two-choices/22968825
            if (o2Value.compareTo(o1.getPiecesTransmitted()) == 0) {
                Random chooser = new Random();
                if (chooser.nextInt(3) == 1) {
                    return -1;
                } else {
                    return 1;
                }
            }
            return o2Value.compareTo(o1.getPiecesTransmitted());

        });
    }

    private void resetPeerInfoPiecesTransmitted() {
        for (RemotePeerInfo rpi : peerInfoVector) {
            rpi.resetPiecesTransmitted();
        }
    }

    // Takes in the bitfield of another process and determines if it is interested.
    // This method is used after accepting a piece.
    public boolean checkInterested(Vector<Boolean> otherBitfield) {
        for (int i = 0; i < otherBitfield.size(); i++) {
            if (!bitfield.get(i) && otherBitfield.get(i)) {
                return true;
            }
        }
        return false;
    }

    // this is only used for debugging. It prints the bitfield.
    void printBitfield() {
        for (Boolean b: bitfield) {
            System.out.print(b ? "\u001B[31m" + "1" + "\u001B[0m" : "0");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        int peerId = GetProcessId(args);
        if (peerId == -1) {
            return;
        }
        peerProcess pp = new peerProcess(peerId);
        StartRemotePeers srp = new StartRemotePeers(pp);
        // srp.Start(peerId);
        // if PeerInfo.cfg lists the current peerId as having the file
        for (int i = 0; i < pp.bitfield.size(); i++) {
            pp.bitfield.set(i, pp.hasFile);
        }

        try {
            pp.startTCPConnection(srp, peerId);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
