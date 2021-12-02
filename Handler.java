import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Handler extends Thread {
        private String message;    //message received from the client
		private String MESSAGE;    //uppercase message send to the client
		private Socket connection;
        private ObjectInputStream in;	//stream read from the socket
        private ObjectOutputStream out;    //stream write to the socket
		private int peerConnected;		//The index number of the client

        public Handler(Socket connection, int peerConnected) {
           		this.connection = connection;
	    		this.peerConnected = peerConnected;
        }

        public void run() {
 		try{
			System.out.println("Connected to client number " + peerConnected);
			//initialize Input and Output streams
			out = new ObjectOutputStream(connection.getOutputStream());
			out.flush();
			in = new ObjectInputStream(connection.getInputStream());
			//try{
			//	while(true)
			//	{
					//receive the message sent from the client
					//message = (String)in.readObject();
					//show the message to the user
					//System.out.println("Receive message: " + message + " from client " + peerConnected);
					//Capitalize all letters in the message
					//MESSAGE = message.toUpperCase();
					//send MESSAGE back to the client
					//sendMessage(MESSAGE);
				//}
			//}
			//catch(ClassNotFoundException classnot){
					//System.err.println("Data received in unknown format");
				//}
		}
		catch(IOException ioException){
			System.out.println("Disconnect with Client " + peerConnected);
		} // try / catch / finally
		finally{
			//Close connections
			try{
				in.close();
				out.close();
				connection.close();
			}
			catch(IOException ioException){
				System.out.println("Disconnect with Client " + peerConnected);
			}
		} // try / catch / finally
	} // run
} // Handler
