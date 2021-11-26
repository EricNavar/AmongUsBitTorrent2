import java.math.BigInteger;
import java.util.Vector;

import java.net.*;
import java.io.*;
import java.nio.*;
import java.io.File;
import java.util.*;

import java.io.FileWriter;   // https://www.w3schools.com/java/java_files_create.asp examples utilized as basis for creating file i/o code
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
                             // idean of file output streams came from https://www.techiedelight.com/how-to-write-to-a-binary-file-in-java/
import java.io.IOException; 
import java.nio.channels.FileChannel;
import java.io.FileOutputStream;

// This class is to hold the logic for creating and decoding messages.
public class Messages {

    // takes an enum for the message type and creates a 32-bit binary string 
    private static byte encodeType(int type) {
        return  (byte) (type & (int) 0xff);
    }

    // ==============================================================
    // ====================== MESSAGE CREATORS ======================
    // ==============================================================
    public static ByteBuffer createHandshakeMessage(int peerId) {
        // This code is the best
		ByteBuffer MessageAssembly = ByteBuffer.allocate(32);  // Handshake Messages are 32 byte payload messages
        String HeaderInformation = "P2PFILESHARINGPROJ";
		MessageAssembly.put(HeaderInformation.getBytes());
        // Add 10 bytes of zeroes
		MessageAssembly.put(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
		MessageAssembly.putInt(peerId);
        return MessageAssembly;
    }
	//  Message Format
	//  [ Length of 4 bytes ] [ Message Type 1 byte ] { Message Payload ]
    //   The 4-byte message length specifies the message length in bytes. It does not include the
    //   length of the message length field itself.
    //   The 1-byte message type field specifies the type of the message.
	//   ‘choke’, ‘unchoke’, ‘interested’ and ‘not interested’ messages have no payload.
	//   Therefore, the four messages above are all of length 1.
	//   NOTE: Length should be 4 bytes Uunsigned Integer.  Switching length over to unsigned integers.

    // message contains no body, length set to 1 because of above statement "does not include length" but guessing it does
	//         include the message type as part of the message.
    public static ByteBuffer createChokeMessage() {
		ByteBuffer MessageAssembly = ByteBuffer.allocate(5);  // Message is 5 bytes
		MessageAssembly.putInt(1);  // length is equal to 1
		MessageAssembly.put(encodeType(MessageType.CHOKE.ordinal()));
        return MessageAssembly;
    }

    public static ByteBuffer createUnchokeMessage() {
		ByteBuffer MessageAssembly = ByteBuffer.allocate(5);  // Message is 5 bytes
		MessageAssembly.putInt(1);  // length is equal to 1
		MessageAssembly.put(encodeType(MessageType.UNCHOKE.ordinal()));
        return MessageAssembly;
    }

    // message contains no body, length set to 1 because of above statement "does not include length" but guessing it does
	//         include the message type as part of the message.
    public static ByteBuffer createInterestedMessage() {
		ByteBuffer MessageAssembly = ByteBuffer.allocate(5);  // Message is 5 bytes
		MessageAssembly.putInt(1);  // length is equal to 1
		MessageAssembly.put(encodeType(MessageType.INTERESTED.ordinal()));
        return MessageAssembly;
    }

    // message contains no body, length set to 1 because of above statement "does not include length" but guessing it does
	//         include the message type as part of the message.
    public static ByteBuffer createNotInterestedMessage() {
		ByteBuffer MessageAssembly = ByteBuffer.allocate(5);  // Message is 5 bytes
		MessageAssembly.putInt(1);  // length is equal to 1
		MessageAssembly.put(encodeType(MessageType.NOT_INTERESTED.ordinal()));
        return MessageAssembly;
    }
	
    // message contains no body, length set to 1 because of above statement "does not include length" but guessing it does
	//         include the message type as part of the message.
    public static ByteBuffer createHaveMessage(int index) {
		ByteBuffer MessageAssembly = ByteBuffer.allocate(9);  // Message is 9 bytes
		MessageAssembly.putInt(5);  // length is equal to 5
		MessageAssembly.put(encodeType(MessageType.BITFIELD.ordinal()));
		MessageAssembly.putInt(index);  // piece number is index 
        return MessageAssembly;
    }
   
    // exracts the payload length of this message
    public static int ExtractPayloadLength(ByteBuffer Message) {
        return (int) (Message.array()[4]);
    }


    // BitField Messages
    // ‘bitfield’ messages is only sent as the first message right after handshaking is done when 
    // a connection is established. ‘bitfield’ messages have a bitfield as its payload. Each bit in 
    // the bitfield payload represents whether the peer has the corresponding piece or not. The 
    // first byte of the bitfield corresponds to piece indices 0 – 7 from high bit to low bit, 
    // respectively. The next one corresponds to piece indices 8 – 15, etc. Spare bits at the end 
    // are set to zero. Peers that don’t have anything yet may skip a ‘bitfield’ message
    //
    public static ByteBuffer createBitfieldMessage(Vector<Boolean> bitfield) {
        // This may work weird if the bitfield size is not divisible by 8
        int lengthBytes = (int) (Math.ceil(((float) bitfield.size()) / 8.0f));  // odd bitfields are a problem so ceiling to increase to max size...
		ByteBuffer MessageAssembly = ByteBuffer.allocate(lengthBytes+1+5);  // Message is length_1 bytes
		MessageAssembly.putInt(lengthBytes); 
		MessageAssembly.put(encodeType(MessageType.BITFIELD.ordinal()));
		int bitnumber;
		for (int x = 0; x < lengthBytes; ++x) {
			byte assembleTheByte = 0;
			for (int y = 0; y < 8; ++y) { 
			   bitnumber = 8*x + y;
			   assembleTheByte = (byte) (assembleTheByte << 1);
			   if (bitnumber < bitfield.size()) {
			       if (bitfield.get(bitnumber)) {
			         assembleTheByte = (byte) (assembleTheByte | 0x01);
			      } 
			   } // else it is a zero be default and just keep accumulating those
			}
			MessageAssembly.put(assembleTheByte);
		}
        return MessageAssembly;
    }

    public static ByteBuffer createRequestMessage(int index) {
		ByteBuffer MessageAssembly = ByteBuffer.allocate(9);  // Message is 9 bytes
		MessageAssembly.putInt(5);  // length is equal to 5
		MessageAssembly.put(encodeType(MessageType.REQUEST.ordinal()));
		MessageAssembly.putInt(index);  // piece number is index 
        return MessageAssembly;
    }

    public static ByteBuffer createPieceMessage(ByteBuffer payload, int PieceNumber, int PieceLength) {

		ByteBuffer MessageAssembly = ByteBuffer.allocate(65536);  // Message is 9 bytes
		MessageAssembly.putInt(PieceLength + 5);  // length is equal to 1 (message type) + 4 (piece index size) +  piece size (bytes)
		MessageAssembly.put(encodeType(MessageType.PIECE.ordinal()));
		MessageAssembly.putInt(PieceNumber);  // piece number is index 
		MessageAssembly.put(Arrays.copyOfRange(payload.array(), 0, PieceLength));  // piece number is index 
        return MessageAssembly;
    }
	
    // ==============================================================
    // ====================== MESSAGE HANDLERS ======================
    // ==============================================================

    /*
    * Suppose that peer A tries to make a TCP connection to peer B. Here we
    * describe the behavior of peer A, but peer B should also follow the same
    * procedure as peer A. After the TCP connection is established, peer A sends a
    * handshake message to peer B. It also receives a handshake message from peer B
    * and checks whether peer B is the right neighbor.
    * 
    * The only thing to do is to check whether the handshake header is right and the
    * peer ID is the expected one. After handshaking, peer A sends a "bitfield" message to let
    * peer B know which file pieces it has. Peer B will also send its "bitfield" message to
    * peer A, unless it has no pieces. If peer A receives a "bitfield" message from
    * peer B and it finds out that peer B has pieces that it doesn’t have,
    * peer A sends "interested" message to peer B. Otherwise, it sends
    * "not interested" message.
    */
	
	public static String ParseString( ByteBuffer IncomingBuffer, int startLocation, int length) {
        StringBuilder result = new StringBuilder();
		for (int x = startLocation; x < (startLocation + length); ++x) {
           result.append(String.format("%c", IncomingBuffer.array()[x]));
		}
		return result.toString();
	}
	
	public static int ParseInteger( ByteBuffer IncomingBuffer, int startLocation) {
		 return   ( ((IncomingBuffer.array()[startLocation  ]&0x0FF)<<24) | ((IncomingBuffer.array()[startLocation+1]&0x0FF)<<16) | 
                    ((IncomingBuffer.array()[startLocation+2]&0x0FF)<<8 ) | ((IncomingBuffer.array()[startLocation+3]&0x0FF)<<0 )    );
	}
	
	public static byte ParseByte( ByteBuffer IncomingBuffer, int location) {
		 return   (byte) (IncomingBuffer.array()[location]&0x0FF);
	}
	
	public static int GetMessageLength( ByteBuffer IncomingBuffer ) {
		 return   ParseInteger(IncomingBuffer, 0);
	}
	
	public static int GetMessageType( ByteBuffer IncomingBuffer ) {
		 return   (int) (ParseByte(IncomingBuffer, 4));
	}
	
	public static int GetHandshakePeerID(ByteBuffer IncomingBuffer) {
		return ParseInteger( IncomingBuffer, 28);
	} 
	
	public static String GetHandshakeString(ByteBuffer IncomingBuffer) {
		return ParseString(IncomingBuffer, 0, 18);
	} 
	
	public static int GetHavePieceNumber( ByteBuffer IncomingBuffer ) {
		 return   (int) (ParseInteger(IncomingBuffer, 5));
	}
	
	public static int GetPieceMessageNumber( ByteBuffer IncomingBuffer ) {
		 return   (int) (ParseInteger(IncomingBuffer, 5));
	}
	
	public static int GetRequestMessageIndex( ByteBuffer IncomingBuffer ) {
		 return   (int) (ParseInteger(IncomingBuffer, 5));
	}
	
	
    public static int handleHandshakeMessage(ByteBuffer IncomingBuffer) {
        // The peerID is 4 bytes, located at 
        int handshakeFrom = GetHandshakePeerID(IncomingBuffer);
        System.out.println("Received a Handshake Message from peer " + handshakeFrom);
        //System.out.println("  Message String was [" + GetHandshakeString(IncomingBuffer) + "]");
		//System.out.println("  The first byte value is " + ParseByte(IncomingBuffer, 0));
        return handshakeFrom;
    }

    /* 
     * CHOKE AND UNCHOKE (description from requirements sheet)
     *  The number of concurrent connections on which a peer uploads its pieces is limited. At a
     * moment, each peer uploads its pieces to at most k preferred neighbors and 1 optimistically
     * unchoked neighbor. The value of k is given as a parameter when the program starts. Each
     * peer uploads its pieces only to preferred neighbors and an optimistically unchoked
     * neighbor. We say these neighbors are unchoked and all other neighbors are choked.
     * Each peer determines preferred neighbors every p seconds. Suppose that the unchoking
     * interval is p . Then every p seconds, peer A reselects its preferred neighbors. To make
     * the decision, peer A calculates the downloading rate from each of its neighbors,
     * respectively, during the previous unchoking interval. Among nei ghbors that are interested
     * in its data, peer A picks k neighbors that has fed its data at the highest rate. If more than
     * two peers have the same rate, the tie should be broken randomly. Then it unchokes those
     * preferred neighbors by sending "unchoke" messages and it expects to receive "request" messages
     * from them. If a preferred neighbor is already unchoked, then peer A does not
     * have to send ‘unchoke’ message to it. All other neighbors previously unchoked but not
     * selected as preferred neighbors at this time should be choked unless it is an optimistically
     * should be choked unless it is an optimistically unchoked neighbor. To choke those neighbors,
     * peer A sends unchoked neighbor. To choke those neighbors, peer A sends ‘chokechoke’ messages to
     * them messages to them and stop sending pieces.and stop sending pieces.
     * 
     * If peer A has a complete file, it determines preferred neighbors randomly among those
     * that are interested in its data rather than comparing downloading rates.
     * Each peer determines an optimistically unchoked neighbor every
     * m seconds. We say m is the optimistic unchoking interval. Every m seconds, 
     * peer A reselects an optimistically unchoked neighbor randomly among neighbors that are
     * choked at that moment but are interested in its data. Then peer A sends "unchoke"
     * message to the selected neighbor and it expects to receive "request" messages from it.
     * Suppose that peer C is randomly chosen as the optimistically unchoked neighbor of peer A.
     * Because peer A is sending data to peer C, peer A may become one of peer C’s preferred neighbors,
     * in which case peer C would start to send data to peer A. If the rate at which peer C sends data to
     * peer A is high enough, peer C could then, in turn, become one of peer A’s preferred neighbors.
     * Note that in this case, peer C may be a preferred neighbor and optimistically unchoked neighbor
     * at the same time. This kind of situation is allowed. In the next optimistic unchoking interval,
     * another peer will be selected as an optimistically unchoked neighbor.
     */

    //type 0
    private static void handleChokeMessage(peerProcess pp, int senderPeer) {

	System.out.println(senderPeer + " choked " + pp.getPeerId());

        RemotePeerInfo sender = pp.getRemotePeerInfo(senderPeer);
        if (sender == null)
        {
            System.out.println("remote peer with id " + senderPeer + " info not found");
            return;
        }
        sender.setChoked(true);

        pp.logger.onChoking(senderPeer);
    }

    //type 1

    private static void handleUnchokeMessage(peerProcess pp, int senderPeer) {

       System.out.println(senderPeer + " unchoked " +  pp.getPeerId());
 
        RemotePeerInfo sender = pp.getRemotePeerInfo(senderPeer);
        if (sender == null)
        {
            System.out.println("remote peer with id " + senderPeer + " info not found");
            return;
        }
        sender.setChoked(false);


        pp.logger.onUnchoking(senderPeer);
                                                                                              // DONE: request a random piece that the sender has and the receiver doesn't
        

		Vector<Integer> missingPieces = new Vector<Integer>();     // Create a temporary vector to hold missing piece values
		for (int i = 0; i < pp.bitfield.size(); i++) {                // walk the entire bitfield vector 
			if (!pp.bitfield.get(i)) {                                // look for bitfields that are not true yet, so missing...
				missingPieces.add(i);                              // add them to the missing piecese collection
			} 
		}

        int missingPieceIndex = (int)Math.floor(Math.random()*(pp.bitfield.size()));  
		                                                                                      
	int askForPiece = missingPieces.get(missingPieceIndex);   
  
	pp.client.sendMessageBB(createRequestMessage(askForPiece));
        // ask for this piece
    }

    /* INTERESTED AND NOT INTERESTED
     * Regardless of the connection state of choked or unchoked, if a neighbor has some
     * interesting pieces, then a peer sends "interested" message to the neighbor. Whenever a
     * peer receives a "bitfield" or "have" message from a neighbor, it determines whether it
     * should send an "interested" message to the neighbor. For example, suppose that peer A
     * makes a connection to peer B and receiv es a "bitfield" message that shows peer B has
     * some pieces not in peer A. Then peer A sends an "interested" message to peer B. In
     * another example, suppose that peer A receives a "have" message from peer C that
     * contains the index of a piece not in peer A. T hen peer A sends an "interested" message
     * to peer C. Each peer maintains bitfields for all neighbors and updates them whenever it
     * receives "have" messages from its neighbors. If a neighbor does not have any interesting 
     * pieces, then the peer sends a "not interested" message to the neighbor. Whenever a peer
     * receives a piece completely, it checks a piece completely, it checks the the bitfields of
     * its neighbors and decide bitfields of its neighbors and decides whether it should it should 
     * send "not interested" messages to some neighbors.messages to some neighbors.
    */

    //type 2
    private static void handleInterestedMessage(peerProcess pp, int senderPeer) {
        Vector<Integer> interest = pp.getInterested();
        interest.add(senderPeer);
        pp.setInterested(interest);

        pp.logger.onReceiveInterestedMessage(senderPeer);
    }

    //type 3
    private static void handleNotInterestedMessage(peerProcess pp, int senderPeer) {

        Vector<Integer> interest = pp.getInterested();
        for(int i =0; i < interest.size(); i++)
        {
            if(interest.get(i) == senderPeer)
                interest.remove(i);
        }
        pp.setInterested(interest);
        pp.logger.onReceiveNotInterestedMessage(senderPeer);
    }

    //type 4
    private static void handleHaveMessage(peerProcess pp, int senderPeer, ByteBuffer IncomingMessage) {
        int index = GetHavePieceNumber(IncomingMessage);
        RemotePeerInfo sender = pp.getRemotePeerInfo(senderPeer);
        if (sender == null) {
            System.out.println("getRemotePeer is null");
            return;
        }
        sender.getBitfield().set(index, true);  // sets the index to true of the peer that they have this message
        pp.logger.onReceiveHaveMessage(senderPeer, index);                // log that we received this comment
                                                                          // If the receiver of this message does has the piece
                                                                          //    that the sender has, then send a not_interested message.
                                                                          //  Else, send an interested message.
        if (pp.getCurrBitfield().get(index)) {                                     // if this message is already in possession, skip getting it again
            pp.client.sendMessageBB(createNotInterestedMessage());
        }
        else {                                                            // else ask for the message
            pp.client.sendMessageBB(createInterestedMessage());
        }
    }

    //type 5
    private static void handleBitfieldMessage(ByteBuffer IncomingMessage, peerProcess pp, int senderPeer, int length) {
        // if the payload is empty, then the sender has no pieces.
	
        boolean nowInterested = false;
        if (length == 0) {
                return;
        }
        else {
            for (int i = 0; i < pp.getTotalPieces(); i++) {
				int x = i / 8;
				int y = i % 8;
				int bitvalue = (int) IncomingMessage.array()[x+4]; // if 8 bits, i = 7 and x is 0, if 9th bit i=8 and x = 1
				bitvalue = (bitvalue>>y) & 0x0FF;
                // if the sender has a piece that this process does not, then this process will send an interested message.
                // otherwise, send an uninterested message.
                if((pp.getCurrBitfield().get(i) == false) && (bitvalue == 1)) {
                    nowInterested = true;
                    //  break;  // DO NOT Break, instead keep loading in and tracking the pieces this peer has in it's posession
                }
				pp.getRemotePeerInfo(senderPeer).getBitfield().set(i, true);  // sets the index i to true of the peer that they have this piece
            }
        }
        System.out.println("The interest of " + pp.getPeerId() + " is set to " + nowInterested);

        // TODO: send interested message to sender process
        if(nowInterested)
        {
            pp.messagesToSend.add(Messages.createInterestedMessage());
            // TODO: Question: what purpose do the next two lines serve?
            // Answer: they identify orig/dest peers of message
			// Comment: The message specification is defined as shown, sending two more messages on the wire line won't solve the issue as it isn't 
			// inline with the specification. It seems like we know the sender from the ipV4 packet and need to decipher it in a different manner than
			// adding two more messages to the end of the current message or modifying the defined message.
			// Each Handler is associated with a TCP connection (or should be) and should define the identify of the peer this is connected to.
            //pp.messagesToSend.add(Messages.integerToBinaryString(senderPeer, 2));
            //pp.messagesToSend.add(Messages.integerToBinaryString(pp.getPeerId(), 2));
        }
        else
        {
            pp.messagesToSend.add(Messages.createNotInterestedMessage());
            // TODO: Question: what purpose do the next two lines serve?
            // Answer: they identify orig/dest peers of message
			// Comment: The message specification is defined as shown, sending two more messages on the wire line won't solve the issue as it isn't 
			// inline with the specification.  It seems like we know the sender from the ipV4 packet and need to decipher it in a different manner than
			// adding two more messages to the end of the current message or modifying the defined message.
	        // Each Handler is associated with a TCP connection (or should be) and should define the identify of the peer this is connected to.
            //pp.messagesToSend.add(Messages.integerToBinaryString(senderPeer, 2));
            //pp.messagesToSend.add(Messages.integerToBinaryString(pp.getPeerId(), 2));
        }
        return;
    }

    /* REQUEST AND PIECE
     * When a connection is unchoked by a neighbor, a peer sends a "request" message for
     * requesting a piece that it does not have and has not requested from other neighbors.
     * Suppose that peer A receives an "unchoke" message from peer B. Peer A selects a piece
     * randomly among the pieces that peer B has and peer A does not have, and peer A has
     * not requested yet. Note that we use a random selection strategy, which is not the rarest
     * first strategy usually used in BitTorrent. On receiving peer A’s "request" message, peer
     * B sends a "piece" message that contains the actual piece. After completely downloading
     * the piece, peer A sends another "request" message to peer B. The exchange of
     * request/piece messages continues until peer A is choked by peer B or peer B does not
     * have any more interesting pieces. The next ‘request’ message should be sent after the
     * peer receives the piece message for the previous ‘requestrequest’ message. Note that this
     * behavior is different from the pipelining approach of BitTorrent. This is less efficient but
     * simpler to implement . Note also that you don’t have to implement the "endgame mode"
     * used in BitTorrent. So we don’t have the "cancel" message. Even though peer A sends a 
     * "request" message to peer B, it may not receive a "piece" message corresponding to it.
     * This situation happens when peer B re-determines preferred neighbors or optimistically
     * unchoked a neighbor and peer A is choked as the result before peer B responds to peer
     * A. Your program should consider this case.
    */

    //type 6
    private static void handleRequestMessage(peerProcess pp, int senderPeer, ByteBuffer IncomingMessage) {  // a peer (senderPeer) has requested (payload) index message
             	FileHandling f = pp.getFileObject();                                                                     // DONE: if the receiver of the message has the piece, then send the piece
        int index = GetRequestMessageIndex(IncomingMessage);                      // parse out the requestd item into an integer to look up in the map structure
		if (f.CheckForPieceNumber(index)) {                           // if we actually have this piece in the stored location...
			ByteBuffer ThePiece;
			int ThePieceLength;

			ThePiece = pp.FileObject.MakeCopyPieceByteBuffer(index);
                                                                   // get a copy of the piece
			ThePieceLength = pp.FileObject.GetPieceSize(index);                   // get the piece's length
			
    		pp.pieceMessages.add(createPieceMessage(ThePiece, index, ThePieceLength));                        // send the piece
		} else {
           System.out.println("Some questionable character/actor identified as " + senderPeer + " asked for piece " + index + " but this peer known as " + pp.peerId + " does not have it...");
		}
    }

    //type 7
    private static void handlePieceMessage(peerProcess pp, int senderPeer, int length, ByteBuffer IncomingMessage) {
        int index = GetPieceMessageNumber(IncomingMessage);
                                                                                  // Done: write the piece to a file (wherever it should be written, idk)  See Below, handles logging of the received piece
		ByteBuffer GrabPieceData = ByteBuffer.allocate(65536);                    // Message is longer
		GrabPieceData.put(Arrays.copyOfRange(IncomingMessage.array(), 9, length-9));  // Get the piece
        pp.FileObject.ReceivedAPiece(index, GrabPieceData, length-9);             // insert into the File Handler

        // TODO: What do they mean by "partial files" maintained in current directory?
        //       Are we supposed to support 100GB file transfers and cache to the drive?
        if (pp.FileObject.CheckForAllPieces()) {
	   
            StringBuilder filenameWrite = new StringBuilder();            
filenameWrite.append(String.format("./peer_%04d/TreeCopy.jpg", pp.peerId));


			pp.FileObject.WriteFileOut(filenameWrite.toString());

		}

        pp.getRemotePeerInfo(senderPeer).incrementPiecesTransmitted();
        // update the bitfield
        pp.getCurrBitfield().set(index,true);
        pp.incrementCollectedPieces();
        pp.logger.onDownloadingAPiece(senderPeer, index, pp.getCollectedPieces());
        // log if this process now has the entire file
        if (pp.hasFile()) {
            pp.logger.onCompletionOfDownload();
        }

        updateInterestedStatus(pp);
    }

    // Whenever a peer receives
    //     a piece completely, it checks the bitfields of its neighbors and decides whether it should
    //     send ‘not interested’ messages to some neighbors.
    public static void updateInterestedStatus(peerProcess pp) {
        for (int neighborId: pp.preferredNeighbors) {
            if (!pp.checkInterested(pp.getRemotePeerInfo(neighborId).getBitfield())) {
                pp.messagesToSend.add(createNotInterestedMessage());
            }
        }
    }

    // returns the peerId of the sender if it's a handshake message.
    public static int decodeMessage(ByteBuffer IncomingMessage, peerProcess pp, int sender) {
        return decodeMessage(pp, IncomingMessage, sender);
    }

    // returns the peerId of the sender if it's a handshake message.
    public static int decodeMessage(peerProcess pp, ByteBuffer IncomingMessage, int senderPeer) {
        String handshakeHeader = "P2PFILESHARINGPROJ";
        // if the message starts with the handShake header, then it's a handshake message

        if (IncomingMessage.remaining() >= 32) {
			if (GetHandshakeString(IncomingMessage).equals(handshakeHeader)) {
				return handleHandshakeMessage(IncomingMessage);
			}
		}
		
        /* if it's not a handshake message then it's an actual message. This is the format:
         * 4-byte message length field (length is in bytes)
         * 1-bit message type
         * message payload
         */
	
        int length = GetMessageLength(IncomingMessage);
        int type   = GetMessageType(IncomingMessage);

	System.out.println("Message type received: " + type);


        // The logic for handling the message types are here
        if (type == MessageType.CHOKE.ordinal()) { //type 0
            handleChokeMessage(pp, senderPeer);
        }
        else if (type == MessageType.UNCHOKE.ordinal()) { //type 1

            handleUnchokeMessage(pp, senderPeer);
        }
        else if (type == MessageType.INTERESTED.ordinal()) { //type 2

            handleInterestedMessage(pp, senderPeer);

        }
        else if (type == MessageType.NOT_INTERESTED.ordinal()) { //type 3

            handleNotInterestedMessage(pp, senderPeer);
        }
        else if (type == MessageType.HAVE.ordinal()) { //type 4
            handleHaveMessage(pp, senderPeer, IncomingMessage);
        }
        else if (type == MessageType.BITFIELD.ordinal()) { //type 5

            handleBitfieldMessage(IncomingMessage, pp, senderPeer, length);
        }
        else if (type == MessageType.REQUEST.ordinal()) { //type 6
            handleRequestMessage(pp, senderPeer, IncomingMessage);
        }
        else if (type == MessageType.PIECE.ordinal()) { //type 7
	    
            handlePieceMessage(pp, senderPeer, length, IncomingMessage);
        }
        else {
            System.out.println("Invalid message type");
        }

        return -1;
    }
	
	
    public static String HexPrint(ByteBuffer bytes) {  // modifed from idea at https://mkyong.com/java/java-how-to-convert-bytes-to-hex/ By mkyong
        StringBuilder result = new StringBuilder();
        for (int x = 0; x < bytes.remaining(); ++x) {
            result.append(String.format("%02x ", bytes.array()[x]));
            // upper case
            // result.append(String.format("%02X", aByte));
        }
        return result.toString();
    }

}
