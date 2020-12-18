/** A description of a MCTS Agent. Implementations may differ in optimization details */
trait MCTSAgent {
  /** Runs a MCTS on a given game_state to produce the "best" neighboring game state. Requires
   * the game_state to have neighbors (the game is not over). */
  def make_move(game_state: GameState): GameState

  /** Does the same as make_move but returns more than just the resulting GameState. Namely
   * the number of simulations is returned. And the stats of each neighbor is returned in a
   * List of tuples where the first number is how many "points" were scored with that move and
   * the second number is how many times the move was played. The List is sorted in descending order
   * by the number of times the move was played. The head of the list corresponds to the stats of the
   * move actually taken */
  def make_move_data(game_state: GameState): (GameState, Int, List[(Double, Int)])

}