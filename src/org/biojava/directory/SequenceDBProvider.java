package org.biojava.directory;

import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.db.*;

public interface SequenceDBProvider {
    public String getName();
    public SequenceDB getSequenceDB(Map config) throws RegistryException, BioException;
}
