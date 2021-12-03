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
		boolean chokingTimerFlag;
		boolean optimisticTimerFlag;
   
   
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
				this.chokingTimerFlag = true;
				this.optimisticTimerFlag = true;
                pp.logger.log("Connected to client number Handler Constructor Message");
        }
		
		public boolean WaitForInput(ObjectInputStream inStream, int TimeOut) {   // busy wait for input
			try{
				int x = 0;
                while (inStream.available() <= 0) {
					//SleepTimer(1);  // 1 second so we sleep for 1000 msec... which is 1 second
					//x++;               // inc the counter
					//if (x > TimeOut) { // exceeeded threshold
					//	return false;
					//}
					checkTimers();
				}
			}
		    catch(IOException ioException){
		    	System.out.println("Disconnect with Client " + peerConnected);
		    } // try / catch / finally
			return true;
		}

        public synchronized void sendChokeUnchokedMyselfOnly() {
			for(int i = 0; i < pp.UnChokingNeighbors.size(); ++i) {
				if (pp.UnChokingNeighbors.get(i) == pp.getPeerId()) {
					pp.UnChokingNeighbors.remove(i);
                  pp.messagesToSend.add(Messages.createUnchokeMessage());  
				}
			}
			for(int i = 0; i < pp.ChokingNeighbors.size(); ++i) {
				if (pp.ChokingNeighbors.get(i) == pp.getPeerId()) {
					pp.ChokingNeighbors.remove(i);
                  pp.messagesToSend.add(Messages.createChokeMessage());  
				}
			}
			if (this.DEBUG_MODE()) System.out.println(" Peer ID " + pp.getPeerId() + " connected to " + peerConnected + " Choking " + pp.ChokingNeighbors.size() + " and unchoking " + pp.UnChokingNeighbors.size());
			ByteBuffer aNewMessageToSend;  
			while (pp.messagesToSend.size() > 0) {
				aNewMessageToSend = pp.messagesToSend.get(0);
				sendMessage(aNewMessageToSend, out);
    			String MyMessage = " Sending Choke/Unchoke " + pp.messagesToSend.size() + " in pp.messagesToSend with " + aNewMessageToSend.array();
	    		if (this.DEBUG_MODE()) pp.logger.log("DEBUG " + MyMessage);
				pp.messagesToSend.remove(0);
			}
		}

		public void checkTimers() {
			//System.out.println("Check timers: " + this.chokingTimerFlag + " " + pp.chokingTimerFlag + " " + this.optimisticTimerFlag + " " + pp.optimisticTimerFlag);
			if (this.chokingTimerFlag == pp.chokingTimerFlag) {
			    //System.out.println("Check timers: " + this.chokingTimerFlag + " " + pp.chokingTimerFlag + " " + this.optimisticTimerFlag + " " + pp.optimisticTimerFlag);
				sendChokeUnchokedMyselfOnly();
				this.chokingTimerFlag = !this.chokingTimerFlag;
			}
			if (this.optimisticTimerFlag == pp.optimisticTimerFlag) {
			    //System.out.println("Check timers: " + this.chokingTimerFlag + " " + pp.chokingTimerFlag + " " + this.optimisticTimerFlag + " " + pp.optimisticTimerFlag);
				sendChokeUnchokedMyselfOnly();
				this.optimisticTimerFlag = !this.optimisticTimerFlag;
			}
		}

        public void run() {
 		try{
			String MyMessage = "Running Handler connected to peer " + this.peerConnected;
            DebugLog(MyMessage);
			//initialize Input and Output streams
			out = new ObjectOutputStream(connection.getOutputStream());
			out.flush();
			ByteBuffer newMessageToSend;
			in = new ObjectInputStream(connection.getInputStream());
			//try{
				while(true)
				{
					checkTimers();
					switch (CurrentState) {
						case 0: // Send Handshake
							// create handshake message 
							int peerIDToSend;
							peerIDToSend = pp.getPeerId();
							newMessageToSend = Messages.createHandshakeMessage(pp.getPeerId());
							// send handshake message 
							sendMessage(newMessageToSend, out);
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
							newMessageToSend = Messages.createBitfieldMessage(pp.getCurrBitfield());  // create bitfield message 
							sendMessage(newMessageToSend, out);                                       // send handshake message 
							CurrentState++;
						case 3: // Receive a Bitfield Or Some Other message (noe gurantee what the message was/is)
							// wait for incoming Bitfield message 
							if (WaitForInput(in, 100) == false) {  // see if this was successful
								CurrentState = 3; // try waiting again
							} else { // get Generic incoming message 
								dataFromPeer = new byte[in.available()];
								in.read(dataFromPeer);
								ByteBuffer IncomingMessage = ByteBuffer.wrap(dataFromPeer);
								int messageDecode = Messages.decodeMessage(pp, IncomingMessage, peerConnected);
								int messageLength = Messages.GetMessageLength(IncomingMessage);
								//   Decoding in this order (hard coded for now) enum MessageType {CHOKE, UNCHOKE, INTERESTED, NOT_INTERESTED, HAVE, BITFIELD, REQUEST, PIECE }
								switch(messageDecode) {
									case 0: // MessageType.CHOKE.ordinal():
											Messages.handleChokeMessage(pp, peerConnected);
											break;
									case 1: // MessageType.UNCHOKE.ordinal():
											Messages.handleUnchokeMessage(pp, peerConnected);
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
												newMessageToSend = Messages.createInterestedMessage();
											} else {
												newMessageToSend = Messages.createNotInterestedMessage();
											}
											sendMessage(newMessageToSend, out); // send iinterst message 	
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
