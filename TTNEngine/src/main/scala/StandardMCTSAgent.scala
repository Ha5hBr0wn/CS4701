import scala.concurrent.duration._
import scala.math.pow
import scala.math.log


// stats: reward/attempts for the parent node
// total_occurrences: number of times this particular node has been passed through
private case class Node(game_state: GameState, unevaluated_neighbors: LazyList[GameState],
                           evaluated_neighbors: Set[Node], stats: (Double, Int), total_occurrences: Int)


/** Provides that standard textbook version of MCTS using UCB and no clever optimizations. Thinking
 * time is how long the MCTS will run in seconds. c is a parameter for the UCB heuristic. Higher values of c
 * favor more exploration */
case class StandardMCTSAgent(thinking_time: Double, c: Double = pow(2, 0.5)) extends MCTSAgent {

  def make_move(game_state: GameState): GameState = {
    val start_tree = Node(game_state, game_state.generate_all_neighbors(), Set.empty, (0, 0), 0)
    val searched_tree = mcts(start_tree, thinking_time.seconds.fromNow)
    most_played_neighboring_state(searched_tree)
  }

  def make_move_data(game_state: GameState): (GameState, Int, List[(Double, Int)]) = {
    val start_tree = Node(game_state, game_state.generate_all_neighbors(), Set.empty, (0, 0), 0)
    val searched_tree = mcts(start_tree, thinking_time.seconds.fromNow)
    (most_played_neighboring_state(searched_tree), searched_tree.total_occurrences,
      extract_stats(searched_tree))
  }

  /** Runs MCTS until deadline has passed and then returns the resulting tree */
  @scala.annotation.tailrec
  private def mcts(tree: Node, deadline: Deadline): Node = {
    if (deadline.hasTimeLeft()){
      mcts(select(tree)._1, deadline)
    }
    else{
      //println(tree.total_occurrences)
      tree
    }
  }

  /** Runs the selection, expansion, simulation, and backpropgation phases of a single MCTS iteration on tree.
   * Returns the resulting tree along with the terminal_state the simulation ended with. */
  private def select(tree: Node): (Node, GameState) = {
    if(tree.game_state.is_terminal()){
      (tree.copy(stats = (tree.stats._1 + tree.game_state.get_worth(tree.game_state), tree.stats._2 + 1),
      total_occurrences = tree.total_occurrences + 1), tree.game_state)
    }
    else if(tree.unevaluated_neighbors.isEmpty) {
      val best_neighbor = best_choice(tree)
      val (updated_subtree, terminal_state) = select(best_neighbor)
      (tree.copy(evaluated_neighbors = tree.evaluated_neighbors - best_neighbor + updated_subtree,
                 stats = (tree.stats._1 + tree.game_state.get_worth(terminal_state), tree.stats._2 + 1),
                 total_occurrences = tree.total_occurrences + 1), terminal_state)
    }
    else{
      val new_game_state = tree.unevaluated_neighbors.head
      val remaining_neighbors = tree.unevaluated_neighbors.tail
      val terminal_state = randomly_simulate(new_game_state)
      val expanded_node = Node(new_game_state, new_game_state.generate_all_neighbors(), Set.empty,
        (new_game_state.get_worth(terminal_state), 1), 1)
      (tree.copy(unevaluated_neighbors = remaining_neighbors,
                 evaluated_neighbors = tree.evaluated_neighbors + expanded_node,
                 stats = (tree.stats._1 + tree.game_state.get_worth(terminal_state), tree.stats._2 + 1),
                 total_occurrences = tree.total_occurrences + 1), terminal_state)
    }
  }

  /** Returns the neighbor with the highest UCB score. Requires tree to have a non empty
   * evaluated neighbors. Should only be called when unevaluated neighbors is empty and all moves
   * are in the evaluated set, and tree is not a terminal node */
  private def best_choice(tree: Node): Node = {
    tree.evaluated_neighbors.maxBy((node: Node) => choice_heuristic(node.stats, tree.total_occurrences))
  }

  /** Randomly simulates a game from game_state to a terminal state and returns it */
  @scala.annotation.tailrec
  private def randomly_simulate(game_state: GameState): GameState = {
    if (game_state.is_terminal()) {
      game_state
    }
    else {randomly_simulate(game_state.generate_random_neighbor())}
  }

  /** Returns the GameState simulated the most times from node. Requires node not to contain a terminal
   * state */
  private def most_played_neighboring_state(node: Node): GameState = {
    node.evaluated_neighbors.maxBy((n: Node) => n.total_occurrences).game_state
  }

  /** The UCB heuristic applied during the selection phase of MCTS.
   * The tuple type represents a fraction of reward/attempts for a specific branch.
   * Returns the result of the heuristic */
  private def choice_heuristic(ratio: (Double, Int), total_attempts: Int): Double = {
    (ratio._1 / ratio._2) + (c * pow(log(total_attempts)/ratio._2, 0.5))
  }

  /** Extracts the stats of the evaluated neighbors from this tree. Returns them in descending order
   * by the number of times each was played. So the head corresponds to the move that was actually
   * played */
  private def extract_stats(tree: Node): List[(Double, Int)] = {
    val scores = tree.evaluated_neighbors.foldLeft(List.empty[(Double, Int)])(
      (left_sol, ele) => ele.stats :: left_sol)
    scores.sortBy(stats => -stats._2)
  }


}
