// This file uses a lot of logic from the sample file from Canvas

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.HashMap;

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

        // do not compile on the first few because lots of people are doing that, so it takes a very long time
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
        ipAddresses.put("localhost","localhost");
        ipAddresses.put("ubuntu","localhost");
    }

    private void closeConnections() {
        // Close connections
        try {
            for(int i = 0; i < socketlist.size(); i++){
                //OutputStreamlist.get(i).close();
                //InputStreamlist.get(i).close();
                socketlist.get(i).close();
            }
        } 
        catch (IOException ioException) {
            ioException.printStackTrace();
        } 
        catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    void run() {

        try {
            // create a socket to connect to the server
			//System.out.println(" pp.getPeerID() " + pp.getPeerId() + " pp.allPeers.get(i).getPeerId() " + pp.allPeers.get(0).getPeerId());
            if (Handler.DEBUG_MODE()) System.out.println(" peerID " + this.peerID + " fist one is " + pp.allPeers.get(0).getPeerId());
            int indexOfThisPeer = pp.GetPeerIndexNumber(pp.getPeerId()); // index of this peer in allPeers

            // open to peers with a lower ID
            for (int i = 0; i < indexOfThisPeer; i++) {
                    // if peer 1002 is trying to open up for peer 1001, then thisAddress = 10.242.94.35 and otherAddress = 10.242.94.34

					int  thisPort =  pp.allPeers.get(indexOfThisPeer).getPeerPort();
					String thisAddress =  ipAddresses.get(pp.allPeers.get(indexOfThisPeer).getPeerAddress());
                    InetAddress thisInetAddress = InetAddress.getByName(thisAddress);

					int otherPort = pp.allPeers.get(i).getPeerPort();
					String otherAddress = ipAddresses.get(pp.allPeers.get(i).getPeerAddress());
                    InetAddress otherInetAddress = InetAddress.getByName(otherAddress);
                    
				    if (Handler.DEBUG_MODE()) System.out.println(" I am " + pp.getPeerId() + " Index Number " + pp.GetIndexNumber() + " Attempting to connect to localhost " + pp.allPeers.get(i).getPeerId() + " which is on port " + thisPort + " and address " + otherAddress);
                    boolean connected = false;
                    boolean firstTimeFailed = true;
                    int counter = 0;
                    Socket NewSocket;

                    while(!connected) {
                    try{
                    NewSocket = new Socket(otherAddress, thisPort);
                    NewSocket.setKeepAlive(true);
                    ObjectOutputStream out = new ObjectOutputStream(NewSocket.getOutputStream());
                    out.flush();
                    InputStream inputStream = NewSocket.getInputStream();
                    ObjectInputStream in = new ObjectInputStream(inputStream);

                    socketlist.add(NewSocket);
                    Handler MyHandler = new Handler(NewSocket, pp.allPeers.get(i).getPeerId(), pp, in, out);
                    MyHandler.start();
                    System.out.println();
                    if (Handler.DEBUG_MODE()) System.out.println("Created regular socket:\n\tReceiving from: " + pp.allPeers.get(i).getPeerId() + "\n\tLocal Address: " + NewSocket.getLocalAddress() + "\n\tLocal port " + NewSocket.getLocalPort() + "\n\tRemote address: " + otherAddress + "\n\tRemote port " + NewSocket.getPort());
                    if (Handler.DEBUG_MODE()) pp.logger.log("iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii");
                    connected = true;
                    }
                    catch (ConnectException e) {
                        if (firstTimeFailed) {
                            System.out.print("Busy waiting for server to start");
                            firstTimeFailed = false;
                        }
                        else {
                            if (counter < 5) {
                                System.out.print(".");
                                counter++;
                            }
                            else {
                                System.out.print("\b\b\b\b\b");
                                counter = 0;
                            }
                        }
                    }
                    }


            }
			if (Handler.DEBUG_MODE()) System.out.println(" Done with Lower peer connections ");
            // talk to peers with a higher ID
            for (int i = indexOfThisPeer + 1; i < pp.allPeers.size(); i++) {
					int otherPort = pp.allPeers.get(i).getPeerPort();
					String thisAddress =  ipAddresses.get(pp.allPeers.get(indexOfThisPeer).getPeerAddress());

                    InetAddress thisInetAddress = InetAddress.getByName(thisAddress);
			        if (Handler.DEBUG_MODE()) System.out.println(" I am " + pp.getPeerId() + " Attempting to set up connection to " + pp.allPeers.get(i).getPeerId() + " which is on port " + otherPort);
                    // 100 is the backlog. Not sure what the ideal number is, but 100 probably can't hurt.
					ServerSocket NewSocket = new ServerSocket(otherPort, 100, thisInetAddress);
					if (Handler.DEBUG_MODE()) System.out.println("Trying to accept socket of allPeers(" + i + ") known as peerID " + pp.allPeers.get(i).getPeerId() + " on port " + otherPort + " of address " + thisAddress);
					Socket GetIt = NewSocket.accept();
                    if (Handler.DEBUG_MODE()) System.out.println("Server socket Accepted");
                    GetIt.setKeepAlive(true);
                    ObjectOutputStream out = new ObjectOutputStream(GetIt.getOutputStream());
                    out.flush();
                    InputStream inputStream = GetIt.getInputStream();
                    ObjectInputStream in = new ObjectInputStream(inputStream);
					Handler MyHandler = new Handler(GetIt, pp.allPeers.get(i).getPeerId(), pp, in, out);
                    socketServerlist.add(NewSocket);
					MyHandler.start();
                    if (Handler.DEBUG_MODE()) System.out.println("Created server socket:\n\tTalking to: " + pp.allPeers.get(i).getPeerId() + "\n\tLocal Address: " + GetIt.getLocalAddress() + "\n\tLocal port " + GetIt.getLocalPort() + "\n\tRemote port " + GetIt.getPort());
                    if (Handler.DEBUG_MODE()) pp.logger.log("iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii");
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
            closeConnections();
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
            unknownHost.printStackTrace();
            closeConnections();
        } catch (IOException ioException) {
            ioException.printStackTrace();
            closeConnections();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            closeConnections();
        }
    }
  
}
