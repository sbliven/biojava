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


package org.biojava.bio.dp.twohead;

import java.util.*;
import java.lang.reflect.*;
import java.io.Serializable;

import org.biojava.utils.*;
import org.biojava.utils.bytecode.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.dist.*;
import org.biojava.bio.dp.*;

/**
 * This is an implementation of CellCalculatorFactoryMaker that compiles the
 * HMM object down to Java byte-code that is equivalent in behaviour to the
 * interpreter.
 *
 * @author Matthew Pocock
 * @since 1.1
 */ 
public class DPCompiler implements CellCalculatorFactoryMaker {
  private final GeneratedClassLoader classLoader;
  
  {
    classLoader = new GeneratedClassLoader(getClass().getClassLoader());
  }
  
  public CellCalculatorFactory make(DP dp) {
    Class forwardC = generateForardClass(dp);
    Class backwardC = generateBackwardClass(dp);
    Class viterbiC = generateViterbiClass(dp);
    
    try {
      Constructor forward = forwardC.getConstructor(new Class[] {
        ScoreType.class
      });
      Constructor backward = backwardC.getConstructor(new Class[] {
        ScoreType.class
      });
      Constructor viterbi = viterbiC.getConstructor(new Class[] {
        ScoreType.class,
        BackPointer.class
      });
      return new Factory(dp, forward, backward, viterbi);
    } catch (NoSuchMethodException nsme) {
      throw new BioError(nsme, "Couldn't find constructor on generated class");
    }
  }
  
  public static String makeName(String prefix, MarkovModel model) {
    StringBuffer nameBuffer = new StringBuffer(prefix);
    
    for(Iterator i = model.stateAlphabet().iterator(); i.hasNext(); ) {
      nameBuffer.append("_");
      try {
        nameBuffer.append(model.transitionsFrom((State) i.next()).size());
      } catch (IllegalSymbolException ise) {
        throw new BioError(
          ise, "Assertion Failure: State dissapeared from model"
        );
      }
    }
    
    return nameBuffer.toString();
  }

  public Class generateForardClass(DP dp) {
    String name = makeName("org.biojava.bio.seq.twohead.Forward", dp.getModel());
    if(classLoader.hasGeneratedClass(name)) {
      try {
        return classLoader.loadClass(name);
      } catch (Exception e) {
        throw new BioError(e, "Can't find previously generated class for " + name);
      }
    }
    
    // forward recursion is:
    // score[state_i] =
    //   emission * sum_j(prev_score[state_j] * transition[state_j, state_i])
    // however, in log space (as we use), this becomes:
    // score[state_i] =
    //   emission + log( sum_j (
    //     exp(prev_score[state_j]) + exp(transition[state_j, state_i)
    //   ))
    //
    // In practice, for sequences of any length, the sum terms are too
    // near to zero for numerical instablilty not to play a huge part. So, we
    // take out a factor of max_j( prev_score[state_j] ) from the sum and add
    // it to the total.

    return null;
  }
  
  public Class generateBackwardClass(DP dp) {
    return null;
  }
  
