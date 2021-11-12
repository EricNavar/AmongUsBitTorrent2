// This file uses a lot of logic from the sample file from Canvas


import java.net.*;
import java.io.*;

public class Client {
	Socket requestSocket; // socket connect to the server
	ObjectOutputStream out; // stream write to the socket
	ObjectInputStream in; // stream read from the socket
	String message; // message send to the server
	String fromServer; // capitalized message read from the server
	int peerID;
	int connectedToPeerId;
	String bitfieldHandshake;

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
			while (true) {
				fromServer = (String) in.readObject();
				System.out.println("Receive message"); // debug message

				connectedToPeerId = Messages.decodeMessage(fromServer, pp);

				pp.logger.onConnectingTo(connectedToPeerId);
				String fromServer2 = (String) in.readObject();


				boolean missingPiece = false;
				for(int i = 0; i < fromServer2.length(); i++)
				{// TODO: check for missing pieces
					if(fromServer2.charAt(i) == '1' && pp.bitfield.get(i) == false)
					{
						missingPiece = true;
					}
				}

				String bitfieldMessage = Messages.createBitfieldMessage(pp.bitfield);
				sendMessage(bitfieldMessage);
				if(missingPiece)
				{
					String interestedMessage = Messages.createInterestedMessage();
					// TODO: does this send to the right process?
					sendMessage(interestedMessage);

				}
				else
				{
					String notInterestedMessage = Messages.createNotInterestedMessage();
					// TODO: does this send to the right process?
					sendMessage(notInterestedMessage);

				}
				/*here we have choke/unchoke message sending, to be finished at a later date

				  if(someCondition)
					pp.calculatePreferredNeighbors();
				  for(int i = 0; i < pp.peerInfoVector.size(); i++) {
					  if (pp.peerInfoVector.get(i).isChoked())
					  {
						  String chokeMessage = message.createChokeMessage();
						  sendMessage(chokeMessage);

					  }
					  else
					  {

						  String unchokeMessage = message.createUnchokeMessage();
						  sendMessage(unchokeMessage);
					  }


				  } */




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
