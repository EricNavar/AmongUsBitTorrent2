import java.io.*;
import java.util.Vector;
import static java.lang.Math.ceil;

class peerProcess {
    protected int numberOfPreferredNeighbors;
    protected int unchokingInterval;
    protected int optimisticUnchokingInterval;
    protected String fileName;
    protected int fileSize;
    protected int pieceSize;
    protected int pieceCount;
    protected int peerId;
    protected boolean hasFile;
    final protected int port = 5478; // random port number we will use
	protected Vector<RemotePeerInfo> peerInfoVector;
    // denotes which pieces of the file this process has
    Vector<Boolean> bitfield = new Vector<Boolean>();
    Logger logger;

    public peerProcess(int peerId) {
        this.peerId = peerId;
        logger = new Logger(peerId);
        pieceCount = (int) ceil((double) fileSize / pieceSize);
        bitfield = new Vector<Boolean>(pieceCount);
        hasFile = false;
    }

    public void setHasFile(boolean hasFile) {
        this.hasFile = hasFile;
    }

    public RemotePeerInfo getRemotePeerInfo(int peerId) {
        for (RemotePeerInfo rfi : peerInfoVector) {
            if (rfi.getPeerId() == peerId) {
                return rfi;
            }
        }
        return null;
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
        System.out.println("Attempting to create server socket."); //debug message
		
        if (!hasFile) {
            System.out.print("This process does not have the file. ");
            System.out.println("Attempting to connect as a client to the port...");
			Client client = new Client(this);
			// Handshake just between 1001 and 1002 for now
			client.setPeerID(peerId);
			client.run();
        } else {
            System.out.println("This process has the file. ");
            System.out.println("Starting a listener at the post and try to handshake with other processes...");
            Server.setPp(this);
            Server.startServer();
        }
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