  public Class generateViterbiClass(DP dp) {
    // viterbi recursion is:
    // score[state_i] =
    //   emission * max_j(prev_score[state_j] * transition[state_j, state_i])
    // however, in log space (as we use), this becomes:
    // score[state_i] =
    //   emission + max_j(
    //     prev_score[state_j] + transition[state_j, state_i]
    //   ))
    
    try {
      MarkovModel model = dp.getModel();

      String name = makeName("org.biojava.bio.seq.twohead.Viterbi", model);
      if(classLoader.hasGeneratedClass(name)) {
        try {
          return classLoader.loadClass(name);
        } catch (Exception e) {
          throw new BioError(e, "Can't find previously generated class for " + name);
        }
      }
    

      CodeClass _Object = IntrospectedCodeClass.forClass(Object.class);
      CodeMethod _Object_init = _Object.getMethod("<init>", CodeUtils.EMPTY_LIST);
      CodeClass _State = IntrospectedCodeClass.forClass(State.class);
      CodeClass _State_A = IntrospectedCodeClass.forClass(State [].class);
      CodeClass _CellCalculator = IntrospectedCodeClass.forClass(CellCalculator.class);
      CodeClass _double_Array = IntrospectedCodeClass.forClass(double [].class);
      CodeClass _DP = IntrospectedCodeClass.forClass(DP.class);
      CodeMethod _DP_getStates = _DP.getMethod("getStates", CodeUtils.EMPTY_LIST);
      CodeMethod _DP_getModel = _DP.getMethod("getModel", CodeUtils.EMPTY_LIST);
      CodeClass _BackPointer = IntrospectedCodeClass.forClass(DP.class);
      CodeClass _Distribution = IntrospectedCodeClass.forClass(Distribution.class);
      CodeMethod _Distribution_getAlphabet = _Distribution.getMethod("getAlphabet", CodeUtils.EMPTY_LIST);
      CodeMethod _Distribution_getWeight = _Distribution.getMethod("getWeight", new CodeClass[] {_State});
      CodeClass _Cell = IntrospectedCodeClass.forClass(Cell.class);
      CodeClass _Cell_A_A = IntrospectedCodeClass.forClass(Cell [][].class);
      CodeClass _ScoreType = IntrospectedCodeClass.forClass(ScoreType.class);
      CodeMethod _ScoreType_calculateScore = _ScoreType.getMethod("calculateScore", new CodeClass[] {_State});
      CodeClass _MarkovModel = IntrospectedCodeClass.forClass(MarkovModel.class);
      CodeMethod _MarkovModel_getWeight = _MarkovModel.getMethod("getWeight", new CodeClass[] {_State});

      GeneratedCodeClass clazz = new GeneratedCodeClass(
        name,
        _Object,
        new CodeClass[] {_CellCalculator},
        CodeUtils.ACC_PUBLIC
      );
      
      State[] states = dp.getStates();
      AlphabetIndex stateIndexer = AlphabetManager.getAlphabetIndex(states);
      
      CodeField terminalBP = clazz.createField(
        "terminalBP",
        _BackPointer,
        CodeUtils.ACC_PROTECTED
      );
      
      CodeField stateF = clazz.createField(
        "states",
        _State_A,
        CodeUtils.ACC_PROTECTED
      );
      
      // The fields that contain transition scores as double [].
      // This is built so that if two states shair the same transition
      // distribution, they refer to the same transition field. This gives
      // good optimizers a fighting chance to do some hard-core optimizations.
      CodeField[] transitionFields = new CodeField[states.length];
      int[] indexToFieldIndex = new int[states.length];
      Map distToIndx = new HashMap();
      
      for(int i = 0; i < states.length; i++) {
        State s = states[i];
        Distribution dist = model.getWeights(s);
        Integer indxI = (Integer) distToIndx.get(dist);
        if(indxI == null) {
          indxI = new Integer(i);
          distToIndx.put(dist, indxI);
          transitionFields[i] = clazz.createField(
            "t_" + i,
            _double_Array,
            CodeUtils.ACC_PROTECTED
          );
          indexToFieldIndex[i] = i;
        } else {
          int indx = indxI.intValue();
          transitionFields[i] = transitionFields[indx];
          indexToFieldIndex[i] = indexToFieldIndex[indx];
        }
      }
      
      // The constructor must load in the transition probabilities.
      // It uses the indexToFieldIndex to ensure that parameters are loaded
      // just once.
      GeneratedCodeMethod __init = clazz.createMethod(
        "<init>",
        CodeUtils.TYPE_VOID,
        new CodeClass[] {_DP, _ScoreType, _BackPointer },
        new String[] { "dp", "scoreType", "backPointer" },
        CodeUtils.ACC_PUBLIC
      );
      
      InstructionVector initG = new InstructionVector();
      // invoke super()
      initG.add( ByteCode.make_aload         (__init.getThis()));
      initG.add( ByteCode.make_invokespecial (_Object_init));
      
      // load up the dp object.
      // Store the states array, and HMM
      LocalVariable statesLV = new LocalVariable("states");
      LocalVariable modelLV = new LocalVariable("model");
      initG.add( ByteCode.make_aload         (__init.getVariable("dp")));
      initG.add( ByteCode.make_dup           ());
      initG.add( ByteCode.make_invokevirtual (_DP_getStates));
      initG.add( ByteCode.make_astore        (statesLV));
      initG.add( ByteCode.make_invokevirtual (_DP_getModel));
      initG.add( ByteCode.make_astore        (modelLV));
      initG.add( ByteCode.make_aload         (__init.getThis()));
      initG.add( ByteCode.make_aload         (statesLV));
      initG.add( ByteCode.make_putfield      (stateF));
      
      // store the backPointer thing in terminalBP
      initG.add( ByteCode.make_aload    (__init.getThis()));
      initG.add( ByteCode.make_iload    (__init.getVariable("backPointer")));
      initG.add( ByteCode.make_putfield (terminalBP));
      
      LocalVariable distLV = new LocalVariable("dist");
      LocalVariable sizeLV = new LocalVariable("size");
      // load in the transition probabilities to the transition fields
      for(int i = 0; i < transitionFields.length; i++) {
        // if this field reference is the first one for this distribution
        if(indexToFieldIndex[i] == i) {
          // Distribution dist = model.getWeights(states[i]);
          Distribution dist = model.getWeights(states[i]);
          initG.add( ByteCode.make_aload         (modelLV));
          initG.add( ByteCode.make_aload         (statesLV));
          initG.add( ByteCode.make_iconst        (i));
          initG.add( ByteCode.make_aaload        ());
          initG.add( ByteCode.make_invokevirtual (_MarkovModel_getWeight));
          initG.add( ByteCode.make_astore        (distLV));
          
          int size = ((FiniteAlphabet) dist.getAlphabet()).size();
          
          // t_i = new double[size]; // leave t_i on stack
          initG.add( ByteCode.make_iconst     (size));
          initG.add( ByteCode.make_newarray   (CodeUtils.TYPE_DOUBLE));
          initG.add( ByteCode.make_dup        ());
          initG.add( ByteCode.make_iload      (__init.getThis()));
          initG.add( ByteCode.make_swap       ());
          initG.add( ByteCode.make_putfield   (transitionFields[i]));
          
          // t_i[j] = scoreType.calculateScore(
          //            dist.getWeight(states[ jj ])
          //          );
          // for each jj in dist - j is index from 0 in t_i in same order as jj
          int j = 0;
          for(int jj = 0; jj < states.length; jj++) {
            State state = states[jj];
            if(dist.getAlphabet().contains(state)) {
              initG.add( ByteCode.make_dup             ());
              initG.add( ByteCode.make_iconst          (j));
              initG.add( ByteCode.make_aload           (distLV));
              initG.add( ByteCode.make_aload           (statesLV));
              initG.add( ByteCode.make_iconst          (jj));
              initG.add( ByteCode.make_invokeinterface (_Distribution_getWeight));
              initG.add( ByteCode.make_invokeinterface (_ScoreType_calculateScore));
              initG.add( ByteCode.make_dastore         ());
              j++;
            }
          }
          
          initG.add( ByteCode.make_pop ());
        }
      }
      
      // return nothing
      initG.add( ByteCode.make_return        ());
      clazz.setCodeGenerator(__init, initG);
      
      GeneratedCodeMethod initialize = clazz.createMethod(
        "initialize",
        CodeUtils.TYPE_VOID,
        new CodeClass[] {_Cell_A_A},
        CodeUtils.ACC_PUBLIC
      );
      clazz.setCodeGenerator(
        initialize,
        createVRecursion(
          true,
          model, states, stateIndexer,
          stateF, transitionFields,
          terminalBP,
          initialize
        )
      );
      
      GeneratedCodeMethod calcCell = clazz.createMethod(
        "calcCell",
        CodeUtils.TYPE_VOID,
        new CodeClass[] {_Cell_A_A },
        CodeUtils.ACC_PUBLIC
      );
      clazz.setCodeGenerator(
      
        calcCell,
        createVRecursion(
          false,
          model, states, stateIndexer,
          stateF, transitionFields,
          terminalBP,
          calcCell
        )
      );
    } catch (CodeException ce) {
      throw new BioError(ce, "Couldn't generate class");
    } catch (NoSuchMethodException nsme) {
      throw new BioError(nsme, "Couldn't find method");
    } catch (IllegalSymbolException ise) {
      throw new BioError(ise, "Couldn't find symbol");
    } catch (BioException be) {
      throw new BioError(be, "Couldn't create indexer");
    }
    return null;
  } 

