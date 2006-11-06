#!/usr/bin/env python


# Creates a report and various files that compare
# probeset2transcript, probeset2gene and probeset2conserved_gene
# mappings in two databases. User spcifies databases via property files.

# Files include:
# -report.txt - summary file
# -*venn*.txt - 'venn diagram' set files where files a and b
#               become a.only,a.b.intersection and b.only

# Dependencies: required programs mysql

# For usage run program with no parameters: shell>report.py. It is
# probably worth creating a separate report dir to hold the output
# files.


import sys,fileutil,os,set,format

AFFY_XREF_FILTER_CLAUSE ="external_db_id>3000 and external_db_id<3200"


def help():
    return """report.py:
    
    Compares probeset to transcript/gene mappings in OLD and NEW
    database.  Supports importing tables from an support database
    if NEW database does not contain them (e.g. NEW is a test output
    database).

    The database connection parameters are stored in property files
    with entries for at least host,user,port and database.
    
    Usage:report.py OLD_DB.properties NEW_DB.properties [NEW_DB_SUPPORT.properties]"""




def exec_command(cmd):
    print "\tCOMMAND:",cmd
    for line in os.popen(cmd).readlines():
        print "\t\tOUTPUT:", line

    

def dump_table_to_file(table_name, db):
    f = table_name+".txt"
    if not os.path.exists(f):
        cmd = "mysql -N -B -h %s -P%s -u ensadmin -pensembl -e \"select * from %s\" %s > %s" % (db["host"],
                                                                                          db["port"],
                                                                                          table_name,
                                                                                          db["database"],
                                                                                          f)
        exec_command(cmd)
        



def upload_file_to_table(table_name, db):
    cmd = "mysqlimport -h %s -P%s -u ensadmin -pensembl %s  %s.txt" % (db["host"],
                                                                      db["port"],
                                                                      db["database"],
                                                                      table_name)
    exec_command(cmd)



def dump_probeset2transcript(db):

    """ Creates dump file if it doesn't exist from db. File line
    format is "probesetAccession\ttranscriptAccession". Returns
    filename."""

    f = "_".join(("probeset2transcript",db["database"], db["host"], db["port"],".txt"))
    if not os.path.exists(f):
        cmd = """mysql -h %s -P%s -u ensadmin -pensembl \
        -e "select dbprimary_acc,stable_id from transcript_stable_id tsi, object_xref ox, xref x \
        where  %s and tsi.transcript_id=ox.ensembl_id and ox.ensembl_object_type='Transcript' \
        and ox.xref_id=x.xref_id" %s > %s""" % (db["host"],
                                                db["port"],
	AFFY_XREF_FILTER_CLAUSE,
                                                db["database"],
                                                f)

        exec_command(cmd)
    return f


def dump_probeset2gene(db):

    """ Creates file from db specified in db if that file does not already exist.
    File line format is "probesetAccession\tgeneAccession". Returns filename."""

    f = "_".join(("probeset2gene",db["database"], db["host"], db["port"],".txt"))
    if not os.path.exists(f):
        cmd = """mysql -h %s -P%s -u ensadmin -pensembl \
        -e "select dbprimary_acc, stable_id from xref x, object_xref ox, transcript t, gene_stable_id gsi \
        where %s and x.xref_id=ox.xref_id and t.transcript_id=ensembl_id \
        and ox.ensembl_object_type='Transcript' \
        and gsi.gene_id=t.gene_id group by stable_id, dbprimary_acc " %s > %s""" % (db["host"],
                                                                                    db["port"],
	AFFY_XREF_FILTER_CLAUSE,
                                                                                    db["database"],
                                                                                    f)

        exec_command(cmd)
    return f


def dump_probeset2gene_conserved(conserved_genes, in_filename):

    """ Filters lines in 'in_filename' writing those that contain a
    gene in 'conserved_genes' to a file called
    'in_filename'+.conserved. Returns this new filename."""

    genes = set.list2dict(conserved_genes)
    lines = [line for line in fileutil.load(in_filename) if genes.has_key(line.split()[1])]

    out_filename = in_filename+".conserved"
    out = open(out_filename,"w")
    for line in lines:
        out.write(line.strip())
        out.write("\n")
    out.close()
    
    return out_filename


