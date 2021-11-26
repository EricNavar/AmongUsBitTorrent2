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
	String message; // message send to the server
	String fromServer; // capitalized message read from the server
	int peerID;
	int connectedToPeerId;
	String bitfieldHandshake;
	FileHandling handler;

	int socket;
	peerProcess pp;

	void setPeerID(int t_peerID) {
		peerID = t_peerID;
	}
	public Client(peerProcess pp) {
		this.pp = pp;
	}

	void run() {
		try {
			// create a socket to connect to the server
			requestSocket = new Socket("localhost", 8000);
			System.out.println("Connected to localhost in port 8000");
			// initialize inputStream and outputStream
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(requestSocket.getInputStream());

			// create handshake message and send send to server
			ByteBuffer messageToSend = Messages.createHandshakeMessage(peerID);
			sendMessageBB(messageToSend);


			while (true) {
				fromServer = (String) in.readObject();
				System.out.println("Receive message"); // debug message

				// receive handshake message from server
				connectedToPeerId = Messages.decodeMessage(fromServer, pp, 1001);

				pp.logger.onConnectingTo(connectedToPeerId);
				
				// receive bitfield message from server
				/*String fromServer2 = (String) in.readObject();
				String fromServer3 = (String) in.readObject();

				int newID = Integer.parseInt(fromServer3, 2);

				int bitfieldRes = Messages.decodeMessage(fromServer2, pp, newID);

				// send bitfield message and process id to server
				ByteBuffer bitfieldMessage = Messages.createBitfieldMessage(pp.bitfield);
				sendMessageBB(bitfieldMessage);
				sendMessage(Messages.integerToBinaryString(pp.getPeerId(), 2));
				// receive not interested message from server
				String fromServer4 = (String) in.readObject();
				String fromServer5 = (String) in.readObject();
				String fromServer6 = (String) in.readObject();
				int newID2 = Integer.parseInt(fromServer5, 2);
				int newID3 = Integer.parseInt(fromServer6, 2);

				if(newID2 == pp.getPeerId())
				{
					int interestMessage = Messages.decodeMessage(fromServer4, pp, newID3);
				}
				
				// send interested message to server, this messagesToSend is created in messsages.java
				for(int i =0; i < pp.messagesToSend.size(); i++)
				{
					sendMessageBB(pp.messagesToSend.get(i));
				}
				System.out.println("Peers interested in 1002: none");

				
				// print out any peers interested in 1002
				for(int i =0; i<pp.interested.size(); i++)
				{
					System.out.println(pp.interested.get(i));
				}
				pp.messagesToSend.clear();
				// receive unchoke message from server
				while(true) {
					String fromServer7 = (String) in.readObject();
					String fromServer8 = (String) in.readObject();
					String fromServer9 = (String) in.readObject();
					int newID4 = Integer.parseInt(fromServer8, 2);
					int newID5 = Integer.parseInt(fromServer9, 2);
					if (newID4 == pp.getPeerId()) {
						System.out.println("unchoking " + newID5 + " from " + newID4);

						int chokeMessage = Messages.decodeMessage(fromServer7, pp, newID5);
						break;

					}

				};

				// Every 5 seconds, recalculate the preferred neighbors
				Timer timer = new Timer();
				timer.schedule( new TimerTask() {
					public void run() {
						try {
							pp.calculatePreferredNeighbors();

							for (int i = 0; i < pp.messagesToSend.size(); i++) {
								// send choke/unchoke messages
								sendMessageBB(pp.messagesToSend.get(i));
							}

						}
						catch(Exception e)
						{}
					}

				}, 0, 5*1000);
				while (true)
				{}*/


			}
			
		} catch (ConnectException e) {
			System.err.println("Connection refused. You need to initiate a server first.");
		} catch (ClassNotFoundException e) {
			System.err.println("Class not found");
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
