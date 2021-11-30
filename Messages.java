
import java.util.Vector;

import javax.swing.JFormattedTextField.AbstractFormatterFactory;

import java.nio.*;
import java.rmi.Remote;
import java.util.*;
import java.nio.charset.StandardCharsets;

// import java.io.FileWriter;   // https://www.w3schools.com/java/java_files_create.asp examples utilized as basis for creating file i/o code
// import java.io.FileOutputStream;
// import java.io.FileInputStream;
// import java.io.FileReader;
// idea of file output streams came from https://www.techiedelight.com/how-to-write-to-a-binary-file-in-java/
// import java.io.IOException; 
// import java.nio.channels.FileChannel;
// import java.io.FileOutputStream;

// This class is to hold the logic for creating and decoding messages.
public class Messages {

    // takes an enum for the message type and creates a 32-bit binary string
    private static byte encodeType(int type) {
        return (byte) (type & (int) 0xff);
    }

    // ==============================================================
    // ====================== MESSAGE CREATORS ======================
    // ==============================================================
    public static ByteBuffer createHandshakeMessage(int peerId) {
        // This code is the best
        ByteBuffer MessageAssembly = ByteBuffer.allocate(32); // Handshake Messages are 32 byte payload messages
        String HeaderInformation = "P2PFILESHARINGPROJ";
        MessageAssembly.put(HeaderInformation.getBytes());
        // Add 10 bytes of zeroes
        MessageAssembly.put(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
        MessageAssembly.putInt(peerId);
        return MessageAssembly;
    }
    // Message Format
    // [ Length of 4 bytes ] [ Message Type 1 byte ] { Message Payload ]
    // The 4-byte message length specifies the message length in bytes. It does not
    // include the
    // length of the message length field itself.
    // The 1-byte message type field specifies the type of the message.
    // ‘choke’, ‘unchoke’, ‘interested’ and ‘not interested’ messages have no
    // payload.
    // Therefore, the four messages above are all of length 1.
    // NOTE: Length should be 4 bytes Uunsigned Integer. Switching length over to
    // unsigned integers.

    // message contains no body, length set to 1 because of above statement "does
    // not include length" but guessing it does
    // include the message type as part of the message.
    public static ByteBuffer createChokeMessage() {
        ByteBuffer MessageAssembly = ByteBuffer.allocate(5); // Message is 5 bytes
        MessageAssembly.putInt(1); // length is equal to 1
        MessageAssembly.put(encodeType(MessageType.CHOKE.ordinal()));
        return MessageAssembly;
    }

    public static ByteBuffer createUnchokeMessage() {
        ByteBuffer MessageAssembly = ByteBuffer.allocate(5); // Message is 5 bytes
        MessageAssembly.putInt(1); // length is equal to 1
        MessageAssembly.put(encodeType(MessageType.UNCHOKE.ordinal()));
        return MessageAssembly;
    }

    // message contains no body, length set to 1 because of above statement "does
    // not include length" but guessing it does
    // include the message type as part of the message.
    public static ByteBuffer createInterestedMessage() {
        ByteBuffer MessageAssembly = ByteBuffer.allocate(5); // Message is 5 bytes
        MessageAssembly.putInt(1); // length is equal to 1
        MessageAssembly.put(encodeType(MessageType.INTERESTED.ordinal()));
        return MessageAssembly;
    }

    // message contains no body, length set to 1 because of above statement "does
    // not include length" but guessing it does
    // include the message type as part of the message.
    public static ByteBuffer createNotInterestedMessage() {
        System.out.println("Sending not interested message");
        ByteBuffer MessageAssembly = ByteBuffer.allocate(5); // Message is 5 bytes
        MessageAssembly.putInt(1); // length is equal to 1
        MessageAssembly.put(encodeType(MessageType.NOT_INTERESTED.ordinal()));
        return MessageAssembly;
    }

    // message contains no body, length set to 1 because of above statement "does
    // not include length" but guessing it does
    // include the message type as part of the message.
    public static ByteBuffer createHaveMessage(int index) {
        ByteBuffer MessageAssembly = ByteBuffer.allocate(9); // Message is 9 bytes
        MessageAssembly.putInt(5); // length is equal to 5
        MessageAssembly.put(encodeType(MessageType.BITFIELD.ordinal()));
        MessageAssembly.putInt(index); // piece number is index
        return MessageAssembly;
    }

    // exracts the payload length of this message
    public static int ExtractPayloadLength(ByteBuffer Message) {
        return (int) (Message.array()[4]);
    }

    // BitField Messages
    // ‘bitfield’ messages is only sent as the first message right after handshaking
    // is done when
    // a connection is established. ‘bitfield’ messages have a bitfield as its
    // payload. Each bit in
    // the bitfield payload represents whether the peer has the corresponding piece
    // or not. The
    // first byte of the bitfield corresponds to piece indices 0 – 7 from high bit
    // to low bit,
    // respectively. The next one corresponds to piece indices 8 – 15, etc. Spare
    // bits at the end
    // are set to zero. Peers that don’t have anything yet may skip a ‘bitfield’
    // message
    //
    public static ByteBuffer createBitfieldMessage(Vector<Boolean> bitfield) {
        // This may work weird if the bitfield size is not divisible by 8
        int lengthBytes = (int) (Math.ceil(((float) bitfield.size()) / 8.0f)); // odd bitfields are a problem so ceiling
                                                                               // to increase to max size...
        ByteBuffer MessageAssembly = ByteBuffer.allocate(lengthBytes + 1 + 5); // Message is length_1 bytes
        MessageAssembly.putInt(lengthBytes);
        MessageAssembly.put(encodeType(MessageType.BITFIELD.ordinal()));
        int bitnumber;
        for (int x = 0; x < lengthBytes; ++x) {
            byte assembleTheByte = 0;
            for (int y = 0; y < 8; ++y) {
                bitnumber = 8 * x + y;
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
        ByteBuffer MessageAssembly = ByteBuffer.allocate(9); // Message is 9 bytes
        MessageAssembly.putInt(5); // length is equal to 5
        MessageAssembly.put(encodeType(MessageType.REQUEST.ordinal()));
        MessageAssembly.putInt(index); // piece number is index
        return MessageAssembly;
    }

    public static ByteBuffer createPieceMessage(ByteBuffer payload, int PieceNumber, int PieceLength) {
        ByteBuffer MessageAssembly = ByteBuffer.allocate(65536); // Message is 9 bytes
        // length is equal to 1 (message type) + 4 (piece index size) + piece size (bytes)
        MessageAssembly.putInt(PieceLength + 5);
        MessageAssembly.put(encodeType(MessageType.PIECE.ordinal()));
        MessageAssembly.putInt(PieceNumber); // piece number is index
        MessageAssembly.put(Arrays.copyOfRange(payload.array(), 0, PieceLength)); // piece number is index
        
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
     * The only thing to do is to check whether the handshake header is right and
     * the
     * peer ID is the expected one. After handshaking, peer A sends a "bitfield"
     * message to let
     * peer B know which file pieces it has. Peer B will also send its "bitfield"
     * message to
     * peer A, unless it has no pieces. If peer A receives a "bitfield" message from
     * peer B and it finds out that peer B has pieces that it doesn’t have,
     * peer A sends "interested" message to peer B. Otherwise, it sends
     * "not interested" message.
     */

    public static String ParseString(ByteBuffer IncomingBuffer, int startLocation, int length) {
        StringBuilder result = new StringBuilder();
        for (int x = startLocation; x < (startLocation + length); ++x) {
            result.append(String.format("%c", IncomingBuffer.array()[x]));
        }
        return result.toString();
    }

    public static int ParseInteger(ByteBuffer IncomingBuffer, int startLocation) {
        int result; 
        try {
            result = (((IncomingBuffer.array()[startLocation] & 0x0FF) << 24)
                | ((IncomingBuffer.array()[startLocation + 1] & 0x0FF) << 16) |
                ((IncomingBuffer.array()[startLocation + 2] & 0x0FF) << 8)
                | ((IncomingBuffer.array()[startLocation + 3] & 0x0FF) << 0));
        }
        catch (ArrayIndexOutOfBoundsException e) {
            result = -1;
            System.out.println(IncomingBuffer.array());
        }
        return result;
    }

    public static byte ParseByte(ByteBuffer IncomingBuffer, int location) {
        return (byte) (IncomingBuffer.array()[location] & 0x0FF);
    }

    public static int GetMessageLength(ByteBuffer IncomingBuffer) {
        return ParseInteger(IncomingBuffer, 0);
    }

    public static int GetMessageType(ByteBuffer IncomingBuffer) {
        return (int) (ParseByte(IncomingBuffer, 4));
    }

    public static int GetHandshakePeerID(ByteBuffer IncomingBuffer) {
        return ParseInteger(IncomingBuffer, 28);
    }

    public static String GetHandshakeString(ByteBuffer IncomingBuffer) {
        return ParseString(IncomingBuffer, 0, 18);
    }

    public static int GetHavePieceNumber(ByteBuffer IncomingBuffer) {
        return (int) (ParseInteger(IncomingBuffer, 5));
    }

    public static int GetPieceMessageNumber(ByteBuffer IncomingBuffer) {
        int result = (int) (ParseInteger(IncomingBuffer, 5));
        return result;
    }

    public static int GetRequestMessageIndex(ByteBuffer IncomingBuffer) {
        return (int) (ParseInteger(IncomingBuffer, 5));
    }

    public static int handleHandshakeMessage(ByteBuffer IncomingBuffer) {
        // The peerID is 4 bytes, located at
        int handshakeFrom = GetHandshakePeerID(IncomingBuffer);
        //System.out.println("Received a Handshake Message from peer " + handshakeFrom);
        // System.out.println(" Message String was [" +
        // GetHandshakeString(IncomingBuffer) + "]");
        // System.out.println(" The first byte value is " + ParseByte(IncomingBuffer,
        // 0));
        return handshakeFrom;
    }

    // type 0
    private static void handleChokeMessage(peerProcess pp, int senderPeer) {
        //System.out.println("Choked by " + senderPeer);

        RemotePeerInfo sender = pp.getRemotePeerInfo(senderPeer);
        if (sender == null) {
            //System.out.println("remote peer with id " + senderPeer + " info not found");
            return;
        }

        pp.logger.onChoking(senderPeer);
    }

    // type 1
    private static void handleUnchokeMessage(peerProcess pp, int senderPeer) {

        //System.out.println("Unchoked by " + senderPeer);

        RemotePeerInfo sender = pp.getRemotePeerInfo(senderPeer);
        if (sender == null) {
            //System.out.println("remote peer with id " + senderPeer + " info not found");
            return;
        }

        pp.logger.onUnchoking(senderPeer);
        // DONE: request a random piece that the sender has and the receiver doesn't

        pp.pieceMessages.add(createRequestMessage(pp.randomMissingPiece()));
        // ask for this piece
    }

    // type 2
    private static void handleInterestedMessage(peerProcess pp, int senderPeer) {
        //System.out.println("Peer " + senderPeer + " is interested");
        Vector<Integer> interest = pp.getInterested();
        interest.add(senderPeer);
        pp.setInterested(interest);

        pp.logger.onReceiveInterestedMessage(senderPeer);
    }

    // type 3
    private static void handleNotInterestedMessage(peerProcess pp, int senderPeer) {
        //System.out.println("Peer " + senderPeer + " is NOT interested");
        Vector<Integer> interest = pp.getInterested();
        for (int i = 0; i < interest.size(); i++) {
            if (interest.get(i) == senderPeer)
                interest.remove(i);
        }
        pp.setInterested(interest);
        pp.logger.onReceiveNotInterestedMessage(senderPeer);
    }

    // type 4
    private static void handleHaveMessage(peerProcess pp, int senderPeer, ByteBuffer IncomingMessage) {
        int index = GetHavePieceNumber(IncomingMessage);
        RemotePeerInfo sender = pp.getRemotePeerInfo(senderPeer);
        if (sender == null) {
            //System.out.println("getRemotePeer is null");
            return;
        }
        sender.getBitfield().set(index, true); // sets the index to true of the peer that they have this message
        pp.logger.onReceiveHaveMessage(senderPeer, index); // log that we received this comment
                                                           // If the receiver of this message does has the piece
                                                           // that the sender has, then send a not_interested message.
                                                           // Else, send an interested message.
        if (pp.getCurrBitfield().get(index)) { // if this message is already in possession, skip getting it again
            pp.client.sendMessageBB(createNotInterestedMessage());
        } else { // else ask for the message
            pp.client.sendMessageBB(createInterestedMessage());
        }
    }

    // type 5
    private static void handleBitfieldMessage(ByteBuffer IncomingMessage, peerProcess pp, int senderPeer, int length) {
        // if the payload is empty, then the sender has no pieces.
        boolean nowInterested = false;
        RemotePeerInfo rpi = pp.getRemotePeerInfo(senderPeer);
        if (length == 0) {
            return;
        } else {
            for (int i = 0; i < pp.getTotalPieces(); i++) {
                int x = i / 8;
                int y = i % 8;
                // if 8 bits, i = 7 and x is 0, if 9th bit i=8 and x = 1
                int bitvalue = ((int) IncomingMessage.array()[x + 5] );
                bitvalue = (bitvalue >> (y-1)) & 1;
                // If the sender has a piece that this process does not, then this process will
                // send an interested message. Otherwise, send an uninterested message.
                if ((pp.getCurrBitfield().get(i) == false) && (bitvalue == 1)) {
                    nowInterested = true;
                    // break; // DO NOT Break, instead keep loading in and tracking the pieces this
                    // peer has in it's posession
                }
                if (rpi == null) {
                   // System.out.println("ERROR: could not find peer info with id " + senderPeer);
                    return;
                }
                // sets the index i to true of the peer that they have this piece
                rpi.getBitfield().set(i, bitvalue == 1);
            }
        }
        pp.logger.log( "Received bitfield from " + senderPeer + ": " + pp.printBitfield(rpi.getBitfield()) + "\n");
        //System.out.println("The interest of " + pp.getPeerId() + " in " + senderPeer + " is set to " + nowInterested);

        if (nowInterested) {
            pp.messagesToSend.add(Messages.createInterestedMessage());
        } else {
            pp.messagesToSend.add(Messages.createNotInterestedMessage());

        }
        return;
    }

    // type 6
    private static void handleRequestMessage(peerProcess pp, int senderPeer, ByteBuffer IncomingMessage) {
        // a peer (senderPeer) has requested (payload) index message
        FileHandling f = pp.getFileObject(); // DONE: if the receiver of the message has the piece, then send the piece

        // parse out the requested item into an integer to look up in the map structure
        int index = GetRequestMessageIndex(IncomingMessage);

        pp.logger.log("Peer " + senderPeer + " has requested piece " + index + "\n"); // debug statement. remove this later.

        if (f.CheckForPieceNumber(index)) { // if we actually have this piece in the stored location...
            ByteBuffer ThePiece;
            int ThePieceLength;

            ThePiece = pp.FileObject.MakeCopyPieceByteBuffer(index);
            // get a copy of the piece
            ThePieceLength = pp.FileObject.GetPieceSize(index); // get the piece's length
            ByteBuffer toSend = createPieceMessage(ThePiece, index, ThePieceLength);
		    pp.logger.log("Send piece " + index + "=" + GetPieceMessageNumber(toSend) + ".\n"); //debug log. Remove this later.
            pp.pieceMessages.add(toSend); // send the piece
        } else {
            //System.out.println("Some questionable character/actor identified as " + senderPeer + " asked for piece "
                    //+ index + " but this peer known as " + pp.peerId + " does not have it...");
        }
    }

    // type 7
    private static void handlePieceMessage(peerProcess pp, int senderPeer, int length, ByteBuffer IncomingMessage) {
        //System.out.println("Receive piece message");

        pp.pieceMessages.add(createRequestMessage(pp.randomMissingPiece()));
        int index = GetPieceMessageNumber(IncomingMessage);
        pp.logger.log("Received piece " + index + '\n');
        // Done: write the piece to a file (wherever it should be written, idk) See
        // Below, handles logging of the received piece
        ByteBuffer GrabPieceData = ByteBuffer.allocate(65536); // Message is longer
        GrabPieceData.put(Arrays.copyOfRange(IncomingMessage.array(), 9, length - 9)); // Get the piece
        pp.FileObject.ReceivedAPiece(index, GrabPieceData, length - 9); // insert into the File Handler

        // TODO: What do they mean by "partial files" maintained in current directory?
        // Are we supposed to support 100GB file transfers and cache to the drive?
        // TODO: Santosh - I negated this condition, not sure what its supposed to be
        // doing
        if (!pp.FileObject.CheckForAllPieces()) {

            StringBuilder filenameWrite = new StringBuilder();
            filenameWrite.append(String.format("./peer_%04d/thefile", pp.peerId));

            pp.FileObject.WriteFileOut(filenameWrite.toString());

        }

        RemotePeerInfo rpi = pp.getRemotePeerInfo(senderPeer);
        if (rpi == null) {
            pp.logger.log("ERROR: could not find peer info with id " + senderPeer);
            return;
        }
        rpi.incrementPiecesTransmitted();
        // it may be the case that the peer already has the piece, so it's not new.
        boolean isNewPiece = !pp.getCurrBitfield().get(index);
        // update the bitfield
        pp.getCurrBitfield().set(index, true);
        if (isNewPiece) {
            pp.incrementCollectedPieces();
        }
        pp.logger.onDownloadingAPiece(senderPeer, index, pp.getCollectedPieces());
        // log if this process now has the entire file
        if (pp.hasFile()) {
            pp.logger.onCompletionOfDownload();
            System.out.println("No longer interested");
            pp.messagesToSend.add(createNotInterestedMessage());
            pp.pieceMessages.clear();
        }

        updateInterestedStatus(pp);
        pp.logger.log(pp.printBitfield(pp.bitfield) + "\n");
    }

    // Whenever a peer receives a piece completely, it checks the bitfields of
    // its neighbors and decides whether it should send ‘not interested’ messages to
    // some neighbors.
    public static void updateInterestedStatus(peerProcess pp) {
        for (int neighborId : pp.preferredNeighbors) {
            RemotePeerInfo preferredNeighbor = pp.getRemotePeerInfo(neighborId);
            if (preferredNeighbor != null && !pp.checkInterested(preferredNeighbor.getBitfield())) {
                System.out.println("Not interested anymore");
                pp.messagesToSend.add(createNotInterestedMessage());
            } else if (preferredNeighbor == null) {
                //System.out.println("ERROR: could not find remote peer");
            }
        }
        if (pp.optimisticallyUnchokedPeer != -1) {
            RemotePeerInfo optimisticallyUnchoked = pp.getRemotePeerInfo(pp.optimisticallyUnchokedPeer);
            if (optimisticallyUnchoked != null && !pp.checkInterested(optimisticallyUnchoked.getBitfield())) {
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
        // if the message starts with the handShake header, then it's a handshake
        // message

        if (IncomingMessage.remaining() >= 32) {
            if (GetHandshakeString(IncomingMessage).equals(handshakeHeader)) {
                pp.logger.onConnectingTo(senderPeer);

                return handleHandshakeMessage(IncomingMessage);
            }
        }

        /*
         * if it's not a handshake message then it's an actual message. This is the
         * format:
         * 4-byte message length field (length is in bytes)
         * 1-bit message type
         * message payload
         */
        int length = GetMessageLength(IncomingMessage);
        int type = GetMessageType(IncomingMessage);

        // The logic for handling the message types are here
        if (type == MessageType.CHOKE.ordinal()) {
            // type 0
            handleChokeMessage(pp, senderPeer);
        } else if (type == MessageType.UNCHOKE.ordinal()) {
            // type 1
            handleUnchokeMessage(pp, senderPeer);
        } else if (type == MessageType.INTERESTED.ordinal()) {
            // type 2
            handleInterestedMessage(pp, senderPeer);
        } else if (type == MessageType.NOT_INTERESTED.ordinal()) {
            // type 3
            handleNotInterestedMessage(pp, senderPeer);
        } else if (type == MessageType.HAVE.ordinal()) {
            // type 4
            handleHaveMessage(pp, senderPeer, IncomingMessage);
        } else if (type == MessageType.BITFIELD.ordinal()) {
            // type 5
            handleBitfieldMessage(IncomingMessage, pp, senderPeer, length);
        } else if (type == MessageType.REQUEST.ordinal()) {
            // type 6
            handleRequestMessage(pp, senderPeer, IncomingMessage);
        } else if (type == MessageType.PIECE.ordinal()) {
            // type 7
            handlePieceMessage(pp, senderPeer, length, IncomingMessage);
        } else {
            //System.out.println("Invalid message of type " + ParseByte(IncomingMessage, 4));
            //System.out.println(StandardCharsets.UTF_8.decode(IncomingMessage).toString());
        }

        return -1;
    }

    public static String HexPrint(ByteBuffer bytes) {
        // modifed from idea at https://mkyong.com/java/java-how-to-convert-bytes-to-hex/ By mkyong
        StringBuilder result = new StringBuilder();
        for (int x = 0; x < bytes.remaining(); ++x) {
            result.append(String.format("%02x ", bytes.array()[x]));
            // upper case
            // result.append(String.format("%02X", aByte));
        }
        return result.toString();
    }

}
