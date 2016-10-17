import java.io.*;
import java.util.HashMap;

/**
 * Buffer manager. Manages a memory-based buffer pool of pages.
 */
public class BufferManager
{
    public static class PageNotPinnedException
        extends RuntimeException {};
    public static class PagePinnedException extends RuntimeException {};

    /**
     * Value to use for an invalid page id.
     */
    public static final int INVALID_PAGE = -1;

    private static class FrameDescriptor
    {
        private int pageNum;
        private String fileName;
        private int pinCount;
        private boolean dirty;
        private boolean referenceBit;
        
        public FrameDescriptor()
        {
            pageNum = INVALID_PAGE;
            pinCount = 0;
            fileName = null;
            dirty = false;
            referenceBit = false;
        }

    }

    // Here are some private variables to get you started. You'll
    // probably need more.
    private Page[] bufferPool;
    private FrameDescriptor[] frameTable;
    private HashMap<Integer, Integer> allPages;
    private int poolCapacity;
    private int numpages;
    private int clockHand;

    /**
     * Creates a buffer manager with the specified size.
     * @param poolSize the number of pages that the buffer pool can hold.
     */
    public BufferManager(int poolSize)
    {
        bufferPool = new Page[poolSize];
        poolCapacity = poolSize;
        frameTable = new FrameDescriptor[poolSize];
        allPages = new HashMap<Integer,Integer>(); //create hashmap with pageId as key, index frame of page as value
        clockHand = -1;
    }

    /**
     * Returns the pool size.
     * @return the pool size.
     */
    public int poolSize()
    {
        return poolCapacity;
    }