  CodeGenerator createVRecursion(
    boolean isInit,
    MarkovModel model,
    State[] states,
    AlphabetIndex stateIndex,
    CodeField stateF,
    CodeField [] transitionFields, 
    CodeField terminalBP,
    GeneratedCodeMethod method
  ) throws
    NoSuchMethodException,
    IllegalSymbolException
  {
    CodeClass _Cell = IntrospectedCodeClass.forClass(Cell.class);
    CodeField _Cell_score = _Cell.getFieldByName("scores");
    CodeField _Cell_backpointer = _Cell.getFieldByName("backPointers");
    CodeField _Cell_emission = _Cell.getFieldByName("emission");
    CodeClass _BackPointer = IntrospectedCodeClass.forClass(BackPointer.class);
    CodeMethod _BackPointer_init = _BackPointer.getMethod("<init>", CodeUtils.EMPTY_LIST);

    InstructionVector ccV = new InstructionVector();
    
    // if(isInit && state instanceof emission) {
    //   cell[0][0] = (state == Magical) ? 0.0 : NaN;
    // } else {
    //   cell[0][0].score[i] = cell[0][0].emissions[i] + 
    //                         max_j(cell[adv_i_0][adv_i_1] + t_j[i])
    // }
    
    // cell_00 = cell[0][0];
    // cell_01 = cell[0][1];
    // cell_10 = cell[1][0];
    // cell_11 = cell[1][1];
    LocalVariable[][] cell = new LocalVariable[2][2];
    cell[0][0] = new LocalVariable("cell_00");
    cell[0][1] = new LocalVariable("cell_01");
    cell[1][0] = new LocalVariable("cell_10");
    cell[1][1] = new LocalVariable("cell_11");
    
    ccV.add( ByteCode.make_aload  (method.getVariable("cells")));
    ccV.add( ByteCode.make_dup    ());
    ccV.add( ByteCode.make_iconst (0));
    ccV.add( ByteCode.make_aaload ());
    ccV.add( ByteCode.make_dup    ());
    ccV.add( ByteCode.make_iconst (0));
    ccV.add( ByteCode.make_aaload ());
    ccV.add( ByteCode.make_astore (cell[0][0]));
    ccV.add( ByteCode.make_iconst (1));
    ccV.add( ByteCode.make_astore (cell[0][1]));
    ccV.add( ByteCode.make_iconst (1));
    ccV.add( ByteCode.make_aaload ());
    ccV.add( ByteCode.make_dup    ());
    ccV.add( ByteCode.make_iconst (0));
    ccV.add( ByteCode.make_aaload ());
    ccV.add( ByteCode.make_astore (cell[1][0]));
    ccV.add( ByteCode.make_iconst (1));
    ccV.add( ByteCode.make_astore (cell[1][1]));
    
    // score_00 = cell[0][0].score;
    // score_01 = cell[0][1].score;
    // score_10 = cell[1][0].score;
    // score_11 = cell[1][1].score;
    LocalVariable[][] score = new LocalVariable[2][2];
    score[0][0] = new LocalVariable("score_00");
    score[0][1] = new LocalVariable("score_01");
    score[1][0] = new LocalVariable("score_10");
    score[1][1] = new LocalVariable("score_11");
    ccV.add( ByteCode.make_aload    (cell[0][0]));
    ccV.add( ByteCode.make_aload    (method.getThis()));
    ccV.add( ByteCode.make_getfield ( _Cell_score));
    ccV.add( ByteCode.make_astore   (score[0][0]));
    ccV.add( ByteCode.make_aload    (cell[0][1]));
    ccV.add( ByteCode.make_aload    (method.getThis()));
    ccV.add( ByteCode.make_getfield ( _Cell_score));
    ccV.add( ByteCode.make_astore   (score[0][1]));
    ccV.add( ByteCode.make_aload    (cell[1][0]));
    ccV.add( ByteCode.make_aload    (method.getThis()));
    ccV.add( ByteCode.make_getfield ( _Cell_score));
    ccV.add( ByteCode.make_astore   (score[1][0]));
    ccV.add( ByteCode.make_aload    (cell[1][1]));
    ccV.add( ByteCode.make_aload    (method.getThis()));
    ccV.add( ByteCode.make_getfield ( _Cell_score));
    ccV.add( ByteCode.make_astore   (score[1][1]));
    
    // backpointer_00 = cell[0][0].backpointer;
    // backpointer_01 = cell[0][1].backpointer;
    // backpointer_10 = cell[1][0].backpointer;
    // backpointer_11 = cell[1][1].backpointer;
    LocalVariable[][] backpointer = new LocalVariable[2][2];
    backpointer[0][0] = new LocalVariable("backpointer_00");
    backpointer[0][1] = new LocalVariable("backpointer_01");
    backpointer[1][0] = new LocalVariable("backpointer_10");
    backpointer[1][1] = new LocalVariable("backpointer_11");
    ccV.add( ByteCode.make_aload    (cell[0][0]));
    ccV.add( ByteCode.make_getfield ( _Cell_backpointer));
    ccV.add( ByteCode.make_astore   (backpointer[0][0]));
    ccV.add( ByteCode.make_aload    (cell[0][1]));
    ccV.add( ByteCode.make_getfield ( _Cell_backpointer));
    ccV.add( ByteCode.make_astore   (backpointer[0][1]));
    ccV.add( ByteCode.make_aload    (cell[1][0]));
    ccV.add( ByteCode.make_getfield ( _Cell_backpointer));
    ccV.add( ByteCode.make_astore   (backpointer[1][0]));
    ccV.add( ByteCode.make_aload    (cell[1][1]));
    ccV.add( ByteCode.make_getfield ( _Cell_backpointer));
    ccV.add( ByteCode.make_astore   (backpointer[1][1]));
    
    LocalVariable emissions = new LocalVariable("emissions");
    ccV.add( ByteCode.make_aload    (cell[0][0] ));
    ccV.add( ByteCode.make_getfield (_Cell_emission));
    ccV.add( ByteCode.make_astore   (emissions));
    
    LocalVariable max = new LocalVariable(2, "max");
    LocalVariable max_j = new LocalVariable("max_j");
    for(int i = 0; i < states.length; i++) {
      State state = states[i];
      InstructionVector stateV = new InstructionVector();
      if(isInit && state instanceof EmissionState) {
        stateV.add( ByteCode.make_aload   (score[0][0]));
        stateV.add( ByteCode.make_iconst  (i));
        if(state instanceof MagicalState) {
          stateV.add( ByteCode.make_ldc2_w  (0.0));
        } else {
          stateV.add( ByteCode.make_ldc2_w   (Double.NaN));
          stateV.add( ByteCode.make_aload    (backpointer[0][0]));
          stateV.add( ByteCode.make_iconst   (i));
          stateV.add( ByteCode.make_aload    (method.getThis()));
          stateV.add( ByteCode.make_getfield (terminalBP));
          stateV.add( ByteCode.make_aastore  ());
        }
        stateV.add( ByteCode.make_dastore ());
      } else {
        int[] advance;
        FiniteAlphabet trans = model.transitionsFrom(state);
        if(state instanceof EmissionState) {
          advance = ((EmissionState) state).getAdvance();
        } else {
          advance = new int[] { 0, 0 };
        }
        
        // find max/argmax of t_j[i] + v[j]
        // if there is just one possibility then that is it
        // if there are more, do a max loop
        // leave max,argmax on stack
        if(trans.size() == 1) {
          State state_j = (State) trans.iterator().next();
          int state_jIndx = stateIndex.indexForSymbol(state_j);
          // put t_j[i] + v[j] into max
          stateV.add( createTransitionLastSum(
            method,
            i, state_jIndx,
            transitionFields,
            score[0][0]
          ));
          stateV.add( ByteCode.make_dstore (max));
          // put j into max_j
          stateV.add( ByteCode.make_iconst (state_jIndx));
          stateV.add( ByteCode.make_astore (max_j));
        } else {
          // make a max pipeline
          Iterator each_j = trans.iterator();
          State state_j;
          int state_jIndx;
          
          // first state primes the pump
          state_j = (State) each_j.next();
          state_jIndx = stateIndex.indexForSymbol(state_j);
          stateV.add( createTransitionLastSum(
            method,
            i, state_jIndx,
            transitionFields,
            score[0][0]
          ));
          stateV.add( ByteCode.make_dstore (max));
          stateV.add( ByteCode.make_iconst (state_jIndx));
          stateV.add( ByteCode.make_istore (max_j));
          
          while(each_j.hasNext()) {
            state_j = (State) each_j.next();
            state_jIndx = stateIndex.indexForSymbol(state_j);
            stateV.add( createTransitionLastSum(
              method,
              i, state_jIndx,
              transitionFields,
              score[0][0]
            ));
            stateV.add( ByteCode.make_dup2  ());
            stateV.add( ByteCode.make_dload (max));
            stateV.add( ByteCode.make_dcmpl ());
            
            // if dcmpl is -1, we need to store the new max & imax
            InstructionVector saveNewMax = new InstructionVector();
            saveNewMax.add( ByteCode.make_dstore (max));
            saveNewMax.add( ByteCode.make_iconst (state_jIndx));
            saveNewMax.add( ByteCode.make_istore (max_j));
            
            // if they are equal or max is greater or either is NaN
            // dump current value
            Instruction useOldMax = ByteCode.make_pop2();
            
            stateV.add( new IfExpression(
              ByteCode.op_ifle, // branch if int on stack is <= 0
              saveNewMax,
              useOldMax
            ));
          }
        }
        
        // if max == NaN
        //   score[i] = NaN
        //   bp[i] = null
        // else
        //   sum = emissions[i] + max
        //   score[i] = sum
        //   bp[i] = new BackPointer(state[i], bp[adv_0][adv_1], sum)
        // endif

        // max != NaN as max == max
        stateV.add( ByteCode.make_iload (max));
        stateV.add( ByteCode.make_dup2  ());
        stateV.add( ByteCode.make_dcmpl ());
        
        InstructionVector ifIsNaN = new InstructionVector();
        InstructionVector ifNotNaN = new InstructionVector();
        
        ifIsNaN.add( ByteCode.make_aload   (score[0][0]));
        ifIsNaN.add( ByteCode.make_iconst  (i));
        ifIsNaN.add( ByteCode.make_dastore ());
        ifIsNaN.add( ByteCode.make_aload   (backpointer[0][0]));
        ifIsNaN.add( ByteCode.make_iconst  (i));
        ifIsNaN.add( ByteCode.make_ldc2_w  (Double.NaN));
        ifIsNaN.add( ByteCode.make_aastore ());
        
        // score[i] = emissions[i] + max
        ifNotNaN.add( ByteCode.make_aload   (score[0][0]));
        ifNotNaN.add( ByteCode.make_iconst  (i));
        ifNotNaN.add( ByteCode.make_aload   (emissions));
        ifNotNaN.add( ByteCode.make_iconst  (i));
        ifNotNaN.add( ByteCode.make_daload  ());
        ifNotNaN.add( ByteCode.make_dload   (max));
        ifNotNaN.add( ByteCode.make_dadd    ());
        ifNotNaN.add( ByteCode.make_dastore ());
        
        // backpointer[i] = new BackPointer(
        //  state[i],
        //  backPointer[adv_0][adv_1],
        //  score
        // );
        
        // backpointer[i] =
        ifNotNaN.add( ByteCode.make_aload    (backpointer[0][0]));
        ifNotNaN.add( ByteCode.make_iconst   (i));
        
        // new BackPointer
        ifNotNaN.add( ByteCode.make_new (_BackPointer));
        
        // state[i]
        ifNotNaN.add( ByteCode.make_aload    (method.getThis()));
        ifNotNaN.add( ByteCode.make_getfield (stateF));
        ifNotNaN.add( ByteCode.make_iconst   (i));
        ifNotNaN.add( ByteCode.make_aaload   ());
        
        // backpointer[adv_0][adv_1] [max_j]
        ifNotNaN.add( ByteCode.make_aload  (backpointer[advance[0]][advance[1]]));
        ifNotNaN.add( ByteCode.make_iload  (max_j));
        ifNotNaN.add( ByteCode.make_aaload ());
        
        // score[i]
        ifNotNaN.add( ByteCode.make_aload  (score[0][0]));
        ifNotNaN.add( ByteCode.make_iconst (i));
        ifNotNaN.add( ByteCode.make_daload  ());
        
        // backpointer.<init>
        ifNotNaN.add( ByteCode.make_invokespecial( _BackPointer_init));
        
        // store backpointer
        ifNotNaN.add( ByteCode.make_aastore ());
        
        stateV.add( new IfExpression(
          ByteCode.op_ifeq, // ifeq 0 means max == max
          ifNotNaN,
          ifIsNaN
        ));
      }      
     ccV.add(stateV);
    }
    ccV.add( ByteCode.make_pop ());
    
    ccV.add( ByteCode.make_return ());
    
    return ccV;
  }
  
