// This file uses a lot of logic from the sample file from Canvas

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;
import java.math.BigInteger; 
import java.security.MessageDigest; 
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.nio.channels.*;
import java.lang.Thread;
 
import java.nio.*;
import java.io.IOException;
// idea of file output streams came from https://www.techiedelight.com/how-to-write-to-a-binary-file-in-java/
 
public class Client {

    volatile Vector<Socket> socketlist;
	  volatile Vector<ServerSocket> socketServerlist;
    volatile Vector<ObjectInputStream> InputStreamlist;
    volatile Vector<ObjectOutputStream> OutputStreamlist;
    volatile HashMap<String, String> ipAddresses;

    volatile int peerID;

    // String bitfieldHandshake;
    // FileHandling handler;

    // int socket;
    volatile peerProcess pp;

    void setPeerID(int t_peerID) {
        peerID = t_peerID;
    }

    public Client(peerProcess pp) {
        this.pp          = pp;
        socketlist       = new Vector<Socket>();
        socketServerlist = new Vector<ServerSocket>();
        InputStreamlist  = new Vector<ObjectInputStream>();
        OutputStreamlist = new Vector<ObjectOutputStream>();

        ipAddresses = new HashMap<String, String>();
        ipAddresses.put("lin114-00.cise.ufl.edu","10.242.94.34");
        ipAddresses.put("lin114-01.cise.ufl.edu","10.242.94.35");
        ipAddresses.put("lin114-02.cise.ufl.edu","10.242.94.36");
        ipAddresses.put("lin114-03.cise.ufl.edu","10.242.94.37");
        ipAddresses.put("lin114-04.cise.ufl.edu","10.242.94.38");
        ipAddresses.put("lin114-05.cise.ufl.edu","10.242.94.39");
        ipAddresses.put("lin114-06.cise.ufl.edu","10.242.94.40");
        ipAddresses.put("lin114-07.cise.ufl.edu","10.242.94.41");
        ipAddresses.put("lin114-08.cise.ufl.edu","10.242.94.42");
        ipAddresses.put("lin114-09.cise.ufl.edu","10.242.94.43");
        ipAddresses.put("lin114-10.cise.ufl.edu","10.242.94.44");
        ipAddresses.put("lin114-11.cise.ufl.edu","10.242.94.45");
    }