    /**
     * Checks if this page is in buffer pool. If it is, returns a
     * pointer to it. Otherwise, it finds an available frame for this
     * page, reads the page, and pins it. Writes out the old page, if
     * it is dirty, before reading.
     * @param pinPageId the page id for the page to be pinned
     * @param fileName the name of the database that contains the page
     * to be pinned
     * @param emptyPage determines if the page is known to be
     * empty. If true, then the page is not actually read from disk
     * since it is assumed to be empty.
     * @return a reference to the page in the buffer pool. If the buffer
     * pool is full, null is returned.
     * @throws IOException passed through from underlying file system.
     */
    public Page pinPage(int pinPageId, String fileName, boolean emptyPage)
        throws IOException
    {
        if (emptyPage) { //if page is empty, return null
            System.out.println("The page is empty.");
            return null;
        }
        numPages();
        int numPinned = 0;
        for (int i=0; i<numpages; i++){
            if (frameTable[i].pinCount != 0){
                numPinned++;
            }
        }
        if (numPinned == poolCapacity){
            return null;
        }
        if (allPages.containsKey(pinPageId)) { //if page is in bufferpool
            System.out.println("tester 1");
            numPages();
            int pageIndex = allPages.get(pinPageId);
            FrameDescriptor curPage = frameTable[pageIndex];
            curPage.pinCount ++;
            curPage.dirty = true;
            return bufferPool[pageIndex];
        }
        else{ //if page is not in bufferpool, get from disk
            numPages();
            if (numpages < poolCapacity) { //if there is empty frame
                //update buffer pool
                numPages();
                int newIndex = numpages; //index to insert at
                Page insertPage = null; //create new page object in memory
                
                DBFile db = new DBFile(fileName); //open the target database containing the page
                insertPage = new Page();
                db.readPage(pinPageId, insertPage); //read page into the new page object
                bufferPool[newIndex] = insertPage; //insert the new page object into bufferpool
                numPages();
                
                //update FrameDescriptor
                FrameDescriptor newFrame = new FrameDescriptor();
                newFrame.pageNum = pinPageId;
                newFrame.pinCount = 1; //pin page
                newFrame.fileName = fileName;
                newFrame.dirty = true;
                frameTable[newIndex] = newFrame;
                
                //update allPages
                allPages.put(pinPageId, newIndex);
                
                return bufferPool[newIndex];
            }
            numPages();
            if (numpages >= poolCapacity) { //if there are no empty frames, use clock
                //choose a frame for replacement using clock replacement
                numPages();
                boolean frameFound = false;
                clockHand = (clockHand + 1) % (poolCapacity); //advance clockhand
                FrameDescriptor curFrame = frameTable[clockHand];
                while (frameFound == false) {
                    if (curFrame.pinCount != 0) { //if page pinned, advance clockhand
                        clockHand = (clockHand + 1) % (poolCapacity);
                        curFrame = frameTable[clockHand];
                    }
                    if (curFrame.pinCount == 0 && curFrame.referenceBit == false) {
                        curFrame.referenceBit = true;
                        clockHand = (clockHand + 1) % (poolCapacity);
                        curFrame = frameTable[clockHand];
                    }
                    if (curFrame.referenceBit == true) {
                        frameFound = true;
                    }
                    
                }
                
                int curPageId = curFrame.pageNum;
                //check if the page to be replaced is dirty
                if (curFrame.dirty == true) {
                    DBFile dbFile = new DBFile(fileName);
                    dbFile.writePage(curFrame.pageNum, bufferPool[clockHand]);;
                }
                //update bufferpool
                Page insertPage = new Page(); //create new page object in memory
                DBFile db = new DBFile(fileName); //open the target database containing the page
                db.readPage(pinPageId, insertPage); //read page into the new page object
                bufferPool[clockHand] = insertPage; //insert the new page object into bufferpool
                numPages();
                
                //update frameTable
                FrameDescriptor newFrame = new FrameDescriptor();
                newFrame.pageNum = pinPageId;
                newFrame.pinCount = 1; //pin page
                newFrame.fileName = fileName;
                newFrame.dirty = true;
                frameTable[clockHand] = newFrame; 
                
                //update allPages
                allPages.remove(curPageId);
                allPages.put(pinPageId, clockHand);
                
                return bufferPool[clockHand];
            }
        }
        return null;
    }

    
    /**
     * If the pin count for this page is greater than 0, it is
     * decremented. If the pin count becomes zero, it is appropriately
     * included in a group of replacement candidates.
     * @param unpinPageId the page id for the page to be unpinned
     * @param fileName the name of the database that contains the page
     * to be unpinned
     * @param dirty if false, then the page does not actually need to
     * be written back to disk.
     * @throws PageNotPinnedException if the page is not pinned, or if
     * the page id is invalid in some other way.
     * @throws IOException passed through from underlying file system.
     */
    public void unpinPage(int unpinPageId, String fileName, boolean dirty)
        throws IOException
    {
        boolean success = false;
        int pageIndex = allPages.get(unpinPageId);
        FrameDescriptor curPage = frameTable[pageIndex];
        
        if (curPage.pinCount > 0) {
            curPage.pinCount --;
            success = true;
        }
        if (curPage.pinCount == 0) {
            curPage.referenceBit = false;
        }
        
        //write back if the page unpinned is dirty
        if (curPage.dirty == true) {
            DBFile dbFile = new DBFile(fileName);
            dbFile.writePage(curPage.pageNum, bufferPool[pageIndex]);
        }
        
        if (!success) {
            throw new PageNotPinnedException();
        }
    }


