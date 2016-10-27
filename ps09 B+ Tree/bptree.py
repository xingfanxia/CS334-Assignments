#!/usr/bin/env python
# -*- coding: utf-8 -*-
# @Date    : 2016-10-23 21:37:29
# @Author  : Xingfan Xia (xiax@carleton.edu)
# @Link    : http://xiax.tech
# @Version : $Id$
# A very simple BPlus Tree Implementation
# Credits: Inspiration from project partner Vianne

# class BNode(object):
# 	def __init__(self, tree, index=None, children=None):
# 		self.tree = tree
# 		self.index = index or []
# 		self.children = children or []
# 		if self.children:
# 			assert len(self.index) + 1  == len(self.children), "Should have one more child than data"

# 	def __str__(self):
# 		return "The Node has index {} \n and children pointers {} \n".format(self.index, self.children)



# class BLeaf(BNode):
# 	def __init__(self, tree, index=None, data=None):
# 		self.tree = tree
# 		self.index = index or []
# 		self.data = data or []
# 		assert len(self.index) == len(self.data), "requires a pair of key and data"

# 	def __str__(self):
# 		return "The Node has index {} \n and data {} \n".format(self.index, self.data)


class Bptree(object):

	'''
	init the func
	'''
	def __init__(self, order):
		self.order = order
		self.root = [] #empty root
		self.capacity = order
		self.curLevel = 0
		self.height = 1

	'''
	helper for insert
	'''
	def insert_helper(self, node, key, value):
		if node == self.root:
			self.curLevel = 1

		if self.curLevel != self.height: # if a leaf node
			k = 1 # tracking counter 1
			while (k < len(node)) and (key > node[k]):
				k += 2	#find where to put k

			self.curLevel += 1
			new_record = self.insert_helper(node[k-1], key, value)
			self.curLevel -= 1

			if new_record == None: #if no split happening
				return None

			elif (len(node) < 2*self.capacity+1): # if split parent no full
				m = 1
				while (m < len(node)) and (new_record[0] >= node[m]):
					m += 2	#find where to put k	

				node.insert(m, new_record[0])
				node.insert(m+1, new_record[1])
				return None

			else: # parent also full, double split
				m = 1
				while(m < len(node) and (new_record[0] >= node[m])):
					m += 2
				node.insert(m, new_record[0])
				node.insert(m+1, new_record[1])	

				neighbor = []
				l = self.capacity - (self.capacity%2 -1)
				to_push = node.pop(l)
				while (l<len(node)):
					neighbor.append(node.pop(l))			

				new_record = (to_push, neighbor)

				# update level
				if self.curLevel == 1:
					temp = []
					temp.append(node)
					temp.append(to_push)
					temp.append(neighbor)
					self.root = temp
					self.height += 1

				return new_record

				
		# if not a leaf
		else:
			k = 0 # tracking counter 1
			while (k < len(node)) and (key > node[k]):
				k += 2

			if (k < len(node)) and (key == node[k]):
				raise ValueError("Duplicate Entry not allowed")

			# put in values to the node list
			node.insert(k,key) 
			node.insert(k+1, value)

			# if the leaf node is over full
			if len(node) > 2*self.capacity+1:
				new_n = []
				m = self.capacity%2 + self.capacity # tracking counter
				while (m < len(node)):
					new_n.append(node[m])
					m += 1

				# Remove old list and copy to new
				del node[self.capacity % 2 + self.capacity : len(node)]
				new_record = (new_n[0], new_n)

				# if splitting the root
				if self.curLevel == 1:
					new_root = []
					new_root.append(node)
					new_root.append(new_n[0])
					new_root.append(new_n)
					self.root = new_root
					self.height += 1 # increment height of tree
				else:
					return new_record
			else:
				return None
	'''			
	Inserts a key-value pair into the tree.
	'''
	def insert(self, key, value):
		self.level = 0
		self.insert_helper(self.root, key, value)

	def nextEntry(self, curNode, key):
		i = 1
		while (i < len(curNode) and (key >= curNode[i])):
			i += 2

		return curNode[i-1]

	def getData(self, curNode, key):
		i = 0
		while (i < len(curNode) and (key > curNode[i])):
			i += 2

		if i >= len(curNode) or curNode[i] != key:
			return None

		else:
			return curNode[i+1]


	'''
	Returns the value associated with a particular key. Returns None if the key is not in the tree.
	'''
	def getValue(self, key):
		curNode = self.root
		curLevel = 1

		while (curLevel < self.height):
			curNode = self.nextEntry(curNode, key)
			curLevel += 1

		data = self.getData(curNode, key)

		if data == None:
			return None

		else:
			return data

	def printNode(self, node, level):
		i = len(node) - 1
		curLevel = level
		print_list = []

		if (curLevel != self.height):
			j = 1
		else:
			j = 0

		for k in range(2*(level-1)):
			print_list.append(" ") # Depth

		while j < len(node):
			print_list.append(node[j]) # add everything in the curNode to the print_list
			j += 2

		for c in range(len(print_list)):
			print print_list[c], 
		print
		if (curLevel != self.height): #if not leaf yet, recursive call
			while i >= 0:
				self.printNode(node[i], curLevel+1)
				i -= 2 #Going back

		else: # end
			return


	'''
	Prints out a text version of the tree to the screen. The easiest way I found to do this was to rotate the tree vertically, showing the root on the left hand side of the terminal window, expanding to the right, and indenting further for deeper levels. Doing a recursive depth-first traversal of the tree made this pretty easy.
	'''
	def printTree(self):
		print '''
This is the BPTree:
======================================================
	 '''
		self.printNode(self.root, 1)

def main():
	b = Bptree(4)
	data = ['a', 'b', 'd', 'e', 'c', 'f', 'g', 'w', 'm', 'z', 'x', 'v']
	for i in range(len(data)):
		b.insert(i, data[i])
	b.insert(12,"hello")
	b.insert(24,"bye")

	print b.getValue(24)
	print b.getValue(3)
	b.printTree()

if __name__ == '__main__':
	main()