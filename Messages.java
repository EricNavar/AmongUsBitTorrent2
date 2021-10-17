import java.math.BigInteger;
import java.util.Vector;

// This class is to hold the logic for creating and decoding messages.
public class Messages {
    // this function is to take a string of binary and pad it on the left with
    // zeroes such that it is a certain length.
    static String padWithZeroes(String s, int length) {
        for (int i = s.length(); i < length; i++) {
            s = "0" + s;
        }
        return s;
    }

    // ALL MESSAGES SHOULD BE SENT AS A BINARY STRING

    // The following 2 methods use this logic: 
    // https://stackoverflow.com/questions/4416954/how-to-convert-a-string-to-a-stream-of-bits-in-java/4417069
    static String stringToBinary(String s) {
        return new BigInteger(s.getBytes()).toString(2);
    }

    static String binaryToString(String b) {
        return new String(new BigInteger(b, 2).toByteArray());
    }

    // ==============================================================
    // ====================== MESSAGE CREATORS ======================
    // ==============================================================
    static String createHandshakeMessage(int peerId) {
        // This code is the best
        String result = stringToBinary("P2PFILESHARINGPROJ");
        // Add 10 bytes of zeroes
        result = result + "00000000000000000000000000000000000000000000000000000000000000000000000000000000";
        result = result + padWithZeroes(Integer.toBinaryString(peerId), 32);
        return result;
    }

    // no body in message
    static String createChokeMessage() {
        String result = padWithZeroes(Integer.toBinaryString(0), 32);
        result = result + padWithZeroes(Integer.toBinaryString(MessageType.CHOKE.ordinal()), 8);
        return result;
    }

    // no body in message
    static String createUnchokeMessage() {
        String result = padWithZeroes(Integer.toBinaryString(0), 32);
        result = result + padWithZeroes(Integer.toBinaryString(MessageType.UNCHOKE.ordinal()), 8);
        return result;
    }

    // no body in message
    static String createInterestedMessage() {
        String result = padWithZeroes(Integer.toBinaryString(0), 32);
        result = result + padWithZeroes(Integer.toBinaryString(MessageType.INTERESTED.ordinal()), 8);
        return result;
    }

    // no body in message
    static String createNotInterestedMessage() {
        String result = padWithZeroes(Integer.toBinaryString(0), 32);
        result = result + padWithZeroes(Integer.toBinaryString(MessageType.NOT_INTERESTED.ordinal()), 8);
        return result;
    }

    static String createHaveMessage(int payload) {
        String result = padWithZeroes(Integer.toBinaryString(0), 32);
        result = result + padWithZeroes(Integer.toBinaryString(MessageType.HAVE.ordinal()), 8);
        // result = result + Integer.toBinaryString(payload);

        return result;
    }

    static String createBitfieldMessage(int payload) {
        String result = padWithZeroes(Integer.toBinaryString(0), 32);
        result = result + padWithZeroes(Integer.toBinaryString(MessageType.BITFIELD.ordinal()), 8);
        return result;
    }

    static String createRequestMessage(int payload) {
        String result = padWithZeroes(Integer.toBinaryString(0), 32);
        result = result + padWithZeroes(Integer.toBinaryString(MessageType.REQUEST.ordinal()), 8);
        return result;
    }

    static String createPieceMessage(int payload) {
        String result = padWithZeroes(Integer.toBinaryString(0), 32);
        result = result + padWithZeroes(Integer.toBinaryString(MessageType.PIECE.ordinal()), 8);
        return result;
    }
    // ==============================================================
    // ====================== MESSAGE HANDLERS ======================
    // ==============================================================


    static void handleHandshakeMessage(String binary, peerProcess pp, int senderPeer) {
        // (18 + 10) * 8 - 1 = 223 is the bit where the peerId starts. The peerId is 4 bytes.
        int handshakeFrom = Integer.parseInt(binary.substring(223, 255),2);
        System.out.println("Handshake message from peer " + handshakeFrom);
        return;
    }

    static void handleChokeMessage(String binary, peerProcess pp, int senderPeer) { //type 0
        pp.getRemotePeerInfo(senderPeer).setChoked(true);
    }

