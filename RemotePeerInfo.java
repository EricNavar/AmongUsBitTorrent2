import java.util.Vector;

/*
 * This is the program starting remote processes.
 * This program was only tested on CISE SunOS environment.
 * If you use another environment, for example, linux environment in CISE 
 * or other environments not in CISE, it is not guaranteed to work properly.
 * It is your responsibility to adapt this program to your running environment.
 */

public class RemotePeerInfo {
	public int peerId;
	public String peerAddress;
	public int peerPort;
	public boolean hasFile;
	public Vector<Boolean> bitfield;
	
	public RemotePeerInfo(String pId, String pAddress, String pPort, String hasFile) {
		this.peerId = Integer.parseInt(pId);
		this.peerAddress = pAddress;
		this.peerPort = Integer.parseInt(pPort);
		this.hasFile = "1".equals(hasFile);
	}

	public void setBitfield(Vector<Boolean> bitfield) {
		this.bitfield = bitfield;
	}
}
