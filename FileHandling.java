// This class will be attached to a peer and handle assembling the file pieces, writing them to a file, opening
// them up, clearling partial files, etc...
// This class attemps to accumulate an entire file from samll pieces
// each piece is inserted and then when the entire file is ready to be printed
// it can be printed out to a file name and directory location
//
//
// Note: The file print must print out to a location that doss not already have a file
// this means the startup script must delete the files if they are present.
// ToDo: Automatically delete unwanted files

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


public class FileHandling {
	// the name of the file
	String fileNameWithPath;
	// the handle name of the peer if added later to the file name
	String peerHandleName;
	// total number of pieces for storage in a hash table
	int    totalPieces;
	// the max size of each piece for allocation
	int    pieceSize;
	// the peer ID to log into the correct writing file
	int    peerID;
	// a local buffer that can be loaded wtih a command and then operated against
	ByteBuffer localByteBuffer;
	int localByteBufferPieceSize;
	// hash table for the binary data
	Map<Integer, ByteBuffer> EntireFile;
	// hash table to keep track of the piece lenghts and only write out what is needed at the end
	Map<Integer, Integer> PieceLengths;


    // Constructor
	// Note: This class will not copye objects and as such does not have a copy constructor
	
	public FileHandling(int peerID, int totalPieces, int pieceSize) {  // 
	    // ToDo Open File at the path for this peer under the correct directory.  If no directory exists, create the directory
		// If this is a client, it needs to empty the contents of the file, if this is a sserver, it needs to read in all the 
		// contents of the file and load a buffer.
		
	    this.totalPieces = totalPieces; // transfer the needed information from the peer process to write files
		// copy the piece size and keep track to allocate right buffer amount
	    this.pieceSize = pieceSize;
		// copy this peerID to use it to write the correct file output (or input)
		this.peerID = peerID;
		// create a common buffer array that can be used to find problems and print out values
		localByteBuffer 	= ByteBuffer.allocate(pieceSize);
		PieceLengths = new HashMap<>();
		EntireFile   = new HashMap<>();
	}
	
	
	public void Shutdown () {  // this can be called before shutting down if there are things that need to be done like closing files
		System.out.println("Shutting down the FileHandler for peer ID " + peerID );  
	}

	// writes out the entire file.
	// assumes the file is presenent in the hash table.
	// suggest the application call the check for all pieces method first before writing the file to 
	// avoid an error.
	// this will iterate over the expected number of pieces
	// the write will send out only the number of bytes expected to be written
	// the number of bytes expected to be written in kept in a similar hash table called PieceLengths
	// The file that is at an odd boundary can be written even if there are fewer than the buffersize of bytes
	// in the piece.
	public boolean WriteFileOut(String FileNameInput) { 
	     // Might fail if for some reason it didn't have totalPieces as needed.
		 
		int x;
		int pieceLength;
		
		//fileNameWithPath = "â€˜~/project/log_peer_" + String.format("%04d", peerID) + "/" + FileNameInput;
		//fileNameWithPath = "./" + FileNameInput;
		fileNameWithPath = FileNameInput;
								
		File writingFile = new File(fileNameWithPath);

		//writingFile.write(EntireFile.get(Integer.valueOf(x)));fs
		try (FileChannel writingFileStream = new FileOutputStream(writingFile, false).getChannel())
			{
				for(x = 0; x < totalPieces; ++x) {
				
					// this only writes the number of bytes neededs
					// two hash tables are kept
					localByteBuffer.clear();
					loadLocalByteBufferPieceNumber(x);  // this says copy data from EntireFile to local byte buffer
					//localByteBuffer = EntireFile.get(Integer.valueOf(x)); // this was a mistake, it moved the pointer to EntireFile's copy and then cleared it on next round... bad...
					pieceLength = PieceLengths.get(Integer.valueOf(x));
				    //System.out.println(" Round Number " + x + " First Byte  " + localByteBuffer.get(1370)  + " Writing " + pieceLength + " bytes from buffer with " + localByteBuffer.position() + " bytes in it.");
					localByteBuffer.flip();  // flips from reading to writing data
					//System.out.println(" Wrote " + writingFileStream.write( localByteBuffer ) + " bytes " ); // , PieceLengths.get(Integer.valueOf(x)) ); // EntireFile.get(Integer.valueOf(x)));
					writingFileStream.write( localByteBuffer );
				} 
				writingFileStream.close();
			} catch (IOException e) {
				// error so print out stack trace
				System.out.println(" ERROR Trapped in writing to file " + fileNameWithPath);
				e.printStackTrace();
			}
			
		
		
		
		return true;
	}



