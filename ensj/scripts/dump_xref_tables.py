#!/usr/bin/env python

import report,sys,fileutil

db = fileutil.load_properties(sys.argv[1])

for table in ["xref","object_xref"]:
    cmd = "mysqldump -h %s -P%s -uensro -t %s %s > %s" % (db["host"],
                                                          db["port"],
                                                          db["database"],
                                                          table,
                                                          table)
    #print cmd
    report.exec_command(cmd)

