import java.util.ArrayList;

class Logger {
    public void onConnectingTo (String time, String peer_ID_1, String peer_ID_2) {
        log(time + ": Peer " + peer_ID_1 + " makes a connection to Peer " + peer_ID_2 + ".");
    }

    public void onConnectingFrom (String time, String peer_ID_1, String peer_ID_2) {
        log(time + ": Peer " + peer_ID_1 + " is connected from Peer " + peer_ID_2 + ".");
    }

    //could be made quicker with string builders, but who cares
    public void onChangeOfPreferredNeighbors (String time, String peer_ID, ArrayList<String> preferredNeighbors) {
        String toPrint = time + ": Peer " + peer_ID + " has the preferred neighbors ";
        for (int i = 0; i < preferredNeighbors.size(); i++) {
            if (i != 0) {
                toPrint += ", ";
            }
            toPrint += preferredNeighbors.get(i);
        }
        toPrint += ".";
        log(toPrint);
    }

    public void onChangeOfOptimisticallyUnchokedNeighbor(String time, String peer_ID, String optimisticallyUnchockedNeighbor_ID) {
        log(time + ": Peer " + peer_ID + "has the optimistically unchoked neighbor " + optimisticallyUnchockedNeighbor_ID + ".");
    } 

    public void onUnchoking(String time, String peer_ID_1, String peer_ID_2) {
        log(time + ": Peer " + peer_ID_1 + " is unchoked by " + peer_ID_2 + ".");
    }

    public void onChoking(String time, String peer_ID_1, String peer_ID_2) {
        log(time + ": Peer " + peer_ID_1 + " is choked by " + peer_ID_2 + ".");
    }

    public void onReceiveHaveMessage(String time, String peer_ID_1, String peer_ID_2, String pieceIndex) {
        log(time + ": Peer " + peer_ID_1 + " received the  ‘have’ message from " + peer_ID_2 + " for the piece " + pieceIndex + ".");
    }

    public void onReceiveInterestedMessage(String time, String peer_ID_1, String peer_ID_2) {
        log(time + ": Peer " + peer_ID_1 + " received the ‘interested’ message from " + peer_ID_2 + ".");
    }

    public void onReceiveNotInterestedMessage(String time, String peer_ID_1, String peer_ID_2) {
        log(time + ": Peer " + peer_ID_1 + " received the ‘not interested’ message from " + peer_ID_2 + ".");
    }

    public void onDownloadingAPiece(String time, String peer_ID_1, String peer_ID_2, int piece_index, int number_of_pieces) {
        log(time + ": Peer " + peer_ID_1 + " has downloaded the piece " + String.valueOf(piece_index) + " from " + peer_ID_2 + ". Now the number of pieces it has is " + String.valueOf(number_of_pieces) + ".");
    }

    public void onCompletionOfDownload(String time, String peer_ID) {
        log(time + ": Peer " + peer_ID + "has downloaded the complete file.");
    }

    public void log(String toPrint) {
        System.out.println(toPrint);
    }
}