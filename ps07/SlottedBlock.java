import java.nio.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 * Slotted file block. This is a wrapper around a traditional Block that
 * adds the appropriate structure to it.
 *
 * @author Dave Musicant, with considerable inspiration from the UW-Madison
 * Minibase project
 */
public class SlottedBlock
{
    public static class BlockFullException extends RuntimeException {};
    public static class BadSlotIdException extends RuntimeException {};
    public static class BadBlockIdException extends RuntimeException {};

    private static class SlotArrayOutOfBoundsException
        extends RuntimeException {};

    /**
     * Value to use for an invalid block id.
     */
    public static final int INVALID_BLOCK = -1;
    public static final int SIZE_OF_INT = 4;

    private byte[] data;
    private IntBuffer intBuffer;
    private int intBufferLength;
    private int lenHeader;

    /**
     * Constructs a slotted block by wrapping around a block object already
     * provided.
     * @param block the block to be wrapped.
     */
    public SlottedBlock(Block block)
    {
        data = block.data;
        intBuffer = (ByteBuffer.wrap(data)).asIntBuffer();
        intBufferLength = data.length / SIZE_OF_INT;
        lenHeader = 5; //reserve place for #entries, bId, nextbId, prevbId and 
        intBuffer.put (4, data.length - 1); //index of the end of free space in byte (1023)
    }

    /**
     * Initializes values in the block as necessary. This is separated out from
     * the constructor since it actually modifies the block at hand, where as
     * the constructor simply sets up the mechanism.
     */
    public void init()
    {
    	intBuffer.put(0, 0); //initialize the number of entries to 0
    }

    /**
     * Update the length of the header slot array (in intBuffer index)
     */
    public void lenHeader()
    {
    	int numEntries = intBuffer.get(0);
    	int countEntries = 0;
    	int cur = 5;
    	while (countEntries < numEntries) {
    		int offSet = intBuffer.get(cur);
    		if (offSet != 0){
    			countEntries ++;
    		}
    		cur += 2;
    	}
    	lenHeader = cur;
    }
    
    /**
     * Sets the block id.
     * @param blockId the new block id.
     */
    public void setBlockId(int blockId)
    {
    	if (blockId == SlottedBlock.INVALID_BLOCK) {
    		intBuffer.put(1, -1);
    	}
    	else {
    		intBuffer.put(1, blockId);
    	}
    }

    /**
     * Gets the block id.
     * @return the block id.
     */
    public int getBlockId()
    {
        return intBuffer.get(1);
    }

    /**
     * Sets the next block id.
     * @param blockId the next block id.
     */
    public void setNextBlockId(int blockId)
    {
    	if (blockId == SlottedBlock.INVALID_BLOCK) {
    		intBuffer.put(2, -1);
    	}
    	else {
    		intBuffer.put(2, blockId);
    	}
    }

    /**
     * Gets the next block id.
     * @return the next block id.
     */
    public int getNextBlockId()
    {
        return intBuffer.get(2);
    }

    /**
     * Sets the previous block id.
     * @param blockId the previous block id.
     */
    public void setPrevBlockId(int blockId)
    {
    	if (blockId == SlottedBlock.INVALID_BLOCK) {
    		intBuffer.put(3, -1);
    	}
    	else {
    		intBuffer.put(3, blockId);
    	}
    }

    /**
     * Gets the previous block id.
     * @return the previous block id.
     */
    public int getPrevBlockId()
    {
        return intBuffer.get(3);
    }

    /**
     * Determines how much space, in bytes, is actually available in the block,
     * which depends on whether or not a new slot in the slot array is
     * needed. If a new spot in the slot array is needed, then the amount of
     * available space has to take this into consideration. In other words, the
     * space you need for the addition to the slot array shouldn't be included
     * as part of the available space, because from the user's perspective, it
     * isn't available for adding data.
     * @return the amount of available space in bytes
     */
    public int getAvailableSpace()
    {
        lenHeader();
    	int startOfFree = ((lenHeader * 4) - 1); //the first free index after the slot array
    	return intBuffer.get(4) - startOfFree + 1;
    }
        

