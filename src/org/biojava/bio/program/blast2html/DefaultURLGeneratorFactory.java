
package org.biojava.bio.program.blast2html;

import java.util.*;

public class DefaultURLGeneratorFactory implements URLGeneratorFactory {

    private static List oList = (List) new ArrayList();

    {
	//	oList.add( new EbiDatabaseURLGenerator() );
	oList.add( new NcbiDatabaseURLGenerator() );
    }

    public  List getDatabaseURLGenerators() {
	return oList;
    }
}
