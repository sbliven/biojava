/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */
package org.biojavax.bio.phylo.io.nexus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.biojava.bio.seq.io.ParseException;

/**
 * Parses Nexus characters blocks.
 * 
 * @author Richard Holland
 * @author Tobias Thierer
 * @author Jim Balhoff
 * @since 1.6
 */
public class CharactersBlockParser extends NexusBlockParser.Abstract {

	private boolean expectingDimension;

	private boolean expectingNewTaxa;

	private boolean expectingNTax;

	private boolean expectingNTaxEquals;

	private boolean expectingNTaxValue;

	private boolean expectingNChar;

	private boolean expectingNCharEquals;

	private boolean expectingNCharValue;

	private boolean expectingFormat;

	private boolean expectingEliminate;

	private boolean expectingTaxLabel;

	private boolean expectingTaxLabelValue;

	private boolean expectingCharStateLabel;

	private boolean expectingCharLabel;

	private boolean expectingStateLabel;

	private boolean expectingMatrix;

	private boolean expectingDataType;

	private boolean expectingDataTypeEquals;

	private boolean expectingDataTypeContent;

	private boolean expectingRespectCase;

	private boolean expectingMissing;

	private boolean expectingMissingEquals;

	private boolean expectingMissingContent;

	private boolean expectingGap;

	private boolean expectingGapEquals;

	private boolean expectingGapContent;

	private boolean expectingSymbols;

	private boolean expectingSymbolsEquals;

	private boolean expectingSymbolsContent;

	private boolean expectingEquate;

	private boolean expectingEquateEquals;

	private boolean expectingEquateContent;

	private boolean expectingMatchChar;

	private boolean expectingMatchCharEquals;

	private boolean expectingMatchCharContent;

	private boolean expectingLabels;

	private boolean expectingTranspose;

	private boolean expectingInterleave;

	private boolean expectingItems;

	private boolean expectingItemsEquals;

	private boolean expectingItemsContent;

	private boolean itemsInBrackets;

	private boolean expectingStatesFormat;

	private boolean expectingStatesFormatEquals;

	private boolean expectingStatesFormatContent;

	private boolean expectingTokens;

	private String specifiedDataType;

	private boolean tokenizedMatrix;

	private boolean expectingEliminateRange;

	private boolean expectingCharStateLabelKey;

	private boolean expectingCharStateLabelName;

	private boolean expectingCharStateLabelSynonym;

	private boolean expectingCharLabelValue;

	private boolean expectingStateLabelKey;

	private boolean expectingStateLabelContent;

	private boolean expectingMatrixKey;

	private boolean expectingMatrixContent;

	private String currentCharStateLabelKey;

	private String currentStateLabelKey;

	private String currentMatrixKey;

	private List currentMatrixBracket;

	/**
	 * Delegates to NexusBlockParser.Abstract.
	 * 
	 * @param blockListener
	 *            the listener to send parse events to.
	 */
	public CharactersBlockParser(CharactersBlockListener blockListener) {
		super(blockListener);
	}

	public void resetStatus() {
		this.expectingDimension = true;
		this.expectingNewTaxa = false;
		this.expectingNTax = false;
		this.expectingNTaxEquals = false;
		this.expectingNTaxValue = false;
		this.expectingNChar = false;
		this.expectingNCharEquals = false;
		this.expectingNCharValue = false;
		this.expectingFormat = false;
		this.expectingEliminate = false;
		this.expectingTaxLabel = false;
		this.expectingTaxLabelValue = false;
		this.expectingCharStateLabel = false;
		this.expectingCharLabel = false;
		this.expectingStateLabel = false;
		this.expectingMatrix = false;
		this.tokenizedMatrix = false;
		this.specifiedDataType = null;
		this.expectingDataType = false;
		this.expectingDataTypeEquals = false;
		this.expectingDataTypeContent = false;
		this.expectingRespectCase = false;
		this.expectingMissing = false;
		this.expectingMissingEquals = false;
		this.expectingMissingContent = false;
		this.expectingGap = false;
		this.expectingGapEquals = false;
		this.expectingGapContent = false;
		this.expectingSymbols = false;
		this.expectingSymbolsEquals = false;
		this.expectingSymbolsContent = false;
		this.expectingEquate = false;
		this.expectingEquateEquals = false;
		this.expectingEquateContent = false;
		this.expectingMatchChar = false;
		this.expectingMatchCharEquals = false;
		this.expectingMatchCharContent = false;
		this.expectingLabels = false;
		this.expectingTranspose = false;
		this.expectingInterleave = false;
		this.expectingItems = false;
		this.expectingItemsEquals = false;
		this.expectingItemsContent = false;
		this.itemsInBrackets = false;
		this.expectingStatesFormat = false;
		this.expectingStatesFormatEquals = false;
		this.expectingStatesFormatContent = false;
		this.expectingTokens = false;
		this.expectingEliminateRange = false;
		this.expectingCharStateLabelKey = false;
		this.expectingCharStateLabelName = false;
		this.expectingCharStateLabelSynonym = false;
		this.expectingCharLabelValue = false;
		this.expectingStateLabelKey = false;
		this.expectingStateLabelContent = false;
		this.expectingMatrixKey = false;
		this.expectingMatrixContent = false;
		this.currentCharStateLabelKey = null;
		this.currentStateLabelKey = null;
		this.currentMatrixKey = null;
		this.currentMatrixBracket = null;
	}

