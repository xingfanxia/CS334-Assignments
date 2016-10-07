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
    public int nextID;
    public int prevID;
    public int freespace;

    private byte[] data;
    private IntBuffer intBuffer;
    private int intBufferLength;
    private int blockId;

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
        blockId = 1;
        nextID = INVALID_BLOCK;
        prevID = INVALID_BLOCK;
    }


    /**
     * Sets the block id.
     * @param blockId the new block id.
     */
    public void setBlockId(int blockId)
    {
        this.blockId = blockId;
    }

    /**
     * Gets the block id.
     * @return the block id.
     */
    public int getBlockId()
    {
        return this.blockId;
    }

    /**
     * Sets the next block id.
     * @param blockId the next block id.
     */
    public void setNextBlockId(int blockId)
    {
       nextID = blockId;
    }

    /**
     * Gets the next block id.
     * @return the next block id.
     */
    public int getNextBlockId()
    {
        return nextID;
    }

    /**
     * Sets the previous block id.
     * @param blockId the previous block id.
     */
    public void setPrevBlockId(int blockId)
    {
        prevID = blockId;
    }

    /**
     * Gets the previous block id.
     * @return the previous block id.
     */
    public int getPrevBlockId()
    {
        return prevID;
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
        return -1;
    }
        

    /**
     * Dumps out to the screen the # of entries in the block, the location where
     * the free space starts, the slot array in a readable fashion, and the
     * actual contents of each record. (This method merely exists for debugging
     * and testing purposes.)
    */ 
    public void dumpBlock()
    {
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
        return null;
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
        return null;
    }

    /**
     * Whether or not the block is empty.
     * @return true if the block is empty, false otherwise.
     */
    public boolean empty()
    {
        return false;
    }
}
