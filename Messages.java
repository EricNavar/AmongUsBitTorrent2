import java.math.BigInteger;

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

    static void decodeMessage(String binary) {
        System.out.println( binaryToString(binary) ); //debug message
        String handshakeHeader = stringToBinary("P2PFILESHARINGPROJ");
        // if the message starts with the handShake header, then it's a handshake message
        if (binary.substring(0,144) == handshakeHeader) {
            // (18 + 10) * 8 = 336 is the bit where the peerId starts. The peerId is 4 bytes.
            int handshakeFrom = Integer.parseInt(binary.substring(336, 368));
            System.out.println("Handshake message from peer " + Integer.toString(handshakeFrom));
            return;
        }
        /* if it's not a handshake message then it's an actual message. This is the format:
         * 4-byte message length field (length is in bytes)
         * 1-bit message type
         * message payload
         */
        int length = Integer.parseInt(binary.substring(0,32));
        int type = Integer.parseInt(binary.substring(32,40));
        String payload = binary.substring(40,length * 8);
        if (type == MessageType.HAVE.ordinal()) {
            int index = Integer.parseInt(payload); //TODO: do something with this
        }

    }
}