  private InstructionVector createTransitionLastSum(
    GeneratedCodeMethod method,
    int i, int j, 
    CodeField transition[], LocalVariable lastScore
  ) {
    InstructionVector sumV = new InstructionVector(); 

    // transition_j[i];
    sumV.add( ByteCode.make_aload    (method.getThis()));
    sumV.add( ByteCode.make_getfield (transition[j]));
    sumV.add( ByteCode.make_iconst (i));
    sumV.add( ByteCode.make_daload ());

    // lastScore[j]
    sumV.add( ByteCode.make_aload  (lastScore));
    sumV.add( ByteCode.make_iconst (j));
    sumV.add( ByteCode.make_daload  ());

    // add
    sumV.add( ByteCode.make_dadd   ());
    
    return sumV;
  }
  
  private static class Factory implements CellCalculatorFactory {
    private final DP dp;
    private final Constructor forwards;
    private final Constructor backwards;
    private final Constructor viterbi;
    
    public Factory(
      DP dp, 
      Constructor forwards, 
      Constructor backwards, 
      Constructor viterbi
    ) {
      this.dp = dp;
      this.viterbi = viterbi;
      this.forwards = forwards;
      this.backwards = backwards;
    }
    
    public CellCalculator forwards(ScoreType scoreType)
    throws
      IllegalSymbolException,
      IllegalAlphabetException,
      IllegalTransitionException
    {
      try {
        return (CellCalculator) forwards.newInstance(new Object[] { dp, scoreType });
      } catch (InstantiationException ie) {
        throw new BioError("Counld not instantiate auto-generated class");
      } catch (IllegalAccessException ie) {
        throw new BioError("Counld not instantiate auto-generated class");
      } catch (InvocationTargetException ie) {
        throw new BioError("Counld not instantiate auto-generated class");
      }
    }
  
    public CellCalculator backwards(ScoreType scoreType)
    throws
      IllegalSymbolException,
      IllegalAlphabetException,
      IllegalTransitionException
    {
      try {
        return (CellCalculator) backwards.newInstance(new Object[] { dp, scoreType });
      } catch (InstantiationException ie) {
        throw new BioError("Counld not instantiate auto-generated class");
      } catch (IllegalAccessException ie) {
        throw new BioError("Counld not instantiate auto-generated class");
      } catch (InvocationTargetException ie) {
        throw new BioError("Counld not instantiate auto-generated class");
      }
    }
    
    public CellCalculator viterbi(ScoreType scoreType, BackPointer terminal)
    throws
      IllegalSymbolException,
      IllegalAlphabetException,
      IllegalTransitionException
    {
      try {
        return (CellCalculator) viterbi.newInstance(new Object[] { dp, scoreType, terminal });
      } catch (InstantiationException ie) {
        throw new BioError("Counld not instantiate auto-generated class");
      } catch (IllegalAccessException ie) {
        throw new BioError("Counld not instantiate auto-generated class");
      } catch (InvocationTargetException ie) {
        throw new BioError("Counld not instantiate auto-generated class");
      }
    }
  }  
}
