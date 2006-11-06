#!/usr/bin/env python


"""Updates a cvs directory with the contents of another directory.
All files/dirs are copied from SRC_DIR_ROOT to CVS_DIR_ROOT and any new ones
are added to cvs and obsolete ones removed from cvs.

USAGE:
      update-cvs-dir-tree.py -h                          - this help
      update-cvs-dir-tree.py --help                      - this help
      update-cvs-dir-tree.py CVS_DIR_ROOT SRC_DIR_ROOT   - update CVS_DIR_ROOT with contents of SRC_DIR_ROOT

Useful command for comparing 2 dirs:
      diff -qr CVS_DIR_ROOT SRC_DIR_ROOT | grep -v CVS | grep Only | more
      
"""

import os,sets,shutil, sys, getopt

def safe_dir(d):
    return os.path.abspath(d)+os.path.sep


def recurse(root):
    paths = []
    def add_paths(paths, dir, files):
        if dir.endswith("CVS"):
            return
        for f in files:
            if f.endswith("CVS") or f.endswith("#") or f.endswith("~") :
                continue
            else:
                paths.append(os.path.join(dir,f))
    os.path.walk(root, add_paths, paths)
    root_len = len(root)
    paths = [path[root_len:] for path in paths]
    paths.sort()
    return paths


def update(cvs_root, src_root):

    cvs_root = safe_dir(cvs_root)
    src_root = safe_dir(src_root)

    def cvsd(path):
        return os.path.join(cvs_root, path)


    def srcd(path):
        return os.path.join(src_root, path)

    cvs_set = sets.ImmutableSet(recurse(cvs_root))
    src_set = sets.ImmutableSet(recurse(src_root))

    added = list(src_set.difference(cvs_set))
    deleted = list(cvs_set.difference(src_set))
    
    new_dirs = [cvsd(f) for f in added if os.path.isdir(srcd(f))]
    for d in new_dirs:
        os.mkdir(d)

    frm2to = [(srcd(f), cvsd(f)) for f in src_set if os.path.isfile(srcd(f)) ]
    for (frm, to) in frm2to:
        shutil.copy(frm,to)

    added.sort() # so we add dirs before files
    for a in added:
        os.chdir(os.path.dirname(cvsd(a)))
        os.system("cvs add " + os.path.basename(a))
    
    for f in deleted:
        if os.path.isfile(cvsd(f)):
            os.chdir(os.path.dirname(cvsd(f)))
            os.system("cvs rm -f " + os.path.basename(f))
                      
    os.chdir(cvs_root)
    # commit
    os.system("cvs ci -m 'Automatically updated java documentation.'")
    # Update and prune
    os.system("cvs up -Ad")

    print "\n".join(["DELETED\t%s" % p for p in deleted])
    print "\n".join(["ADDED\t%s" % p for p in added])



def main(argv=None):
    """ Runs cvs directory update.
    If argv is undefined parameters will be read from sys.argv at run time.
    Returns 0 if function completes successfully and non-0 otherwise."""
    
    original_dir = os.getcwd()
    
    if argv is None:
        argv = sys.argv
    try:
        opts, args = getopt.getopt(argv[1:], "h", ["help"])
        if "-h" in argv or "--help" in argv: # using argv saves us processing opts
            print __doc__
            return 0
        elif len(args)>=2:
            cvs_root = args[0]
            src_root = args[1]
            update(cvs_root, src_root)
            os.chdir(original_dir)
            return 0
        else:
            raise "Error: you must provide 2 parameters: cvs directory and source directory\n"+__doc__

    except Exception, err:
        os.chdir(original_dir)
        print >>sys.stderr, err
        print >>sys.stderr, "for help use --help"
        return 2

    
if __name__=="__main__":

    # set to 1 and enter default dirs as params for development and testing
    if 0:
        main([__file__,
              "/home/craig/dev/ensembl-website/htdocs/info/software/java",
              "/home/craig/dev/ensj-core/build/docs"])
    else:
        sys.exit(main())
    

