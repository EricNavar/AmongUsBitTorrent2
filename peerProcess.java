import java.io.*;
import java.net.*;

import java.nio.file.Files;
import java.util.Vector;
import static java.lang.Math.ceil;
import java.util.Collections;
import java.util.Random;
import java.util.List;
import java.nio.file.Paths;
import java.nio.file.Path;


class peerProcess {
    protected int numberOfPreferredNeighbors;
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
    final protected int port = 5478;
    protected Vector<RemotePeerInfo> peerInfoVector;
    // denotes which pieces of the file this process has
    Vector<Boolean> bitfield = new Vector<Boolean>(0);
    Vector<Integer> preferredNeighbors;
    Vector<Integer> interested = new Vector<Integer>(0);
    Vector<String> messagesToSend = new Vector<String>(0);

    Logger logger;
    Client client;
    Server server;
    Messages message;

    public void incrementCollectedPieces() {
        collectedPieces++;
        if (collectedPieces == totalPieces) {
            hasFile = true;
        }
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


    public int getCollectedPieces() {
        return collectedPieces;
    }

    public peerProcess(int peerId) {
        this.peerId = peerId;
        logger = new Logger(peerId);

        try {
            //https://www.educative.io/edpresso/reading-the-nth-line-from-a-file-in-java
            Path tempFile = Paths.get("Common.cfg");
            List<String> fileLines = Files.readAllLines(tempFile);
            String fileSizeString = fileLines.get(4);
            String pieceSizeString = fileLines.get(5);
            String[] fileSizes = fileSizeString.split(" ");
            String[] pieceSizes = pieceSizeString.split(" ");

            fileSize = Integer.parseInt(fileSizes[1]);
            pieceSize = Integer.parseInt(pieceSizes[1]);
        }
        catch(Exception e)
        {

        }
        totalPieces = (int) ceil((double) fileSize / pieceSize);
        bitfield.setSize(totalPieces);
        hasFile = false;
        preferredNeighbors = new Vector<Integer>(5);
    }


    public boolean hasFile() {
        return hasFile;
    }

    public void setHasFile(boolean hasFile) {
        this.hasFile = hasFile;
    }
                                                           // finds the peer's handle to be able to get that peer's detailed information
    public RemotePeerInfo getRemotePeerInfo(int peerId) {  // searches through a list of peers to find the one with the matching ID
        for (RemotePeerInfo rfi : peerInfoVector) {        // iterates over the set of rfi peers
            if (rfi.getPeerId() == peerId) {               // if the rfi's peerID matches, then return the rfi handle
                return rfi;
            }
        }
        return null;
    }

    public void setPrefferedNeighbors(Vector<Integer> preferredNeighbors) {
        this.preferredNeighbors = preferredNeighbors;
    }

    public void ReadCommongConfig(int peerId) {
        String st = "";

        try {
            BufferedReader in = new BufferedReader(new FileReader("Common.cfg"));

            st = in.readLine();
            String[] tokens = st.split("\\s+");
            this.numberOfPreferredNeighbors = Integer.parseInt(tokens[1]);

            st = in.readLine();
            tokens = st.split("\\s+");
            this.unchokingInterval = Integer.parseInt(tokens[1]);

            st = in.readLine();
            tokens = st.split("\\s+");
            this.optimisticUnchokingInterval = Integer.parseInt(tokens[1]);

            st = in.readLine();
            tokens = st.split("\\s+");
            this.fileName = tokens[1];

            st = in.readLine();
            tokens = st.split("\\s+");
            this.fileSize = Integer.parseInt(tokens[1]);

            st = in.readLine();
            tokens = st.split("\\s+");
            this.pieceSize = Integer.parseInt(tokens[1]);

            in.close();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
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

        if (!hasFile) {
            System.out.print("This process does not have the file. ");
            System.out.println("Attempting to connect as a client to the port...");
            client = new Client(this);
            // Handshake just between 1001 and 1002 for now
            client.setPeerID(peerId);
            client.run();
        } else {
            System.out.println("This process has the file.");
            System.out.println("Starting a listener at the post and try to handshake with other processes...");
            server = new Server(this);
            server.startServer();
        }
    }

    /*
     * CALCULATING PREFERRED NEIGHBORS
     * Each peer determines preferred neighbors every p seconds. Suppose that the 
     * unchoking interval is p. Then every p seconds, peer A reselects its preferred
     * neighbors. To make the decision, peer A calculates the downloading rate from
     * each of its neighbors, respectively, during the previous unchoking interval.
     * Among neighbors that are interested in its data, peer A picks k neighbors that
     * has fed its data at the highest rate. If more than two peers have the same rate,
     * the tie should be broken randomly. Then it unchokes those preferred neighbors by
     * sending "unchoke" messages and it expects to receive "request" messages from
     * them. If a preferred neighbor is already unchoked, then peer A does not have
     * to send ‘unchokeunchoke’ message to it. All other neighbors previously
     * unchoked but not selected as preferred neighbors at this time should be
     * choked unless it is an optimistically should be choked unless it is an
     * optimistically unchoked neighbor. To choke those neighbors, peer A sends
     * unchoked neighbor. To choke those neighbors, peer A sends "choke" messages
     * to them messages to them and stop sending pieces.and stop sending pieces.
     */
    public void calculatePreferredNeighbors() {
        preferredNeighbors.clear();
        // Sort the vector of peers
        sortPeerInfoVector();
        // The first 4 peers are the peers that have transmitted the most.
        // Add their peerId to the list of preferred vectors
        for (int i = 0; i < 4 && i < peerInfoVector.size(); i++) {
            // if tie, randomly choose among tied processes
            if(interested.size() > 0) {
                for (int j = 0; j < interested.size(); j++) {
                    if (peerInfoVector.get(i).getPeerId() == interested.get(j))
                        preferredNeighbors.add(peerInfoVector.get(i).getPeerId());
                }
            }


        }

        // choose another random peer from the rest
        int optimisicallyUnchokedNeighbor = chooseOptimisticallyUnchokedPeer();
        preferredNeighbors.add(optimisicallyUnchokedNeighbor);
        //choke unchosen peers, unchoke chosen peers
        for(RemotePeerInfo rpi : peerInfoVector)
        {
            if(!preferredNeighbors.contains(rpi.getPeerId()))
            {
                // TODO: do we have to do anything else here?
                rpi.setChoked(true);

            }
            else
            {   // we need to unchoke the peers we selected

                // already unchoked
                if(!rpi.isChoked())
                    continue;
                // TODO: do we have to do anything else here?

                rpi.setChoked(false);

            }
        }
        logger.onChangeOfOptimisticallyUnchokedNeighbor(optimisicallyUnchokedNeighbor);
        // after recalculating the preferred neighbors, reset the value of the
        // transmitted data of all remote peers
        resetPeerInfoPiecesTransmitted();

        logger.onChangeOfPreferredNeighbors(preferredNeighbors);
    }

    private void sortPeerInfoVector() {
        Collections.sort(peerInfoVector, (o1, o2) -> {
            // We want the Vector to be in decreasing order, so we're comparing it backwards
            Integer o2Value = o2.getPiecesTransmitted();
            // need to break ties - 2 or more?
            //https://stackoverflow.com/questions/22968012/how-to-randomly-choose-between-two-choices/22968825
            if(o2Value.compareTo(o1.getPiecesTransmitted()) == 0)
            {
                Random chooser = new Random();
                if(chooser.nextInt(3) == 1)
                {
                    return -1;
                }
                else
                    return 1;

            }
            return o2Value.compareTo(o1.getPiecesTransmitted());

        });
    }

    private void resetPeerInfoPiecesTransmitted() {
        for (RemotePeerInfo rpi : peerInfoVector) {
            rpi.resetPiecesTransmitted();
        }
    }

    // this chooses which peer to optimisically unchoke. The peerInfoVector is
    // sorted by pieces transmitted, so choose any peer other than the first 4
    // https://www.educative.io/edpresso/how-to-generate-random-numbers-in-java
    private int chooseOptimisticallyUnchokedPeer() {
        int min = 4;
        int max = peerInfoVector.size();
        int randomPeerIndex = (int)Math.floor(Math.random()*(max-min+1)+min);
        if(randomPeerIndex > peerInfoVector.size()-1)
            randomPeerIndex = peerInfoVector.size()-1;
        return peerInfoVector.get(randomPeerIndex).getPeerId();
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
