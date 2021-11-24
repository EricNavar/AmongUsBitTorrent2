// This file uses a lot of logic from the sample file from Canvas


import java.net.*;
import java.io.*;
import java.math.BigInteger;
import java.util.Timer;
import java.util.TimerTask;

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
			String messageToSend = Messages.createHandshakeMessage(peerID);
			sendMessage(messageToSend);

			// expect a handshake message back
			Timer timer = new Timer();
			timer.schedule( new TimerTask() {
				public void run() {
					pp.calculatePreferredNeighbors();
				}
			}, 0, 5*1000);
			while (true) {
				fromServer = (String) in.readObject();
				System.out.println("Receive message"); // debug message

				connectedToPeerId = Messages.decodeMessage(fromServer, pp, 1001);

				pp.logger.onConnectingTo(connectedToPeerId);
				String fromServer2 = (String) in.readObject();
				String fromServer3 = (String) in.readObject();

				int newID = Integer.parseInt(fromServer3, 2);

				int bitfieldRes = Messages.decodeMessage(fromServer2, pp, newID);

				String bitfieldMessage = Messages.createBitfieldMessage(pp.bitfield);
				sendMessage(bitfieldMessage);
				sendMessage(Messages.integerToBinaryString(pp.getPeerId(), 2));
				// TODO: send interested/not interested messages
				String fromServer4 = (String) in.readObject();
				String fromServer5 = (String) in.readObject();
				String fromServer6 = (String) in.readObject();
				int newID2 = Integer.parseInt(fromServer5, 2);
				int newID3 = Integer.parseInt(fromServer6, 2);

				if(newID2 == pp.getPeerId())
				{

					int interestMessage = Messages.decodeMessage(fromServer4, pp, newID3);

				}
				for(int i =0; i < pp.messagesToSend.size(); i++)
				{
					sendMessage(pp.messagesToSend.get(i));
				}
				System.out.println("Peers interested in 1002");
				for(int i =0; i<pp.interested.size(); i++)
				{
					System.out.println(pp.interested.get(i));
				}
				
				








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
}