	public boolean ReadFileIn(String FileNameInput) { 
			 // Might fail if for some reason it didn't have totalPieces as needed.
			 
			int x;
			int numberOfBytesRead;
			
			//fileNameWithPath = "â€˜~/project/log_peer_" + String.format("%04d", peerID) + "/" + FileNameInput;
		    //fileNameWithPath = "./" + FileNameInput;
		    fileNameWithPath = FileNameInput;
									
			File readingFile = new File(fileNameWithPath);

			try (FileChannel readingFileStream = new FileInputStream(readingFile).getChannel())
			{
				for(x = 0; x < totalPieces; ++x) {
					//readingFile.read(EntireFile.get(Integer.valueOf(x)));
					// this only reads the number of bytes needed
					// two hash tables are kept
			        ByteBuffer tempByteBuffer = ByteBuffer.allocate(pieceSize); ;
					numberOfBytesRead = readingFileStream.read(tempByteBuffer); // , 0, pieceSize);
				    //System.out.println(" Round Number " + x + " Read " + numberOfBytesRead + " bytes " + " First Byte " + tempByteBuffer.get(1370));
					PieceLengths.put(Integer.valueOf(x), Integer.valueOf(numberOfBytesRead));
					EntireFile.put(Integer.valueOf(x), tempByteBuffer);
				}
			} catch (IOException e) {
				// error so print out stack trace
				e.printStackTrace();
			}
				
				
			
			
			return true;
		}


    // creates a new ByteBuffer and copies the contents of the piece pieceNumber into it
	public ByteBuffer MakeCopyPieceByteBuffer(int pieceNumber) { 
	
	    // allocate the buffer for this piece
		ByteBuffer newByteBuffer = ByteBuffer.allocate(pieceSize);	
		//System.out.println(" Received and loaded a piece of length " + PieceLength + " comparing incomingPiece.remaining() " + incomingPiece.remaining() + " to newByteBuffer.remaining() " + newByteBuffer.remaining() + " versus PieceLength " + PieceLength);
        newByteBuffer.put(EntireFile.get(Integer.valueOf(pieceNumber)).array(), 0, PieceLengths.get(Integer.valueOf(pieceNumber)));  // put(byte[] src, int offset, int length) wjere byte[] is incomingPiece.array(), start 0, length PieceLength
		
		return newByteBuffer;

	}

    // loads a received piece of form incomingPiece into the hash table.
	// note these pieces are ByteBuffers because the jpg file is a binary file
	// and not a string file.  So the transfer of the data must be binary data 
	// transfers.
	public boolean ReceivedAPiece(int pieceNumber, ByteBuffer incomingPiece, int PieceLength) { 
	
	    // allocate the buffer for this piece
		ByteBuffer newByteBuffer = ByteBuffer.allocate(pieceSize);	
		//System.out.println(" Received and loaded a piece of length " + PieceLength + " comparing incomingPiece.remaining() " + incomingPiece.remaining() + " to newByteBuffer.remaining() " + newByteBuffer.remaining() + " versus PieceLength " + PieceLength);
        newByteBuffer.put(incomingPiece.array(), 0, PieceLength);  // put(byte[] src, int offset, int length) wjere byte[] is incomingPiece.array(), start 0, length PieceLength
		
		// transfer the bynary data into the allocated buffer
		EntireFile.put(Integer.valueOf(pieceNumber), newByteBuffer);
		// and keep track of the size of this piece.  
		// could convert this to load another class object that contains piece size, but 
		// maybe not do it because it is more than needed
		PieceLengths.put(Integer.valueOf(pieceNumber), Integer.valueOf(PieceLength));
		
		return true;

	}

