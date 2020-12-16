import scala.annotation.tailrec
import scala.util.Random
object TicTacState {
  def create_new_game_state(dim: Int, connect_num: Int, current_player: Char = 'r'): TicTacState = {
    new TicTacState(Vector.fill(dim, dim)('e'), connect_num, current_player, last_move = null, create_new_move_set(dim),
      winner = None)
  }

  private def create_new_move_set(dim: Int): Set[(Int,Int)] ={
    @tailrec
    def generate_move_set(row: Int, col: Int, move_set: Set[(Int, Int)]): Set[(Int,Int)] = {
      if (move_set.size == dim * dim) move_set
      else if (col == dim - 1) {
        generate_move_set(row + 1, col = 0, move_set + ((row, col)))
      } else {
        generate_move_set(row, col + 1, move_set + ((row, col)))
      }
    }
    Random.shuffle(generate_move_set(row = 0, col = 0, Set.empty))
  }
}

class TicTacState(val board: Vector[Vector[Char]], val connect_num: Int, val current_player: Char,
                  val last_move: (Int, Int), val move_set: Set[(Int, Int)], val winner: Option[Char])
  extends GameState {
  val EMPTY: Char = 'e'
  val TIE: Char = 't'
  val dim : Int = board.length - 1
  val other_player: Char = if (current_player == 'r') 'b' else 'r'

  // For Playing
  def is_move_valid(row: Int, col: Int): Boolean = {move_set.contains((row, col))}

  def move(row: Int, col: Int): TicTacState = {
    val new_row = board(row).updated(col, current_player)
    val new_board = board.updated(row, new_row)
    val new_move_set = move_set-((row, col))
    new TicTacState(new_board, connect_num, other_player, (row, col), new_move_set,
      find_winner(new_board, current_player, (row, col), new_move_set))
  }

  // For MCTS
  private def find_winner_helper(board: Vector[Vector[Char]], last_player: Char, row: Int, col: Int, row_delta: Int,
                                 col_delta: Int): Int ={
    val new_row = row + row_delta
    val new_col = col + col_delta
    if (new_row > dim || new_row < 0 || new_col > dim || new_col < 0 || board(new_row)(new_col) != last_player) 0
    else 1 + find_winner_helper(board, last_player, new_row, new_col, row_delta, col_delta)
  }
  private def find_winner(board: Vector[Vector[Char]], last_player: Char, last_move: (Int, Int),
                  move_set: Set[(Int, Int)]): Option[Char] = {
    if (1 + find_winner_helper(board, last_player, last_move._1, last_move._2, row_delta = -1, col_delta = 0) +
      find_winner_helper(board, last_player, last_move._1, last_move._2, row_delta = 1, col_delta = 0) >= connect_num ||
      1 + find_winner_helper(board, last_player, last_move._1, last_move._2, row_delta = 0, col_delta = -1) +
        find_winner_helper(board, last_player, last_move._1, last_move._2, row_delta = 0, col_delta = 1) >= connect_num ||
      1 + find_winner_helper(board, last_player, last_move._1, last_move._2, row_delta = 1, col_delta = 1) +
        find_winner_helper(board, last_player, last_move._1, last_move._2, row_delta = -1, col_delta = -1) >= connect_num ||
      1 + find_winner_helper(board, last_player, last_move._1, last_move._2, row_delta = -1, col_delta = 1) +
        find_winner_helper(board, last_player, last_move._1, last_move._2, row_delta = 1, col_delta = -1) >= connect_num){
      Some(last_player)
    } else if (move_set.isEmpty) Some(TIE) else None
  }

  override def generate_all_neighbors(): LazyList[TicTacState] = {
    val lazy_neighbor_list: LazyList[TicTacState] = {
      def generate(move_set: Set[(Int, Int)]): LazyList[TicTacState] = {
        move_set.headOption match {
          case None => LazyList.empty
          case Some(head) => move(head._1, head._2) #:: generate(move_set - head)
        }
      }
      generate(move_set)
    }
    lazy_neighbor_list
  }

  override def generate_random_neighbor(): TicTacState = {
    val rand = util.Random.nextInt(move_set.size)
    val rand_move = move_set.iterator.drop(rand).next
    move(rand_move._1, rand_move._2)
  }

  override def is_terminal(): Boolean = {
    winner match {
      case Some(_) => true
      case None => false
    }
  }

  override def get_worth(terminal_state: GameState): Double = {
    val terminal_tic_tac_state = terminal_state.asInstanceOf[TicTacState]
    if (terminal_tic_tac_state.winner.get == TIE) 0.5
    else if (terminal_tic_tac_state.winner.get == current_player) 0.0 else 1.0
  }
}
