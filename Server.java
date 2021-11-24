// This file uses a lot of logic from the sample file from Canvas

import java.net.*;
import java.io.*;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.math.BigInteger;

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
		private String message; // message received from the client
		private String MESSAGE; // uppercase message send to the client
		private Socket connection;
		private ObjectInputStream in; // stream read from the socket
		private ObjectOutputStream out; // stream write to the socket
		private int no; // The index number of the client

		public Handler(Socket connection, int no) {
			this.connection = connection;
			this.no = no;
		}

		private void sampleClientLoop() throws ClassNotFoundException, IOException {
			while (true) {
				// receive the message sent from the client
				message = (String) in.readObject();
				// show the message to the user
				System.out.println("Receive message: " + message + " from client " + no);
				// Capitalize all letters in the message
				MESSAGE = message.toUpperCase();
				// send MESSAGE back to the client
				sendMessage(MESSAGE);
			}
		}

		private void serverLoop() throws ClassNotFoundException, IOException {
			// Every 5 seconds, recalculate the preferred neighbors
			Timer timer = new Timer(); 
			timer.schedule( new TimerTask() {
				public void run() {
					pp.calculatePreferredNeighbors();
				}
			}, 0, 5*1000);
			// https://stackoverflow.com/ques1tions/2702980/java-loop-every-minute

			while (true) {
				message = (String) in.readObject();
				int connectedFrom = Messages.decodeMessage(message, pp, 1002);
				pp.logger.onConnectingFrom(connectedFrom);
				String messageToSend = Messages.createHandshakeMessage(pp.peerId);
				sendMessage(messageToSend);
				String bitfieldMessage = Messages.createBitfieldMessage(pp.bitfield);
				sendMessage(bitfieldMessage);
				sendMessage(Messages.integerToBinaryString(pp.getPeerId(), 2));

				String fromClient = (String) in.readObject();
				String fromClient2 = (String) in.readObject();
				int newID = Integer.parseInt(fromClient2, 2);
				int bitfieldRes = Messages.decodeMessage(fromClient, pp, newID);




				if(handlers.size() >= 2)
				{

					for(int i=0; i < handlers.size(); i++)
					{
						// start sending piece messages here
						// request piece from client
						// exclude server
						// coordinate piece distributuion between clients
						handlers.get(i).sendMessage(messageToSend);


					}
					// choke and unchoke different processes
				}
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
		public void sendMessage(String msg) {
			try {
				out.writeObject(msg);
				out.flush();
				System.out.println("Send message to Client " + no); // debug message
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}
}
