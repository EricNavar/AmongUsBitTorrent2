// This class will be attached to a peer and handle assembling the file pieces, writing them to a file, opening
// them up, clearling partial files, etc...


import java.net.*;
import java.io.*;
import java.nio.*;
import java.io.File;
import java.util.*;

import java.io.FileWriter;   // https://www.w3schools.com/java/java_files_create.asp examples utilized as basis for creating file i/o code
import java.io.FileOutputStream;
                             // idean of file output streams came from https://www.techiedelight.com/how-to-write-to-a-binary-file-in-java/
import java.io.IOException; 
import java.nio.channels.FileChannel;
import java.io.FileOutputStream;

public class FileHandling {
	String fileNameWithPath;
	String pathToFile;
	String peerHandleName;
	int    totalPieces;
	int    pieceSize;
	int    peerID;
	ByteBuffer localByteBuffer;
	// hash table for the binary data
	Map<Integer, ByteBuffer> EntireFile;
	// hash table to keep track of the piece lenghts and only write out what is needed at the end
	Map<Integer, Integer> PieceLengths;

	public FileHandling(int peerID, int totalPieces, int pieceSize) {  // 
	    // ToDo Open File at the path for this peer under the correct directory.  If no directory exists, create the directory
		// If this is a client, it needs to empty the contents of the file, if this is a sserver, it needs to read in all the 
		// contents of the file and load a buffer.
		
	    this.totalPieces = totalPieces; // transfer the needed information from the peer process to write files
	    this.pieceSize = pieceSize;
		this.peerID = peerID;
		localByteBuffer 	= ByteBuffer.allocate(pieceSize);
	}
	
	
	
	public boolean WriteFileOut() { 
	     // Might fail if for some reason it didn't have totalPieces as needed.
		 
		int x;
		
		String DirectoryFileName = "‘~/project/log_peer_" + String.format("%04d", peerID) + "/" + "tree.jpg";   // may have to change this file name later
								
		File writingFile = new File(DirectoryFileName);

		for(x = 0; x < totalPieces; ++x) {
			
			//writingFile.write(EntireFile.get(Integer.valueOf(x)));
			
			try (FileChannel writingFileStream = new FileOutputStream(writingFile).getChannel())
			{
				// this only writes the number of bytes needed
				// two hash tables are kept
				writingFileStream.write(EntireFile.get(Integer.valueOf(x)), PieceLengths.get(Integer.valueOf(x))); // EntireFile.get(Integer.valueOf(x)));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		return true;
	}

	public boolean ReceivedAPiece(int pieceNumber, String incomingPiece, int PieceLength) { 
	
		ByteBuffer bbuf = ByteBuffer.allocate(pieceSize);	
		
		EntireFile.put(Integer.valueOf(pieceNumber), bbuf);
		PieceLengths.put(Integer.valueOf(pieceNumber), Integer.valueOf(PieceLength));
		
		return true;

	}

	public boolean CheckForAllPieces() { 
	                                
		int x;
		for(x = 0; x < totalPieces; ++x) {
			if (EntireFile.get(Integer.valueOf(x)) == null) {
				return false;
			}
		}
	return true;
	}

	public boolean CheckForPieceNumber( int pieceNumber ) { 
	                                
		
		if (EntireFile.get(Integer.valueOf(pieceNumber)) == null) {
				return false;
			}
			
	return true;
	}

	public boolean loadLocalByteBufferPieceNumber(int pieceNumber) { 

		ByteBuffer bbuf = ByteBuffer.allocate(pieceSize);	
		localByteBuffer = EntireFile.get(Integer.valueOf(pieceNumber));
		if (localByteBuffer == null) {
			return false;
		} 
		return true;
	}

	public ByteBuffer getCurrentLocalByteBuffer() { 
	                                // Might fail if for some reason it didn't have totalPieces as needed.
		
		return localByteBuffer;
	}
}

