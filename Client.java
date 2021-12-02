// This file uses a lot of logic from the sample file from Canvas

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;

import java.nio.*;
import java.io.IOException;
// idea of file output streams came from https://www.techiedelight.com/how-to-write-to-a-binary-file-in-java/

public class Client {

    Vector<ServerSocket> socketlist;
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
        socketlist       = new Vector<ServerSocket>();
        InputStreamlist  = new Vector<ObjectInputStream>();
        OutputStreamlist = new Vector<ObjectOutputStream>();
    }

    void run() {

        try {
            // create a socket to connect to the server
            System.out.println(" peerID " + this.peerID + " fist one is " + pp.allPeers.get(0).getPeerId());
            //for (int i = 0; ((pp.allPeers.get(i).getPeerId()) <= this.peerID); i++) {
            for (int i = 0; (i < pp.allPeers.size()); i++) {
			    if (pp.getPeerId() == pp.allPeers.get(i).getPeerId() ) {
					//break;
				} else {
                    Socket nextSock;
			        System.out.println(" I am " + pp.getPeerId() + " Attempting to connect to localhost " + pp.allPeers.get(i).getPeerId() + " which is on port " + pp.allPeers.get(i).getPeerPort());
                    socketlist.add(new ServerSocket(pp.allPeers.get(i).getPeerPort()));
			        System.out.println("Created a server socket " + pp.allPeers.get(i).getPeerId() + " and peer port " + pp.allPeers.get(i).getPeerPort());
                    pp.logger.log("iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii");
				}
            }
		int clientNum = 1;
	
            for (int i = 0; (i < pp.allPeers.size()); i++) {
			    if (pp.getPeerId() == pp.allPeers.get(i).getPeerId() ) {
					//break;
				} else {
					Socket GetIt = new Handler(socketlist.get(i).accept(), pp.allPeers.get(i).getPeerId());
					System.out.println("Trying to accept socket " + i + " as " + GetIt);
				}
            }
			
			System.out.println("A4 " + pp.getPortNumber());
			/*
            for (int i = 0; i < pp.allPeers.size(); i++) {
                //ObjectOutputStream out; // stream write to the socket
                //ObjectInputStream in; // stream read from the socket
				System.out.println(" Output at " + i);
				OutputStreamlist.add(new ObjectOutputStream(socketlist.get(i).getOutputStream()));
				System.out.println(" Flushing at " + i);
				OutputStreamlist.get(i).flush();
				System.out.println(" Input at " + i);
				ObjectInputStream TempOStream = new ObjectInputStream(socketlist.get(i).getInputStream());
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

    // send a message to the output stream
    void sendMessage(ByteBuffer msg, int streamID) {
        try {
            // stream write the message
			System.out.println(" Getting number 0 sendMessageBB " + msg.array());
            OutputStreamlist.get(streamID).write(msg.array());
            OutputStreamlist.get(streamID).flush();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
  
}
