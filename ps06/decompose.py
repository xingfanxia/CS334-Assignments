class HashableList(list):
    def __hash__(self):
        return "hashable_list:{}".format(str(self)).__hash__()

# helper find all subsets of a set and return in list
def subsets(s):
    L = list(s)
    subs = [{L[j] for j in range(len(L)) if 1<<j&i} for i in range(1,1<<len(L))]
    return subs

def closure(elems, fds):
    """
    Find closure for given functional dependencies.
    Args:
        elems <list>            Elements in question for closure
        fds <list of tuples>    Tuples of x -> b in form ([x1,x2,x3], [y1,y2])
    Returns:
        set of elements under closure.
    """
    fp = set()
    result = subsets(elems)
    for subset in subsets(elems):
        print(subset)
        for i in allfds:
            if i[0] in subset:
                result.union(i[1])
    print("the X+: {}".format(result))


elems = [1, 2, 3, 4,5]
fd1 = (HashableList([1]),HashableList([2]))
fd2 = (HashableList([2,3]),HashableList([4,5]))
allfds = [fd1, fd2]
print(allfds[0][0])
a = set(allfds) # => {([1],[2]),([2,3],[4,5])}
print(a)
closure(elems, allfds)
# print(closure(elems, allfds))