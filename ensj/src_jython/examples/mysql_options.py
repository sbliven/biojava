#!/usr/bin/env jython

from __future__ import nested_scopes

__version__ = "$Revision$"

import sys

import ensembl

PARAM_NAMES = ["host", "port", "user", "password", "database"]

def make_option(param_name, configuration):
    return "--%s=%s" % (param_name, configuration[param_name])

def mysql_options(facade_name):
    configuration = getattr(ensembl, facade_name).configuration
    options = [make_option(param_name, configuration)
               for param_name in PARAM_NAMES
               if configuration[param_name] is not None]
    print " ".join(options)
    
def main(args):
    mysql_options(*args)

if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
