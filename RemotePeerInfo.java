import java.util.Vector;
import java.nio.file.Files;
import static java.lang.Math.ceil;
import java.util.List;
import java.nio.file.Paths;
import java.nio.file.Path;


/*
 * This is the program starting remote processes.
 * This program was only tested on CISE SunOS environment.
 * If you use another environment, for example, linux environment in CISE 
 * or other environments not in CISE, it is not guaranteed to work properly.
 * It is your responsibility to adapt this program to your running environment.
 */

public class RemotePeerInfo {
	private int peerId;
	private String peerAddress;
	private int peerPort;
	private boolean hasFile;
	private Vector<Boolean> bitfield = new Vector<Boolean>(0);
	private boolean choked;
	// this field means that the peer is interested in something from the running process
	private boolean interested;
	private int piecesTransmitted;
	
	public RemotePeerInfo(String pId, String pAddress, String pPort, String hasFile) {
		this.peerId = Integer.parseInt(pId);
		this.peerAddress = pAddress;
		this.peerPort = Integer.parseInt(pPort);
		this.hasFile = "1".equals(hasFile);
		choked = false;
		int fileSize=0;
		int pieceSize =0;
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
        	int totalPieces = (int) ceil((double) fileSize / pieceSize);
        	bitfield.setSize(totalPieces);
        	for (int i = 0; i < bitfield.size(); i++) {
            		bitfield.set(i, this.hasFile);
        	}
	}

	public int getPeerId() {
		return peerId;
	}

	public int getPeerPort() {
		return peerPort;
	}

	public boolean hasFile() {
		return hasFile;
	}

	public String getPeerAddress() {
		return peerAddress;
	}

	public void setBitfield(Vector<Boolean> bitfield) {
		this.bitfield = bitfield;
	}
	
	public Vector<Boolean> getBitfield() {
		return bitfield;
	}

	public boolean isChoked() {
		return choked;
	}

	public void setChoked(boolean choked) {
		this.choked = choked;
	}

	public boolean isInterested() {
		return interested;
	}

	public void setInterested(boolean interested) {
		this.interested = interested;
	}

	public int getPiecesTransmitted() {
		return piecesTransmitted;
	}

	public void resetPiecesTransmitted() {
		piecesTransmitted = 0;
	}

	public void incrementPiecesTransmitted() {
		piecesTransmitted++;
	}

	                                                               // gets the index of a random piece that is missing.
	                                                               // Return -1 if no pieces are missing.
	public int selectRandomMissingPiece() {
		if (hasFile) {                                             // if this peer has everything it needs, this will return -1
			return -1;
		}
		Vector<Integer> missingPieces = new Vector<Integer>();     // Create a temporary vector to hold missing piece values
		for (int i = 0; i < bitfield.size(); i++) {                // walk the entire bitfield vector 
			if (!bitfield.get(i)) {                                // look for bitfields that are not true yet, so missing...
				missingPieces.add(i);                              // add them to the missing piecese collection
			} 
		}
        int missingPieceIndex = (int)Math.floor(Math.random()*(bitfield.size()));  // randomly select an item from the missing pieces
		return missingPieces.get(missingPieceIndex);                               // and return that index's misisng piece location
	}
}
