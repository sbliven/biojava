#!/usr/bin/env jython

import fileinput,sys
from ensembl import DriverFacade

if len(sys.argv)<5:
    print """Prints where probeset and transcript pairs overlap.

    File/stdin format is 2 tab separated values per line:

    PROBESET_NAME1\tTRANSCRIPT_STABLE_ID1
    PROBESET_NAME2\tTRANSCRIPT_STABLE_ID2
    PROBESET_NAME3\tTRANSCRIPT_STABLE_ID3
    ...
    
    usage:
    # Read values from file
    affy_debug.py HOST PORT DATABASE USER [FILE1 [FILE2 ...] ] | STDIN

    # Read values from STDIN
    affy_debug.py HOST PORT DATABASE USER """
    sys.exit()

#sys.argv = [sys.argv[0], "127.0.0.1", "23364", "homo_sapiens_core_30_35c", "ensro", "/tmp/human_lost.txt"]
host,port,database,user = sys.argv[1:5]
d = DriverFacade({"database": database, "user":user,"port":port,"host":host})

def displayTranscriptAndProbeHits(d,pName,tAccession, printMisses=1):
    t = d.transcriptAdaptor.fetch(tAccession)
    print pName,tAccession,"t=",t
    if t:
        tl = t.location.transform(0,2000)
        ps = d.affyProbeAdaptor.fetch(pName)
        if len(ps)==0:
            print pName,tAccession, "ps=",ps
        else:
            for p in ps:
                for l in p.uniqueAffyFeatureLocations:
                    hit = tl.overlaps(l,0)
                    if hit or printMisses:
                        cdnaLoc = t.getCDNALocation().transform(0,2000)
                        print pName,tAccession,"f=",l, "transcript_overlap=",tl.overlapSize(l), "cdna_overlap=",cdnaLoc.overlapSize(l)

sys.argv = sys.argv[0:1]+sys.argv[5:]
for line in fileinput.input():
   s = line.strip().split()
   if len(s)==2:
       displayTranscriptAndProbeHits(d,s[0],s[1],0)
