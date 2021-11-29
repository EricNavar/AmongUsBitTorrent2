// This code was taken from the Computer Network Fundamentals Canvas page
// It works on my Windows computer

/*
 *                     CEN5501C Project2
 * This is the program starting remote processes.
 * This program was only tested on CISE SunOS environment.
 * If you use another environment, for example, linux environment in CISE 
 * or other environments not in CISE, it is not guaranteed to work properly.
 * It is your responsibility to adapt this program to your running environment.
 */

import java.io.*;
import java.util.*;

/*
 * The StartRemotePeers class begins remote peer processes. 
 * It reads configuration file PeerInfo.cfg and starts remote peer processes.
 * You must modify this program a little bit if your peer processes are written in C or C++.
 * Please look at the lines below the comment saying IMPORTANT.
 */
public class StartRemotePeers {
	private peerProcess pp;

	public StartRemotePeers(peerProcess pp) {
		this.pp = pp;
		getConfiguration();
	}

	public void getConfiguration() {
		String st;
		pp.peerInfoVector = new Vector<RemotePeerInfo>();
		int count =0;
		try {
			BufferedReader in = new BufferedReader(new FileReader("PeerInfo.cfg"));
			while ((st = in.readLine()) != null) {

				String[] tokens = st.split("\\s+");
				// don't include this process in the vector of remote peers so that it can't
				// be selected as a preferred neighbor
				pp.allPeers.addElement(new RemotePeerInfo(tokens[0], tokens[1], tokens[2], tokens[3]));

				if (Integer.parseInt(tokens[0]) == pp.peerId) {
					if (tokens[3].equals("1")) {
						pp.setHasFile(true);
					}
				}
				else {	
					pp.peerInfoVector.addElement(new RemotePeerInfo(tokens[0], tokens[1], tokens[2], tokens[3]));

				}
			}

			in.close();
		} catch (Exception ex) {
			System.out.println(ex.toString());
		}
	}

	public void Start(int currentProcess) {
		// TODO Auto-generated method stub
		try {

			// get current path
			String path = System.getProperty("user.dir");

			if (currentProcess != pp.peerInfoVector.get(0).getPeerId()) {
				return;
			}
			System.out.println("attempting to start remote processes");
			// start clients at remote hosts
			for (int i = 0; i < pp.peerInfoVector.size(); i++) {
				RemotePeerInfo pInfo = (RemotePeerInfo) pp.peerInfoVector.elementAt(i);

				System.out.println("Start remote peer " + pInfo.getPeerId() + " at " + pInfo.getPeerAddress());
				
				//Runtime.getRuntime().exec("ssh " + pInfo.peerAddress + " cd " + path + "; java peerProcess " + pInfo.peerId);
				Runtime.getRuntime().exec("java peerProcess " + pInfo.getPeerId());
			}
			System.out.println("Starting all remote peers has done.");

		} catch (Exception ex) {
			System.out.println(ex);
		}
	}
}
