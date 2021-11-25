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
    // this function is to take a string of binary and pad it on the left with
    // zeroes such that it is a certain length.
    private static String padWithZeroes(String s, int length) {
        StringBuilder sBuilder = new StringBuilder(s);
        for (int i = sBuilder.length(); i < length; i++) {
            sBuilder.insert(0, "0");
        }
        s = sBuilder.toString();
        return s;
    }

    // ALL MESSAGES SHOULD BE SENT AS A BINARY STRING
	// ToDo:
	// This probably won't work for allm messages because pieces that are 32,768 bytes long will create strings that are
	// 32,768*8 in length or 262,144 charactesrs (bytes) of transmitted data.  UDP/TCP/IP are limite to 65,536 byte packets.
	// so the data needs to be encoded in binary format only and transmitted as a sequence of bytes.

    // The following 2 methods use this logic: 
    // https://stackoverflow.com/questions/4416954/how-to-convert-a-string-to-a-stream-of-bits-in-java/4417069
    private static String stringToBinary(String s) {
        return new BigInteger(s.getBytes()).toString(2);
    }

    private static String binaryToString(String b) {
        return new String(new BigInteger(b, 2).toByteArray());
    }

    // takes a length (in bytes) and creates a 32-bit binary string 
    private static String encodeLength(int length) {
        return padWithZeroes(Integer.toBinaryString(length), 32);
    }


    // takes an enum for the message type and creates a 32-bit binary string 
    private static byte encodeType(int type) {
        return  (byte) (type & (int) 0xff);
    }


    // takes an integer and creates a binary string of the specified length in bytes
    public static String integerToBinaryString(int i, int length) {
        return padWithZeroes(Integer.toBinaryString(i), length * 8);
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

    public static String createBitfieldMessage(Vector<Boolean> bitfield) {
        // This may work weird if the bitfield size is not divisible by 8
        int length = bitfield.size() / 8;
        StringBuilder message = new StringBuilder(encodeLength(length) + encodeType(MessageType.BITFIELD.ordinal()));
        for (Boolean b : bitfield) {
            message.append(b ? "1" : "0");
        }
        return message.toString();
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
    public static int handleHandshakeMessage(String binary, peerProcess pp, int senderPeer) {
        // (18 + 10) * 8 - 1 = 223 is the bit where the peerId starts. The peerId is 4 bytes.
        int handshakeFrom = Integer.parseInt(binary.substring(223, 255),2);
        System.out.println("Handshake message from peer " + handshakeFrom);
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
        pp.getRemotePeerInfo(senderPeer).setChoked(true);
        pp.logger.onChoking(senderPeer);
    }

    //type 1

    private static void handleUnchokeMessage(peerProcess pp, int senderPeer) {
        pp.getRemotePeerInfo(senderPeer).setChoked(false);

        pp.logger.onUnchoking(senderPeer);
                                                                                              // DONE: request a random piece that the sender has and the receiver doesn't
                                                                                                  // There's a method in RemotePeerInfo to select a random missing piece that can help.
		int askForPiece = pp.getRemotePeerInfo(senderPeer).selectRandomMissingPiece();        // asking for this piece from the remote peer that is now unchoked
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
    private static void handleHaveMessage(peerProcess pp, int senderPeer, String payload) {
        int index = Integer.parseInt(payload);
        pp.getRemotePeerInfo(senderPeer).getBitfield().set(index, true);  // sets the index to true of the peer that they have this message
        pp.logger.onReceiveHaveMessage(senderPeer, index);                // log that we received this comment
        /* If the receiver of this message does has the piece
          that the sender has, then send a not_interested message.
          Else, send an interested message.
        */ 
        if (pp.bitfield.get(index)) {                                     // if this message is already in possession, skip getting it again
            pp.client.sendMessageBB(createNotInterestedMessage());
        }
        else {                                                            // else ask for the message
            pp.client.sendMessageBB(createInterestedMessage());
        }
    }

    //type 5
    private static void handleBitfieldMessage(String binary, peerProcess pp, int senderPeer, int length, String payload) {
        // if the payload is empty, then the sender has no pieces.
        boolean nowInterested = false;

        if (length == 0) {
                return;
        }
        else {

            for (int i = 0; i < pp.getTotalPieces(); i++) {
                if(pp.getCurrBitfield().get(i) == false && payload.charAt(i) == '1') {
                    nowInterested = true;
                    break;
                }
            }
        }
        System.out.println(nowInterested);

        // TODO: send interested message to sender process
        if(nowInterested)
        {
            pp.messagesToSend.add(Messages.createInterestedMessage());
            // TODO: Question: what purpose do the next two lines serve?
            //pp.messagesToSend.add(Messages.integerToBinaryString(senderPeer, 2));
            //pp.messagesToSend.add(Messages.integerToBinaryString(pp.getPeerId(), 2));
        }
        else
        {
            pp.messagesToSend.add(Messages.createNotInterestedMessage());
            // TODO: Question: what purpose do the next two lines serve?
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
    private static void handleRequestMessage(peerProcess pp, int senderPeer, ByteBuffer payload) {  // a peer (senderPeer) has requested (payload) index message
                                                                                  // DONE: if the receiver of the message has the piece, then send the piece
        //int index = Integer.parseInt(payload);                                    // parse out the requestd item into payload
		//pp.client.sendMessageBB(createPieceMessage(payload));                        // send the piece
		                                                                          // ToDo: Read the file piece and send it with the pay load encoded as a bitstream
		                                                                          // doesn't set peer information of this payload to true until the peer says the message was received with a HAVE message
    }

    //type 7
    private static void handlePieceMessage(peerProcess pp, int senderPeer, int length, String payload) {
        int index = Integer.parseInt(payload.substring(0,32),2);
        String piece = binaryToString(payload.substring(32,length-32));
                                                                                  // Done: write the piece to a file (wherever it should be written, idk)  See Below, handles logging of the received piece
       
        pp.getRemotePeerInfo(senderPeer).incrementPiecesTransmitted();
        // update the bitfield
        pp.bitfield.set(index,true);
        pp.incrementCollectedPieces();
        pp.logger.onDownloadingAPiece(senderPeer, index, pp.getCollectedPieces());
        // log if this process now has the entire file
        if (pp.hasFile()) {
            pp.logger.onCompletionOfDownload();
        }
    }

    // returns the peerId of the sender if it's a handshake message.
    public static int decodeMessage(String binary, peerProcess pp, int sender) {
        return decodeMessage(pp, binary, sender);
    }

    public static int decodeMessage(peerProcess pp, String binary, int senderPeer) {
        String handshakeHeader = stringToBinary("P2PFILESHARINGPROJ");
        // if the message starts with the handShake header, then it's a handshake message

        if (binary.length() >= 143 && binary.substring(0,143).equals(handshakeHeader)) {
            return handleHandshakeMessage(binary, pp, senderPeer);
        }
        /* if it's not a handshake message then it's an actual message. This is the format:
         * 4-byte message length field (length is in bytes)
         * 1-bit message type
         * message payload
         */
        int length = Integer.parseInt(binary.substring(0,32), 2);
        int type = Integer.parseInt(binary.substring(32,40), 2);
        String payload = null;
        try {
            payload = binary.substring(40, length * 8);
        }
        catch(Exception e)
        {
            
        }


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
            handleHaveMessage(pp, senderPeer, payload);
        }
        else if (type == MessageType.BITFIELD.ordinal()) { //type 5

            handleBitfieldMessage(binary, pp, senderPeer, length, payload);
        }
        else if (type == MessageType.REQUEST.ordinal()) { //type 6
            //handleRequestMessage(pp, senderPeer, payload);
        }
        else if (type == MessageType.PIECE.ordinal()) { //type 7
            handlePieceMessage(pp, senderPeer, length, payload);
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