    /**
     * Dumps out to the screen the # of entries in the block, the location where
     * the free space starts, the slot array in a readable fashion, and the
     * actual contents of each record. (This method merely exists for debugging
     * and testing purposes.)
    */ 
    public void dumpBlock()
    {
    	System.out.println("# Entries: " + Integer.toString(intBuffer.get(0)));
    	System.out.println("Free space starts at: " + Integer.toString(lenHeader) +"in intBuffer.");
    	System.out.println("Free space starts at: " + Integer.toString(lenHeader * 4 - 1) + "in byte");

    	for (int i = 0; i < lenHeader; i++) {
    		System.out.println("In index " + Integer.toString(i) + " the entry is: " + Integer.toString(intBuffer.get(i)));
    	}
    	int entryLength = 0;
    	int entryIndex = 0;
    	for (int j = 5; j < lenHeader; j+=2){
    		if (intBuffer.get(j) != 0){
    	    	entryLength = intBuffer.get(j + 1) / 4 ;//length in index of desired entry
    	    	entryIndex = intBuffer.get(j) / 4;
    	    	
    	    	List<Integer> ret = new ArrayList<Integer>();
    	    	for (int a = 0; a < entryLength; a++) {
    	    		ret.add(intBuffer.get(entryIndex));
    	    	}
    	    	System.out.println(ret);
    		}
    	}
    }

    /**
     * Inserts a new record into the block.
     * @param record the record to be inserted. A copy of the data is
     * placed in the block.
     * @return the RID of the new record 
     * @throws BlockFullException if there is not enough room for the
     * record in the block.
    */
    public RID insertRecord(byte[] record)
    {
    	lenHeader();
    	int insertAt = 1 + intBuffer.get(4) - record.length; //the index at which to insert data (in byte) (slotNum)
        Boolean emptySlot = false; //check if there is an empty slot in the uncollapsed header array
        int c = 5;
        while (emptySlot == false && c < lenHeader) {
        	int check = intBuffer.get(c);
        	emptySlot = (check == 0);
        	c += 2;
        }
        if ((emptySlot == false) && (record.length > getAvailableSpace() - 8)) { //available space with no extra header space needed
        	throw new BlockFullException();
        }
        if ((emptySlot == true) && (record.length > getAvailableSpace())) { // available space with space needed for slot entry removed
        	throw new BlockFullException();
        }
        
        
        intBuffer.put(0, intBuffer.get(0) +1); //update entry number
        int slotIndex = 5;
        if (emptySlot == true) {
        	while (intBuffer.get(slotIndex) != 0) {
        		slotIndex += 2;
        	}
         }
        else {
        	slotIndex = lenHeader;
        }
        intBuffer.put(slotIndex, insertAt); //update slot array (offset)
        intBuffer.put(slotIndex + 1, record.length); //update slot array (size in byte)
        intBuffer.put(4, insertAt - 1); //update the end of free space (byte)
        lenHeader();
        
        int recordIndex = 0;
        for (int i = insertAt; i < (insertAt + record.length); i++) {
        	data[i] = record[recordIndex];
        	recordIndex += 1;
        }
        RID ret = new RID (intBuffer.get(1), slotIndex);
        return ret;
    }

    /**
     * Deletes the record with the given RID from the block, 
     * compacting the hole created. Compacting the hole, in turn, requires that
     * all the offsets (in the slot array) of all records after the
     * hole be adjusted by the size of the hole, because you are
     * moving these records to "fill" the hole. You should leave a
     * "hole" in the slot array for the slot which pointed to the
     * deleted record, if necessary, to make sure that the rids of the
     * remaining records do not change. The slot array should be
     * compacted only if the record corresponding to the last slot is
     * being deleted.
     * @param rid the RID to be deleted.
     * @return true if successful, false if the rid is actually not
     * found in the block.
    */
    public boolean deleteRecord(RID rid)
    {
    	if (!(rid.blockId == intBuffer.get(1) && intBuffer.get(rid.slotNum) != 0)){
    		return false;
    	}
    	lenHeader();
    	int toDel = intBuffer.get(rid.slotNum)/4; //(index) access the slot array with the address to the record to be deleted
        int sizeDel = intBuffer.get(rid.slotNum + 1)/4; //(index) get the size of the record to be deleted
        for (int i = toDel  + sizeDel - 1; i > intBuffer.get(4)/4 + sizeDel; i--) { //delete record and fill hole
        	intBuffer.put(i, intBuffer.get(i - sizeDel));
        }
        for (int a = 5; a < lenHeader; a += 2){
        	if (intBuffer.get(a) < toDel && intBuffer.get(a)!=0) { //if the entry is to the left of the deleted record, update offset
        		intBuffer.put(a, intBuffer.get(a) + sizeDel);
        	}
        }
        intBuffer.put(0, intBuffer.get(0) - 1); //update number of entries
        intBuffer.put(4, intBuffer.get(4) + sizeDel*4); //update the end of free space
        intBuffer.put(rid.slotNum, 0);
        intBuffer.put(rid.slotNum + 1, 0);
        lenHeader();
        return true;
    }

