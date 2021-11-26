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
	// FileHandling handler;

	// int socket;
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
				while(in.available() <= 0) {}
				fromServer = new byte[in.available()];
				in.read(fromServer);
				ByteBuffer buff = ByteBuffer.wrap(fromServer);

				// receive handshake message from server
				connectedToPeerId = Messages.decodeMessage(buff, pp, -1);

				pp.logger.onConnectingTo(connectedToPeerId);
				System.out.println("I am peer " + pp.getPeerId() + " (client) and I am connected to " + connectedToPeerId);

				//send bitfield to server
				messageToSend = Messages.createBitfieldMessage(pp.getCurrBitfield());
				System.out.println("Sending bitfield message to " + connectedToPeerId);
				sendMessageBB(messageToSend);
				//expect a bitfield back
				while(in.available() <= 0) {}
				fromServer = new byte[in.available()];

				in.read(fromServer);
				buff = ByteBuffer.wrap(fromServer);
				// expect bitfield message back
				System.out.println("Receieve bitfield message");
				Messages.decodeMessage(pp, buff, connectedToPeerId);

				
				// send interested message to server, this messagesToSend is created in messsages.java
				for(int i =0; i < pp.messagesToSend.size(); i++)
				{
					sendMessageBB(pp.messagesToSend.get(i));
				}

				// continue reading from server
				// TODO: make a better loop to handle a connection with the server
				while (true) {
					while(in.available() <= 0) {}
					fromServer = new byte[in.available()];
					in.read(fromServer);
				}

				// receive bitfield message from server
				/*String fromServer2 = (String) in.readObject();
				String fromServer3 = (String) in.readObject();


				while(in.available() <= 0) {}	
				
				fromServer = new byte[in.available()];
				in.read(fromServer);
				buff = ByteBuffer.wrap(fromServer);

				connectedToPeerId = Messages.decodeMessage(buff, pp, -1);

				System.out.println("Peers interested in 1002: none");

				// print out any peers interested in 1002
				for(int i =0; i<pp.interested.size(); i++)
				{
					System.out.println(pp.interested.get(i));
				}
				pp.messagesToSend.clear();

				/*
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
				*/
			}

		} catch (ConnectException e) {
			System.err.println("Connection refused. You need to initiate a server first.");
		} catch (UnknownHostException unknownHost) {
			System.err.println("You are trying to connect to an unknown host!");
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
		finally {
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
			// System.out.println("Send message to");

		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}
}