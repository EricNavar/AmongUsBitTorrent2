// This file uses a lot of logic from the sample file from Canvas

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;
import java.math.BigInteger; 
import java.security.MessageDigest; 
import java.security.NoSuchAlgorithmException; 
 
import java.nio.*;
import java.io.IOException;
// idea of file output streams came from https://www.techiedelight.com/how-to-write-to-a-binary-file-in-java/
 
public class Client {

    Vector<Socket> socketlist;
	Vector<ServerSocket> socketServerlist;
    Vector<ObjectInputStream> InputStreamlist;
    Vector<ObjectOutputStream> OutputStreamlist;

    int peerID;

    // String bitfieldHandshake;
    // FileHandling handler;

    // int socket;
    peerProcess pp;

    void setPeerID(int t_peerID) {
        peerID = t_peerID;
    }

    public Client(peerProcess pp) {
        this.pp          = pp;
        socketlist       = new Vector<Socket>();
        socketServerlist = new Vector<ServerSocket>();
        InputStreamlist  = new Vector<ObjectInputStream>();
        OutputStreamlist = new Vector<ObjectOutputStream>();
    }

    void run() {

        try {
            // create a socket to connect to the server
			System.out.println(" pp.getPeerID() " + pp.getPeerId() + " pp.allPeers.get(i).getPeerId() " + pp.allPeers.get(0).getPeerId());
            System.out.println(" peerID " + this.peerID + " fist one is " + pp.allPeers.get(0).getPeerId());
 //           for (int i = 0; pp.getPeerId() != pp.allPeers.get(i).getPeerId(); i++) {
            for (int i = 0; i<1; i++) {
			    if (Handler.DEBUG_MODE()) System.out.println(" i = " + i);
			    if (pp.getPeerId() == pp.allPeers.get(i).getPeerId() ) {
					//break;
				} else {
                    Socket nextSock;
					int  PeerPortToUse =  pp.allPeers.get(pp.GetPeerIndexNumber(pp.getPeerId())).getPeerPort();
				    if (Handler.DEBUG_MODE()) System.out.println(" I am " + pp.getPeerId() + " Index Number " + pp.GetIndexNumber() + " Attempting to connect to localhost " + pp.allPeers.get(i).getPeerId() + " which is on port " + PeerPortToUse);
					Socket NewSocket;
					NewSocket = new Socket("localhost", PeerPortToUse);
					Handler MyHandler = new Handler(NewSocket, pp.allPeers.get(i).getPeerId(), pp);
					MyHandler.start();
                    socketlist.add(NewSocket);
			        if (Handler.DEBUG_MODE()) System.out.println("Created a initiator socket with peer " + pp.allPeers.get(i).getPeerId() + " on their port " + PeerPortToUse);
                    pp.logger.log("iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii");
				}
            }
			// @ERIC_N  These next three lines truncate off the process from making the remaining connections.  Just won't happen... safe for Ubuntu not for CISE
			if (pp.getPeerId() >= 1002) {
				while(true);
			}
			if (Handler.DEBUG_MODE()) System.out.println(" Done with Lower peer connections ");
			if (Handler.DEBUG_MODE()) System.out.println(" Done with Higher peer connections ");
			boolean start = false;
            for (int i =  0 ; i < pp.allPeers.size(); i++) {
			    if (pp.getPeerId() == pp.allPeers.get(i).getPeerId() ) {
					start = true;
				} else {
					if (start) {
                    Socket nextSock;
					int  PeerPortToUse = pp.allPeers.get(i).getPeerPort();
			        if (Handler.DEBUG_MODE()) System.out.println(" I am " + pp.getPeerId() + " Attempting to set up connection to " + pp.allPeers.get(i).getPeerId() + " which is on port " + PeerPortToUse);
					ServerSocket NewSocket;
					NewSocket = new ServerSocket(PeerPortToUse);
					if (Handler.DEBUG_MODE()) System.out.println("Trying to accept socket of allpeeres(" + i + ") known as peerID " + pp.allPeers.get(i).getPeerId());
					Socket GetIt = NewSocket.accept();
					Handler MyHandler = new Handler(GetIt, pp.allPeers.get(i).getPeerId(), pp);
					MyHandler.start();
                    socketServerlist.add(NewSocket);
			        if (Handler.DEBUG_MODE()) System.out.println("Created a server socket " + pp.allPeers.get(i).getPeerId() + " and peer port " + PeerPortToUse);
                    pp.logger.log("iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii");
					}
				}
            }
		    
//			int clientNum = 1;
//	
//            for (int i = 0; (i < pp.allPeers.size()); i++) {
//			    if (pp.getPeerId() == pp.allPeers.get(i).getPeerId() ) {
//					//break;
//				} else {
//					System.out.println("Trying to accept socket of allpeeres(" + i + ") known as peerID " + pp.allPeers.get(i).getPeerId());
//					Socket GetIt = socketServerlist.get(i).accept();
//					System.out.println("Trying to accept socket of allpeeres(" + i + ") known as peerID " + pp.allPeers.get(i).getPeerId());
//					Handler MyHandler = new Handler(GetIt, pp.allPeers.get(i).getPeerId());
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
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
            unknownHost.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            // Close connections
            try {
                for(int i = 0; i < pp.allPeers.size()-1; i++){
            
                    //OutputStreamlist.get(i).close();
                    //InputStreamlist.get(i).close();
                    socketlist.get(i).close();
        
                    }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
  
}
