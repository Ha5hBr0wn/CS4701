package Connect4
import mcts._

class Connect4State(val board: Connect4Board, val players: (Player, Player), val turn: Int) extends State{

  /** Prints out the game state to the terminal */
  override def disp(): Unit = board.disp()

  /** Returns the state that results from taking action [a] */
  override def perform(a: Action): State = {
    a match{
      case action: Connect4Action => new Connect4State(board.place(turn, action.column), players, (turn%2) + 1)
      case _ => throw new Exception("[a] must be a Connect4Action")
    }
  }

  /** Returns the current player */
  override def getCurrentPlayer: Player = players.productElement(turn-1).asInstanceOf[Player]

  /** Returns true if the game is over, false otherwise */
  override def isTerminal: Boolean = board.finished

  /** Returns None if there is no winner. Some winner otherwise. Note the game could be over or ongoing if None */
  override def getWinner: Option[Player] = {
    board.winner match{
      case None => None
      case Some(n) => Some(players.productElement(n-1).asInstanceOf[Player])
    }
  }
}

class Connect4Action(val column: Int) extends Action
case class AI(id: Int) extends Player
case class Human(id: Int) extends Player

/*
 * Represents a connect4 board
 * matrix is the board. 0's in empty spot, 1's in spots taken by player 1, 2's in spots taken by player 2
 * the board is 6x7
 * finished is only true if a winning condition is satisfied or the game ends in a tie
 * winner is only not None when finished is true and a player won
 * once a board is finished no more pieces can be placed
 */
class Connect4Board(val matrix: Vector[Vector[Int]], val finished: Boolean, val winner: Option[Int]){
  def disp(): Unit = {
    val transpose = matrix.transpose
    val printRow = (row: Vector[Int]) => row.foreach((ele: Int) => print(ele.toString + " "))
    transpose.foreach((row: Vector[Int]) => {printRow(row); println(" ")})
  }

  /** places a piece numbered [turn] in [col_idx]. Note [col_idx] is numbered 0 through 6.
   * Requires: col_idx is in bounds, the game is not already finished, and the column is not already full */
  def place(turn: Int, col_idx: Int): Connect4Board = {
    checkConditions(col_idx) //checks column and ensures the game is not finished
    val row_idx = if(matrix(col_idx).forall(_ == 0)) 5 else matrix(col_idx).indexWhere(_ != 0) - 1
    val updated_matrix = matrix.updated(col_idx, matrix(col_idx).updated(row_idx, turn))
    val winning_move = Connect4Board.isWinningMove(row_idx, col_idx, updated_matrix, turn)
    val full = Connect4Board.isFull(updated_matrix)
    if(winning_move){
      new Connect4Board(updated_matrix, true, Some(turn))
    }
    else if(full){
      new Connect4Board(updated_matrix, true, None)
    }
    else{
      new Connect4Board(updated_matrix, false, None)
    }
  }

  private def checkConditions(column: Int): Unit = {
    if(column < 0 || column > 6){throw new Exception("column out of bounds")}
    if(finished) {throw new Exception("The game is already finished")}
    if(matrix(column)(0) != 0) {throw new Exception("This column is full")}
  }

}

/** Defines some static methods  */
object Connect4Board{
  def isWinningMove(row_idx: Int, col_idx: Int, matrix: Vector[Vector[Int]], turn: Int): Boolean = {
    numInARow(matrix, (0, -1), row_idx, col_idx-1, turn) + numInARow(matrix, (0, 1), row_idx, col_idx+1, turn) >= 3 ||
      numInARow(matrix, (-1, 0), row_idx-1, col_idx, turn) + numInARow(matrix, (1, 0), row_idx+1, col_idx, turn) >= 3 ||
      numInARow(matrix, (-1, -1), row_idx-1, col_idx-1, turn) + numInARow(matrix, (1, 1), row_idx+1, col_idx+1, turn) >= 3 ||
      numInARow(matrix, (-1, 1), row_idx-1, col_idx+1, turn) + numInARow(matrix, (1, -1), row_idx+1, col_idx-1, turn) >= 3
  }

  def numInARow(matrix: Vector[Vector[Int]], offset: (Int, Int), row_idx: Int, col_idx: Int, turn: Int): Int ={
    if(col_idx < 0 || col_idx > 6 || row_idx < 0 || row_idx > 5) 0
    else if(matrix(col_idx)(row_idx) == turn) 1 + numInARow(matrix, offset, row_idx + offset._1, col_idx + offset._2, turn)
    else 0
  }

  def isFull(matrix: Vector[Vector[Int]]): Boolean = {
    !matrix.flatten.contains(0)
  }

  def init(): Connect4Board = {
    new Connect4Board(Vector.fill(7, 6)(0), false, None)
  }

}


