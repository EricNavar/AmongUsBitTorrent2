import java.nio.file.Files;
import java.util.Vector;

import static java.lang.Math.ceil;

import java.util.Collections;
import java.util.Random;
import java.util.List;
import java.nio.file.Paths;
import java.nio.file.Path;

import java.nio.*;

import java.io.*;
import java.util.*;

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
	protected boolean ExitThread;
    // if this process has the entire file
    protected boolean hasFile;

    volatile protected Vector<RemotePeerInfo> peerInfoVector;
    volatile protected Vector<RemotePeerInfo> allPeers = new Vector<RemotePeerInfo>(0);

    // denotes which pieces of the file this process has
    Vector<Boolean> bitfield = new Vector<Boolean>(0);
    Vector<Integer> preferredNeighbors;
    Vector<Integer> interested = new Vector<Integer>(0);
	volatile Vector<Integer> ChokingNeighbors = new Vector<Integer>();
	volatile Vector<Boolean> NEWChokingNeighbors = new Vector<Boolean>();
	volatile Vector<Boolean> NEWUnChokingNeighbors = new Vector<Boolean>();
	HashMap<Integer, String> hash_map = new HashMap<Integer, String>();
    Vector<ByteBuffer> messagesToSend = new Vector<ByteBuffer>(0);
    Vector<ByteBuffer> pieceMessages = new Vector<ByteBuffer>(0);
    public Logger logger;
    Client client;
    Messages message;
    FileHandling FileObject;
    int optimisticallyUnchokedPeer;
    int port;
    boolean chokingTimerFlag;
    boolean optimisticTimerFlag;
    boolean chokingComputeCompleteTimerFlag;
    boolean optimisticComputeCompleteTimerFlag;
    String peerAddress;
    int numberOfPreferredNeighbors = 5; // set 5 as default

    public void incrementCollectedPieces() {
        collectedPieces++;
        boolean hasFileFlag = true;
        for (Boolean b : bitfield) {
            if (!b) {
                hasFileFlag = false;
                break;
            }
        }
        hasFile = hasFileFlag;
    }

    public int getPortNumber() {
        return port;
    }

    public int getTotalPieces() {
        return totalPieces;
    }

    public synchronized int getPeerId() {
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

    public int GetIndexNumber() {
        for(int i =0; i < this.allPeers.size(); i++)
        {
            if(this.peerId == this.allPeers.get(i).getPeerId())
            {
                return i;
            }
        }
        return -1;
	}

    public int GetPeerIndexNumber(int peerNumber) {
        for(int i =0; i < this.allPeers.size(); i++)
        {
            if(peerNumber == this.allPeers.get(i).getPeerId())
            {
                return i;
            }
        }
        return -1;
	}

    public int getCollectedPieces() {
        return collectedPieces;
    }

    public  peerProcess(int peerId) {
        this.peerId = peerId;
        logger = new Logger(peerId);

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
            e.printStackTrace();
        }
        totalPieces = (int) ceil((double) fileSize / pieceSize);
        bitfield.setSize(totalPieces);
        hasFile = false;  // the next function will set this if this peer has the file 
        getConfiguration();
        FileObject = new FileHandling(this.peerId, totalPieces, pieceSize, fileName, hasFile);

        preferredNeighbors = new Vector<Integer>(numberOfPreferredNeighbors);
        optimisticallyUnchokedPeer = -1;

        chokingTimerFlag = false;
        optimisticTimerFlag = false;
        chokingComputeCompleteTimerFlag = false;
        optimisticComputeCompleteTimerFlag  = false;
		ExitThread = false;
    }

    public void ShutdownTimers(){
        ExitThread = true;
    }

    public boolean GetExitStatus(){
        return ExitThread;
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

        return peerId;
    }

    public void startTCPConnection(int peerId) throws Exception {
        // start server
        //System.out.println("Attempting to create server socket."); // debug message
        //if (peerId != 1000) { // if client
            if (Handler.DEBUG_MODE()) System.out.println("Attempting to connect as a peer to the port...");
            client = new Client(this);
            client.setPeerID(peerId);
            client.run();
        //} else { // if server
        //    System.out.println("Starting a listener at the port and try to handshake with other processes...");
        //    server = new Client(this);
        //    System.out.println("Step 1...");
        //    server.startServer();
        //}
    }

    // Calculate the peers sending the most data. The optimistically unchoked
    // neighbor is calculated at a different interval in Common.cfg
    public synchronized void calculatePreferredNeighbors() {
        Vector<Integer> newPreferredNeighbors = new Vector<Integer>(preferredNeighbors.size());
        // Sort the vector of peers
        sortPeerInfoVector();
        // The first 4 peers are the peers that have transmitted the most.
        // Add their peerId to the list of preferred vectors
		// if (Handler.DEBUG_MODEL2()) System.out.println(" Intersted Neighbors are " + interested);
        for (int i = 0; i < numberOfPreferredNeighbors && i < peerInfoVector.size(); i++) {
            // if tie, randomly choose among tied processes
            if (interested.size() > 0) {
                for (int j = 0; j < interested.size(); j++) {
                    int tempPeerId = peerInfoVector.get(i).getPeerId();
                    int tempInterested = interested.get(j);
                    if (tempPeerId == tempInterested) {
			if(!newPreferredNeighbors.contains(peerInfoVector.get(i).getPeerId()))                      
 				newPreferredNeighbors.add(peerInfoVector.get(i).getPeerId());
                    }
                }
            }
        }
        if(newPreferredNeighbors.size() < numberOfPreferredNeighbors)
        {
            if (interested.size() > 0) {
                for (int j = 0; j < interested.size(); j++) {
			if(!newPreferredNeighbors.contains(interested.get(j)))
                       newPreferredNeighbors.add(interested.get(j));
			if(newPreferredNeighbors.size() == numberOfPreferredNeighbors)
			break;
                    
                }
            }
        }
        preferredNeighbors = newPreferredNeighbors;
        resetPeerInfoPiecesTransmitted();
        if (Handler.DEBUG_MODE()) System.out.println("dwqwqdqwdqw"+preferredNeighbors);

		if (Handler.DEBUG_MODEL2()) System.out.println("Preferred Neighbors are " + preferredNeighbors);

        // after recalculating the preferred neighbors, reset the value of the
        // transmitted data of all remote peers
        resetPeerInfoPiecesTransmitted();

        logger.onChangeOfPreferredNeighbors(preferredNeighbors);
    }

    // this chooses which peer to optimisically unchoke. The peerInfoVector is
    // sorted by pieces transmitted, so choose any peer other than the first 4
    // https://www.educative.io/edpresso/how-to-generate-random-numbers-in-java
    public synchronized int chooseOptimisticallyUnchokedPeer() {
        // this is the vector of peers to consider. It's the peers that are in
        // interested but not already in preferredNeighbors
        Vector<Integer> toConsider = new Vector<Integer>();
        for (Integer i : interested) {
            if (!preferredNeighbors.contains(i)) {
                toConsider.add(i);
            }
        }
        if (toConsider.size() == 0) {
            optimisticallyUnchokedPeer = -1;
            return optimisticallyUnchokedPeer;
        }
        Random rn = new Random();
        int randomPeerIndex = rn.nextInt(interested.size());
        optimisticallyUnchokedPeer = interested.get(randomPeerIndex);
        logger.onChangeOfOptimisticallyUnchokedNeighbor(optimisticallyUnchokedPeer);
		return optimisticallyUnchokedPeer;
    }

    // returns true if the given id belongs to either a preffered peer or an
    // optimistically unchoked peer
    public synchronized boolean isNeighbor(int id) {
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

    private synchronized void sortPeerInfoVector() {

        Collections.sort(peerInfoVector, (o1, o2) -> {
            // We want the Vector to be in decreasing order, so we're comparing it backwards
            Integer o2Value = o2.getPiecesTransmitted();
            // need to break ties - 2 or more?
            // https://stackoverflow.com/questions/22968012/how-to-randomly-choose-between-two-choices/22968825
            if (o2Value.compareTo(o1.getPiecesTransmitted()) == 0) {
                Random chooser = new Random();
                if (chooser.nextInt(2) == 0) {
                    return -1;
                } else {
                    return 1;
                }
            }
            return o2Value.compareTo(o1.getPiecesTransmitted());

        });
    }

    private synchronized void resetPeerInfoPiecesTransmitted() {
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
    String printBitfield(Vector<Boolean> bf) {
        StringBuilder s = new StringBuilder("");
        for (Boolean b : bf) {
            //s.append(b ? "\u001B[31m" + "1" + "\u001B[0m" : "\u001B[34m" + "0" + "\u001B[0m");
            s.append(b ? 1 : 0);
        }
        return s.toString();
    }

    public int randomMissingPiece() {
        Vector<Integer> missingPieces = new Vector<Integer>(); // Create a temporary vector to hold missing piece values
        for (int i = 0; i < bitfield.size(); i++) { // walk the entire bitfield vector
            if (!bitfield.get(i)) { // look for bitfields that are not true yet, so missing...
                missingPieces.add(i); // add them to the missing piecese collection
            }
        }

        int missingPieceIndex = (int) Math.floor(Math.random() * (missingPieces.size()));
        int askForPiece = 0;
        if (missingPieceIndex < missingPieces.size())
            askForPiece = missingPieces.get(missingPieceIndex);
        return askForPiece;
    }

    public boolean doAllProcessesHaveTheFile() {
        for (RemotePeerInfo peer: peerInfoVector) {
            if (!peer.hasFile()) {
                return false;
            }
        }
        return true;
    }

    public void getConfiguration() {
		String st;
		peerInfoVector = new Vector<RemotePeerInfo>();
		try {
			BufferedReader in = new BufferedReader(new FileReader("PeerInfo.cfg"));
			while ((st = in.readLine()) != null) {

				String[] tokens = st.split("\\s+");
				// don't include this process in the vector of remote peers so that it can't
				// be selected as a preferred neighbor
                // <PeerID> <DNS Machine> <Port> <HasFile>
				// 1001 lin114-00.cise.ufl.edu 6001 1
				allPeers.add(new RemotePeerInfo(tokens[0], tokens[1], tokens[2], tokens[3]));

				if (Integer.parseInt(tokens[0]) == peerId) {
                    peerAddress = tokens[1];
                    hasFile = tokens[3].equals("1");
                }
                peerInfoVector.add(new RemotePeerInfo(tokens[0], tokens[1], tokens[2], tokens[3]));
				NEWChokingNeighbors.add(false);
				NEWUnChokingNeighbors.add(false);
			}

			in.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

    public synchronized void onChokingTimeout() {
        // if (Handler.DEBUG_MODEL1()) System.out.println("onChokingTimeout()");
        try {
            calculatePreferredNeighbors();
            messagesToSend.clear();
            // choke unchosen peers, unchoke chosen peers
            for (int i = 0; i < peerInfoVector.size(); i++) {
                RemotePeerInfo rpi = peerInfoVector.get(i);
				//if (Handler.Handler.DEBUG_MODE()) System.out.println(" i = " + i + " peer id = " + rpi.getPeerId() + " !isNeighbor(rpi.getPeerId()) = " + !isNeighbor(rpi.getPeerId()) + " !rpi.isChoked() = " + !rpi.isChoked());
                if (!isNeighbor(rpi.getPeerId()) && !rpi.isChoked()) { 
					this.NEWChokingNeighbors.set(GetPeerIndexNumber(rpi.getPeerId()), true);  // same as above
                    rpi.setChoked(true);
                }
                else if (isNeighbor(rpi.getPeerId()) && rpi.isChoked()) {
					this.NEWUnChokingNeighbors.set(GetPeerIndexNumber(rpi.getPeerId()), true);  // same as above
					if (Handler.DEBUG_MODE()) System.out.println("Unchoking " + rpi.getPeerId() + " NEWUnChokingNeighbors = " + this.NEWUnChokingNeighbors);
                    rpi.setChoked(false);
                }
            }
            chokingTimerFlag = !chokingTimerFlag;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Timer for unchoking the neighbors who send the most data. Optimistically
    // unchoked neighbors is unchoked
    // in the runOptimisticallyUnchokedTimer()
    private void runUnchokingTimer() {
        // Every 5 seconds, recalculate the preferred neighbors
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                //chokingTimerFlag = !chokingTimerFlag;
				onChokingTimeout();
				if (GetExitStatus() == true) {
					timer.cancel();
				}
            }
        }, 0, unchokingInterval * 1000);
    }

    public synchronized void onOptimisticTimeout() {
        // if (Handler.DEBUG_MODE()) System.out.println("onOptimisticTimeout()");
        try {
            int getPeerUnchoke = chooseOptimisticallyUnchokedPeer();
            if (getPeerUnchoke == -1) {
                return;
            }
            RemotePeerInfo rpi = getRemotePeerInfo(getPeerUnchoke);
            messagesToSend.clear();
            //messagesToSend.add(Messages.createUnchokeMessage());
			this.NEWUnChokingNeighbors.set(GetPeerIndexNumber(getPeerUnchoke), true);  // same as above
            if (Handler.DEBUG_MODE()) System.out.println("Optimistically unchoking " + getPeerUnchoke + " NEWUnChokingNeighbors = " + this.NEWUnChokingNeighbors);
            rpi.setChoked(false);
            //sendMessageBB(messagesToSend.get(0));
            optimisticTimerFlag = !optimisticTimerFlag;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void runOptimisticallyUnchokedTimer() {
        // Every 10 seconds, recalculate the preferred neighbors
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
				onOptimisticTimeout();
				if (GetExitStatus() == true) {
					timer.cancel();
				}
            }
        }, 0, optimisticUnchokingInterval * 1000);
    }

    public static void main(String[] args) {
        int peerId = GetProcessId(args);
        if (peerId == -1) {
            return;
        }
        peerProcess pp = new peerProcess(peerId);

        for(int i =0; i < pp.allPeers.size(); i++)
        {
            if(pp.peerId == pp.allPeers.get(i).getPeerId())
            {
                pp.port = pp.allPeers.get(i).getPeerPort();
                break;
            }
        }

        // if PeerInfo.cfg lists the current peerId as having the file

            for (int i = 0; i < pp.bitfield.size(); i++) {
                pp.bitfield.set(i, pp.hasFile);
            }

        pp.runUnchokingTimer();
        pp.runOptimisticallyUnchokedTimer();

        try {
            pp.startTCPConnection(peerId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
