package org.biojava.utils.query;

/**
 * A node that will result in items being selected for return to the user.
 * <P>
 * When a query is used to process some objects, the query will end a particular
 * processing path when it hits a ResultNode. If the query is used to return a
 * set of matching objects then every object in scope when hitting a ResultNode
 * will be returned. If all paths through the data are collected (as a table
 *, for example) then each row of the table will terminate with items selected
 * by a ResultNode. If an object graph is pruned then the sub-graph will never
 * extend beyond nodes matched by a ResultNode.
 *
 * @author Matthew Pocock
 */
public class ResultNode implements Node {
}
