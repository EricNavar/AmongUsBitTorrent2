// This code was taken from the Computer Network Fundamentals Canvas page
// It works on my Windows computer

import java.io.*;
import java.util.*;

/*
 * The StartRemotePeers class begins remote peer processes. 
 * It reads configuration file PeerInfo.cfg and starts remote peer processes.
 * You must modify this program a little bit if your peer processes are written in C or C++.
 * Please look at the lines below the comment saying IMPORTANT.
 */
public class StartRemotePeers {
	Vector<RemotePeerInfo> remotePeerInfos;

	public StartRemotePeers() {
		getConfiguration();
	}

	public void getConfiguration() {
		String st;
		remotePeerInfos = new Vector<RemotePeerInfo>();
		try {
			BufferedReader in = new BufferedReader(new FileReader("PeerInfo.cfg"));
			while ((st = in.readLine()) != null) {

				String[] tokens = st.split("\\s+");
				// don't include this process in the vector of remote peers so that it can't
				// be selected as a preferred neighbor
				remotePeerInfos.add(new RemotePeerInfo(tokens[0], tokens[1], tokens[2], tokens[3]));
			}

			in.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void Start() {
		try {

			// get current path
			String path = System.getProperty("user.dir");

			System.out.println("attempting to start remote processes");
			// start clients at remote hosts
			for (int i = 0; i < remotePeerInfos.size(); i++) {
				RemotePeerInfo pInfo = (RemotePeerInfo) remotePeerInfos.elementAt(i);

				System.out.println("Start remote peer " + pInfo.getPeerId() + " at " + pInfo.getPeerAddress());
				Runtime rt = Runtime.getRuntime();
				Process proc = rt.exec("ssh ericnavar@storm.cise.ufl.edu");
				rt.exec("ssh ericnavar@" + pInfo.getPeerAddress());
				rt.exec("java peerProcess " + pInfo.getPeerId());
			}
			System.out.println("Starting all remote peers has done.");

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {
		StartRemotePeers srp = new StartRemotePeers();
		srp.Start();
	}
}
