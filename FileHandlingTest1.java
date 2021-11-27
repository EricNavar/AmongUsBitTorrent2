import java.nio.*;
import java.util.*;
import java.math.BigInteger;

// https://www.w3schools.com/java/java_files_create.asp examples utilized as basis for creating file i/o code
// idea of file output streams came from https://www.techiedelight.com/how-to-write-to-a-binary-file-in-java/

public class FileHandlingTest1{
	
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
    private static String encodeType(int type) {
        return padWithZeroes(Integer.toBinaryString(type), 8);
    }

    // takes an integer and creates a binary string of the specified length in bytes
    public static String integerToBinaryString(int i, int length) {
        return padWithZeroes(Integer.toBinaryString(i), length * 8);
    }



public static void main(String args[]) {
	
	    int pieceSize = 16384;
		int numPieces = 1484;
		int thisPieceSize;
		
		FileHandling FirstObject  = new FileHandling(1001, numPieces, pieceSize, "thefile");
		FileHandling SecondObject = new FileHandling(1002, numPieces, pieceSize, "thefile");
		
		System.out.println(" ");		
		System.out.println("This program will test File Handling for it's feature set.");		
		System.out.println(" ");		
		System.out.println("FirstObject Check for All Pieces " + FirstObject.CheckForAllPieces());
		System.out.println("FirstObject Check for Piece Mumber 1 " + FirstObject.CheckForPieceNumber(1));
		System.out.println("SecondObject Check for All Pieces " + SecondObject.CheckForAllPieces());
		System.out.println("SecondObject Check for Piece Mumber 1 " + SecondObject.CheckForPieceNumber(1));
		System.out.println(" ");		
		System.out.println("Now reading the entire file tree.jpg into the FirstObject");
		System.out.println(" ");		
		FirstObject.ReadFileIn("project_config_file_large/1001/tree.jpg");
		System.out.println("FirstObject Check for All Pieces " + FirstObject.CheckForAllPieces());
		System.out.println("FirstObject Check for Piece Mumber 1 " + FirstObject.CheckForPieceNumber(1));
		System.out.println("SecondObject Check for All Pieces " + SecondObject.CheckForAllPieces());
		System.out.println("SecondObject Check for Piece Mumber 1 " + SecondObject.CheckForPieceNumber(1));
		System.out.println(" ");		
		
		
		//for(int i = 0; i < FirstObject.totalPieces; i++)
		for(int i = FirstObject.totalPieces-1; i >= 0; i--)
		{
		   //System.out.println("Retrieving Piece Number " + i + " Into the Local Byte Buffer of FirstObject " + FirstObject.loadLocalByteBufferPieceNumber(i));
		   ByteBuffer myBuffer; 
		   FirstObject.loadLocalByteBufferPieceNumber(i);
		   myBuffer      = FirstObject.getCurrentLocalByteBuffer();
		   thisPieceSize = FirstObject.getCurrentLocalByteBufferPieceSize();
		   //System.out.println(myBuffer.get(0) + " " + myBuffer.get(1) + " " + myBuffer.get(1370));
		   SecondObject.ReceivedAPiece(i, myBuffer, thisPieceSize);
		   if (i == 23)   {
			   FirstObject.printLocalBuffer(512);
		   }
		   if (i == (FirstObject.totalPieces-1)) { 
        		System.out.println("Piece Number " + (FirstObject.totalPieces-1) + " is " + SecondObject.GetPieceSize(FirstObject.totalPieces-1) + " bytes in length.\n");
		   }
		}
		
		System.out.println(" ");		
		System.out.println("FirstObject Check for All Pieces " + FirstObject.CheckForAllPieces());
		System.out.println("FirstObject Check for Piece Mumber 1 " + FirstObject.CheckForPieceNumber(1));
		System.out.println("SecondObject Check for All Pieces " + SecondObject.CheckForAllPieces());
		System.out.println("SecondObject Check for Piece Mumber 1 " + SecondObject.CheckForPieceNumber(1));
		System.out.println(" ");		
		FirstObject.WriteFileOut("UnitTest/TreeCopy0.jpg");
		FirstObject.WriteFileOut("UnitTest/TreeCopy1.jpg");
		SecondObject.WriteFileOut("UnitTest/TreeCopy2.jpg");
		SecondObject.WriteFileOut("UnitTest/TreeCopy3.jpg");
		
		//System.out.println(" ");		
	    //String messageToSend = Messages.createHandshakeMessageOld_DO_NOT_USE(1001);
        //System.out.println("Handshake Message is  [" + messageToSend + "]");
		//System.out.println("           and has a length of " + messageToSend.length() + " bytes.\n");
		
		System.out.println(" ");		
	    ByteBuffer messageToSendBB = Messages.createHandshakeMessage(1001);
		String getData = new String(messageToSendBB.array());
        System.out.println("Handshake Message Byte Buffer is  [" + getData + "] and");
		messageToSendBB.flip();
		System.out.println("in Hex it is [" + Messages.HexPrint(messageToSendBB)+ "]");
		System.out.println("           and has a length of " + messageToSendBB.remaining() + " bytes.\n");
		
		System.out.println(" ");		
		Messages.handleHandshakeMessage(messageToSendBB);
		
		System.out.println(" ");		
        ByteBuffer messageToSend;
        messageToSend = Messages.createChokeMessage();
		messageToSend.flip();
		System.out.println("Choke Message is  [" + Messages.HexPrint(messageToSend)  + "] and has a length of " + messageToSend.remaining() + " bytes.\n");
		
		System.out.println(" ");	
        messageToSend = Messages.createUnchokeMessage();
		messageToSend.flip();
		System.out.println("UnChoke Message is  [" + Messages.HexPrint(messageToSend)  + "] and has a length of " + messageToSend.remaining() + " bytes.\n");
		
		System.out.println(" ");;
        messageToSend = Messages.createInterestedMessage();
		messageToSend.flip();
		System.out.println("Interested Message is  [" + Messages.HexPrint(messageToSend)  + "] and has a length of " + messageToSend.remaining() + " bytes.\n");
		
		System.out.println(" ");	
        messageToSend = Messages.createNotInterestedMessage();
		messageToSend.flip();
		System.out.println("Not Interested Message is  [" + Messages.HexPrint(messageToSend)  + "] and has a length of " + messageToSend.remaining() + " bytes.\n");

		System.out.println(" ");	
        messageToSend = Messages.createHaveMessage(13);
		messageToSend.flip();
		System.out.println("Have Message for Piece Number 13 is  [" + Messages.HexPrint(messageToSend)  + "] and has a length of " + messageToSend.remaining() + " bytes.\n");

		System.out.println(" ");	
        messageToSend = Messages.createRequestMessage(13);
		messageToSend.flip();
		System.out.println("Request Message for Piece Number 13 is  [" + Messages.HexPrint(messageToSend)  + "] and has a length of " + messageToSend.remaining() + " bytes.\n");
		
		
		ByteBuffer MessagePayloadAssemblyBB = ByteBuffer.allocate(70);  // Handshake Messages are 32 byte payload messages
        String PayloadInfo = "01234567890123456789  Payload 0123456789012345678912";
		MessagePayloadAssemblyBB.put(PayloadInfo.getBytes());
        messageToSend = Messages.createPieceMessage(MessagePayloadAssemblyBB, 13, 52);  // payload has 42 characters and is piece number 13 to be sent...
		messageToSend.flip();
		System.out.println("Create Piece to Send Message for Piece Number 13 is  [" + Messages.HexPrint(messageToSend)  + "] and has a length of " + messageToSend.remaining() + " bytes.\n");
		
		int peerId = 1001;
	    peerProcess pp = new peerProcess(peerId);	
        int totalPieces = (int) Math.ceil((double) 24301474 / 16394);
 
		System.out.println(" ");	
        totalPieces = (int) 12;
        pp.bitfield.setSize(totalPieces);
		Random randomnum = new Random();
        for (int i = 0; i < pp.bitfield.size(); i++) {
			boolean hasfile = false;;
			if ((randomnum.nextInt() & 0x01) == 0x01) {
				hasfile = true;
			}
            pp.bitfield.set(i, hasfile);
        }
        messageToSend = Messages.createBitfieldMessage(pp.bitfield);
		messageToSend.flip();
		System.out.println("Create BitField Message for " + totalPieces + " pieces  [" + Messages.HexPrint(messageToSend)  + "] and has a length of " + messageToSend.remaining() + " bytes.\n");
	
 
		System.out.println(" ");	
        totalPieces = (int) 32;
        pp.bitfield.setSize(totalPieces);
        for (int i = 0; i < pp.bitfield.size(); i++) {
			boolean hasfile = false;;
			if ((randomnum.nextInt() & 0x01) == 0x01) {
				hasfile = true;
			}
            pp.bitfield.set(i, hasfile);
        }
        messageToSend = Messages.createBitfieldMessage(pp.bitfield);
		messageToSend.flip();
		System.out.println("Create BitField Message for " + totalPieces + " pieces  [" + Messages.HexPrint(messageToSend)  + "] and has a length of " + messageToSend.remaining() + " bytes.\n");		
 
		System.out.println(" ");	
        totalPieces = (int) 254;
        pp.bitfield.setSize(totalPieces);
        for (int i = 0; i < pp.bitfield.size(); i++) {
			boolean hasfile = false;;
			if ((randomnum.nextInt() & 0x01) == 0x01) {
				hasfile = true;
			}
            pp.bitfield.set(i, hasfile);
        }
        messageToSend = Messages.createBitfieldMessage(pp.bitfield);
		messageToSend.flip();
		System.out.println("Create BitField Message for " + totalPieces + " pieces  [" + Messages.HexPrint(messageToSend)  + "] and has a length of " + messageToSend.remaining() + " bytes.\n");

		System.out.println(" ");	
		System.out.println(" ");	
		FirstObject.Shutdown();
		SecondObject.Shutdown();
		
	}
	

}