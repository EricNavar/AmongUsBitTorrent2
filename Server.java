// This file uses a lot of logic from the sample file from Canvas

import java.net.*;
import java.io.*;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.math.BigInteger;
import java.nio.*;
import java.util.*;
                            // idean of file output streams came from https://www.techiedelight.com/how-to-write-to-a-binary-file-in-java/
import java.io.IOException; 
import java.nio.channels.FileChannel;
import java.io.FileOutputStream;

public class Server {

	private static final int sPort = 8000; // The server will be listening on this port number
	static private Vector<Integer> haveFile;
	static private ArrayList<Handler> handlers = new ArrayList<Handler>();

	private static peerProcess pp;
	public Server(peerProcess pp_) {
		pp = pp_;
	}

	public void startServer() throws Exception {
		ServerSocket listener = new ServerSocket(sPort);
		System.out.println("The server is running.");
		int clientNum = 1;

		// make list of peerIds that have the file
		haveFile = new Vector<Integer>();
		for (RemotePeerInfo rpi : pp.peerInfoVector) {
			if (rpi.hasFile()) {
				haveFile.addElement(rpi.getPeerId());
			}
		}

		try {
			while (true) {
				Handler h = new Handler(listener.accept(), clientNum);
				h.start();
				handlers.add(h);
				System.out.println("Client " + clientNum + " is connected!");
				clientNum++;
			}
		} finally {
			listener.close();
		}
	}

	/**
	 * A handler thread class. Handlers are spawned from the listening loop and are
	 * responsible for dealing with a single client's requests.
	 */
	private static class Handler extends Thread {
		private byte[] message = new byte[50]; // message received from the client
		private String MESSAGE; // uppercase message send to the client
		private Socket connection;
		private ObjectInputStream in; // stream read from the socket
		private ObjectOutputStream out; // stream write to the socket
		private int no; // The index number of the client
		int connectedFrom;
		public Handler(Socket connection, int no) {
			this.connection = connection;
			this.no = no;
		}

		private void sampleClientLoop() throws ClassNotFoundException, IOException {
			while (true) {
				// receive the message sent from the client
				in.read(message);
				// show the message to the user
				System.out.println("Receive message: " + message + " from client " + no);
				// Capitalize all letters in the message
				// Question: Why are we resending the incoming message back to the sender?
				//MESSAGE = message.toUpperCase();
				// send MESSAGE back to the client
				//sendMessage(MESSAGE);
			}
		}

		private void serverLoop() throws ClassNotFoundException, IOException {

			// https://stackoverflow.com/ques1tions/2702980/java-loop-every-minute

			while (true) {
				while(in.available() <= 0)
				{}
				message = new byte[in.available()];

				in.read(message);

				ByteBuffer buff = ByteBuffer.wrap(message);

				connectedFrom = Messages.decodeMessage(buff, pp, -1);
				pp.logger.onConnectingFrom(connectedFrom);
				ByteBuffer messageToSend = Messages.createHandshakeMessage(pp.peerId);
				sendMessageBB(messageToSend);
				
				System.out.println("I am peer " +pp.getPeerId()+ " and I am connected to " + connectedFrom);

				// receive bitfield message
				
				while(in.available() <= 0)
				{}
				byte [] message2 = new byte[in.available()];
				

				in.read(message2);
				System.out.println(message2);

				ByteBuffer buff2 = ByteBuffer.wrap(message2);

				int bitfieldRes = Messages.decodeMessage(buff2, pp, connectedFrom);

				ByteBuffer bitfieldMessage = Messages.createBitfieldMessage(pp.bitfield);
				sendMessageBB(bitfieldMessage);


				
				/*// send interested/not interested
				for(int i =0; i < pp.messagesToSend.size(); i++)
				{
					sendMessageBB(pp.messagesToSend.get(i));
				}
				//receive interested/not interested
				String fromClient3 = (String) in.readObject();
				String fromClient4 = (String) in.readObject();
				String fromClient5 = (String) in.readObject();
				int newID2 = Integer.parseInt(fromClient4, 2);
				int newID3 = Integer.parseInt(fromClient5, 2);

				if(newID2 == pp.getPeerId())
				{
					int interestMessage = Messages.decodeMessage(fromClient3, pp, newID3);
				}
				System.out.println("Peers interested in 1001");
				for(int i =0; i<pp.interested.size(); i++)
				{
					System.out.println(pp.interested.get(i));
				}
				pp.messagesToSend.clear();
				// Every 5 seconds, recalculate the preferred neighbors
				Timer timer = new Timer();
				timer.schedule( new TimerTask() {
					public void run() {
						try {
							pp.calculatePreferredNeighbors();

							for (int i = 0; i < pp.messagesToSend.size(); i++) {
								//send choke/unchoke messages
								sendMessageBB(pp.messagesToSend.get(i));
							}

						}
						catch(Exception e)
						{}
					}

				}, 0, 5*1000);

				// after calculating and sending choke/unchoke, get ready to receive any messages to choke/unchoke
				while(true) {
					String fromClient7 = (String) in.readObject();
					String fromClient8 = (String) in.readObject();
					String fromClient9 = (String) in.readObject();
					int newID4 = Integer.parseInt(fromClient8, 2);
					int newID5 = Integer.parseInt(fromClient9, 2);
					if (newID4 == pp.getPeerId()) {
						System.out.println("unchoking " + newID5 + " from " + newID4);

						int chokeMessage = Messages.decodeMessage(fromClient7, pp, newID5);
						break;

					}

				};*/

						if(handlers.size() >= 2)
						{

							for(int i=0; i < handlers.size(); i++)
							{
								// start sending piece messages here
								// request piece from client
								// exclude server
								// coordinate piece distributuion between clients
								if(handlers.get(i).connectedFrom == connectedFrom)
									continue;
								messageToSend = Messages.createHandshakeMessage(connectedFrom);
								handlers.get(i).sendMessageBB(messageToSend);
								messageToSend = Messages.createHandshakeMessage(handlers.get(i).connectedFrom);
								sendMessageBB(messageToSend);




							}
							// choke and unchoke different processes
						}



				/*while(true)
				{}*/



			}
		}

		// try to handshake with processes that have the file
		// for (Integer i : haveFile) {
		// 	String messageToSend = createHandshakeMessage(peerId);
		// 	// new Handler(listener.accept(), peerId).sendMessage(messageToSend);
		// }

		public void run() {
			try {
				// initialize Input and Output streams
				out = new ObjectOutputStream(connection.getOutputStream());
				out.flush();
				in = new ObjectInputStream(connection.getInputStream());
				try {
					serverLoop();
				} catch (ClassNotFoundException classnot) {
					System.err.println("Data received in unknown format");
				}
			} catch (IOException ioException) {
				System.out.println("Disconnect with Client " + no);
			} finally {
				// Close connections
				try {
					in.close();
					out.close();
					connection.close();
				} catch (IOException ioException) {
					System.out.println("Disconnect with Client " + no);
				}
			}
		}

		// send a message to the output stream
		//public void sendMessage(String msg) {
		//	try {
		//		out.writeObject(msg);
		//		out.flush();
		//		System.out.println("Send message to Client " + no); // debug message
		//	} catch (IOException ioException) {
		//		ioException.printStackTrace();
		//	}
		//}
		// send a message to the output stream
		public void sendMessageBB(ByteBuffer msg) {
			try {
				out.write(msg.array());
				out.flush();
				System.out.println("Send message to Client " + no); // debug message
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}
}