	public void parseToken(String token) throws ParseException {
		final String trimmed = token.trim();
		if (this.expectingMatrixContent
				&& NexusFileFormat.NEW_LINE.equals(token)) {
			// Special handling for new lines inside matrix data.
			if (this.currentMatrixBracket != null) {
				((CharactersBlockListener) this.getBlockListener())
						.appendMatrixData(this.currentMatrixKey,
								this.currentMatrixBracket);
				this.currentMatrixBracket = null;
			}
			this.expectingMatrixContent = false;
			this.expectingMatrixKey = true;
		} else if (trimmed.length() == 0)
			return;
		else if (this.expectingDimension
				&& "DIMENSIONS".equalsIgnoreCase(trimmed)) {
			this.expectingDimension = false;
			this.expectingNewTaxa = true;
			this.expectingNChar = true;
		} else if (this.expectingNewTaxa && "NEWTAXA".equalsIgnoreCase(trimmed)) {
			this.expectingNewTaxa = false;
			this.expectingNTax = true;
			this.expectingNChar = false;
		} else if (this.expectingNTax
				&& trimmed.toUpperCase().startsWith("NTAX")) {
			this.expectingNTax = false;
			if (trimmed.indexOf('=') >= 0) {
				final String[] parts = trimmed.split("=");
				if (parts.length > 1) {
					this.expectingNChar = true;
					try {
						((CharactersBlockListener) this.getBlockListener())
								.setDimensionsNTax(Integer.parseInt(parts[1]));
					} catch (NumberFormatException e) {
						throw new ParseException("Invalid NTAX value: "
								+ parts[1]);
					}
				} else
					this.expectingNTaxValue = true;
			} else
				this.expectingNTaxEquals = true;
		} else if (this.expectingNTaxEquals && trimmed.startsWith("=")) {
			this.expectingNTaxEquals = false;
			final String[] parts = trimmed.split("=");
			if (parts.length > 1) {
				this.expectingNChar = true;
				try {
					((CharactersBlockListener) this.getBlockListener())
							.setDimensionsNTax(Integer.parseInt(parts[1]));
				} catch (NumberFormatException e) {
					throw new ParseException("Invalid NTAX value: " + parts[1]);
				}
			} else
				this.expectingNTaxValue = true;
		} else if (this.expectingNTaxValue) {
			this.expectingNTaxValue = false;
			try {
				((CharactersBlockListener) this.getBlockListener())
						.setDimensionsNTax(Integer.parseInt(trimmed));
			} catch (NumberFormatException e) {
				throw new ParseException("Invalid NTAX value: " + trimmed);
			}
			this.expectingNChar = true;
		} else if (this.expectingNChar
				&& trimmed.toUpperCase().startsWith("NCHAR")) {
			this.expectingNChar = false;
			if (trimmed.indexOf('=') >= 0) {
				final String[] parts = trimmed.split("=");
				if (parts.length > 1) {
					this.expectingFormat = true;
					this.expectingEliminate = true;
					this.expectingTaxLabel = true;
					this.expectingCharStateLabel = true;
					this.expectingCharLabel = true;
					this.expectingStateLabel = true;
					this.expectingMatrix = true;
					try {
						((CharactersBlockListener) this.getBlockListener())
								.setDimensionsNChar(Integer.parseInt(parts[1]));
					} catch (NumberFormatException e) {
						throw new ParseException("Invalid NCHAR value: "
								+ parts[1]);
					}
				} else
					this.expectingNCharValue = true;
			} else
				this.expectingNCharEquals = true;
		} else if (this.expectingNCharEquals && trimmed.startsWith("=")) {
			this.expectingNCharEquals = false;
			final String[] parts = trimmed.split("=");
			if (parts.length > 1) {
				this.expectingFormat = true;
				this.expectingEliminate = true;
				this.expectingTaxLabel = true;
				this.expectingCharStateLabel = true;
				this.expectingCharLabel = true;
				this.expectingStateLabel = true;
				this.expectingMatrix = true;
				try {
					((CharactersBlockListener) this.getBlockListener())
							.setDimensionsNChar(Integer.parseInt(parts[1]));
				} catch (NumberFormatException e) {
					throw new ParseException("Invalid NCHAR value: " + parts[1]);
				}
			} else
				this.expectingNCharValue = true;
		} else if (this.expectingNCharValue) {
			this.expectingNCharValue = false;
			try {
				((CharactersBlockListener) this.getBlockListener())
						.setDimensionsNChar(Integer.parseInt(trimmed));
			} catch (NumberFormatException e) {
				throw new ParseException("Invalid NCHAR value: " + trimmed);
			}
			this.expectingFormat = true;
			this.expectingEliminate = true;
			this.expectingTaxLabel = true;
			this.expectingCharStateLabel = true;
			this.expectingCharLabel = true;
			this.expectingStateLabel = true;
			this.expectingMatrix = true;
		}

		else if (this.expectingFormat && "FORMAT".equalsIgnoreCase(trimmed)) {
			this.expectingFormat = false;
			this.expectingDataType = true;
			this.expectingRespectCase = true;
			this.expectingMissing = true;
			this.expectingGap = true;
			this.expectingSymbols = true;
			this.expectingEquate = true;
			this.expectingMatchChar = true;
			this.expectingLabels = true;
			this.expectingTranspose = true;
			this.expectingInterleave = true;
			this.expectingItems = true;
			this.expectingStatesFormat = true;
			this.expectingTokens = true;
		}

		else if (this.expectingDataType
				&& trimmed.toUpperCase().startsWith("DATATYPE")) {
			this.expectingDataType = false;

			if (trimmed.indexOf("=") >= 0) {
				final String[] parts = token.split("=");
				if (parts.length > 1) {
					this.specifiedDataType = parts[1];
					((CharactersBlockListener) this.getBlockListener())
							.setDataType(parts[1]);
				} else
					this.expectingDataTypeContent = true;
			} else
				this.expectingDataTypeEquals = true;
		}

		else if (this.expectingDataTypeEquals && token.startsWith("=")) {
			this.expectingDataTypeEquals = false;
			if (token.length() > 1) {
				token = token.substring(1);
				this.specifiedDataType = token;
				((CharactersBlockListener) this.getBlockListener())
						.setDataType(token);
			} else
				this.expectingDataTypeContent = true;
		}

		else if (this.expectingDataTypeContent) {
			this.specifiedDataType = token;
			((CharactersBlockListener) this.getBlockListener())
					.setDataType(token);
			this.expectingDataTypeContent = false;
		}

		else if (this.expectingRespectCase
				&& "RESPECTCASE".equalsIgnoreCase(trimmed)) {
			((CharactersBlockListener) this.getBlockListener())
					.setRespectCase(true);
			this.expectingDataType = false;
			this.expectingRespectCase = false;
		}

		else if (this.expectingMissing
				&& trimmed.toUpperCase().startsWith("MISSING")) {
			this.expectingDataType = false;
			this.expectingRespectCase = false;
			this.expectingMissing = false;

			if (trimmed.indexOf("=") >= 0) {
				final String[] parts = token.split("=");
				if (parts.length > 1)
					((CharactersBlockListener) this.getBlockListener())
							.setMissing(parts[1]);
				else
					this.expectingMissingContent = true;
			} else
				this.expectingMissingEquals = true;
		}

		else if (this.expectingMissingEquals && token.startsWith("=")) {
			this.expectingMissingEquals = false;
			if (token.length() > 1)
				((CharactersBlockListener) this.getBlockListener())
						.setMissing(token.substring(1));
			else
				this.expectingMissingContent = true;
		}

		else if (this.expectingMissingContent) {
			((CharactersBlockListener) this.getBlockListener())
					.setMissing(token);
			this.expectingMissingContent = false;
		}

		else if (this.expectingGap && trimmed.toUpperCase().startsWith("GAP")) {
			this.expectingDataType = false;
			this.expectingRespectCase = false;
			this.expectingMissing = false;
			this.expectingGap = false;

			if (trimmed.indexOf("=") >= 0) {
				final String[] parts = token.split("=");
				if (parts.length > 1)
					((CharactersBlockListener) this.getBlockListener())
							.setGap(parts[1]);
				else
					this.expectingGapContent = true;
			} else
				this.expectingGapEquals = true;
		}

		else if (this.expectingGapEquals && token.startsWith("=")) {
			this.expectingGapEquals = false;
			if (token.length() > 1)
				((CharactersBlockListener) this.getBlockListener())
						.setGap(token.substring(1));
			else
				this.expectingGapContent = true;
		}

		else if (this.expectingGapContent) {
			((CharactersBlockListener) this.getBlockListener()).setGap(token);
			this.expectingGapContent = false;
		}

		else if (this.expectingSymbols
				&& trimmed.toUpperCase().startsWith("SYMBOLS")) {
			this.expectingDataType = false;
			this.expectingRespectCase = false;
			this.expectingMissing = false;
			this.expectingGap = false;
			this.expectingSymbols = false;

			if (trimmed.indexOf("=") >= 0) {
				final String[] parts = token.split("=");
				if (parts.length > 1) {
					if (!parts[1].startsWith("\""))
						throw new ParseException(
								"Symbols string must start with '\"'");
					parts[1] = parts[1].substring(1);
					this.expectingSymbolsContent = true;
					if (parts[1].endsWith("\"")) {
						parts[1] = parts[1].substring(0, parts[1].length() - 1);
						this.expectingSymbolsContent = false;
					}
					((CharactersBlockListener) this.getBlockListener())
							.addSymbol(parts[1]);
				} else
					this.expectingSymbolsContent = true;
			} else
				this.expectingSymbolsEquals = true;
		}

		else if (this.expectingSymbolsEquals && token.startsWith("=")) {
			this.expectingSymbolsEquals = false;
			if (token.length() > 1) {
				token = token.substring(1);
				if (!token.startsWith("\""))
					throw new ParseException(
							"Symbols string must start with '\"'");
				token = token.substring(1);
				this.expectingSymbolsContent = true;

				if (token.endsWith("\"")) {
					token = token.substring(0, token.length() - 1);
					this.expectingSymbolsContent = false;
				}
				((CharactersBlockListener) this.getBlockListener())
						.addSymbol(token);
			} else
				this.expectingSymbolsContent = true;
		}

		else if (this.expectingSymbolsContent) {
			if (token.startsWith("\""))
				token = token.substring(1);
			if (token.endsWith("\"")) {
				token = token.substring(0, token.length() - 1);
				this.expectingSymbolsContent = false;
			}
			((CharactersBlockListener) this.getBlockListener())
					.addSymbol(token);
		}

		else if (this.expectingEquate
				&& trimmed.toUpperCase().startsWith("EQUATE")) {
			this.expectingDataType = false;
			this.expectingRespectCase = false;
			this.expectingMissing = false;
			this.expectingGap = false;
			this.expectingSymbols = false;
			this.expectingEquate = false;

			if (trimmed.indexOf("=") >= 0) {
				final String[] parts = token.split("=");
				if (parts.length > 1) {
					if (!parts[1].startsWith("\""))
						throw new ParseException(
								"Symbols string must start with '\"'");
					parts[1] = parts[1].substring(1);
					this.expectingEquateContent = true;
					if (parts[1].endsWith("\"")) {
						parts[1] = parts[1].substring(0, parts[1].length() - 1);
						this.expectingEquateContent = false;
					}
					final String[] subParts = parts[1].split("=");
					final String symbol = subParts[0];
					final StringBuffer text = new StringBuffer();
					for (int i = 1; i < subParts.length; i++) {
						if (i >= 2)
							text.append('=');
						text.append(subParts[i]);
					}
					final List symbols = new ArrayList();
					if (text.charAt(0) == '(')
						symbols.addAll(Arrays.asList(text.substring(1,
								text.length() - 2).split("")));
					else
						symbols
								.addAll(Arrays
										.asList(text.toString().split("")));
					((CharactersBlockListener) this.getBlockListener())
							.addEquate(symbol, symbols);
				} else
					this.expectingEquateContent = true;
			} else
				this.expectingEquateEquals = true;
		}

		else if (this.expectingEquateEquals && token.startsWith("=")) {
			this.expectingEquateEquals = false;
			if (token.length() > 1) {
				token = token.substring(1);
				if (!token.startsWith("\""))
					throw new ParseException(
							"Symbols string must start with '\"'");
				token = token.substring(1);
				this.expectingEquateContent = true;

				if (token.endsWith("\"")) {
					token = token.substring(0, token.length() - 1);
					this.expectingEquateContent = false;
				}
				final String[] subParts = token.split("=");
				final String symbol = subParts[0];
				final StringBuffer text = new StringBuffer();
				for (int i = 1; i < subParts.length; i++) {
					if (i >= 2)
						text.append('=');
					text.append(subParts[i]);
				}
				final List symbols = new ArrayList();
				if (text.charAt(0) == '(')
					symbols.addAll(Arrays.asList(text.substring(1,
							text.length() - 2).split("")));
				else
					symbols.addAll(Arrays.asList(text.toString().split("")));
				((CharactersBlockListener) this.getBlockListener()).addEquate(
						symbol, symbols);
			} else
				this.expectingEquateContent = true;
		}

		else if (this.expectingEquateContent) {
			if (token.startsWith("\""))
				token = token.substring(1);
			if (token.endsWith("\"")) {
				token = token.substring(0, token.length() - 1);
				this.expectingEquateContent = false;
			}
			final String[] subParts = token.split("=");
			final String symbol = subParts[0];
			final StringBuffer text = new StringBuffer();
			for (int i = 1; i < subParts.length; i++) {
				if (i >= 2)
					text.append('=');
				text.append(subParts[i]);
			}
			final List symbols = new ArrayList();
			if (text.charAt(0) == '(')
				symbols.addAll(Arrays.asList(text.substring(1,
						text.length() - 2).split("")));
			else
				symbols.addAll(Arrays.asList(text.toString().split("")));
			((CharactersBlockListener) this.getBlockListener()).addEquate(
					symbol, symbols);
		}

		else if (this.expectingMatchChar
				&& trimmed.toUpperCase().startsWith("MATCHCHAR")) {
			this.expectingDataType = false;
			this.expectingRespectCase = false;
			this.expectingMissing = false;
			this.expectingGap = false;
			this.expectingSymbols = false;
			this.expectingEquate = false;
			this.expectingMatchChar = false;

			if (trimmed.indexOf("=") >= 0) {
				final String[] parts = token.split("=");
				if (parts.length > 1)
					((CharactersBlockListener) this.getBlockListener())
							.setMatchChar(parts[1]);
				else
					this.expectingMatchCharContent = true;
			} else
				this.expectingMatchCharEquals = true;
		}

		else if (this.expectingMatchCharEquals && token.startsWith("=")) {
			this.expectingMatchCharEquals = false;
			if (token.length() > 1)
				((CharactersBlockListener) this.getBlockListener())
						.setMatchChar(token.substring(1));
			else
				this.expectingMatchCharContent = true;
		}

		else if (this.expectingMatchCharContent) {
			((CharactersBlockListener) this.getBlockListener())
					.setMatchChar(token);
			this.expectingMatchCharContent = false;
		}

		else if (this.expectingLabels && "LABELS".equalsIgnoreCase(trimmed)) {
			((CharactersBlockListener) this.getBlockListener()).setLabels(true);
			this.expectingDataType = false;
			this.expectingRespectCase = false;
			this.expectingMissing = false;
			this.expectingGap = false;
			this.expectingSymbols = false;
			this.expectingEquate = false;
			this.expectingMatchChar = false;
			this.expectingLabels = false;
		}

		else if (this.expectingLabels && "NOLABELS".equalsIgnoreCase(trimmed)) {
			((CharactersBlockListener) this.getBlockListener())
					.setLabels(false);
			this.expectingDataType = false;
			this.expectingRespectCase = false;
			this.expectingMissing = false;
			this.expectingGap = false;
			this.expectingSymbols = false;
			this.expectingEquate = false;
			this.expectingMatchChar = false;
			this.expectingLabels = false;
		}

		else if (this.expectingTranspose
				&& "TRANSPOSE".equalsIgnoreCase(trimmed)) {
			((CharactersBlockListener) this.getBlockListener())
					.setTransposed(true);
			this.expectingDataType = false;
			this.expectingRespectCase = false;
			this.expectingMissing = false;
			this.expectingGap = false;
			this.expectingSymbols = false;
			this.expectingEquate = false;
			this.expectingMatchChar = false;
			this.expectingLabels = false;
			this.expectingTranspose = false;
		}

		else if (this.expectingInterleave
				&& "INTERLEAVE".equalsIgnoreCase(trimmed)) {
			((CharactersBlockListener) this.getBlockListener())
					.setInterleaved(true);
			this.expectingDataType = false;
			this.expectingRespectCase = false;
			this.expectingMissing = false;
			this.expectingGap = false;
			this.expectingSymbols = false;
			this.expectingEquate = false;
			this.expectingMatchChar = false;
			this.expectingLabels = false;
			this.expectingTranspose = false;
			this.expectingInterleave = false;
		}

		else if (this.expectingItems
				&& trimmed.toUpperCase().startsWith("ITEMS")) {
			this.expectingDataType = false;
			this.expectingRespectCase = false;
			this.expectingMissing = false;
			this.expectingGap = false;
			this.expectingSymbols = false;
			this.expectingEquate = false;
			this.expectingMatchChar = false;
			this.expectingLabels = false;
			this.expectingTranspose = false;
			this.expectingInterleave = false;
			this.expectingItems = false;

			if (trimmed.indexOf("=") >= 0) {
				final String[] parts = token.split("=");
				if (parts.length > 1) {
					if (parts[1].startsWith("(")) {
						parts[1] = parts[1].substring(1);
						this.itemsInBrackets = true;
						this.expectingItemsContent = true;
					}
					if (parts[1].endsWith(")")) {
						parts[1] = parts[1].substring(0, parts[1].length() - 1);
						this.itemsInBrackets = false;
						this.expectingItemsContent = false;
					}
					((CharactersBlockListener) this.getBlockListener())
							.setStatesFormat(parts[1]);
				} else
					this.expectingItemsContent = true;
			} else
				this.expectingItemsEquals = true;
		}

		else if (this.expectingItemsEquals && token.startsWith("=")) {
			this.expectingItemsEquals = false;
			if (token.length() > 1) {
				token = token.substring(1);
				if (token.startsWith("(")) {
					token = token.substring(1);
					this.itemsInBrackets = true;
					this.expectingItemsContent = true;
				}
				if (token.endsWith(")")) {
					token = token.substring(0, token.length() - 1);
					this.itemsInBrackets = false;
					this.expectingItemsContent = false;
				}
				((CharactersBlockListener) this.getBlockListener())
						.setStatesFormat(token);
			} else
				this.expectingItemsContent = true;
		}

		else if (this.expectingItemsContent) {
			if (token.startsWith("(")) {
				token = token.substring(1);
				this.itemsInBrackets = true;
				this.expectingItemsContent = true;
			}
			if (token.endsWith(")")) {
				token = token.substring(0, token.length() - 1);
				this.itemsInBrackets = false;
				this.expectingItemsContent = false;
			}
			((CharactersBlockListener) this.getBlockListener())
					.setStatesFormat(token);
			this.expectingItemsContent = this.itemsInBrackets;
		}

		else if (this.expectingStatesFormat
				&& trimmed.toUpperCase().startsWith("STATESFORMAT")) {
			this.expectingDataType = false;
			this.expectingRespectCase = false;
			this.expectingMissing = false;
			this.expectingGap = false;
			this.expectingSymbols = false;
			this.expectingEquate = false;
			this.expectingMatchChar = false;
			this.expectingLabels = false;
			this.expectingTranspose = false;
			this.expectingInterleave = false;
			this.expectingItems = false;
			this.expectingStatesFormat = false;

			if (trimmed.indexOf("=") >= 0) {
				final String[] parts = token.split("=");
				if (parts.length > 1)
					((CharactersBlockListener) this.getBlockListener())
							.setStatesFormat(parts[1]);
				else
					this.expectingStatesFormatContent = true;
			} else
				this.expectingStatesFormatEquals = true;
		}

		else if (this.expectingStatesFormatEquals && token.startsWith("=")) {
			this.expectingStatesFormatEquals = false;
			if (token.length() > 1)
				((CharactersBlockListener) this.getBlockListener())
						.setStatesFormat(token.substring(1));
			else
				this.expectingStatesFormatContent = true;
		}

		else if (this.expectingStatesFormatContent) {
			((CharactersBlockListener) this.getBlockListener())
					.setStatesFormat(token);
			this.expectingStatesFormatContent = false;
		}

		else if (this.expectingTokens && "TOKENS".equalsIgnoreCase(trimmed)) {
			((CharactersBlockListener) this.getBlockListener()).setTokens(true);
			this.expectingDataType = false;
			this.expectingRespectCase = false;
			this.expectingMissing = false;
			this.expectingGap = false;
			this.expectingSymbols = false;
			this.expectingEquate = false;
			this.expectingMatchChar = false;
			this.expectingLabels = false;
			this.expectingTranspose = false;
			this.expectingInterleave = false;
			this.expectingItems = false;
			this.expectingStatesFormat = false;
			this.expectingTokens = false;
			this.tokenizedMatrix = true;
		}

		else if (this.expectingTokens && "NOTOKENS".equalsIgnoreCase(trimmed)) {
			((CharactersBlockListener) this.getBlockListener())
					.setTokens(false);
			this.expectingDataType = false;
			this.expectingRespectCase = false;
			this.expectingMissing = false;
			this.expectingGap = false;
			this.expectingSymbols = false;
			this.expectingEquate = false;
			this.expectingMatchChar = false;
			this.expectingLabels = false;
			this.expectingTranspose = false;
			this.expectingInterleave = false;
			this.expectingItems = false;
			this.expectingStatesFormat = false;
			this.expectingTokens = false;
			this.tokenizedMatrix = false;
		}

		else if (this.expectingEliminate
				&& "ELIMINATE".equalsIgnoreCase(trimmed)) {
			this.expectingFormat = false;
			this.expectingDataType = false;
			this.expectingRespectCase = false;
			this.expectingMissing = false;
			this.expectingGap = false;
			this.expectingSymbols = false;
			this.expectingEquate = false;
			this.expectingMatchChar = false;
			this.expectingLabels = false;
			this.expectingTranspose = false;
			this.expectingInterleave = false;
			this.expectingItems = false;
			this.expectingStatesFormat = false;
			this.expectingTokens = false;
			this.expectingEliminate = false;
			this.expectingEliminateRange = true;
		}

		else if (this.expectingEliminateRange) {
			final String parts[] = trimmed.split("-");
			if (parts.length != 2)
				throw new ParseException("Eliminate range " + trimmed
						+ " not in form X-Y");
			try {
				final int eliminateStart = Integer.parseInt(parts[0]);
				final int eliminateEnd = Integer.parseInt(parts[1]);
				((CharactersBlockListener) this.getBlockListener())
						.setEliminateStart(eliminateStart);
				((CharactersBlockListener) this.getBlockListener())
						.setEliminateEnd(eliminateEnd);
			} catch (NumberFormatException e) {
				throw new ParseException("Values in eliminate range " + trimmed
						+ " not parseable integers");
			}
			this.expectingEliminateRange = false;
		}

		else if (this.expectingTaxLabel
				&& "TAXLABELS".equalsIgnoreCase(trimmed)) {
			this.expectingFormat = false;
			this.expectingDataType = false;
			this.expectingRespectCase = false;
			this.expectingMissing = false;
			this.expectingGap = false;
			this.expectingSymbols = false;
			this.expectingEquate = false;
			this.expectingMatchChar = false;
			this.expectingLabels = false;
			this.expectingTranspose = false;
			this.expectingInterleave = false;
			this.expectingItems = false;
			this.expectingStatesFormat = false;
			this.expectingTokens = false;
			this.expectingEliminate = false;
			this.expectingEliminateRange = false;
			this.expectingTaxLabel = false;
			this.expectingTaxLabelValue = true;
		}

		else if (this.expectingCharStateLabel
				&& "CHARSTATELABELS".equalsIgnoreCase(trimmed)) {
			this.expectingFormat = false;
			this.expectingDataType = false;
			this.expectingRespectCase = false;
			this.expectingMissing = false;
			this.expectingGap = false;
			this.expectingSymbols = false;
			this.expectingEquate = false;
			this.expectingMatchChar = false;
			this.expectingLabels = false;
			this.expectingTranspose = false;
			this.expectingInterleave = false;
			this.expectingItems = false;
			this.expectingStatesFormat = false;
			this.expectingTokens = false;
			this.expectingEliminate = false;
			this.expectingEliminateRange = false;
			this.expectingTaxLabel = false;
			this.expectingTaxLabelValue = false;
			this.expectingCharStateLabel = false;
			this.expectingCharStateLabelKey = true;
		}

		else if (this.expectingCharLabel
				&& "CHARLABELS".equalsIgnoreCase(trimmed)) {
			this.expectingFormat = false;
			this.expectingDataType = false;
			this.expectingRespectCase = false;
			this.expectingMissing = false;
			this.expectingGap = false;
			this.expectingSymbols = false;
			this.expectingEquate = false;
			this.expectingMatchChar = false;
			this.expectingLabels = false;
			this.expectingTranspose = false;
			this.expectingInterleave = false;
			this.expectingItems = false;
			this.expectingStatesFormat = false;
			this.expectingTokens = false;
			this.expectingEliminate = false;
			this.expectingEliminateRange = false;
			this.expectingTaxLabel = false;
			this.expectingTaxLabelValue = false;
			this.expectingCharStateLabel = false;
			this.expectingCharStateLabelKey = false;
			this.expectingCharStateLabelName = false;
			this.expectingCharStateLabelSynonym = false;
			this.expectingCharLabel = false;
			this.expectingCharLabelValue = true;
		}

		else if (this.expectingStateLabel
				&& "STATELABELS".equalsIgnoreCase(trimmed)) {
			this.expectingFormat = false;
			this.expectingDataType = false;
			this.expectingRespectCase = false;
			this.expectingMissing = false;
			this.expectingGap = false;
			this.expectingSymbols = false;
			this.expectingEquate = false;
			this.expectingMatchChar = false;
			this.expectingLabels = false;
			this.expectingTranspose = false;
			this.expectingInterleave = false;
			this.expectingItems = false;
			this.expectingStatesFormat = false;
			this.expectingTokens = false;
			this.expectingEliminate = false;
			this.expectingEliminateRange = false;
			this.expectingTaxLabel = false;
			this.expectingTaxLabelValue = false;
			this.expectingCharStateLabel = false;
			this.expectingCharStateLabelKey = false;
			this.expectingCharStateLabelName = false;
			this.expectingCharStateLabelSynonym = false;
			this.expectingCharLabel = false;
			this.expectingStateLabel = false;
			this.expectingStateLabelKey = true;
		}

		else if (this.expectingMatrix && "MATRIX".equalsIgnoreCase(trimmed)) {
			this.expectingFormat = false;
			this.expectingDataType = false;
			this.expectingRespectCase = false;
			this.expectingMissing = false;
			this.expectingGap = false;
			this.expectingSymbols = false;
			this.expectingEquate = false;
			this.expectingMatchChar = false;
			this.expectingLabels = false;
			this.expectingTranspose = false;
			this.expectingInterleave = false;
			this.expectingItems = false;
			this.expectingStatesFormat = false;
			this.expectingTokens = false;
			this.expectingEliminate = false;
			this.expectingEliminateRange = false;
			this.expectingTaxLabel = false;
			this.expectingTaxLabelValue = false;
			this.expectingCharStateLabel = false;
			this.expectingCharStateLabelKey = false;
			this.expectingCharStateLabelName = false;
			this.expectingCharStateLabelSynonym = false;
			this.expectingCharLabel = false;
			this.expectingStateLabel = false;
			this.expectingStateLabelKey = false;
			this.expectingStateLabelContent = false;
			this.expectingMatrix = false;
			this.expectingMatrixKey = true;
		}

		else if (this.expectingTaxLabelValue)
			// Use untrimmed version to preserve spaces.
			((CharactersBlockListener) this.getBlockListener())
					.addTaxLabel(token);

		else if (this.expectingCharStateLabelKey) {
			this.currentCharStateLabelKey = token;
			// Use untrimmed version to preserve spaces.
			((CharactersBlockListener) this.getBlockListener())
					.addCharState(token);
			this.expectingCharStateLabelKey = false;
			this.expectingCharStateLabelName = true;
		}

		else if (this.expectingCharStateLabelName) {
			String actualName = token;
			String firstSynonym = null;
			if (token.indexOf("/") >= 0) {
				actualName = token.substring(0, token.indexOf("/"));
				if (token.indexOf("/") < token.length() - 2)
					firstSynonym = token.substring(token.indexOf("/") + 1);
			}
			final boolean skipSynonyms = actualName.endsWith(",")
					|| (firstSynonym != null && firstSynonym.endsWith(","));
			if (skipSynonyms) {
				if (firstSynonym != null)
					firstSynonym = firstSynonym.substring(0, firstSynonym
							.length() - 1);
				else
					actualName = actualName.substring(0,
							actualName.length() - 1);
			}
			// Use untrimmed version to preserve spaces.
			((CharactersBlockListener) this.getBlockListener())
					.setCharStateLabel(this.currentCharStateLabelKey,
							actualName);
			if (firstSynonym != null)
				((CharactersBlockListener) this.getBlockListener())
						.addCharStateKeyword(this.currentCharStateLabelKey,
								token);
			this.expectingCharStateLabelName = false;
			if (!skipSynonyms)
				this.expectingCharStateLabelSynonym = true;
			else
				this.expectingCharStateLabelKey = true;
		}

		else if (this.expectingCharStateLabelSynonym) {
			if (token.startsWith("/") && token.length() > 1)
				token = token.substring(1);
			final boolean skipSynonyms = token.endsWith(",");
			if (skipSynonyms)
				token = token.substring(0, token.length() - 1);
			if (!"/".equals(token))
				// Use untrimmed version to preserve spaces.
				((CharactersBlockListener) this.getBlockListener())
						.addCharStateKeyword(this.currentCharStateLabelKey,
								token);
			if (skipSynonyms) {
				this.expectingCharStateLabelSynonym = false;
				this.expectingCharStateLabelKey = true;
			}
		}

		else if (this.expectingCharLabelValue)
			// Use untrimmed version to preserve spaces.
			((CharactersBlockListener) this.getBlockListener())
					.addCharLabel(token);

		else if (this.expectingStateLabelKey) {
			final boolean skipContent = token.endsWith(",");
			if (skipContent)
				token = token.substring(0, token.length() - 1);
			this.currentStateLabelKey = token;
			// Use untrimmed version to preserve spaces.
			((CharactersBlockListener) this.getBlockListener()).addState(token);
			if (!skipContent) {
				this.expectingStateLabelKey = false;
				this.expectingStateLabelContent = true;
			}
		}

		else if (this.expectingStateLabelContent) {
			final boolean skipContent = token.endsWith(",");
			if (skipContent)
				token = token.substring(0, token.length() - 1);
			// Use untrimmed version to preserve spaces.
			((CharactersBlockListener) this.getBlockListener()).addStateLabel(
					this.currentStateLabelKey, token);
			if (skipContent) {
				this.expectingStateLabelKey = true;
				this.expectingStateLabelContent = false;
			}
		}

		else if (this.expectingMatrixKey) {
			this.currentMatrixKey = token;
			// Use untrimmed version to preserve spaces.
			((CharactersBlockListener) this.getBlockListener())
					.addMatrixEntry(token);
			this.expectingMatrixKey = false;
			this.expectingMatrixContent = true;
		}

		else if (this.expectingMatrixContent) {
			final boolean reallyUseTokens = (this.tokenizedMatrix || "CONTINUOUS"
					.equals(this.specifiedDataType))
					&& !("DNA".equals(this.specifiedDataType)
							|| "RNA".equals(this.specifiedDataType) || "NUCLEOTIDE"
							.equals(this.specifiedDataType));
			final String[] toks = token.split("");
			StringBuffer buff = new StringBuffer();
			// Iterate over chars in token
			// If bracket, open new matrix entry.
			// else if close bracket, save current entry and open new matrix
			// entry.
			// else if not tokenized, output char in current entry.
			// else if tokenized, output all remaining chars in current entry
			// till close bracket or end of token.
			for (int i = 0; i < toks.length; i++) {
				final String tok = toks[i];
				if ("(".equals(tok))
					this.currentMatrixBracket = new ArrayList();
				else if (")".equals(tok)) {
					if (reallyUseTokens) {
						if (this.currentMatrixBracket != null)
							this.currentMatrixBracket.add(buff.toString());
						else
							((CharactersBlockListener) this.getBlockListener())
									.appendMatrixData(this.currentMatrixKey,
											buff.toString());
					}
					if (this.currentMatrixBracket != null) {
						((CharactersBlockListener) this.getBlockListener())
								.appendMatrixData(this.currentMatrixKey,
										this.currentMatrixBracket);
						this.currentMatrixBracket = null;
					}
				} else if (reallyUseTokens)
					buff.append(tok);
				else if (this.currentMatrixBracket != null)
					this.currentMatrixBracket.add(tok);
				else
					((CharactersBlockListener) this.getBlockListener())
							.appendMatrixData(this.currentMatrixKey, tok);
			}
			if (reallyUseTokens) {
				if (this.currentMatrixBracket != null)
					this.currentMatrixBracket.add(buff.toString());
				else
					((CharactersBlockListener) this.getBlockListener())
							.appendMatrixData(this.currentMatrixKey, buff
									.toString());
			}
		}

		else
			throw new ParseException("Found unexpected token " + token
					+ " in CHARACTERS block");
	}
}
