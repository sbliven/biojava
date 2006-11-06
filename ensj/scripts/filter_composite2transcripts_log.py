#!/usr/bin/env python

# Filters log_file entries that do not contain the composite and
# transcript lines in mapping_file.

import os,sys

if len(sys.argv)<3:
    print "Usage: filter_composite2transcripts_log.py LOG_FILE MAPPED_FILE"
    sys.exit(0)

    
log_file = sys.argv[1]
mapping_file = sys.argv[2]

try:
    lines
except NameError:
    lines = open(log_file).readlines()
    #lines.sort()

try:
    mappings
except NameError:
    mappings = open(mapping_file).readlines();

import time,re,random

random.shuffle(mappings)

for mapping in mappings:
    (c,t) = mapping.strip().split()
    p = re.compile("^%s.*%s" % (c,t))
    for l in lines:
        if p.match(l):
            print l, # use , because line has a new line char
            break    # might be multiple entries because composite in N arrays
            

