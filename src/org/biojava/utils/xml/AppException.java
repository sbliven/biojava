/*
 *                   Eponine development code
 *
 * Eponine is developed by Thomas Down at the Sanger Centre.
 * For more information, see:
 *
 *    http://www.sanger.ac.uk/~td2/eponine/
 *
 * This code is currently not intended for public distribution.
 * If you wish to use or distribute it, or if you have any queries,
 * please contact the author:
 *
 *    td2@sanger.ac.uk
 *
 */

package org.biojava.utils.xml;

public class AppException extends Exception {
    public AppException(String reason) {
	super(reason);
    }
}