def num_genes(db):
    f = "gene_count_"+db["database"]+"_"+db["host"]+"_"+db["port"]+".txt"
    if not os.path.exists(f):
        cmd = "mysql -N -B -h %s -P%s -u ensro -e \"select count(*) from gene\" %s > %s" % (db["host"], db["port"], db["database"], f)
        exec_command(cmd)

    return int(open(f).readlines()[0].strip())



def num_probesets(db):
    f = "probeset_count_"+db["database"]+"_"+db["host"]+"_"+db["port"]+".txt"
    if not os.path.exists(f):
        cmd = "mysql -N -B -h %s -P%s -u ensro -e \"select count(distinct(value)) from misc_attrib ma, attrib_type at where at.attrib_type_id=ma.attrib_type_id and code='probesetName'\" %s > %s" % (db["host"], db["port"], db["database"], f)
        exec_command(cmd)
        
    return int(open(f).readlines()[0].strip())



def summary(title, (a,b,intersection)):

    """ Returns a summary string containing the the specified title
    and row for a,b, and intersection.  """

    n_a = len(a)
    n_b = len(b)
    n_intersection = len(intersection)
    total =n_a + n_b +n_intersection
    import format
    return title + ":\n" + format.table_format((("old mappings", n_a, total),
                                                ("new mappings", n_b, total),
                                                ("same mappings", n_intersection, total)))



if __name__=="__main__":
    n_args = len(sys.argv)
    if n_args<3:
        print help()
        sys.exit(0)
        
    old_db = fileutil.load_properties(sys.argv[1])
    new_db = fileutil.load_properties(sys.argv[2])


    if n_args>3:
        support_db = fileutil.load_properties(sys.argv[3])
    else:
        support_db = None


    if support_db and raw_input("Import support tables gene_stable_id, transcript_stable_id and transcript (y/n)?").lower()=="y":
        dump_table_to_file("transcript", support_db)
        upload_file_to_table("transcript", new_db)

        dump_table_to_file("transcript_stable_id", support_db)
        upload_file_to_table("transcript_stable_id", new_db)
        
        dump_table_to_file("gene_stable_id",support_db)
        upload_file_to_table("gene_stable_id",new_db)


    # Generate intermediate and categorisation files
    old_probeset2gene_file = dump_probeset2gene(old_db)
    new_probeset2gene_file = dump_probeset2gene(new_db)
    conserved_genes = set.venn(fileutil.load(old_probeset2gene_file,":",[1]),
                               fileutil.load(new_probeset2gene_file,":",[1]))[2]
    old_probeset2gene_conserved_file = dump_probeset2gene_conserved(conserved_genes,
                                                                      old_probeset2gene_file)
    new_probeset2gene_conserved_file = dump_probeset2gene_conserved(conserved_genes,
                                                                      new_probeset2gene_file)

    gs={}
    cs={}
    c2gs = fileutil.load(new_probeset2gene_file)
    for c2g in c2gs:
        (c,g) = c2g.strip().split()
        cs[c]=1
        gs[g]=1
    n_mapped_genes = len(gs.keys())
    n_genes = num_genes(new_db)
    n_mapped_probesets = len(cs.keys())
    n_probesets = num_probesets(new_db)

    report = "Mapped items in new database (" + new_db["database"]+"):\n"
    report = report + format.table_format((("Genes", n_mapped_genes, n_genes),
                                           ("Probesets", n_mapped_probesets, n_probesets)))

    report = report + "\n\nComparing mappings in old database (%s) and new database (%s)\n\n" % (old_db["database"], new_db["database"])

    report = report + summary("probeset2transcript",
                     set.venn_and_dump(dump_probeset2transcript(old_db), dump_probeset2transcript(new_db)))

    report = report + "\n" + summary("probeset2gene", set.venn_and_dump(old_probeset2gene_file,
                                                                         new_probeset2gene_file))

    report = report + "\n" + summary("probeset2gene_conserved",
                                     set.venn_and_dump(old_probeset2gene_conserved_file,
                                                       new_probeset2gene_conserved_file))


    out = open("report.txt","w")
    out.write(report)
    out.close()

    print report
    print "Above summary stored in report.txt."


    
    

    
