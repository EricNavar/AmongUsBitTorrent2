import java.net.*;
import java.io.*;
import java.util.Vector;
import java.util.ArrayList;


public class Server {

	private static final int sPort = 8000; // The server will be listening on this port number
	static Vector<Integer> haveFile;
	static ArrayList<Handler> handlers = new ArrayList<Handler>();

	private static StartRemotePeers srp;
	private int peerID;
	
	void setPeerID(int t_peerID) {
		peerID = t_peerID;
	}
	public static StartRemotePeers getSRP() {
		return srp;
	}

	public static void startServer(StartRemotePeers srp) throws Exception {
		Server.srp = srp;
		ServerSocket listener = new ServerSocket(sPort);
		System.out.println("The server is running.");
		int clientNum = 1;

		// make list of peerIds that have the file
		haveFile = new Vector<Integer>();
		for (RemotePeerInfo rpi : srp.peerInfoVector) {
			if (rpi.hasFile) {
				haveFile.addElement(rpi.peerId);
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

		// try to handshake with processes that have the file
		// for (Integer i : haveFile) {
		// 	String messageToSend = createHandshakeMessage(peerId);
		// 	// new Handler(listener.accept(), peerId).sendMessage(messageToSend);
		// }

		private void serverLoop() throws ClassNotFoundException, IOException {
			while (true) {
				message = (String) in.readObject(); //recieve handshake
				String messageToSend = Messages.createHandshakeMessage(getSRP().peerId);
				sendMessage(messageToSend);
				if(handlers.size() >= 2)
				{
					for(int i=0; i < handlers.size(); i++)
					{
						handlers.get(i).sendMessage(messageToSend);
					}
				}
			}
		}

		public void run() {
			try {
				// initialize Input and Output streams
				out = new ObjectOutputStream(connection.getOutputStream());
				out.flush();
				in = new ObjectInputStream(connection.getInputStream());
				try {
					// this is the loop that is run by default. It's good for testing.
					// sampleClientLoop();

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
				System.out.println("Send message: \"" + Messages.decodeBinaryString(msg) + "\" to Client " + no); // debug message
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}

	}

}