    /**
     * Returns RID of first record in block. Remember that some slots may be
     * empty, so you should skip over these.
     * @return the RID of the first record in the block. Returns null
     * if the block is empty.
     */
    public RID firstRecord()
    {
    	lenHeader();
    	if (empty()) {
    		return null;
    	}
    	else {
    		int i = 5;
    		while (intBuffer.get(i) == 0) {
    			i++;
    		}
    	RID ret = new RID(intBuffer.get(1), i);
    	return ret;
    	}
    }

    /**
     * Returns RID of next record in the block, where "next in the block" means
     * "next in the slot array after the rid passed in." Remember that some
     * slots may be empty, so you should skip over these.
     * @param curRid an RID
     * @return the RID immediately following curRid. Returns null if
     * curRID is the last record in the block.
     * @throws BadBlockIdException if the block id within curRid is
     * invalid
     * @throws BadSlotIdException if the slot id within curRid is invalid
    */
    public RID nextRecord(RID curRid)
    {
    	lenHeader();
    	if (curRid.blockId == INVALID_BLOCK) {
    	    throw new BadBlockIdException();
    	}
    	if (curRid.slotNum < 5) {
    		throw new BadSlotIdException();
    	}
    	if (curRid.slotNum % 2 == 0){
    		throw new BadSlotIdException();
    	}
    	int check = curRid.slotNum + 2;
    	Boolean found = false;
    	while ((check < lenHeader) && (!found)) { //check is the slotNum of next record
    		if (intBuffer.get(check) != 0) {
    			found = true;
    		}
    	}
    	int startFreeSpace = lenHeader; //start of free space in intBuffer (index)
    	if (curRid.slotNum == startFreeSpace - 2){
    		return null;
    	}
    	if (curRid.slotNum >= startFreeSpace) {
    		throw new BadSlotIdException();
    	}
//    	int cur = curRid.slotNum +2;
//    	while (intBuffer.get(cur) == 0) {
//    		cur = cur+2;
    	
    	RID ret = new RID(intBuffer.get(1), check);
    	return ret;
    }

    /**
     * Returns the record associated with an RID.
     * @param rid the rid of interest
     * @return a byte array containing a copy of the record. The array
     * has precisely the length of the record (there is no padded space).
     * @throws BadBlockIdException if the block id within curRid is
     * invalid
     * @throws BadSlotIdException if the slot id within curRid is invalid
    */
    public byte[] getRecord(RID rid)
    {
        lenHeader();
    	if (rid.blockId == INVALID_BLOCK) {
    	    throw new BadBlockIdException();
    	}
    	if (rid.slotNum < 5) {
    		throw new BadSlotIdException();
    	}
    	if (rid.slotNum % 2 == 0){
    		throw new BadSlotIdException();
    	}
    	int startFreeSpace = lenHeader; //start of free space in intBuffer (index)
    	if (rid.slotNum >= startFreeSpace) {
    		throw new BadSlotIdException();
    	}
    	int entryIndex = intBuffer.get(rid.slotNum); //byte index of the desired entry
    	int entryLength = intBuffer.get(rid.slotNum + 1);//length in byte of desired entry
    	byte[] ret = new byte[entryLength];
    	for (int a = 0; a < entryLength; a++) {
    		ret[a] = data[entryIndex];
    		entryIndex ++;	
    	}
    	return ret;
    }

    /**
     * get the last rid
     * @return last rid in slot array
     */
    public RID lastRid()
    {
    	lenHeader();
    	RID ret = new RID (intBuffer.get(1), lenHeader-2);
    	return ret;
    }
    
    /**
     * Whether or not the block is empty.
     * @return true if the block is empty, false otherwise.
     */
    public boolean empty()
    {
    	lenHeader();
    	return (lenHeader * 4 - 1) + getAvailableSpace() == data.length;
    }
}