    /**
     * Requests a run of pages from the underlying database, then
     * finds a frame in the buffer pool for the first page and pins
     * it. If the buffer pool is full, no new pages are allocated from
     * the database.
     * @param numPages the number of pages in the run to be allocated.
     * @param fileName the name of the database from where pages are
     * to be allocated.
     * @return an Integer containing the first page id of the run, and
     * a references to the Page which has been pinned in the buffer
     * pool. Returns null if there is not enough space in the buffer
     * pool for the first page.
     * @throws DBFile.FileFullException if there are not enough free pages.
     * @throws IOException passed through from underlying file system.
     */
    public Pair<Integer,Page> newPage(int numPages, String fileName)
        throws IOException
    {
        DBFile db = new DBFile(fileName); //open the target database containing the page
        int firstPId = db.allocatePages(numPages);
        numPages();
        if (numpages < poolCapacity){ //if there is empty frame
            //update buffer pool
            int newIndex = numpages; //index to insert at
            Page insertPage = new Page(); //create new page object in memory
            db.readPage(firstPId, insertPage); //read page into the new page object
            bufferPool[newIndex] = insertPage; //insert the new page object into bufferpool
            numPages();
            
            //update FrameDescriptor
            FrameDescriptor newFrame = new FrameDescriptor();
            newFrame.pageNum = firstPId;
            newFrame.pinCount = 1; //pin page
            newFrame.fileName = fileName;
            newFrame.dirty = true;
            frameTable[newIndex] = newFrame;
            
            //update allPages
            allPages.put(firstPId, newIndex);
            Pair<Integer, Page> ret = new Pair<Integer, Page>(firstPId, bufferPool[newIndex]);
            System.out.println(newIndex);
            return ret;
        }
        else {
            System.out.println("There is not enough space in the bufferpool.");
            return null;
        }
    }

    /**
     * Deallocates a page from the underlying database. Verifies that
     * page is not pinned.
     * @param pageId the page id to be deallocated.
     * @param fileName the name of the database from where the page is
     * to be deallocated.
     * @throws PagePinnedException if the page is pinned
     * @throws IOException passed through from underlying file system.
     */
    public void freePage(int pageId, String fileName) throws IOException
    {
        DBFile temp = new DBFile(fileName);
        int pageIndex = allPages.get(pageId);
        FrameDescriptor curPage = frameTable[pageIndex];
        
        if (curPage.pinCount > 0)
            throw new PagePinnedException();
        temp.deallocatePages(pageId, 1);
    }

    
    
    /**
     * Count the number of pages in the bufferpool
     */
    public void numPages() {
        int counter = 0;
        for (int i = 0; i < bufferPool.length; i ++)
            if (bufferPool[i] != null)
                counter ++;
        numpages = counter;
    }
    /**
     * Flushes page from the buffer pool to the underlying database if
     * it is dirty. If page is not dirty, it is not flushed,
     * especially since an undirty page may hang around even after the
     * underlying database has been erased. If the page is not in the
     * buffer pool, do nothing, since the page is effectively flushed
     * already.
     * @param pageId the page id to be flushed.
     * @param fileName the name of the database where the page should
     * be flushed.
     * @throws IOException passed through from underlying file system.
     */
    public void flushPage(int pageId, String fileName) throws IOException
    {
        if (allPages.containsKey(pageId)) {
            if (frameTable[allPages.get(pageId)].dirty) {
                DBFile dbFile = new DBFile(fileName);
                int pageIndex = allPages.get(pageId);
                FrameDescriptor curPage = frameTable[pageIndex];
                dbFile.writePage(curPage.pageNum, bufferPool[pageIndex]);
                frameTable[allPages.get(pageId)].dirty = false;
            }
        }
    }

    /**
     * Flushes all dirty pages from the buffer pool to the underlying
     * databases. If page is not dirty, it is not flushed, especially
     * since an undirty page may hang around even after the underlying
     * database has been erased.
     * @throws IOException passed through from underlying file system.
     */
    public void flushAllPages(String fileName) throws IOException
    {
        for (int i = 0; i < bufferPool.length; i++) {
            if (frameTable[i] != null)
                flushPage(frameTable[i].pageNum, fileName);
            // else
            //     break;
        }
    }
        
    /**
     * Returns buffer pool location for a particular pageId. This
     * method is just used for testing purposes: it probably doesn't
     * serve a real purpose in an actual database system.
     * @param pageId the page id to be looked up.
     * @param fileName the file name to be looked up.
     * @return the frame location for the page of interested. Returns
     * -1 if the page is not in the pool.
    */
    public int findFrame(int pageId, String fileName)
    {
        if (allPages.get(pageId) == null) {
            return -1;
        }
        int pageIndex = allPages.get(pageId);
        return pageIndex;
    }
}