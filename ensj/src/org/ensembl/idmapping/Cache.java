/*
 * Created on Aug 24, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.ensembl.idmapping;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ensembl.datamodel.Exon;
import org.ensembl.datamodel.Gene;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.Transcript;
import org.ensembl.datamodel.Translation;
import org.ensembl.driver.AdaptorException;
import org.ensembl.util.ProgressPrinter;
import org.ensembl.util.StringUtil;
import org.ensembl.util.Util;

import cern.colt.list.ObjectArrayList;
import cern.colt.map.OpenLongObjectHashMap;

/**
 * @author arne
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class Cache implements Serializable {

	private static final long serialVersionUID = 1L;

	private OpenLongObjectHashMap sourceGenesByInternalID = new OpenLongObjectHashMap();

	private OpenLongObjectHashMap targetGenesByInternalID = new OpenLongObjectHashMap();

	private Map sourceGenesByStableID = new HashMap();

	private Map sourceTranscriptsByStableID = new HashMap();

	private OpenLongObjectHashMap sourceExonsByInternalID = new OpenLongObjectHashMap();

	private OpenLongObjectHashMap targetExonsByInternalID = new OpenLongObjectHashMap();

	private OpenLongObjectHashMap sourceTranscriptsByInternalID = new OpenLongObjectHashMap();

	private OpenLongObjectHashMap targetTranscriptsByInternalID = new OpenLongObjectHashMap();

	private OpenLongObjectHashMap sourceTranslationsByInternalID = new OpenLongObjectHashMap();

	private OpenLongObjectHashMap targetTranslationsByInternalID = new OpenLongObjectHashMap();

	// Map of lists
	private OpenLongObjectHashMap sourceTranscriptsByExonInternalID = new OpenLongObjectHashMap();

	private OpenLongObjectHashMap targetTranscriptsByExonInternalID = new OpenLongObjectHashMap();

	private Map sourceTranslationsByTranscriptInternalID = new HashMap();

	private Map targetTranslationsByTranscriptInternalID = new HashMap();

	private Map sourceTranslationsByStableID = new HashMap();

	private OpenLongObjectHashMap sourceGeneByTranscriptInternalID = new OpenLongObjectHashMap();

	private OpenLongObjectHashMap targetGeneByTranscriptInternalID = new OpenLongObjectHashMap();

	private OpenLongObjectHashMap sourceGeneByExonInternalID = new OpenLongObjectHashMap();

	private OpenLongObjectHashMap targetGeneByExonInternalID = new OpenLongObjectHashMap();

	private Map exonMappingsMap = new HashMap();

	private Map transcriptMappingsMap = new HashMap();

	private Map translationMappingsMap = new HashMap();

	private Map geneMappingsMap = new HashMap();

	public Cache(Config conf) {

		try {

			// We need separate sourceLoc and targetLoc Locations because
			// the first time one a Location used in a fetch(Location) call
			// it's coordinate system will be set and the coordinate
			// could be different between the source and target databases.
			Location sourceLoc = null;
			Location targetLoc = null;
			String location = System.getProperty("idmapping.location");
			if (location != null)
				try {
					sourceLoc = (location != null) ? new Location(location) : null;
					targetLoc = sourceLoc.copy();
				} catch (ParseException e) {
					throw new RuntimeException("Invalid location parameter: " + location, e);
				}
			List allSourceGenes = null;
			if (sourceLoc != null)
				allSourceGenes = conf.getSourceDriver().getGeneAdaptor().fetch(sourceLoc);
			else
				allSourceGenes = conf.getSourceDriver().getGeneAdaptor().fetchAll(true);

			System.out.println("Total of " + allSourceGenes.size() + " source genes before filtering");
			
			allSourceGenes = filterByBiotype(allSourceGenes);

			System.out.println("Total of " + allSourceGenes.size() + " source genes after filtering");
			
			ProgressPrinter spp = new ProgressPrinter(0, allSourceGenes.size(), "% of filtered source genes read");
			int i = 0;

			Iterator sgit = allSourceGenes.iterator();
			while (sgit.hasNext()) {

				Gene gene = (Gene) sgit.next();
				gene.isKnown(); // force lazy-load of isKnown
        gene.getAnalysis().getLogicalName(); // force analysis lazy-load
				getSourceGenesByInternalID().put(gene.getInternalID(), gene);
				getSourceGenesByStableID().put(gene.getAccessionID(), gene);

				List transcripts = gene.getTranscripts();
				Iterator stit = transcripts.iterator();
				while (stit.hasNext()) {

					Transcript transcript = (Transcript) stit.next();
					transcript.isKnown(); // force lazy-load of isKnown
					final long transcriptID = transcript.getInternalID();
					getSourceTranscriptsByInternalID().put(transcriptID, transcript);
					getSourceGeneByTranscriptInternalID().put(transcriptID, gene);
					getSourceTranscriptsByStableID().put(transcript.getAccessionID(), transcript);

					Translation translation = transcript.getTranslation();
					if (translation != null) { // ignore pseudogenes etc
						translation.isKnown(); // force lazy-load of isKnown
						getSourceTranslationsByInternalID().put(translation.getInternalID(), translation);
						getSourceTranslationsByTranscriptInternalID().put(new Long(transcriptID), translation);
						getSourceTranslationsByStableID().put(translation.getAccessionID(), translation);
					}

					List exons = transcript.getExons();
					Iterator seit = exons.iterator();
					while (seit.hasNext()) {

						Exon exon = (Exon) seit.next();
						exon.getSequence(); // forces sequence to be loaded
						long exonID = exon.getInternalID();
						getSourceExonsByInternalID().put(exon.getInternalID(), exon);
						Util.addToMapList(getSourceTranscriptsByExonInternalID(), exonID, transcript);
						getSourceGeneByExonInternalID().put(exonID, gene);

					}
				}

				spp.printUpdate(i++);

			}
			spp.printUpdate(allSourceGenes.size());
			System.out.println("");

			// ------------------------
			List allTargetGenes = null;
			if (targetLoc != null)
				allTargetGenes = conf.getTargetDriver().getGeneAdaptor().fetch(targetLoc);
			else
				allTargetGenes = conf.getTargetDriver().getGeneAdaptor().fetchAll(true);

			System.out.println("Total of " + allTargetGenes.size() + " target genes before filtering");
			
			allTargetGenes = filterByBiotype(allTargetGenes);

			System.out.println("Total of " + allTargetGenes.size() + " target genes after filtering");
		
			ProgressPrinter tpp = new ProgressPrinter(0, allTargetGenes.size(), "% of filtered target genes read");
			int j = 0;
			Iterator tgit = allTargetGenes.iterator();
			while (tgit.hasNext()) {

				Gene gene = (Gene) tgit.next();
        gene.isKnown(); // force lazy-load of isKnown
        gene.getAnalysis().getLogicalName(); // force analysis lazy-load
        getTargetGenesByInternalID().put(gene.getInternalID(), gene);

				List transcripts = gene.getTranscripts();
				Iterator ttit = transcripts.iterator();
				while (ttit.hasNext()) {

					Transcript transcript = (Transcript) ttit.next();
					transcript.isKnown();
					long transcriptID = transcript.getInternalID();
					Long transcriptIDLong = new Long(transcriptID);
					getTargetTranscriptsByInternalID().put(transcriptID, transcript);
					getTargetGeneByTranscriptInternalID().put(transcriptID, gene);

					Translation translation = transcript.getTranslation();
					if (translation != null) { // ignore pseudogenes etc
						translation.isKnown();
						getTargetTranslationsByInternalID().put(translation.getInternalID(), translation);
						getTargetTranslationsByTranscriptInternalID().put(transcriptIDLong, translation);
					}

					List exons = transcript.getExons();
					Iterator teit = exons.iterator();
					while (teit.hasNext()) {

						Exon exon = (Exon) teit.next();
						final long exonID = exon.getInternalID();
						exon.getSequence(); // forces sequence to be loaded
						getTargetExonsByInternalID().put(exon.getInternalID(), exon);
						Util.addToMapList(getTargetTranscriptsByExonInternalID(), exonID, transcript);
						getTargetGeneByExonInternalID().put(exonID, gene);

					}
				}

				tpp.printUpdate(j++);

			}
			tpp.printUpdate(allTargetGenes.size());
			System.out.println("");

		} catch (AdaptorException ae) {

			ae.printStackTrace();

		}

	}

	/**
	 * @return Returns the sourceExonsByInternalID.
	 */
	public OpenLongObjectHashMap getSourceExonsByInternalID() {

		return sourceExonsByInternalID;
	}

	/**
	 * @return Returns the sourceGenesByInternalID.
	 */
	public OpenLongObjectHashMap getSourceGenesByInternalID() {

		return sourceGenesByInternalID;
	}

	/**
	 * @return Returns the sourceGenesByTranscriptInternalID.
	 */
	public OpenLongObjectHashMap getSourceGeneByTranscriptInternalID() {

		return sourceGeneByTranscriptInternalID;
	}

	/**
	 * @return Returns the sourceTranscriptsByExonInternalID.
	 */
	public OpenLongObjectHashMap getSourceTranscriptsByExonInternalID() {

		return sourceTranscriptsByExonInternalID;
	}

	/**
	 * @return Returns the sourceTranscriptsByInternalID.
	 */
	public OpenLongObjectHashMap getSourceTranscriptsByInternalID() {

		return sourceTranscriptsByInternalID;
	}

	/**
	 * @return Returns the sourceTranslationsByInternalID.
	 */
	public OpenLongObjectHashMap getSourceTranslationsByInternalID() {

		return sourceTranslationsByInternalID;
	}

	/**
	 * @return Returns the targetTranslationsByInternalID.
	 */
	public OpenLongObjectHashMap getTargetTranslationsByInternalID() {

		return targetTranslationsByInternalID;
	}

	/**
	 * @return Returns the sourceTranslationsByTranscriptInternalID.
	 */
	public Map getSourceTranslationsByTranscriptInternalID() {

		return sourceTranslationsByTranscriptInternalID;
	}

	/**
	 * @return Returns the targetExonsByInternalID.
	 */
	public OpenLongObjectHashMap getTargetExonsByInternalID() {

		return targetExonsByInternalID;
	}

	/**
	 * @return Returns the targetGenesByInternalID.
	 */
	public OpenLongObjectHashMap getTargetGenesByInternalID() {

		return targetGenesByInternalID;
	}

	/**
	 * @return Returns the targetGenesByTranscriptInternalID.
	 */
	public OpenLongObjectHashMap getTargetGeneByTranscriptInternalID() {

		return targetGeneByTranscriptInternalID;
	}

	/**
	 * @return Returns the targetTranscriptsByExonInternalID.
	 */
	public OpenLongObjectHashMap getTargetTranscriptsByExonInternalID() {

		return targetTranscriptsByExonInternalID;
	}

	/**
	 * @return Returns the targetTranscriptsByInternalID.
	 */
	public OpenLongObjectHashMap getTargetTranscriptsByInternalID() {

		return targetTranscriptsByInternalID;
	}

	/**
	 * @return Returns the targetTranslationsByTranscriptInternalID.
	 */
	public Map getTargetTranslationsByTranscriptInternalID() {

		return targetTranslationsByTranscriptInternalID;
	}

	// -------------------------------------------------------------------------

	/**
	 * Build and store hashtables of source-target internal ID mappings from lists
	 * of Entry objects.
	 */
	public void cacheMappings(List exonMappings, List transcriptMappings, List translationMappings, List geneMappings) {

		exonMappingsMap = buildMapping(exonMappings);
		transcriptMappingsMap = buildMapping(transcriptMappings);
		translationMappingsMap = buildMapping(translationMappings);
		geneMappingsMap = buildMapping(geneMappings);

	}

	private Map buildMapping(List mappings) {

		Map result = new HashMap();
		Iterator it = mappings.iterator();
		while (it.hasNext()) {
			Entry entry = (Entry) it.next();
			result.put(new Long(entry.getSource()), new Long(entry.getTarget()));
		}

		return result;

	}

	// -------------------------------------------------------------------------

	/**
	 * @return Returns the exonMappingsMap.
	 */
	public Map getExonMappingsMap() {

		return exonMappingsMap;
	}

	/**
	 * @return Returns the geneMappingsMap.
	 */
	public Map getGeneMappingsMap() {

		return geneMappingsMap;
	}

	/**
	 * @return Returns the transcriptMappingsMap.
	 */
	public Map getTranscriptMappingsMap() {

		return transcriptMappingsMap;
	}

	/**
	 * @return Returns the translationMappingsMap.
	 */
	public Map getTranslationMappingsMap() {

		return translationMappingsMap;
	}

	/**
	 * @return Returns the sourceGenesByExonInternalID.
	 */
	public OpenLongObjectHashMap getSourceGeneByExonInternalID() {

		return sourceGeneByExonInternalID;
	}

	/**
	 * @return Returns the targetGenesByExonInternalID.
	 */
	public OpenLongObjectHashMap getTargetGeneByExonInternalID() {

		return targetGeneByExonInternalID;
	}

	/**
	 * @param sourceGenesByExonInternalID
	 *          The sourceGenesByExonInternalID to set.
	 */
	public void setSourceGeneByExonInternalID(OpenLongObjectHashMap sourceGenesByExonInternalID) {

		this.sourceGeneByExonInternalID = sourceGenesByExonInternalID;
	}

	/**
	 * @param targetGenesByExonInternalID
	 *          The targetGenesByExonInternalID to set.
	 */
	public void setTargetGeneByExonInternalID(OpenLongObjectHashMap targetGenesByExonInternalID) {

		this.targetGeneByExonInternalID = targetGenesByExonInternalID;
	}

	/**
	 * @return Returns the sourceTranslationsByStableID.
	 */
	public Map getSourceTranslationsByStableID() {

		return sourceTranslationsByStableID;
	}

	/**
	 * @return Returns the sourceGenesByStableID.
	 */
	public Map getSourceGenesByStableID() {

		return sourceGenesByStableID;
	}

	/**
	 * @return Returns the sourceTranscriptsByStableID.
	 */
	public Map getSourceTranscriptsByStableID() {

		return sourceTranscriptsByStableID;
	}

	/**
	 * Hopefully removes all sequences from cached Exons as we need to be careful
	 * with the space ..
	 * 
	 */
	public void flushSequences() {

		flushExonSequences(sourceExonsByInternalID);
		flushExonSequences(targetExonsByInternalID);
	}

	private final void flushExonSequences(OpenLongObjectHashMap exonMap) {

		final ObjectArrayList exons = exonMap.values();
		final int n = exonMap.size();
		for (int i = 0; i < n; i++)
			((Exon) exons.getQuick(i)).setSequence(null);

	}

	public Gene getSourceGeneByInternalID(long id) {
		return (Gene) sourceGenesByInternalID.get(id);
	}

	public Gene getTargetGeneByInternalID(long id) {
		return (Gene) targetGenesByInternalID.get(id);
	}

	/**
	 * Filter a list of genes by biotype. Biotypes are specified as one or more
	 * comma-separated values in the system property idmapping.biotypes
	 */
	public List filterByBiotype(List genes) {

		String biotypeStr = System.getProperty("idmapping.biotypes");

		// do nothing if this isn't defined
		if (biotypeStr == null) {
			return genes;
		}

		// otherwise filter
		String[] biotypes = biotypeStr.split(",");
		
		System.out.println("Filtering genes on biotype(s): " + StringUtil.toString(biotypes));
		
		List result = new ArrayList();

		Iterator it = genes.iterator();
		while (it.hasNext()) {
		    Gene g = (Gene)it.next();
			if (StringUtil.stringInArray(g.getBioType(), biotypes, false)) {
				result.add(g);
			}
		}
		
		
		return result;

	}

}
