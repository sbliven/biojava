# SQL that can be run against a target database to clean it up before ID mapping
# Deletes LOTs of data from LOTS of tables. Use with care!

delete from gene_stable_id;
delete from transcript_stable_id;
delete from translation_stable_id;
delete from exon_stable_id;
delete from mapping_session;
delete from stable_id_event;
delete from gene_archive;
delete from peptide_archive;