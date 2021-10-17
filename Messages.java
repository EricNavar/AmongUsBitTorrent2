import java.math.BigInteger;
import java.util.Vector;

// This class is to hold the logic for creating and decoding messages.
public class Messages {
    // this function is to take a string of binary and pad it on the left with
    // zeroes such that it is a certain length.
    private static String padWithZeroes(String s, int length) {
        for (int i = s.length(); i < length; i++) {
            s = "0" + s;
        }
        return s;
    }

    // ALL MESSAGES SHOULD BE SENT AS A BINARY STRING

    // The following 2 methods use this logic: 
    // https://stackoverflow.com/questions/4416954/how-to-convert-a-string-to-a-stream-of-bits-in-java/4417069
    private static String stringToBinary(String s) {
        return new BigInteger(s.getBytes()).toString(2);
    }

    private static String binaryToString(String b) {
        return new String(new BigInteger(b, 2).toByteArray());
    }

    // ==============================================================
    // ====================== MESSAGE CREATORS ======================
    // ==============================================================
    public static String createHandshakeMessage(int peerId) {
        // This code is the best
        String result = stringToBinary("P2PFILESHARINGPROJ");
        // Add 10 bytes of zeroes
        result = result + "00000000000000000000000000000000000000000000000000000000000000000000000000000000";
        result = result + padWithZeroes(Integer.toBinaryString(peerId), 32);
        return result;
    }

    // no body in message
    public static String createChokeMessage() {
        String result = padWithZeroes(Integer.toBinaryString(0), 32);
        result = result + padWithZeroes(Integer.toBinaryString(MessageType.CHOKE.ordinal()), 8);
        return result;
    }

    // no body in message
    public static String createUnchokeMessage() {
        String result = padWithZeroes(Integer.toBinaryString(0), 32);
        result = result + padWithZeroes(Integer.toBinaryString(MessageType.UNCHOKE.ordinal()), 8);
        return result;
    }

    // no body in message
    public static String createInterestedMessage() {
        String result = padWithZeroes(Integer.toBinaryString(0), 32);
        result = result + padWithZeroes(Integer.toBinaryString(MessageType.INTERESTED.ordinal()), 8);
        return result;
    }

    // no body in message
    public static String createNotInterestedMessage() {
        String result = padWithZeroes(Integer.toBinaryString(0), 32);
        result = result + padWithZeroes(Integer.toBinaryString(MessageType.NOT_INTERESTED.ordinal()), 8);
        return result;
    }

    public static String createHaveMessage(int payload) {
        String result = padWithZeroes(Integer.toBinaryString(0), 32);
        result = result + padWithZeroes(Integer.toBinaryString(MessageType.HAVE.ordinal()), 8);
        // result = result + Integer.toBinaryString(payload);

        return result;
    }

    public static String createBitfieldMessage(int payload) {
        String result = padWithZeroes(Integer.toBinaryString(0), 32);
        result = result + padWithZeroes(Integer.toBinaryString(MessageType.BITFIELD.ordinal()), 8);
        return result;
    }

    public static String createRequestMessage(int payload) {
        String result = padWithZeroes(Integer.toBinaryString(0), 32);
        result = result + padWithZeroes(Integer.toBinaryString(MessageType.REQUEST.ordinal()), 8);
        return result;
    }

