import java.nio.*;

/**
 * Slotted file block. This is a wrapper around a traditional Block that
 * adds the appropriate struture to it.
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
    // private IntBuffer[] records;

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
    }

    /**
     * Initializes values in the block as necessary. This is separated out from
     * the constructor since it actually modifies the block at hand, where as
     * the constructor simply sets up the mechanism.
     */
    public void init()
    {
        //prev blcokID
        intBuffer.put(0, INVALID_BLOCK);
        //current blockID
        intBuffer.put(1, INVALID_BLOCK);
        //next blockID
        intBuffer.put(2, INVALID_BLOCK);
        //slot number count
        intBuffer.put(3, 0);
        //pointer to beginning of free space
        intBuffer.put(4, data.length - 1);
    }


    /**
     * Sets the block id.
     * @param blockId the new block id.
     */
    public void setBlockId(int blockId)
    {
        intBuffer.put(1, blockId);
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
        intBuffer.put(2, blockId);
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
        intBuffer.put(0, blockId);
    }
    /**
     * Gets the previous block id.
     * @return the previous block id.
     */
    public int getPrevBlockId()
    {
        return intBuffer.get(0);
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
        int freePointer = intBuffer.get(4);
        int slot_cnt = intBuffer.get(3);
        int avail = (freePointer - (7*SIZE_OF_INT)- (2*slot_cnt*SIZE_OF_INT));
        return avail;
    }


    /**
     * Dumps out to the screen the # of entries in the block, the location where
     * the free space starts, the slot array in a readable fashion, and the
     * actual contents of each record. (This method merely exists for debugging
     * and testing purposes.)
    */
    public void dumpBlock()
    {
        int slot_cnt = intBuffer.get(3);
        System.out.println("Number of entries: " + slot_cnt);
        int start_free = (2*slot_cnt + 5) * SIZE_OF_INT;
        System.out.println("Free space starts at: " + start_free);
        // the Slot array index
        System.out.println("Here is the slot array : [\n");
        for (int i = 5; i < start_free; i++) {
            System.out.println(intBuffer.get(i));
        }
        System.out.println("]\n");
        // the Records
        int r_pos;
        int r_size;
        for(int i=5; i< (slot_cnt*2+5); i+=2){
            r_pos = intBuffer.get(i);
            r_size = intBuffer.get(i+1);
            for (int j=r_pos; j>(r_pos-r_size); j--) {
                System.out.print(data[j]);
            }
            System.out.print("\n");
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
        int slot_cnt = intBuffer.get(3);
        if (this.getAvailableSpace() < record.length)
            throw new BlockFullException();

        // Pointer to end fo free space
        int freePointer = intBuffer.get(4);
        // update pos
        intBuffer.put((slot_cnt*2+5), freePointer);
        // update size
        intBuffer.put((slot_cnt*2+6), record.length);

        //actually do it
        int i = 0;
        while (i<record.length) {
            data[freePointer] = record[i];
            freePointer--;
            i++;
        }

        //update freePointer
        intBuffer.put(4, freePointer);
        intBuffer.put(3, slot_cnt + 1);
        int bID = intBuffer.get(1);
        int sID = intBuffer.get(3) - 1;
        RID n_record  = new RID(bID, sID);
        return n_record;
    }

    /**
     * Deletes the record with the given RID from the block, compacting
     * the hole created. Compacting the hole, in turn, requires that
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
        return false;
    }

    /**
     * Returns RID of first record in block. Remember that some slots may be
     * empty, so you should skip over these.
     * @return the RID of the first record in the block. Returns null
     * if the block is empty.
     */
    public RID firstRecord()
    {
        return null;
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
        return null;
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
        // if the block id within curRid is invalid
        int sID = rid.slotNum;
        int bID = rid.blockId;
        if (sID == INVALID_BLOCK) {
            throw new BadBlockIdException();
        }
        if (bID == INVALID_BLOCK) {
            throw new BadSlotIdException();
        }
        int r_pos = intBuffer.get(5+(2*sID));
        int r_size = intBuffer.get(5+(2*sID+1));

        if (r_size == -1) { //if invalid
            byte[] result = new byte[0];
            return result;
        }


        byte[] r_content = new byte[r_size];
        int cur = 0;
        for (int i = r_pos; i > (r_pos-r_size); i--) {
            r_content[cur] = data[i];
            cur++;
        }
        return r_content;
    }

    /**
     * Whether or not the block is empty.
     * @return true if the block is empty, false otherwise.
     */
    public boolean empty()
    {
        boolean a = (intBuffer.get(4) == intBufferLength-1);
        return a;
    }
}
