import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.io.*;
import java.lang.Thread;

public class Handler extends Thread {
        private String message;    //message received from the client
		private String MESSAGE;    //uppercase message send to the client
		private Socket connection;
        private ObjectInputStream in;	//stream read from the socket
        private ObjectOutputStream out;    //stream write to the socket
		private int peerConnected;		//The index number of the client
		private peerProcess pp;
        private int connectedToPeerIdIncoming;
		private int originalId;
		byte[]  dataFromPeer; // data incoming from peer
    

        public synchronized void DebugLog( String MyMessage) {
			pp.logger.log("DEBUG " + MyMessage);
		}
		
        public Handler(Socket connection, int peerConnected, peerProcess pp) {
           		this.connection = connection;
	    		this.peerConnected = peerConnected;
				this.pp = pp;
                pp.logger.log("Connected to client number Handler Constructor Message");
        }
		
		public void WaitForInput(ObjectInputStream inStream) {                // busy wait for input
			try{
                while (inStream.available() <= 0) {}
			}
		    catch(IOException ioException){
		    	System.out.println("Disconnect with Client " + peerConnected);
		    } // try / catch / finally
		}

        public void run() {
 		try{
			String MyMessage = "Running Handler connected to peer " + this.peerConnected;
            DebugLog(MyMessage);
			//initialize Input and Output streams
			out = new ObjectOutputStream(connection.getOutputStream());
			out.flush();
			in = new ObjectInputStream(connection.getInputStream());
			//try{
				while(true)
				{
					//receive the message sent from the client
					//message = (String)in.readObject();
					//show the message to the user
					//System.out.println("Receive message: " + message + " from client " + peerConnected);
					//Capitalize all letters in the message
					//MESSAGE = message.toUpperCase();
					//send MESSAGE back to the client
					//sendMessage(MESSAGE);
					
                 // create handshake message 
				 MyMessage = "Peparing Handshake Message";
                 DebugLog(MyMessage);
                 ByteBuffer messageToSend;
				 int peerIDToSend;
				 peerIDToSend = pp.getPeerId();
				 messageToSend = Messages.createHandshakeMessage(pp.getPeerId());
                 // send handshake message 
                 sendMessage(messageToSend, out);
				 MyMessage = "Sending Handshake Message";
                 DebugLog(MyMessage);
                 // wait for incoming handshake message 
				 WaitForInput(in);
                 // get incoming message 
				 MyMessage = "Message Received";
                 DebugLog(MyMessage);
                 dataFromPeer = new byte[in.available()];
                 in.read(dataFromPeer);
                 ByteBuffer buff = ByteBuffer.wrap(dataFromPeer);
                 // System.out.println("Receive message"); // debug message

                 // receive handshake message from server
                 this.connectedToPeerIdIncoming = Messages.decodeMessage(buff, pp, -1);
                 //System.out.println("I am peer " + pp.getPeerId() + " and I am connected to " + connectedToPeerIdIncoming);
                 this.originalId = this.connectedToPeerIdIncoming;

				 try {
					 Thread.sleep(10000);
				 } catch (Exception ex) {
                        System.out.println(ex);
				 }
				 finally {}
				}
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
	
    // send a message to the output stream
    synchronized void sendMessage(ByteBuffer msg, ObjectOutputStream outputLocation) {
        try {
            // stream write the message
            outputLocation.write(msg.array());
            outputLocation.flush();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

} // Handler
