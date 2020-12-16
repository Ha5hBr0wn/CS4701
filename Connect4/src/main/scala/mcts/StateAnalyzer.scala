package mcts

import scala.collection.immutable.HashSet

/**
 * An abstract representation of a game/state analyzer
 */
abstract class StateAnalyzer {
  /** Returns the set of all possible actions that can be taken in state [s] */
  def generateAllActions(s: State): HashSet[Action]

  /** Returns an action that can be taken in state [s]. Requires [s] is not terminal*/
  def generateRandomAction(s: State): Action

  /** Simulates the game from the state [s] to the end. Returns the winning player if there is one.
   * May or may not actually go all the way to a terminal node depending on whether the
   * implementation wants to use a heuristic to determine the winning player at some depth bound */
  def simulate(s: State): Option[Player]
}
