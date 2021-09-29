// This class is to hold the logic for creating and decoding messages.
public class Messages {
    // this function is to take a string of binary and pad it on the left with
    // zeroes such that it is a certain length.
    static String padWithZeroes(String s, int length) {
        return s + "0".repeat(length - s.length());
    }

    static String createHandshakeMessage(int peerId) {
        // This code is the best
        String result = "P2PFILESHARINGPROJ";
        char c = 0;
        for (int i = 0; i < 10; i++) {
            result = result + c;
        }
        result = result + Integer.toBinaryString(peerId);
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

    }
}