    public static String createPieceMessage(int payload) {
        String result = padWithZeroes(Integer.toBinaryString(0), 32);
        result = result + padWithZeroes(Integer.toBinaryString(MessageType.PIECE.ordinal()), 8);
        return result;
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

    /* CHOKE AND UNCHOKE (description from requirements sheet)
        The number of concurrent connections on which a peer uploads its pieces is limited. At a
        moment, each peer uploads its pieces to at most k preferred neighbors and 1 optimistically
        unchoked neighbor. The value of k is given as a parameter when the program starts. Each
        peer uploads its pieces only to preferred neighbors and an optimistically unchoked
        neighbor. We say these neighbors are unchoked and all other neighbors are choked.
        Each peer determines
        preferred neighbors every p seconds. Suppose that the unchoking
        interval is p . Then every p seconds, peer A reselects its preferred neighbors. To make
        the decision, peer A calculates the downloading rate from each of its neighbors,
        respectively, during the previous unchoking interval. Among nei ghbors that are interested
        in its data, peer A picks k neighbors that has fed its data at the highest rate. If more than
        two peers have the same rate, the tie should be broken randomly. Then it unchokes those
        preferred neighbors by sending ‘unchokeunchoke’ messag es and it expects to receive ‘requestrequest’ messages from them. If a preferred neighbor is already unchoked, then peer A does not
        have to send ‘unchokeunchoke’ message to it. All other neighbors previously unchoked but not
        selected as preferred neighbors at this time
        selected as preferred neighbors at this time should be choked unless it is an optimistically
        should be choked unless it is an optimistically unchoked neighbor. To choke those neighbors,
        peer A sends unchoked neighbor. To choke those neighbors, peer A sends ‘chokechoke’ messages to
        them messages to them and stop sending pieces.and stop sending pieces.

        If peer A has a complete file, it determines
        preferred neighbors randomly among those
        that are interested in its data rather than comparing downloading rates.
        Each peer determines an optimistically unchoked neighbor every
        m seconds. We say m
        is the optimistic unchoking interval. Every m seconds, peer A reselects an optimistica lly
        unchoked neighbor randomly among neighbors that are choked at that moment but are
        interested in its data. Then peer A sends ‘unchokeunchoke’ message to the selected neighbor and
        it expects to receive ‘requestrequest’ messages from it.
        Suppose that peer C is randoml
        y chosen as the optimistically unchoked neighbor of peer
        A. Because peer A is sending data to peer C, peer A may become one of peer C C’s
        preferred neighbors, in which case peer C would start to send data to peer A. If the rate
        at which peer C sends data to peer A is high enough, peer C could then, in turn, become
        one of peer A A’s preferred neighbors. Note that in this case, peer C may be a preferred
        neighbor and optimistically unchoked neighbor at the same time. This kind of situation is
        allowed. In the next optimistic unchoking interval, another peer will be selected as an
        optimistically unchoked neighbor.
    */

    //type 0
    private static void handleChokeMessage(String binary, peerProcess pp, int senderPeer) {
        pp.getRemotePeerInfo(senderPeer).setChoked(true);
        pp.logger.onChoking(senderPeer);
    }

    //type 1
    private static void handleUnchokeMessage(String binary, peerProcess pp, int senderPeer) {
        pp.getRemotePeerInfo(senderPeer).setChoked(false);
        pp.logger.onUnchoking(senderPeer);
    }

    /* INTERESTED AND NOT INTERESTED
        Regardless of the connection state of choked or unchoked, if a neighbor has some
        interesting pieces, then a peer sends "interested" message to the neighbor. Whenever a
        peer receives a ‘bitfieldbitfield’ or ‘havehave’ message from a neighbor, it determines whether it
        should send an ‘interestedinterested’ message to the neighbor. For example, suppose that peer A
        makes a connection to peer B and receiv es a ‘bitfieldbitfield’ message that shows peer B has
        some pieces not in peer A. Then peer A sends an ‘interestedinterested’ message to peer B. In
        another example, suppose that peer A receives a ‘havehave’ message from peer C that
        contains the index of a piece not in peer A. T hen peer A sends an ‘interestedinterested’ message
        to peer C.
        Each peer maintains bitfields for all neighbors and updates them whenever it receives
        "have" messages from its neighbors. If a neighbor does not have any interesting pieces,
        then the peer sends a "not interested" message to the neighbor. Whenever a peer receives
        a piece completely, it checks a piece completely, it checks the the bitfields of its neighbors
        and decide bitfields of its neighbors and decides whether it should it should send send
        "not interested" messages to some neighbors.messages to some neighbors.
    */

    //type 2
    private static void handleInterestedMessage(String binary, peerProcess pp, int senderPeer) {
        pp.getRemotePeerInfo(senderPeer).setInterested(true);
        pp.logger.onReceiveInterestedMessage(senderPeer);
    }

    //type 3
    private static void handleNotInterestedMessage(String binary, peerProcess pp, int senderPeer) {
        pp.getRemotePeerInfo(senderPeer).setInterested(false);
        pp.logger.onReceiveNotInterestedMessage(senderPeer);
    }

    /* REQUEST AND PIECE
        When a connection is unchoked by a neighbor, a peer sends a "request" message for
        requesting a piece that it does not have and has not requested from other neighbors.
        Suppose that peer A receives an "unchoke" message from peer B. Peer A selects a piece
        ran domly among the pieces that peer B has and peer A does not have, and peer A has
        not requested yet. Note that we use a random selection strategy , which is not the rarest
        first strategy usually used in BitTorrent. On r eceiving peer A A’s "request" message, pe er
        B sends a ‘piecepiece’ message that contains the actual piece. After completely downloading
        the piece, peer A sends another "request" message to peer B. The exchange of
        request/piece messages continues until peer A is choked by peer B or peer B does not
        have any more interesting pieces. The next ‘requestrequest’ message should be sent after the
        peer receives the piece message for the previous ‘requestrequest’ message. Note that this
        behavior is different from the pipelining approach of BitTorrent. This is less efficient bu t
        simpler to implement . Note also that you don don’t have to implement the ‘endgame mode mode’
        used in BitTorrent. So we don don’t have the "cancel" message.
        Even though peer A sends a "request" message to peer B, it may not receive a "piece"
        message corresponding to it. This situation happens when peer B re-determines
        preferred neighbors or optimistically unchoked a neighbor and peer A is choked as the
        result before peer B responds to peer A. Your program should consider this case.
    */

    //type 4
    private static void handleHaveMessage(String binary, peerProcess pp, int senderPeer, String payload) {
        int index = Integer.parseInt(payload);
        pp.getRemotePeerInfo(senderPeer).getBitfield().set(index, true);
        pp.logger.onReceiveHaveMessage(senderPeer, index);
        // TODO: if the receiver of this message does not have the piece
        // that the sender has, then maybe send an interested message.
    }

    //type 5
    private static void handleBitfieldMessage(String binary, peerProcess pp, int senderPeer, int length, String payload) {
        Vector<Boolean> bitfield = new Vector<Boolean>(pp.getTotalPieces());
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

    //type 6
    private static void handleRequestMessage(String binary, peerProcess pp, int senderPeer, String payload) {
        int index = Integer.parseInt(payload);
        // TODO: if the receiver of the message has the piece and the sender is not
        // choked, then send the piece
    }

    //type 7
    private static void handlePieceMessage(String binary, peerProcess pp, int senderPeer, int length, String payload) {
        int index = Integer.parseInt(payload.substring(0,32),2);
        String piece = binaryToString(payload.substring(32,length-32));
        // TODO: write the piece to a file (wherever it should be written, idk)
        
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
    public static int decodeMessage(String binary, peerProcess pp) {
        return decodeMessage(binary, pp, -1);
    }

    public static int decodeMessage(String binary, peerProcess pp, int senderPeer) {
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
        return -1;
    }
}