    static void handleUnchokeMessage(String binary, peerProcess pp, int senderPeer) { //type 1
        pp.getRemotePeerInfo(senderPeer).setChoked(false);
    }

    static void handleInterestedMessage(String binary, peerProcess pp, int senderPeer) { //type 2
        pp.getRemotePeerInfo(senderPeer).setInterested(true);
    }

    static void handleNotInterestedMessage(String binary, peerProcess pp, int senderPeer) { //type 3
        pp.getRemotePeerInfo(senderPeer).setInterested(false);
    }

    static void handleHaveMessage(String binary, peerProcess pp, int senderPeer, String payload) { //type 4
        int index = Integer.parseInt(payload);
        pp.getRemotePeerInfo(senderPeer).getBitfield().set(index, true);
        // TODO: if the receiver of this message does not have the piece
        // that the sender has, then maybe send an interested message.
    }

    static void handleBitfieldMessage(String binary, peerProcess pp, int senderPeer, int length, String payload) { //type 5
        Vector<Boolean> bitfield = new Vector<Boolean>(pp.pieceCount);
        // if the payload is empty, then the sender has no pieces. 
        if (length == 0) {
            for (int i = 0; i < payload.length(); i++) {
                bitfield.set(i, false);
            }
        }
        else {
            for (int i = 0; i < payload.length(); i++) {
                bitfield.set(i, payload.charAt(i) == '1');
            }
        }
        pp.getRemotePeerInfo(senderPeer).setBitfield(bitfield);
    }

    static void handleRequestMessage(String binary, peerProcess pp, int senderPeer, String payload) { //type 6
        int index = Integer.parseInt(payload);
        // TODO: if the receiver of the message has the piece and the sender is not
        // choked, then send the piece
    }

    static void handlePieceMessage(String binary, peerProcess pp, int senderPeer, int length, String payload) { //type 7
        int index = Integer.parseInt(payload.substring(0,32),2);
        String piece = binaryToString(payload.substring(32,length-32));
        // TODO: write the piece to a file (wherever it should be written, idk)

        // update the bitfield
        pp.bitfield.set(index,true); 
    }


    static void decodeMessage(String binary, peerProcess pp, int senderPeer) {
        String handshakeHeader = stringToBinary("P2PFILESHARINGPROJ");
        // if the message starts with the handShake header, then it's a handshake message
        if (binary.substring(0,143).equals(handshakeHeader)) {
            handleHandshakeMessage(binary, pp, senderPeer);
        }
        /* if it's not a handshake message then it's an actual message. This is the format:
         * 4-byte message length field (length is in bytes)
         * 1-bit message type
         * message payload
         */
        int length = Integer.parseInt(binary.substring(0,32));
        int type = Integer.parseInt(binary.substring(32,40));
        String payload = binary.substring(40,length * 8);

        // The logic for handling the message types are here
        if (type == MessageType.CHOKE.ordinal()) { //type 0
            handleChokeMessage(binary, pp, senderPeer);
        }
        else if (type == MessageType.UNCHOKE.ordinal()) { //type 1
            handleUnchokeMessage(binary, pp, senderPeer);
        }
        else if (type == MessageType.INTERESTED.ordinal()) { //type 2
            handleInterestedMessage(binary, pp, senderPeer);
        }
        else if (type == MessageType.NOT_INTERESTED.ordinal()) { //type 3
            handleNotInterestedMessage(binary, pp, senderPeer);
        }
        else if (type == MessageType.HAVE.ordinal()) { //type 4
            handleHaveMessage(binary, pp, senderPeer, payload);
        }
        else if (type == MessageType.BITFIELD.ordinal()) { //type 5
            handleBitfieldMessage(binary, pp, senderPeer, length, payload);
        }
        else if (type == MessageType.REQUEST.ordinal()) { //type 6
            handleRequestMessage(binary, pp, senderPeer, payload);
        }
        else if (type == MessageType.PIECE.ordinal()) { //type 7
            handlePieceMessage(binary, pp, senderPeer, length, payload);
        }
        else {
            System.out.println("Invalid message type");
        }
    }
}
