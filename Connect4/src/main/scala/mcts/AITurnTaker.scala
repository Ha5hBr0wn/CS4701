package mcts

/**
 * Represents an AI that provides functionality to take a turn in a current state, outputting a new state.
 * A user of this package should implement the abstract classes State, Action, Player, and StateAnalyzer in order to
 * use this class. The code in this package is purely functional, that is it uses only immutable values and collections.
 * All code that uses this package should also be written in a functional manner (with exception to I/O).
 *
 * This AI utilizes Monte Carlo Tree Search and can only be used on two player games in which the end result is a tie
 * or a single player wins. Examples of games this could work on include but is not limited to Connect4, Gomoku, and Chess.
 * Games this would not work on is Monopoly (more than two players), or co-operative games where both players win.
 *
 * Effectiveness of this AI will be low in games with massive search spaces like Chess and Go, but it can handle games on
 * the complexity of Gomoku and Connect4 quite well.
 */
class AITurnTaker(state: State, analyzer: StateAnalyzer, thinking_time: Double = 5) {

  /** Returns the state produced by the AI's turn */
  def takeTurn(): State = {
    val root = new Node(state, analyzer.generateAllActions(state))
    val mc_agent = new MonteCarloAgent(root, analyzer, thinking_time)
    state.perform(mc_agent.search())
  }
}
