


package org.biojava.utils.automata;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import org.biojava.bio.BioError;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.AlphabetIndex;
import org.biojava.bio.symbol.AlphabetManager;
import org.biojava.bio.symbol.Symbol;

/**
 * Class for modelling non-deterministic finite automata.
 * <p>
 * It differs from its superclass by implementing epsilon
 * transitions and its replaceNode() method remove epsilon self-transitions.
 * <p>
 * @author David Huen
 * @since 1.4
 */
public class Nfa
    extends FiniteAutomaton
{
    private Symbol EPSILON = null;

    public Nfa(String name, FiniteAlphabet alfa)
    {
        super(name, alfa);
    }

    public boolean containsNode(Node node)
    {
        return nodes.contains(node);
    }

    public void addEpsilonTransition(Node start, Node end)
    {
        addTransition(start, end, EPSILON);
    }

    /**
     * merge all nodes linked by emission-less transitions.
     */
    void doEpsilonClosure()
    {
        // when accumulating closure set, ensure that it
        // start and end are in there, they become the
        // replacement.
        Set closure = new HashSet();

        boolean foundEpsilonTransitions;
        do {
            closure.clear();
            foundEpsilonTransitions = false;
            for (Iterator transI = transitions.iterator(); transI.hasNext(); ) {
                Transition currTransition = (Transition) transI.next();
    
                if (currTransition.sym == EPSILON) {
                    foundEpsilonTransitions = true;
                    if (closure.isEmpty()) {
                        // start a new closure
                        closure.add(currTransition.source);
                        closure.add(currTransition.dest);
                    }
                    else {
                        // if this transition is linked with any of those I
                        // have previously encountered, coalesce them.
                        if ((closure.contains(currTransition.source)) ||
                            (closure.contains(currTransition.dest))) {
                                closure.add(currTransition.source);
                                closure.add(currTransition.dest);
                        }
                    }
                }
            }

            // found a closure
            if (foundEpsilonTransitions) {
                // specify the Node that will act for rest
                // in closure set.
                boolean containsStart = closure.contains(start);
                boolean containsEnd = closure.contains(end);
                if (containsStart && containsEnd) {
                    throw new BioError("The epsilon transitions span entire model, you fool!");
                }

                Node vicar = null;
                if (containsStart) 
                    vicar = start;
                else if (containsEnd)
                    vicar = end;
                else
                    // silly way to have to retrieve an entry from a set....
                    vicar = (Node) closure.iterator().next();
                
                replaceNode(closure, vicar);
            }
        }
        while (foundEpsilonTransitions);
    }

    /**
     * goes thru data structures replacing every instance
     * of old Node with new Node.  Duplicate entries that
     * arise from the process are removed too.
     * <p>
     * The Nfa version of this method also strips
     * epsilon self-transitions.
     */
    private void replaceNode(Set oldNodes, Node newNode)
    {
        // prepare to replace entire contents of transitions
        Transition [] transitionArray = new Transition[transitions.size()];
        transitions.clear();

        // loop thru' all transitions replacing the oldNodes
        for (int i=0; i < transitionArray.length; i++) {
            Transition currTransition = transitionArray[i];

            if (oldNodes.contains(currTransition.source)) {
                currTransition.source = newNode;
            }
            if (oldNodes.contains(currTransition.dest)) {
                currTransition.dest = newNode;
            }
        }

        // put back in transitions. Set behaviour will remove duplicates.
        for (int i=0; i < transitionArray.length; i++) {
            // put back in all non-silly transitions: epsilon self-transitions are silly.
            Transition currTransition = transitionArray[i];

            if ((currTransition.sym != EPSILON) ||
                (currTransition.source != currTransition.dest))
                transitions.add(transitionArray[i]);
        }

        // now clean up the nodes
        for (Iterator oldI = oldNodes.iterator(); oldI.hasNext(); ) {
            nodes.remove(oldI.next());
        }

        nodes.add(newNode);
    }

}