    // checks to see if all peers have been received.
	// if all expected peers have been accounted for, this returns true
	// if they have not, it returns false.
	public boolean CheckForAllPieces() { 
	                                
		int x;
		// iterates overa all expected pieces
		for(x = 0; x < totalPieces; ++x) {
			// if a piece does not exist then it will return a null
			if (EntireFile.get(Integer.valueOf(x)) == null) {
			    // return false on missing piece
				return false;
			}
		}
	// all pieces were found, so this was a success
	return true;
	}

    // Check to see if this piece number is present
	// Return true if present, false otherwise
	public boolean CheckForPieceNumber( int pieceNumber ) { 
	                                
		// if the get of the value is a null then this value does not exist in map hash table
		
		if (EntireFile.get(Integer.valueOf(pieceNumber)) == null) {
				// if it doesn't exist, error out and do not return a true, instead false
				return false;
			}
			
	return true;
	}

    // Loads a local buffer in this object with the data of piece number
	// might be helpful to find problems in the pieces in the hash table
	// retrieves the value from the hash table.
	public boolean loadLocalByteBufferPieceNumber(int pieceNumber) { 
 
        ByteBuffer CopyOfBufferPtr;
		CopyOfBufferPtr = EntireFile.get(Integer.valueOf(pieceNumber));
		if (CopyOfBufferPtr == null) {
			return false;
		} 
        localByteBuffer.clear();
		int amountToTransfer = PieceLengths.get(Integer.valueOf(pieceNumber));
		localByteBufferPieceSize = amountToTransfer;
		//System.out.println(" Transferring " + amountToTransfer + " bytes," + " found " + CopyOfBufferPtr.get(0) + " " + CopyOfBufferPtr.get(1) + " " + CopyOfBufferPtr.get(2));
        localByteBuffer.put(CopyOfBufferPtr.array(), 0, amountToTransfer);  // put(byte[] src, int offset, int length) wjere byte[] is CopyOfBufferPtr.array(), start 0, length amountToTransfer
		return true;
	}


    // gets the current local byte buffer array handle that was loaded with the above method
	// this can help with moving the data around and might be useful in tranmit / receive binary 
	// files to the port
	public ByteBuffer getCurrentLocalByteBuffer() { 
	                                // Might fail if for some reason it didn't have totalPieces as needed.
		
		return localByteBuffer;
	}
	
    // gets the current local byte buffer size of this piece
	// this can help with moving the data around and might be useful in tranmit / receive binary 
	// files to the port
	public int getCurrentLocalByteBufferPieceSize() { 
	                                // Might fail if for some reason it didn't have totalPieces as needed.
		
		return localByteBufferPieceSize;
	}

	public boolean printLocalBuffer(int length) { 
	                                // Prints out the local buffer
								
		for (int x = 0; (x < pieceSize) && (x < length); ++x) {
			// first get the value from the localByteBuffer
			// then convert it to hex
			// then print it out to the screen wtih space but not a new line character
			System.out.print(Integer.toHexString(this.localByteBuffer.get(x) & 0xFF) + " ");
			// if every 16 then a new line
			if (((x % 32) == 0) && (x > 0)) {
				// new line character
				System.out.println(" ");
			}
		}
		System.out.println(" ");
		// this worked so return true
		return true;
	}
	
	public int GetPieceSize(int pieceNumber) { 
	                                // Prints out the local buffer
		return PieceLengths.get(Integer.valueOf(pieceNumber));
	}
}