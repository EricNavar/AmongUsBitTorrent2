import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.io.*;
import java.lang.Thread;


public class Handler extends Thread {
		volatile private Socket connection;
        volatile private ObjectInputStream in;	//stream read from the socket
        volatile private ObjectOutputStream out;    //stream write to the socket
		volatile private int peerConnected;		//The index number of the client
		volatile public  peerProcess pp;
        volatile private int connectedToPeerIdIncoming;
		volatile private int originalId;
		volatile byte[]  dataFromPeer; // data incoming from peer
		volatile int CurrentState;
		volatile boolean chokingTimerFlag;
		volatile boolean optimisticTimerFlag;
   
   
		public void SleepTimer( int timeperiod ) {
			try {
				Thread.sleep(timeperiod);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			finally {}
		}

        public static boolean DEBUG_MODEL1() {
			return false;
		}


        public static boolean DEBUG_MODEL2() {
			return false;
		}


        public static boolean DEBUG_MODEL3() {
			return false;
		}

        public static boolean DEBUG_MODE() {
			return true;
		}

        public synchronized void DebugLog( String MyMessage) {
			if (this.DEBUG_MODE()) pp.logger.log("DEBUG " + MyMessage);
		}
		
        public Handler(Socket connection, int peerConnected, peerProcess pp, ObjectInputStream in, ObjectOutputStream out) {
           		this.connection = connection;
	    		this.peerConnected = peerConnected;
				this.pp = pp;
				this.CurrentState = 0;
				this.chokingTimerFlag = true;
				this.optimisticTimerFlag = true;
				this.in = in;
				this.out= out;
                pp.logger.log("Connected to client number Handler Constructor Message");
				
        }
		
		public boolean WaitForInput(ObjectInputStream inStream) {   // busy wait for input
			try{
				int x = 0;
                while (inStream.available() <= 0) {
					checkTimers();
				}
			}
		    catch(IOException ioException){
		    	ioException.printStackTrace();
				System.out.println("Disconnect with Client " + peerConnected);
		    } // try / catch / finally
			return true;
		}


		public ByteBuffer GetDataFromInput(ObjectInputStream inStream) {   // synchornized input for read
			ByteBuffer buff = ByteBuffer.wrap("".getBytes());  // return nothing if we have nothing
			ByteBuffer EntireMesageRx = ByteBuffer.allocate(65536);
			try{
				dataFromPeer = new byte[in.available()];
				in.read(dataFromPeer);
				EntireMesageRx.put(ByteBuffer.wrap(dataFromPeer));
				int messageLengthExpected = Messages.GetMessageLength(EntireMesageRx) + 4; // account for length field needed
				int messageLengthReceived = dataFromPeer.length;
				int messageCurrentCount   = messageLengthReceived; // 9 bytes are length + messageid + piece number
				//if (this.DEBUG_MODEL2()) System.out.println(" Message Length Expected = " + messageLengthExpected + " and received " + messageLengthReceived + " current count = " + messageCurrentCount);
				while ((messageCurrentCount < messageLengthExpected) && (messageLengthReceived > 32)) {
					dataFromPeer = new byte[in.available()];
					in.read(dataFromPeer);
					EntireMesageRx.put(ByteBuffer.wrap(dataFromPeer));
					messageCurrentCount = messageCurrentCount + dataFromPeer.length;
					//if (this.DEBUG_MODEL2()) System.out.println(" Message Length Expected = " + messageLengthExpected + " and received " + messageLengthReceived + " current count = " + messageCurrentCount);
				}
    			//if (this.DEBUG_MODE()) System.out.println(" Message Length Expected = " + messageLengthExpected + " and received " + messageLengthReceived + " current count = " + messageCurrentCount);
				EntireMesageRx.flip();
                buff = EntireMesageRx;
			}
		    catch(IOException ioException){
		    	System.out.println("Disconnect with Client " + peerConnected);
		    } // try / catch / finally
			return buff;
		}

		public boolean BusyWaitForInput(ObjectInputStream inStream) {   // busy wait for input, not synchronized
			try{
				int x = 0;
                while (inStream.available() <= 0) {
					checkTimers();
				}
			}
		    catch(IOException ioException){
		    	System.out.println("Disconnect with Client " + peerConnected);
		    } // try / catch / finally
			return true;
		}
		
		public ByteBuffer WaitForInputAndGetMessage(ObjectInputStream inStream) {   // busy wait for input, then read
			BusyWaitForInput(inStream);
			return GetDataFromInput(inStream);
		}

        public synchronized void sendChokeUnchokedMyselfOnly() {
			int keepi = -1;
			ByteBuffer aNewMessageToSend;
			for(int i = 0; i < pp.UnChokingNeighbors.size(); ++i) {
  			    if (this.DEBUG_MODE()) System.out.println(" Send Unchoke Myself 1 " + pp.UnChokingNeighbors.get(i) + " " + connectedToPeerIdIncoming );
				if (pp.UnChokingNeighbors.get(i) == connectedToPeerIdIncoming) {
				  keepi = i;
				  aNewMessageToSend = Messages.createUnchokeMessage();
				  //if (this.DEBUG_MODE()) System.out.println(" Send Unchoke Myself 1.1 " );
                  sendMessage(aNewMessageToSend, out);  
    			  //if (this.DEBUG_MODE()) System.out.println(" Peer ID " + pp.getPeerId() + " connected to " + peerConnected + " UnChoking message sent " + aNewMessageToSend + " keepi = " + keepi + " UnChokingNeighbors " + pp.UnChokingNeighbors);
				}
				if (keepi != -1) {
					pp.UnChokingNeighbors.remove(keepi);
				}
			}
			for(int i = 0; i < pp.ChokingNeighbors.size(); ++i) {
				if (pp.ChokingNeighbors.get(i) == connectedToPeerIdIncoming) {
					keepi = i;
					aNewMessageToSend = Messages.createChokeMessage();
					//if (this.DEBUG_MODE()) System.out.println(" Send Unchoke Myself 1.2 " );
					sendMessage(aNewMessageToSend, out);  
					//if (this.DEBUG_MODE()) System.out.println(" Peer ID " + pp.getPeerId() + " connected to " + peerConnected + " Choking message sent " + aNewMessageToSend);
				}
				if (keepi != -1) {
					pp.ChokingNeighbors.remove(i);
				}
			} 
		}

		public synchronized void checkTimers() {
			boolean doUnchoke = false;
			//System.out.println("Check timers: " + this.chokingTimerFlag + " " + pp.chokingTimerFlag + " " + this.optimisticTimerFlag + " " + pp.optimisticTimerFlag);
			if (this.chokingTimerFlag == pp.chokingTimerFlag) {
			    //System.out.println("Check timers: " + this.chokingTimerFlag + " " + pp.chokingTimerFlag + " " + this.optimisticTimerFlag + " " + pp.optimisticTimerFlag);
				doUnchoke = true;
				//if (this.DEBUG_MODE()) System.out.println(" doUnchoke " );
				this.chokingTimerFlag = !this.chokingTimerFlag;
			}
			if (this.optimisticTimerFlag == pp.optimisticTimerFlag) {
			    //System.out.println("Check timers: " + this.chokingTimerFlag + " " + pp.chokingTimerFlag + " " + this.optimisticTimerFlag + " " + pp.optimisticTimerFlag);
				doUnchoke = true;
				//if (this.DEBUG_MODE()) System.out.println(" doUnchoke " );
				this.optimisticTimerFlag = !this.optimisticTimerFlag;
			}
			if (doUnchoke) {
				sendChokeUnchokedMyselfOnly();
			}
		}

		private void closeConnections() {
			try{
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
				if (connection != null) {
					connection.close();
				}
			}
			catch(IOException ioException){
				System.out.println("Disconnect with Client " + peerConnected);
				ioException.printStackTrace();
			}
		}

        public void run() {
 		try{
			String MyMessage = "Running Handler connected to peer " + this.peerConnected;
            DebugLog(MyMessage);
			//initialize Input and Output streams
			ByteBuffer newMessageToSend;
			ByteBuffer IncomingMessage = ByteBuffer.allocate(65536); 
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
							//WaitForInput(in);
							//dataFromPeer = new byte[in.available()];
							//in.read(dataFromPeer);
							//ByteBuffer buff = ByteBuffer.wrap(dataFromPeer);
							//IncomingMessage2 = WaitForInputAndGetMessage(in);
							// received a handshake message from peer
							IncomingMessage = WaitForInputAndGetMessage(in);
							this.connectedToPeerIdIncoming = Messages.decodeHandshakeMessage(pp, IncomingMessage, -1);
							// TODO Test if this is a good HANDSHAKE Message and handle the issue
							//      Compare this.connectedToPeerIdIncoming to this.peerConnected
							//      If an error doesn't say what to do but maybe we go back to state 0 after 10 seconds?
							this.originalId = this.connectedToPeerIdIncoming;
							CurrentState++;
							break;
						case 2: // Send a Bitfield
							newMessageToSend = Messages.createBitfieldMessage(pp.getCurrBitfield());  // create bitfield message 
							sendMessage(newMessageToSend, out);                                       // send handshake message 
							CurrentState++;
						case 3: // Receive a Bitfield Or Some Other message (noe gurantee what the message was/is)
							// wait for incoming Bitfield message 
							//WaitForInput(in);
							//dataFromPeer = new byte[in.available()];
							//in.read(dataFromPeer);
							//ByteBuffer IncomingMessage = ByteBuffer.wrap(dataFromPeer);
							IncomingMessage = WaitForInputAndGetMessage(in);
							int messageDecode = Messages.decodeMessage(pp, IncomingMessage, peerConnected);
							int messageLength = Messages.GetMessageLength(IncomingMessage);
							//   Decoding in this order (hard coded for now) enum MessageType {CHOKE, UNCHOKE, INTERESTED, NOT_INTERESTED, HAVE, BITFIELD, REQUEST, PIECE }
							if (this.DEBUG_MODE()) System.out.println(" Message Received ID " + messageDecode + " Length " + messageLength + " Peer ID " + pp.getPeerId() + " From " + peerConnected);
							switch(messageDecode) {
								case 0: // MessageType.CHOKE.ordinal():
										Messages.handleChokeMessage(pp, peerConnected);
										break;
								case 1: // MessageType.UNCHOKE.ordinal():
										Messages.handleUnchokeMessage(pp, peerConnected);
										newMessageToSend = Messages.createRequestMessage(pp.randomMissingPiece());
										sendMessage(newMessageToSend, out); // send piece request message 
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
										sendMessage(newMessageToSend, out); // send interest message
										if (pp.doAllProcessesHaveTheFile()) {
											if (this.DEBUG_MODE()) System.out.println("All processes have the file");
											closeConnections();
										} 	
										break;
								case 6: // MessageType.REQUEST.ordinal():
										newMessageToSend = Messages.handleRequestMessage(pp, peerConnected, IncomingMessage);
										sendMessage(newMessageToSend, out); // send iinterst message 	
										//pp.logger.log("Creating piece message. Piece size = " + ThePieceLength + ", Piece message size = " + GetMessageLength(newMessageToSend));
										pp.logger.log("Send piece " + Messages.GetPieceMessageNumber(newMessageToSend) + "."); //debug log. Remove this later.
										break;
								case 7: // MessageType.PIECE.ordinal():
									    //if (this.DEBUG_MODE()) System.out.println(" Captured Piece remaining = " + IncomingMessage.remaining() + " limit = " + IncomingMessage.limit() + " message length +4 " + messageLength+4);
										//if (this.DEBUG_MODE()) System.out.println(Messages.HexPrint(IncomingMessage));
 										Messages.handlePieceMessage(pp, peerConnected, messageLength+4, IncomingMessage);
										if (!pp.hasFile()) {
											newMessageToSend = Messages.createRequestMessage(pp.randomMissingPiece());
											sendMessage(newMessageToSend, out); // send interest message 	
										} else {
												pp.logger.onCompletionOfDownload();
												if (Handler.DEBUG_MODE()) System.out.println("No longer interested, sendind not interested and bitfield back.");
												newMessageToSend = Messages.createNotInterestedMessage();
												sendMessage(newMessageToSend, out); // send new not intersted message 	
												newMessageToSend = Messages.createBitfieldMessage(pp.getCurrBitfield());
												sendMessage(newMessageToSend, out); // send new bitfield message
												if (pp.doAllProcessesHaveTheFile()) {
													if (this.DEBUG_MODE()) System.out.println("All processes have the file");
													closeConnections();
												}
										}
										break;
								default: 
										// TODO Handle Illegal Messages and send alerts, reset if necessary
										break;
							}
							this.originalId = this.connectedToPeerIdIncoming;													
							break;

					} // switch for state machine
				} // while (true)
		} // try / catch
		//catch (EOFException e) {
			//e.printStackTrace();
		//}
		// catch(IOException ioException){
		// 	System.out.println("Disconnect with Client " + peerConnected);
		// 	ioException.printStackTrace();
		// }
		catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		} // try / catch / finally
		finally{
			closeConnections();
		} // try / catch / finally
	} // run
	
    // send a message to the output stream
    public synchronized void sendMessage(ByteBuffer msg, ObjectOutputStream outputLocation) {
        try {
            // stream write the message
		    //if (this.DEBUG_MODEL3()) System.out.println(" Sending a Message " + msg.array());
            outputLocation.write(msg.array());
            outputLocation.flush();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

} // Handler
