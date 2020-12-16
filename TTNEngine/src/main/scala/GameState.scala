/** The main trait required for a user to implement in order to utilize the MCTS engine. The
 * trait should be parameterized by which game it is a GameState of. This way there is no accidental
 * mix and match between GameStates of different games.
 *
 * Note there is no explicit definition of terminal. Meaning the game is not necessarily over in a terminal state.
 * Namely it is just a state at which we can apply a heuristic to deem the worth of previously encountered states
 * during simulation. This would be done in the get_worth function. The simplest heuristic would of course
 * be one that just compares the winner if the terminal state corresponds to one where the game is over. */
trait GameState {

  /** Uses the current GameState to generate a LazyList of all the neighboring GameStates. Namely
   * GameStates that can result from an action in the current one. The order of appearance on the LazyList
   * should be randomized. */
  def generate_all_neighbors(): LazyList[GameState]

  /** Uses the current GameState to generate a random neighboring GameState. Note it is very
   * important for the user implementation to be truly random otherwise the MCTS will perform very poorly. Never
   * invoked on a terminal game state */
  def generate_random_neighbor(): GameState

  /** Returns true if this game state is terminal, false otherwise */
  def is_terminal(): Boolean

  /** Returns a value in the range [0, 1] determining how much the player who brought about this GameState won
   * given the terminal_state that proceeded it at some point in time. IMPORTANT NOTE: This should NOT evaluate
   * the state for the current acting entity but for the acting entity that brought about this state (in other
   * words, how good is this state for the player who went in the previous turn given the terminal state)
   * A 0 corresponds with a complete loss, a 1 with a complete win, and it is the implementers choice to
   * decide if they want any values in between (what a tie corresponds to for example). Also for the root state
   * it is ok to choose an arbitrary value like 0 or 1 as there is no player who brought it about. */
  def get_worth(terminal_state: GameState): Double

}

