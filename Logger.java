import java.util.ArrayList;
import java.io.FileOutputStream;

class Logger {
    String peerID;

    public Logger(String peerID) {
        this.peerID = peerID;
    }

    public void onConnectingTo(String time, String peerID2) {
        log(time + ": Peer " + this.peerID + " makes a connection to Peer " + peerID2 + ".");
    }

    public void onConnectingFrom(String time, String peerID2) {
        log(time + ": Peer " + this.peerID + " is connected from Peer " + peerID2 + ".");
    }

    public void onChangeOfPreferredNeighbors(String time, ArrayList<String> preferredNeighbors) {
        StringBuilder toPrint = new StringBuilder(time + ": Peer " + this.peerID + " has the preferred neighbors ");
        for (int i = 0; i < preferredNeighbors.size(); i++) {
            if (i != 0) {
                toPrint.append(", ");
            }
            toPrint.append(preferredNeighbors.get(i));
        }
        toPrint.append(".");
        log(toPrint.toString());
    }

    public void onChangeOfOptimisticallyUnchokedNeighbor(String time, String optimisticallyUnchockedNeighbor_ID) {
        log(time + ": Peer " + this.peerID + "has the optimistically unchoked neighbor "
                + optimisticallyUnchockedNeighbor_ID + ".");
    }

    public void onUnchoking(String time, String peerID2) {
        log(time + ": Peer " + this.peerID + " is unchoked by " + peerID2 + ".");
    }

    // choke me plz
    public void onChoking(String time, String peerID2) {
        log(time + ": Peer " + this.peerID + " is choked by " + peerID2 + ".");
    }

    public void onReceiveHaveMessage(String time, String peerID2, String pieceIndex) {
        log(time + ": Peer " + this.peerID + " received the  ‘have’ message from " + peerID2 + " for the piece "
                + pieceIndex + ".");
    }

    public void onReceiveInterestedMessage(String time, String peerID2) {
        log(time + ": Peer " + this.peerID + " received the ‘interested’ message from " + peerID2 + ".");
    }

    public void onReceiveNotInterestedMessage(String time, String peerID2) {
        log(time + ": Peer " + this.peerID + " received the ‘not interested’ message from " + peerID2 + ".");
    }

    public void onDownloadingAPiece(String time, String peerID2, int piece_index, int number_of_pieces) {
        log(time + ": Peer " + this.peerID + " has downloaded the piece " + String.valueOf(piece_index) + " from "
                + peerID2 + ". Now the number of pieces it has is " + String.valueOf(number_of_pieces) + ".");
    }

    public void onCompletionOfDownload(String time) {
        log(time + ": Peer " + this.peerID + "has downloaded the complete file.");
    }

    public void log(String toPrint) {
        String fileName = "log_peer_" + this.peerID + ".log";
        try {
            FileOutputStream outputStream = new FileOutputStream(fileName);
            byte[] strToBytes = toPrint.getBytes();
            outputStream.write(strToBytes);
            outputStream.close();
        } catch (Exception e) {
            System.out.println("Oops something bad happened i think");
        }
    }
}