    void run() {

        try {
            // create a socket to connect to the server
			//System.out.println(" pp.getPeerID() " + pp.getPeerId() + " pp.allPeers.get(i).getPeerId() " + pp.allPeers.get(0).getPeerId());
            System.out.println(" peerID " + this.peerID + " fist one is " + pp.allPeers.get(0).getPeerId());
            // open to peers with a lower ID
            for (int i = 0; pp.getPeerId() != pp.allPeers.get(i).getPeerId(); i++) {
            //for (int i = 0; i<1; i++) {
			    //if (Handler.DEBUG_MODE()) System.out.println(" i = " + i);
			    if (pp.getPeerId() == pp.allPeers.get(i).getPeerId() ) { // this condition will never be true
					//break;
				} else {
                    Socket nextSock;
                    // if peer 1002 is trying to open up for peer 1001, then thisAddress = 10.242.94.35 and otherAddress = 10.242.94.34
					int  thisPort =  pp.allPeers.get(pp.GetPeerIndexNumber(pp.getPeerId())).getPeerPort();
					String thisAddress =  ipAddresses.get(pp.allPeers.get(pp.GetPeerIndexNumber(pp.getPeerId())).getPeerAddress());
                    InetAddress thisInetAddress = InetAddress.getByName(thisAddress);
					int otherPort = pp.allPeers.get(i).getPeerPort();
					String otherAddress = ipAddresses.get(pp.allPeers.get(i).getPeerAddress());
                    InetAddress otherInetAddress = InetAddress.getByName(otherAddress);
				    if (Handler.DEBUG_MODE()) System.out.println(" I am " + pp.getPeerId() + " Index Number " + pp.GetIndexNumber() + " Attempting to connect to localhost " + pp.allPeers.get(i).getPeerId() + " which is on port " + thisPort);
					Socket NewSocket = new Socket(otherInetAddress, otherPort, thisInetAddress, thisPort);
                    NewSocket.setKeepAlive(true);

                    ObjectOutputStream out = new ObjectOutputStream(NewSocket.getOutputStream());
                    out.flush();
                    InputStream inputStream = NewSocket.getInputStream();
                    ObjectInputStream in = new ObjectInputStream(inputStream);

                    socketlist.add(NewSocket);
                    System.out.println("is NewSocket closed: " + NewSocket.isClosed());
					Handler MyHandler = new Handler(NewSocket, pp.allPeers.get(i).getPeerId(), pp, in, out);
					MyHandler.start();
                    System.out.println("created receiving socket with address: " + NewSocket.getLocalAddress() + " with local port " + NewSocket.getLocalPort() + " and remote port " + NewSocket.getPort());
			        if (Handler.DEBUG_MODE()) System.out.println("Created a initiator socket with peer " + pp.allPeers.get(i).getPeerId() + " on their port " + thisPort);
                    System.out.println("is NewSocket closed: " + NewSocket.isClosed());
                    pp.logger.log("iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii");
				}
            }
			// @ERIC_N  These next three lines truncate off the process from making the remaining connections.  Just won't happen... safe for Ubuntu not for CISE
			// if (pp.getPeerId() >= 1002) {
			// 	while(true);
			// }
			if (Handler.DEBUG_MODE()) System.out.println(" Done with Lower peer connections ");
			boolean start = false;
            // talk to peers with a higher ID
            for (int i =  0 ; i < pp.allPeers.size(); i++) {
			    if (pp.getPeerId() == pp.allPeers.get(i).getPeerId() ) {
					start = true;
				} else {
					if (start) {
                    Socket nextSock;
                    int  thisPort =  pp.allPeers.get(pp.GetPeerIndexNumber(pp.getPeerId())).getPeerPort();
					String thisAddress =  ipAddresses.get(pp.allPeers.get(pp.GetPeerIndexNumber(pp.getPeerId())).getPeerAddress());
                    InetAddress thisInetAddress = InetAddress.getByName(thisAddress);
					int otherPort = pp.allPeers.get(i).getPeerPort();
					String otherAddress = ipAddresses.get(pp.allPeers.get(i).getPeerAddress());
                    InetAddress otherInetAddress = InetAddress.getByName(otherAddress);
			        if (Handler.DEBUG_MODE()) System.out.println(" I am " + pp.getPeerId() + " Attempting to set up connection to " + pp.allPeers.get(i).getPeerId() + " which is on port " + thisPort);
                    // 100 is the backlog. Not sure what the ideal number is, but 100 probably can't hurt.
					ServerSocket NewSocket = new ServerSocket(thisPort, 100, thisInetAddress);
					if (Handler.DEBUG_MODE()) System.out.println("Trying to accept socket of allPeers(" + i + ") known as peerID " + pp.allPeers.get(i).getPeerId());
					Socket GetIt = NewSocket.accept();
                    System.out.println("accepted");
                    GetIt.setKeepAlive(true);
                    ObjectOutputStream out = new ObjectOutputStream(GetIt.getOutputStream());
                    out.flush();
                    InputStream inputStream = GetIt.getInputStream();
                    ObjectInputStream in = new ObjectInputStream(inputStream);
					Handler MyHandler = new Handler(GetIt, pp.allPeers.get(i).getPeerId(), pp, in, out);
                    System.out.println("is GetIt closed: " + GetIt.isClosed());
                    socketServerlist.add(NewSocket);
					MyHandler.start();
			        if (Handler.DEBUG_MODE()) System.out.println("Created a server socket " + pp.allPeers.get(i).getPeerId() + " and peer port " + thisPort);
                    System.out.println("created talking socket with address: " + GetIt.getLocalAddress() + " with local port " + GetIt.getLocalPort() + " and remote port " + GetIt.getPort());
                    System.out.println("is GetIt closed: " + GetIt.isClosed());
                    pp.logger.log("iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii");
					}
				}
            }
			if (Handler.DEBUG_MODE()) System.out.println(" Done with Higher peer connections ");
		    
//			int clientNum = 1;
//	
//            for (int i = 0; (i < pp.allPeers.size()); i++) {
//			    if (pp.getPeerId() == pp.allPeers.get(i).getPeerId() ) {
//					//break;
//				} else {
//					System.out.println("Trying to accept socket of allpeeres(" + i + ") known as peerID " + pp.allPeers.get(i).getPeerId());
//					Socket NewSocket = socketServerlist.get(i).accept();
//					System.out.println("Trying to accept socket of allpeeres(" + i + ") known as peerID " + pp.allPeers.get(i).getPeerId());
//					Handler MyHandler = new Handler(NewSocket, pp.allPeers.get(i).getPeerId());
//					System.out.println("Managed to accept socket " + i + " as " + MyHandler);
//				}
//           }
			/*
            for (int i = 0; i < pp.allPeers.size(); i++) {
                //ObjectOutputStream out; // stream write to the socket
                //ObjectInputStream in; // stream read from the socket
				System.out.println(" Output at " + i);
				OutputStreamlist.add(new ObjectOutputStream(socketServerlist.get(i).getOutputStream()));
				System.out.println(" Flushing at " + i);
				OutputStreamlist.get(i).flush();
				System.out.println(" Input at " + i);
				ObjectInputStream TempOStream = new ObjectInputStream(socketServerlist.get(i).getInputStream());
                InputStreamlist.add(TempOStream);
				System.out.println(" Starting at " + i);
				System.out.println(" Getting number " + i);
                }      
            */				
        } catch (ConnectException e) {
            System.err.println("Connection refused. You need to initiate a server first.");
            e.printStackTrace();
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
            unknownHost.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        } finally {
            // Close connections
            try {
                for(int i = 0; i < socketlist.size(); i++){
            
                    //OutputStreamlist.get(i).close();
                    //InputStreamlist.get(i).close();
                    //socketlist.get(i).close();
                }
            } 
            // catch (IOException ioException) {
            //     ioException.printStackTrace();
            // } 
            catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
    }
  
}
