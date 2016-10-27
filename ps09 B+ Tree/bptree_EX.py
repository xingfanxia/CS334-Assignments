'''

Created on 10.22.2016
Implement a simulation of the B+ Tree
Able to both insert and delete records.

'''
class duplicateEntryException(Exception):
    def __init__(self, message):
        Exception.__init__(self, message)
        self.message = message
        
class noEntryException(Exception):
    def __init__(self, message):
        Exception.__init__(self, message)
        self.message = message
        
class Bptree:
    '''
    class of Bptree which allows the the indexing  of records stored in databases
    '''

    def __init__(self, numKeys):
        '''
        Initializes a bptree3 with one leaf node 
        and a keeps count of tree levels
        '''
        self.root = list()
        self.keyCapacity = numKeys
        self.levels = 1
        self.curLevel = 0
        self.minP = (self.keyCapacity+1 + (self.keyCapacity+1)%2)//2

        
    def insert(self,key,value):
        '''
        Inserts a key-value pair into the bptree, starting from the root
        '''
        self.curLevel = 0
        self.toInsert(self.root,key,value)
    
    def toInsert(self,aNode,aKey,aValue):
        if aNode == self.root:
            self.curLevel = 1
        
        #'If aNode is a leaf node, insert the key-value pair.'
        if self.curLevel == self.levels:
            i = 0
            while (i < len(aNode)) and (aKey > aNode[i]):
                i += 2
            
            if (i < len(aNode)) and (aKey == aNode[i]):
                raise duplicateEntryException("The entry already exists.")
            
            aNode.insert(i,aKey)
            aNode.insert(i+1,aValue)
            #'If capacity is exceeded, split leaf'
            if len(aNode) > 2 * self.keyCapacity + 1:
                newNode = list()
                j = self.keyCapacity % 2 + self.keyCapacity
                while (j < len(aNode)):
                    newNode.append(aNode[j])
                    j += 1
                    
                del aNode[self.keyCapacity % 2 + self.keyCapacity : len(aNode)]
                newEntry = (newNode[0], newNode)
                
                #If this is the first split in the tree, create a new root'
                if self.curLevel == 1:
                    newRoot = list()
                    newRoot.append(aNode)
                    newRoot.append(newNode[0])
                    newRoot.append(newNode)
                    self.root = newRoot
                    self.levels = 2
                else: #return entry for parent node
                    return newEntry
                
            else:
                return None
        # If aNode is a non-leaf, look for the subtree to search        
        else:
            i = 1
            while (i < len(aNode)) and (aKey >= aNode[i]):
                i += 2
            
            self.curLevel += 1
            newEntry = self.toInsert(aNode[i-1], aKey, aValue)
            # the (key,value) entry to add to the parent is returned
            self.curLevel -= 1        
            
            if newEntry == None: #child not split, nothing happens to parent
                return None
            
            elif len(aNode) < 2*self.keyCapacity + 1: #if child is split and parent is not full
                j = 1
                while (j < len(aNode)) and (newEntry[0] >= aNode[j]):
                    j += 2
                
                aNode.insert(j,newEntry[0])
                aNode.insert(j+1,newEntry[1])
                
                return None
            
            else: #child split, parent full so parent also split
                j=1
                while (j < len(aNode)) and (newEntry[0] >= aNode[j]):
                    j += 2
                
                aNode.insert(j,newEntry[0])
                aNode.insert(j+1,newEntry[1])
                # this is now a overloaded node with length 2*keyCapacity+3
                sibNode = list()
                k = self.keyCapacity - (self.keyCapacity % 2 - 1)
                pushed = aNode.pop(k)
                while (k < len(aNode)):
                    sibNode.append(aNode.pop(k))
                    
                
                
                newEntry = (pushed,sibNode)
                
                # if root is split, add a a level to the tree
                if self.curLevel == 1:
                    tempNode = list()
                    tempNode.append(aNode)
                    tempNode.append(pushed)
                    tempNode.append(sibNode)
                    self.root = tempNode
                    self.levels += 1
                    
                return newEntry
                    
                  
                  
    def getValue(self,key):
        cur = self.root
        curLevel = 1
        
        #traverse all internal nodes
        while (curLevel < self.levels):
            cur = self.getNextNode(cur, key)
            curLevel += 1
        #look for key in leaf
        data = self.getData(cur, key)      
        
        #If the key is not found in the leaf
        if data == None:
            return None 
        
        #If the key is found, return the pointer to data
        else:
            return data                     
    
    #Function to get to the appropriate child node
    def getNextNode(self, curNode, key):
        i = 1
        while (i < len(curNode) and (key >= curNode[i])):
            i += 2
        
        return curNode[i-1]
    
    #Function to look for the data in the leaf node
    def getData(self, curLeaf, key):
        i = 0
        while (i < len(curLeaf) and (key > curLeaf[i])):
            i+= 2
        
        if (i >= len(curLeaf)) or (curLeaf[i] != key):
            return None
        
        else:
            return curLeaf[i+1]

                    
    def printTree(self):
        print "-----Bptree------"
        self.printNode(self.root, 1)
    
    def printNode(self, aNode, Level):
        i = len(aNode) - 1 #i is the last pointer in the node
        curLevel = Level
        toPrint = list()
        
        if (curLevel != self.levels):
            j = 1 #first key in node
            
        else:
            j = 0 #first key in leaf
        
        for c in range(2*(Level-1)): #create indentation to show depth
            toPrint.append(" ")
            
        while j <len(aNode): #add all keys in the current node into a list
            toPrint.append(aNode[j])
            j += 2
        
        #print everything in current node
        for d in range(len(toPrint)):
            print toPrint[d],
        print
        #If not yet root, keep going down
        if (curLevel != self.levels):
            while i >= 0:
                self.printNode(aNode[i], curLevel+1)
                i -= 2    
        
        else: #If reached leaf, print all keys and go back one level
            return
    
    # '''
    # Method to delete a record from the B+tree
    # @param aKey - the key of the record to delete from the B+tree: 
    # '''
    # def delete(self,aKey):        
    #     self.curLevel = 0
    #     self.toDelete(None, self.root, aKey)
    
    # def toDelete(self, parent, aNode, aKey):
    #     if parent == None:
    #         self.curLevel = 1
        
    #     #If aNode is a leaf node, delete the key-value pair.
    #     if self.curLevel == self.levels:
    #         i = 0
    #         while (i < len(aNode)) and (aKey > aNode[i]):
    #             i += 2
        
    #         if (i < len(aNode)) and (aKey != aNode[i]):
    #             raise noEntryException("Entry is not in the database.")
            
    #         del aNode[i:i+2]
            
    #         # If the node is less than half full, join with sibnode
    #         if len(aNode) < self.keyCapacity%2 + self.keyCapacity:
    #             curIndex = parent.index(aNode)
    #             test = parent[curIndex - 2]
                
    #             ##If curnode is the most right one, use another sibling
    #             if curIndex-2 <0:
    #                 test = parent[curIndex+2]
    #                 if len(test) <= self.keyCapacity%2 + self.keyCapacity:
    #                     K = parent[curIndex+1] #index between cur and test
    #                     #copy aNode into beginning of the test node
    #                     while len(aNode)!=0:
    #                         test.insert(0,aNode.pop(len(aNode)-1))
    #                     #del parent[curIndex-1:curIndex+1]
    #                     return K
                    
    #                 #if node more than half full, allocate test into end of aNode
    #                 totalLen = len(aNode) + len(test)
    #                 a = 0
    #                 temp = len(aNode)
    #                 while a < totalLen//2-temp:
    #                     aNode.insert(len(aNode),test.pop(0))
    #                     a += 1
                    
    #                 key = curIndex + 1
    #                 parent[key] = test[0]
    #                 return None
                
    #             else:
    #                 #if node half full, join 
    #                 if len(test) <= self.keyCapacity%2 + self.keyCapacity:
    #                     K = parent[curIndex-1] #index between cur and test
    #                     i = 0
    #                     #copy aNode into test node
    #                     while len(aNode)!=0:
    #                         test.insert(len(test),aNode.pop(i))
    #                     #del parent[curIndex-1:curIndex+1]
    #                     return K
                    
    #                 #if node more than half full, allocate
    #                 totalLen = len(aNode) + len(test)
    #                 a = 0
    #                 temp = len(aNode)
    #                 while a < totalLen//2-temp:
    #                     aNode.insert(0,test.pop(len(test)-1))
    #                     a+=1
    #                 key = curIndex - 1
    #                 parent[key] = aNode[0]
    #                 return None
            
    #         else:
    #             return None
            
        
        
        
    #     #If aNode is a non-leaf, determing which subtree to delete from        
    #     else:
    #         i = self.getNextNode(aNode, aKey)
    #         #old Entry is a key to be deleted
    #         self.curLevel += 1
            
    #         oldEntry = self.toDelete(aNode, i, aKey)
    #         self.curLevel -= 1
            
    #         if oldEntry == None:
    #             return None
            
    #         else:
    #             if aNode[aNode.index(oldEntry)-1]==[] :
    #                 del aNode[aNode.index(oldEntry)-1:aNode.index(oldEntry)+1]
    #             else:
    #                 del aNode[aNode.index(oldEntry):aNode.index(oldEntry)+2]
                
    #             if (len(aNode) >= self.minP*2-1):   
    #                 return None
                
    #             else:
    #                 if self.curLevel == 1:
    #                     return None
    #                 curIndex = parent.index(aNode)
    #                 ###if node is the left most, check the sib on the right
    #                 if curIndex - 2 <0:
    #                     test = parent[curIndex+2]
    #                     #if test is just half full, merge two nodes
    #                     if len(test) <= self.minP*2-1:
    #                         K = parent[curIndex+1] #index between cur and test
    #                         test.insert(0,K)
    #                         #copy aNode into test node
    #                         while len(aNode)!=0:
    #                             test.insert(0, aNode.pop(len(aNode)-1))
    #                         #del parent[curIndex-1:curIndex+1]
    #                         return K
                    
    #                     totalLen = len(aNode) + len(test)
                        
    #                     a = 0
    #                     temp = len(aNode)
    #                     while a < totalLen//2-temp:
    #                         aNode.insert(len(aNode), parent[curIndex+1])
    #                         parent[curIndex+1] = test.pop(1)
    #                         aNode.insert(len(aNode), test.pop(0))
    #                         a += 2
    #                     return None
                    
    #                 else:
    #                     test = parent[curIndex - 2]
    #                     #if node half full, merge
    #                     if len(test) <= self.minP*2-1:
    #                         K = parent[curIndex-1] #index between cur and test
    #                         test.insert(K)
    #                         i = 0
    #                         #copy aNode into test node
    #                         while len(aNode)!=0:
    #                             test.insert(aNode.pop(i))
    #                         #del parent[curIndex-1:curIndex+1]
    #                         return K
                    
    #                     totalLen = len(aNode) + len(test)
    #                     a = 0
    #                     temp=len(aNode)
    #                     while a < totalLen//2-temp:
    #                         test.insert(len(test),parent[curIndex-1])
    #                         parent[curIndex-1] = aNode.pop(1)
    #                         test.insert(len(test), aNode.pop(0))
    #                         a += 2
                            
    #                     return None
                
            
