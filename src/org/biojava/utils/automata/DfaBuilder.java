
package org.biojava.utils.automata;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import org.biojava.bio.symbol.Symbol;

public class DfaBuilder
{

    private Nfa nfa;
    private FiniteAutomaton dfa;
    private Map closureSets = new HashMap();

    private class DfaNode
    {
        FiniteAutomaton.NodeSet closureSet;
        FiniteAutomaton.Node node;

        private DfaNode(FiniteAutomaton.NodeSet closureSet)
        {
            this.closureSet = closureSet;
            node = dfa.addNode(isTerminal());
        }

        /**
         * checks whether this closure set has a terminal
         * Node in it.
         */
        private boolean isTerminal()
        {
            for (Iterator closI = closureSet.iterator(); closI.hasNext(); ) {
                FiniteAutomaton.Node currNode = (FiniteAutomaton.Node) closI.next();
                if (currNode.isTerminal())
                    return true;
            }
            return false;
        }
    }

    DfaBuilder(Nfa nfa)
    {
        this.nfa = nfa;
        dfa = new FiniteAutomaton("dfa_" + nfa.getName(), nfa.getAlphabet());
    }


    /**
     * Given a DFA node representing a Set of NFA nodes,
     * construct other DFA nodes that transit from it.
     */
    private void constructSubsets(DfaNode dfaNode)
        throws AutomatonException
    {
        // retrieve the NFA nodes
        FiniteAutomaton.NodeSet closureSet = dfaNode.closureSet;

        // what Symbols have transitions from the source closure set?
        Set symbolSet = new HashSet();
        for (Iterator closI = closureSet.iterator(); closI.hasNext(); ) {
            org.biojava.utils.automata.Nfa.Node node = (org.biojava.utils.automata.Nfa.Node) closI.next();

            symbolSet.add(nfa.getSymbols(node));
        }

        // for each of the NFA nodes and each Symbol, compute
        // the next closure Sets and their associated DFA node.
        for (Iterator symI = symbolSet.iterator(); symI.hasNext(); ) {
            Symbol currSymbol = (Symbol) symI.next();
            FiniteAutomaton.NodeSet closureForSymbol = nfa.createNodeSet();

            for (Iterator closI = closureSet.iterator(); closI.hasNext(); ) {
                org.biojava.utils.automata.Nfa.Node node = (org.biojava.utils.automata.Nfa.Node) closI.next();

                // compute closure set for current symbol for this NFA node    
                FiniteAutomaton.NodeSet currNodeSet = nfa.getClosure(node, currSymbol);
                closureForSymbol.addNodeSet(currNodeSet);
            }

            if (!closureForSymbol.isEmpty()) {
                // check whether this NodeSet has had a DfaNode assigned to it.
                if (closureSets.containsKey(closureForSymbol)) {
                    // the new Transition ends with a known DfaNode.

                    // add transition from dfa Node to a existing Node for this closure set.
                    DfaNode dfaDestNode = getDfaNode(closureForSymbol);
                    dfa.addTransition(dfaNode.node, dfaDestNode.node, currSymbol);
                }
                else {
                    // this is a novel closure set.

                    // create a DfaNode for this closure set.
                    DfaNode dfaDestNode = getDfaNode(closureForSymbol);

                    // add transition from dfa Node to this new Node.
                    dfa.addTransition(dfaNode.node, dfaDestNode.node, currSymbol);

                    // call self recursively with the new Node.
                    constructSubsets(dfaDestNode);
                }
            }
        }
    }

    /**
     * get the DFA Node associated with the closure Set of NFA nodes.
     */
    private DfaNode getDfaNode(FiniteAutomaton.NodeSet nfaNodes)
    {
        DfaNode dfaNode;

        if ((dfaNode = (DfaNode) closureSets.get(nfaNodes)) == null) {
             return (DfaNode) closureSets.put(nfaNodes, new DfaNode(nfaNodes));
        }
        else
            return dfaNode;
    }
}
