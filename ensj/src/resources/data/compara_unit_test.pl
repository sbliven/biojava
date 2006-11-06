#
# Example of configuration file used by Bio::EnsEMBL::Registry::load_all method
# to store/register all kind of Adaptors.

use strict;
use Bio::EnsEMBL::Utils::ConfigRegistry;
use Bio::EnsEMBL::DBSQL::DBAdaptor;
use Bio::EnsEMBL::Compara::DBSQL::DBAdaptor;

my @aliases;

# CORE databases
################
new Bio::EnsEMBL::DBSQL::DBAdaptor(-host => 'ensembldb.sanger.ac.uk',
                                   -user => 'anonymous',
                                   -port => 3306,
                                   -species => 'Pan troglodytes',
                                   -group => 'core',
                                   -dbname => 'pan_troglodytes_core_29_2');

@aliases = ('chimp','PanTro1');

Bio::EnsEMBL::Utils::ConfigRegistry->add_alias(-species => "Pan troglodytes",
                                               -alias => \@aliases);

new Bio::EnsEMBL::DBSQL::DBAdaptor(-host => 'ensembldb.sanger.ac.uk',
                                   -user => 'anonymous',
                                   -port => 3306,
                                   -species => 'Caenorhabditis elegans',
                                   -group => 'core',
                                   -dbname => 'caenorhabditis_elegans_core_29_130');

@aliases = ('elegans');

Bio::EnsEMBL::Utils::ConfigRegistry->add_alias(-species => "Caenorhabditis elegans",
                                               -alias => \@aliases);

#new Bio::EnsEMBL::DBSQL::DBAdaptor(-host => 'ensembldb.sanger.ac.uk',
#                                   -user => 'anonymous',
#                                   -port => 3306,
#                                   -species => 'Caenorhabditis briggsae',
#                                   -group => 'core',
#                                   -dbname => 'caenorhabditis_briggsae_core_26_25');

#@aliases = ('briggsae');

#Bio::EnsEMBL::Utils::ConfigRegistry->add_alias(-species => "Caenorhabditis briggsae",
#                                               -alias => \@aliases);


new Bio::EnsEMBL::DBSQL::DBAdaptor(-host => 'ensembldb.sanger.ac.uk',
                                   -user => 'anonymous',
                                   -port => 3306,
                                   -species => 'Homo sapiens',
                                   -group => 'core',
                                   -dbname => 'homo_sapiens_core_29_35b');

@aliases = ('H_Sapiens', 'homo sapiens', 'Homo_Sapiens','Homo_sapiens', 'Homo', 'homo', 'human');

Bio::EnsEMBL::Utils::ConfigRegistry->add_alias(-species => "Homo sapiens",
                                               -alias => \@aliases);


new Bio::EnsEMBL::DBSQL::DBAdaptor(-host => 'ensembldb.sanger.ac.uk',
                                   -user => 'anonymous',
                                   -port => 3306,
                                   -species => 'Mus musculus',
                                   -group => 'core',
                                   -dbname => 'mus_musculus_core_29_33e');

@aliases = ('M_Musculus', 'mus musculus', 'Mus_Musculus','Mus_musculus', 'Mus', 'mus', 'mouse','Mm5');

Bio::EnsEMBL::Utils::ConfigRegistry->add_alias(-species => "Mus musculus",
                                               -alias => \@aliases);

new Bio::EnsEMBL::DBSQL::DBAdaptor(-host => 'ensembldb.sanger.ac.uk',
                                   -user => 'anonymous',
                                   -port => 3306,
                                   -species => 'Rattus norvegicus',
                                   -group => 'core',
                                   -dbname => 'rattus_norvegicus_core_29_3f');

@aliases = ('R_Norvegicus', 'rattus norvegicus', 'Rattus_Norvegicus','Rattus_norvegicus', 'Rattus', 'rattus', 'rat', 'Rn3');

Bio::EnsEMBL::Utils::ConfigRegistry->add_alias(-species => "Rattus norvegicus",
                                               -alias => \@aliases);

new Bio::EnsEMBL::DBSQL::DBAdaptor(-host => 'ensembldb.sanger.ac.uk',
                                   -user => 'anonymous',
                                   -port => 3306,
                                   -species => 'Fugu rubripes',
                                   -group => 'core',
                                   -dbname => 'fugu_rubripes_core_29_2e');

@aliases = ('F_Rubripes', 'fugu rubripes', 'Fugu_Rubripes','Fugu_rubripes', 'Fugu', 'fugu');

Bio::EnsEMBL::Utils::ConfigRegistry->add_alias(-species => "Fugu rubripes",
                                               -alias => \@aliases);

new Bio::EnsEMBL::DBSQL::DBAdaptor(-host => 'ensembldb.sanger.ac.uk',
                                   -user => 'anonymous',
                                   -port => 3306,
                                   -species => 'Gallus gallus',
                                   -group => 'core',
                                   -dbname => 'gallus_gallus_core_29_1e');

@aliases = ('G_Gallus', 'gallus gallus', 'Gallus_Gallus','Gallus_gallus', 'Chicken', 'chicken', 'GalGal2');

Bio::EnsEMBL::Utils::ConfigRegistry->add_alias(-species => "Gallus gallus",
                                               -alias => \@aliases);

new Bio::EnsEMBL::DBSQL::DBAdaptor(-host => 'ensembldb.sanger.ac.uk',
                                   -user => 'anonymous',
                                   -port => 3306,
                                   -species => 'Danio rerio',
                                   -group => 'core',
                                   -dbname => 'danio_rerio_core_29_4c');

@aliases = ('D_Rerio', 'danio rerio', 'Danio_Rerio','Danio_rerio', 'Danio', 'zebrafish');

Bio::EnsEMBL::Utils::ConfigRegistry->add_alias(-species => "Danio rerio",
                                               -alias => \@aliases);

# COMPARA databases
###################

new Bio::EnsEMBL::Compara::DBSQL::DBAdaptor(-host => 'ensembldb.sanger.ac.uk',
                                            -user => 'anonymous',
                                            -port => 3306,
                                            -species => 'Compara26',
                                            -dbname => 'ensembl_compara_29');

@aliases = ('ensembl_compara_26_1', 'compara26', 'compara');

Bio::EnsEMBL::Utils::ConfigRegistry->add_alias(-species => "Compara26",
                                               -alias => \@aliases);
1;
