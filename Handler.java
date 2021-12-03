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
		int CurrentState;
   
   
		public void SleepTimer( int timeperiod ) {
			try {
				Thread.sleep(timeperiod);
			} catch (Exception ex) {
				System.out.println(ex);
			}
			finally {}
		}

        public static boolean DEBUG_MODE() {
			return true;
		}

        public synchronized void DebugLog( String MyMessage) {
			if (this.DEBUG_MODE()) pp.logger.log("DEBUG " + MyMessage);
		}
		
        public Handler(Socket connection, int peerConnected, peerProcess pp) {
           		this.connection = connection;
	    		this.peerConnected = peerConnected;
				this.pp = pp;
				this.CurrentState = 0;
                pp.logger.log("Connected to client number Handler Constructor Message");
        }
		
		public boolean WaitForInput(ObjectInputStream inStream, int TimeOut) {   // busy wait for input
			try{
				int x = 0;
                while (inStream.available() <= 0) {
					SleepTimer(1000);  // 1 second so we sleep for 1000 msec... which is 1 second
					x++;               // inc the counter
					if (x > TimeOut) { // exceeeded threshold
						return false;
					}
				}
			}
		    catch(IOException ioException){
		    	System.out.println("Disconnect with Client " + peerConnected);
		    } // try / catch / finally
			return true;
		}

        public void run() {
 		try{
			String MyMessage = "Running Handler connected to peer " + this.peerConnected;
            DebugLog(MyMessage);
			//initialize Input and Output streams
			out = new ObjectOutputStream(connection.getOutputStream());
			out.flush();
			ByteBuffer messageToSend;
			in = new ObjectInputStream(connection.getInputStream());
			//try{
				while(true)
				{
					switch (CurrentState) {
						case 0: // Send Handshake
							// create handshake message 
							int peerIDToSend;
							peerIDToSend = pp.getPeerId();
							messageToSend = Messages.createHandshakeMessage(pp.getPeerId());
							// send handshake message 
							sendMessage(messageToSend, out);
							CurrentState++;
							break;
						case 1: // Receive a Handshake
							// wait for incoming handshake message 
							if (WaitForInput(in, 100) == false) {  // see if this was successful
								CurrentState = 0; // try sending again... it timed out
							} else { // get incoming message 
								dataFromPeer = new byte[in.available()];
								in.read(dataFromPeer);
								ByteBuffer buff = ByteBuffer.wrap(dataFromPeer);
								// received a handshake message from peer
								this.connectedToPeerIdIncoming = Messages.decodeHandshakeMessage(pp, buff, -1);
								// TODO Test if this is a good HANDSHAKE Message and handle the issue
								//      Compare this.connectedToPeerIdIncoming to this.peerConnected
								//      If an error doesn't say what to do but maybe we go back to state 0 after 10 seconds?
								this.originalId = this.connectedToPeerIdIncoming;
								CurrentState++;
							}
							break;
						case 2: // Send a Bitfield
							messageToSend = Messages.createBitfieldMessage(pp.getCurrBitfield());  // create bitfield message 
							sendMessage(messageToSend, out);                                       // send handshake message 
							CurrentState++;
						case 3: // Receive a Bitfield Or Some Other message (noe gurantee what the message was/is)
							// wait for incoming Bitfield message 
							if (WaitForInput(in, 100) == false) {  // see if this was successful
								CurrentState = 0; // try sending again... it timed out
							} else { // get Generic incoming message 
								dataFromPeer = new byte[in.available()];
								in.read(dataFromPeer);
								ByteBuffer IncomingMessage = ByteBuffer.wrap(dataFromPeer);
								int messageDecode = Messages.decodeMessage(pp, IncomingMessage, peerConnected);
								int messageLength = Messages.GetMessageLength(IncomingMessage);
								//   Decoding in this order (hard coded for now) enum MessageType {CHOKE, UNCHOKE, INTERESTED, NOT_INTERESTED, HAVE, BITFIELD, REQUEST, PIECE }
								switch(messageDecode) {
									case 0: // MessageType.CHOKE.ordinal():
										//handleChokeMessage(pp, peerConnected);
										break;
									case 1: // MessageType.UNCHOKE.ordinal():
										//handleUnchokeMessage(pp, peerConnected);
										break;
									case 2: // MessageType.INTERESTED.ordinal():
										Messages.handleInterestedMessage(pp, peerConnected);
										break;
									case 3: // MessageType.NOT_INTERESTED.ordinal():
							Messages.handleNotInterestedMessage(pp, peerConnected);
										break;
									case 4: // MessageType.HAVE.ordinal():
											//handleHaveMessage(pp, peerConnected, IncomingMessage);
										break;
									case 5: // MessageType.BITFIELD.ordinal():
											boolean nowInterested = Messages.handleBitfieldMessage(IncomingMessage, pp, peerConnected, messageLength);
											if (nowInterested) {
												messageToSend = Messages.createInterestedMessage();
											} else {
												messageToSend = Messages.createNotInterestedMessage();
											}
											sendMessage(messageToSend, out); // send iinterst message 	
										break;
									case 6: // MessageType.REQUEST.ordinal():
											//handleRequestMessage(pp, peerConnected, IncomingMessage);
										break;
									case 7: // MessageType.PIECE.ordinal():
											//handlePieceMessage(pp, peerConnected, messageLength, IncomingMessage);
										break;
									default: 
										// TODO Handle Illegal Messages and send alerts, reset if necessary
										break;
								}
								this.originalId = this.connectedToPeerIdIncoming;
								CurrentState++;
							}
							break;

					} // switch for state machine
				} // while (true)
		} // try / catch
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
