package org.Biocorba.Seqcore;

import java.lang.*;

public interface _SeqFeatureIterator_Operations extends GNOME._Unknown_Operations {

  public org.Biocorba.Seqcore.SeqFeature next(org.omg.CORBA.portable.ObjectImpl seqFeatureIterator) throws org.Biocorba.Seqcore.EndOfStream, org.Biocorba.Seqcore.UnableToProcess;

  public boolean has_more(org.omg.CORBA.portable.ObjectImpl seqFeatureIterator);
}
