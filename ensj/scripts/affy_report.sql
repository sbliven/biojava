# Prints useful stats about affy related data in a database

# tip: use 'vertical' mode of mysql client to format output in more readable properties stye format: mysql -E ...

select count(*) as count_xref, min(xref_id), max(xref_id) from xref;

select count(*) as count_object_xref, min(object_xref_id), max(object_xref_id) from object_xref;

select count(*) as count_affys_in_object_xref from object_xref ox, xref x where ox.xref_id=x.xref_id and external_db_id>3000 and external_db_id<3200;

select count(*) as count_affys_in_xref from xref where external_db_id>3000 and external_db_id<3200;

select count(*) as count_transcript from transcript;

select count(distinct(ensembl_id)) as count_transcripts_mapped_to_probesets from object_xref ox, xref x where ox.xref_id=x.xref_id and external_db_id>3000 and external_db_id<3200;

select count(distinct(probeset)) as count_probeset from affy_probe;

select count(distinct(display_label))  as count_probesets_mapped_to_transcripts from object_xref ox, xref x where ox.xref_id=x.xref_id and external_db_id>3000 and external_db_id<3200;
