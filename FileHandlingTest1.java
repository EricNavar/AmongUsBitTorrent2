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

//import FileHandling.java;
import java.math.BigInteger;
import java.util.Vector;


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

     public static String createHandshakeMessage(int peerId) {
        // This code is the best
        String result = stringToBinary("P2PFILESHARINGPROJ");
        // Add 10 bytes of zeroes
        result = result + "00000000000000000000000000000000000000000000000000000000000000000000000000000000";
        result = result + padWithZeroes(Integer.toBinaryString(peerId), 32);
        return result;
    }

    // message contains no body
    public static String createChokeMessage() {
        return encodeLength(0) + encodeType(MessageType.CHOKE.ordinal());
    }


public static void main(String args[]) {
	
	    int pieceSize = 16384;
		int numPieces = 1484;
		int thisPieceSize;
		
		FileHandling FirstObject  = new FileHandling(1001, numPieces, pieceSize);
		FileHandling SecondObject = new FileHandling(1002, numPieces, pieceSize);
		
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
		
	    String messageToSend = Messages.createHandshakeMessage(1001);
        System.out.println("Handshake Message is  [" + messageToSend + "]");
		System.out.println("           and has a length of " + messageToSend.length() + " bytes.\n");
		messageToSend = createChokeMessage();
		System.out.println("Choke Message is  [" + messageToSend + "] and has a length of " + messageToSend.length() + " bytes.\n");
		FirstObject.Shutdown();
		SecondObject.Shutdown();
	}
	

}