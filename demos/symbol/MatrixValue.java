package symbol;
/**
 * This class provides data for the some matrix's found in the dp demos.
 */

public class MatrixValue {

  //0 arg ctor
  public MatrixValue(){
    score=0;
    match=false;
  }

  //2 value ctor
  public MatrixValue(int s, boolean m){
    score=s;
    match=m;
  }
  
  //returns a MatrixValue object
  public MatrixValue getMatrixValues(){
    return this;
  }

  public int getScore(){
    return score;
  }

  public boolean getMatch(){
    return match;
  }

  private int score;//similarity score
  private boolean match;//match status, match=true

}

