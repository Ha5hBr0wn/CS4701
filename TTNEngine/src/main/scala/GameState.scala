/** The main trait required for a user to implement in order to utilize the MCTS engine.
 *
 * Note there is no explicit definition of terminal. Meaning the game is not necessarily over in a terminal state.
 * Namely it is just a state at which we can apply a heuristic to deem the worth of previously encountered states
 * during simulation. This would be done in the get_worth function. The simplest heuristic would of course
 * be one that just compares the winner if the terminal state corresponds to one where the game is over. */
trait GameState {

  /** Uses the current GameState to generate a LazyList of the neighboring GameStates that the
   * user wants to explore (technically does not have to be literally all neighbors).
   * The order of appearance on the LazyList should be randomized. */
  def generate_all_neighbors(): LazyList[GameState]

  /** Uses the current GameState to generate a random neighboring GameState used for simulation. The neighbor
   * does not have to be generated uniformly at random if a user wishes to use a heavy playout */
  def generate_random_neighbor(): GameState

  /** Returns true if this game state is terminal, false otherwise */
  def is_terminal(): Boolean

  /** Returns a value in the range [0, 1] determining how much the player who brought about this GameState won
   * given the terminal_state that proceeded it at some point in time. IMPORTANT NOTE: This should NOT evaluate
   * the state for the current acting entity but for the acting entity that brought about this state
   * A 0 corresponds with a complete loss, a 1 with a complete win, and it is the implementers choice to
   * decide if they want any values in between (what a tie corresponds to for example). Also for the root state
   * it is ok to choose an arbitrary value like 0 or 1 as there is no player who brought it about. */
  def get_worth(terminal_state: GameState): Double

}