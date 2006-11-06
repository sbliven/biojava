#!/usr/bin/env python

import MySQLdb,sets,sys,fileutil,os

__doc__= """Creates a retrofit patch file for stable_event_table that contains entries for
all genes, transcripts and translations that were mapped, deleted and created during a mapping session
between a source and target database.

Parameters are read from a standard IDMapping configuration file.

Config file must include "idmapping.retrofit_mapping_session_id=X" where X is set to value
of the appropriatte mapping session.

Requires python MySQLdb module. See below.

Usage:
  patch_stable_event_created_deleted_mapped.py IDMAPPING.CONFIG_FILE    - create patch between databases
  patch_stable_event_created_deleted_mapped.py -h                       - show this help info

A compiled version of the MySQLdb module is available for alphas in
the authors home directory. You can use this by adding an entry to
your PYTHONPATH. e.g in bash:

   export PYTHONPATH=/nfs/acari/craig/python/alpha-lib

"""

if len(sys.argv)>1 and sys.argv[1]=="-h":
    print __doc__
    sys.exit(0)


# development option.
if len(sys.argv)==1:
    sys.argv.append("/ecs2/work6/craig/idmapping/mouse_39_patch/idmapping.properties")


def extract_db_config(config, prefix):
    return {"host":config[prefix+"host"],
            "user":config[prefix+"user"],
            "passwd":config[prefix+"password"],
            "db":config[prefix+"database"],
            "port":int(config[prefix+"port"])}

def load(config):
    map = {}
    conn = MySQLdb.connect(**config)
    for type in ("gene","transcript","translation"):
        # load (stableID,version,type)
        cursor = conn.cursor()
        sql = "SELECT stable_id, version from %s_stable_id " % (type,)
        cursor.execute(sql)
        rs = cursor.fetchall()
        for r in rs:
            v = (r[0],str(r[1]),type)
            map[v[0]] = v
    conn.close()
    return map

#        self.ids = sets.Set()
#         cursor = self.conn.cursor()
#        cursor.execute("select concat(status,'-',logic_name,'-',biotype) as name, stable_id from gene g ,analysis a, gene_stable_id gsi where g.analysis_id=a.analysis_id and gsi.gene_id=g.gene_id")
#        rs = cursor.fetchall()
#        for r in rs:
#            a = r[0]
#            id = r[1]
#            self.ids.add(id)
#            if self.analysis2id.has_key(a):
#                self.analysis2id[a].append(id)
#            else:
#                self.analysis2id[a] = [id]

#    def summary(self, label, total, n, percentage):
#        return "%(label)40s %(total)10i %(n)10i %(percentage)10.2f%%" % locals()

#        mapped = self.ids.intersection(other.ids)
#        deleted = self.ids.difference(other.ids)
#        deletedSimilar = sets.Set()
#        cursor.execute(sql)
#        rs = cursor.fetchall()
            
if __name__=="__main__":

    config = fileutil.load_properties(sys.argv[1],"=")

    src = load(extract_db_config(config, "idmapping.source."))
    tgt = load(extract_db_config(config, "idmapping.target."))

    srcIDs = sets.Set(src.keys())
    tgtIDs = sets.Set(tgt.keys())

    deleted = srcIDs.difference(tgtIDs)
    mapped = srcIDs.intersection(tgtIDs)
    created = tgtIDs.difference(srcIDs)

    mapping_session_id = config["idmapping.retrofit_mapping_session_id"]

    base_filename = "stable_id_event_retrofit_created_deleted_mapped"
    results_file = os.path.abspath(base_filename+".txt")
    sql_file = os.path.abspath(base_filename+".sql")
    
    OUT = open(results_file, "w")
    for s in [src[x] for x in deleted]:
        print >>OUT, "\t".join((s[0], s[1], "\\N", "0", mapping_session_id, s[2]))
    for s in [src[x] for x in mapped]:
        t = tgt[s[0]]
        print >>OUT, "\t".join((s[0], s[1], t[0], t[1], mapping_session_id, s[2]))
    for t in [tgt[x] for x in created]:
        print >>OUT, "\t".join(("\\N", "0", t[0], t[1], mapping_session_id, t[2]))
    OUT.close()

    OUT = open(sql_file, "w")
    print >>OUT, "LOAD DATA INFILE '%s' INTO TABLE stable_id_event;" % (results_file)
    OUT.close()    

    print "Deleted",len(deleted)
    print "Mapped", len(mapped)
    print "created", len(created)
    print
    print "Results file:", results_file
    print "SQL patch file:", sql_file


