# Simple jython script that configures a registry.

# The script is used to create a RegistryLoaderJython instance and
# when the instance's load(Registry) method is called a jython
# interpreter is created, the registry variable is added to the
# environment and then the script is executed.

# See the documentation for org.ensembl.registry.RegistryLoaderJython
# for more information.

# Craig Melsopp 2005
# Copyright EBI, GRL

from java.util import Properties
from org.ensembl.registry import DriverGroup

# Construct the coreConfig properties object
cf = Properties()
cf.put("host", "ensembldb.ensembl.org")
cf.put("user", "user")
cf.put("database_prefix", "homo_sapiens_core")

# create variation config by copying the core config and cnaging the database_prefix
vf = Properties(cf) 
vf.put("database_prefix", "homo_sapiens_variation")

# construct the homo_sapiens DriverGroup and add it to the registry
registry.add("homo_sapiens", DriverGroup(coreConfig=cf, variationConfig=vf))
