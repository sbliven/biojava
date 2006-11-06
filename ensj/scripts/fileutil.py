def load(filename,separator=":", col_indexes=None):
    """ Loads data from a file stripping off white space/
    newlines and extracting specified columns. If more than one column
    is specified they are concatenated using the _separator_ string.

    Examples:
    -load("somefile.txt") # load all the lines in file, one row per element
    -load("somefile.txt","<->",[0,2]) # for each line in file create an element col0<->col2.
    """
    try:
        lines = file(filename).readlines()
    except NameError:
        lines = open(filename).readlines()
    lines = [x.strip() for x in lines]
    if col_indexes:
        n = len(col_indexes)
        for i in range(0,len(lines)):
            cols = lines[i].split()
            tmp = cols[col_indexes[0]]
            for j in range(1,n):
                tmp = tmp + separator + cols[col_indexes[j]]
            lines[i] = tmp
    return lines


def load_properties(filename, sep=None):

    """ Loads the file into a dictionary where the first element in a
    line is the key and the remaining entries the value. """

    p = {}
    for line in open(filename).readlines():
        if line[0]=="#":
            continue
        elements = line.split(sep)
        if (len(elements)>1):
            p[elements[0].strip()]= line[line.index(elements[1]):].strip()

    return p
