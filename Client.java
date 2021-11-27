// This file uses a lot of logic from the sample file from Canvas

import java.net.*;
import java.math.*;
import java.io.*;
import java.math.BigInteger;
import java.util.Timer;
import java.util.TimerTask;
import java.nio.charset.StandardCharsets;

import java.nio.*;
import java.util.*;
// idean of file output streams came from https://www.techiedelight.com/how-to-write-to-a-binary-file-in-java/
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.io.FileOutputStream;

public class Client {
    Socket requestSocket; // socket connect to the server
    ObjectOutputStream out; // stream write to the socket
    ObjectInputStream in; // stream read from the socket
    // String message; // message send to the server
    byte[] fromServer; // capitalized message read from the server
    int peerID;
    int connectedToPeerId;
    // String bitfieldHandshake;
    // FileHandling handler;

    // int socket;
    peerProcess pp;

    void setPeerID(int t_peerID) {
        peerID = t_peerID;
    }

    public Client(peerProcess pp) {
        this.pp = pp;
    }

    private void runTimer() {
        // Every 5 seconds, recalculate the preferred neighbors
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                try {
                    pp.calculatePreferredNeighbors();
pp.messagesToSend.clear();
                    // choke unchosen peers, unchoke chosen peers
                    int count = 0;
                    for (RemotePeerInfo rpi : pp.peerInfoVector) {
                        if (!pp.preferredNeighbors.contains(rpi.getPeerId())) {
                            pp.messagesToSend.add(Messages.createChokeMessage());
count++;
                            if (connectedToPeerId == rpi.getPeerId()) {
                                rpi.setChoked(true);
                                sendMessageBB(pp.messagesToSend.get(count-1));
                            }
                           
                        } else {
                            pp.messagesToSend.add(Messages.createUnchokeMessage());
 count++;
                            if (connectedToPeerId == rpi.getPeerId()) {
                                rpi.setChoked(false);
                                sendMessageBB(pp.messagesToSend.get(count-1));
                            }
                            
                        }
                    }

                    /*
                    * for (int i = 0; i < pp.messagesToSend.size(); i++) {
                    * // send choke/unchoke messages
                    * sendMessageBB(pp.messagesToSend.get(i));
                    * }
                    */

                } catch (Exception e) {
                }
            }

        }, 0, pp.unchokingInterval* 1000);
    }

    void run() {

        try {
            // create a socket to connect to the server

            requestSocket = new Socket("localhost", 8000);
            System.out.println("Connected to localhost 8000");
            // initialize inputStream and outputStream
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(requestSocket.getInputStream());

            // create handshake message and send to server
            ByteBuffer messageToSend = Messages.createHandshakeMessage(pp.getPeerId());
            sendMessageBB(messageToSend);

            while (true) {
                // busy wait for input
                while (in.available() <= 0) {
                }
               

                fromServer = new byte[in.available()];
                in.read(fromServer);
                ByteBuffer buff = ByteBuffer.wrap(fromServer);
                System.out.println("Receive message"); // debug message

                // receive handshake message from server
                connectedToPeerId = Messages.decodeMessage(buff, pp, -1);
                pp.logger.onConnectingTo(connectedToPeerId);
                System.out.println("I am peer " + pp.getPeerId() + " and I am connected to " + connectedToPeerId);

                // send bitfield to server
                messageToSend = Messages.createBitfieldMessage(pp.getCurrBitfield());
                sendMessageBB(messageToSend);

                // expect a bitfield back
                while (in.available() <= 0) {
                }
                fromServer = new byte[in.available()];
                in.read(fromServer);
                buff = ByteBuffer.wrap(fromServer);
                int bitfieldMsg = Messages.decodeMessage(pp, buff, connectedToPeerId);

                // send interested message to server, this messagesToSend is created in
                // messsages.java
                for (int i = 0; i < pp.messagesToSend.size(); i++) {
                    sendMessageBB(pp.messagesToSend.get(i));
                }

                while (in.available() <= 0) {
                }
                fromServer = new byte[in.available()];
                in.read(fromServer);
                buff = ByteBuffer.wrap(fromServer);
                int interestMsg = Messages.decodeMessage(buff, pp, connectedToPeerId);

                System.out.println("Peers interested in 1002: none");

                // print out any peers interested in 1002
                for (int i = 0; i < pp.interested.size(); i++) {
                    System.out.println(pp.interested.get(i));
                }
                pp.messagesToSend.clear();
 		runTimer();
                // receive unchoke message from server
                while (in.available() <= 0) {
                }
                byte[] message = new byte[in.available()];
                in.read(message);
                buff = ByteBuffer.wrap(message);
                int chokeRes = Messages.decodeMessage(buff, pp, connectedToPeerId);
		for(int i =0; i < pp.pieceMessages.size(); i++)
			sendMessageBB(pp.pieceMessages.get(i));
		pp.pieceMessages.clear();
                while (in.available() <= 0) {
                }
                fromServer = new byte[in.available()];
                in.read(fromServer);
                buff = ByteBuffer.wrap(fromServer);
                int pieceMsg = Messages.decodeMessage(buff, pp, connectedToPeerId);
		
               while(in.available() >0)
			in.read();
                while (true) {
		while (in.available() <= 0) {
                }
pp.pieceMessages.clear();
                fromServer = new byte[in.available()];
                in.read(fromServer);
                buff = ByteBuffer.wrap(fromServer);
	
				if (Messages.GetMessageType(buff) > 7) {
					continue;
				}

                pieceMsg = Messages.decodeMessage(buff, pp, connectedToPeerId);
		for(int i =0; i < pp.pieceMessages.size(); i++)
			sendMessageBB(pp.pieceMessages.get(i));
		pp.pieceMessages.clear();
			
                }
            }

        } catch (ConnectException e) {
            System.err.println("Connection refused. You need to initiate a server first.");
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            // Close connections
            try {
                in.close();
                out.close();
                requestSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    // send a message to the output stream
    void sendMessage(String msg) {
        try {
            // stream write the message
            out.writeObject(msg);
            out.flush();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    // send a message to the output stream
    void sendMessageBB(ByteBuffer msg) {
        try {
            // stream write the message
            out.write(msg.array());
            out.flush();

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}