def main():
    b = Bptree(4)
    b.insert(12,"hello")
    b.insert(1, "bye")
    b.insert(2,'a')
    b.insert(14,'b')
    b.insert(3,'c')
    # b.insert(4, 'd')
    # b.insert(5, 'e')
    b.insert(6, 'f')
    # b.insert(7, 'g')
    # b.insert(8, 'h')
    # b.insert(9, 'i')
    # b.insert(10,'j')
    # b.insert(11, 'k')
    # b.insert(13, 'l')
    # b.insert(14, 'm')
    # b.insert(15, 'n')
    # b.insert(16, 'o')
    # b.insert(17, 'p')
    # b.insert(18, 'q')
    # b.insert(19, 'r')
    # b.insert(20, 's')
    # b.insert(21, 't')
    # b.insert(22, 'u')
    # b.insert(23, 'v')
    # b.insert(25, 'k')
    # b.insert(26, 'k')
    b.insert(27, 'k')
    print b.getValue(6)
    b.printTree()
    # b.delete(3)
    # b.delete(6)
    # b.delete(7)
    # b.delete(1)
    # b.delete(8)
    # b.delete(10)
    # b.delete(11)
#     b.delete(7)
#     b.delete(1)
#     b.delete(8)
    # b.printTree()
main()        
            
        
        
        
        
        
        
        
        
        
        
        