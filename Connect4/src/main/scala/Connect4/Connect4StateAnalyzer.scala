package Connect4
import mcts._

import scala.collection.immutable.HashSet
import scala.util.Random

class Connect4StateAnalyzer extends StateAnalyzer {
  /** Returns the set of all possible actions that can be taken in state [s] */
  override def generateAllActions(s: State): HashSet[Action] = {
    s match{
      case s: Connect4State => genActions(s, 0)
      case _ => throw new Exception("[s] must be a Connect4State")
    }
  }

  private def genActions(state: Connect4State, start_col_idx: Int): HashSet[Action] = {
    if(start_col_idx > 6){
      new HashSet[Action]()
    }
    else if(state.board.matrix(start_col_idx)(0) == 0){
      genActions(state, start_col_idx + 1) + new Connect4Action(start_col_idx)
    }
    else{
      genActions(state, start_col_idx + 1)
    }
  }

  /** Returns an action that can be taken in state [s]. Requires [s] is not terminal*/
  override def generateRandomAction(s: State): Action = {
    s match{
      case s: Connect4State =>
        if(s.isTerminal){throw new Exception("[s] cannot be terminal")}
        genRandAction(s, new Random())
      case _ => throw new Exception("[s] must be a Connect4State")
    }
  }

  @scala.annotation.tailrec
  private def genRandAction(state: Connect4State, r: Random): Connect4Action ={
    val col_idx = r.nextInt(7)
    if(state.board.matrix(col_idx)(0) == 0){
      new Connect4Action(col_idx)
    }
    else{
      genRandAction(state, r)
    }
  }

  /** Simulates the game from the state [s] to the end. Returns the winning player if there is one.
   * May or may not actually go all the way to a terminal node depending on whether the
   * implementation wants to use a heuristic to determine the winning player at some depth bound */
  override def simulate(s: State): Option[Player] = {
    s match{
      case s: Connect4State => simulateConnect4(s)
      case _ => throw new Exception("[s] must be a Connect4State")
    }
  }

  @scala.annotation.tailrec
  private def simulateConnect4(s: Connect4State): Option[Player] = {
    if(s.isTerminal){
      s.getWinner
    }
    else{
      simulateConnect4(s.perform(generateRandomAction(s)).asInstanceOf[Connect4State])
    }
  }
}
