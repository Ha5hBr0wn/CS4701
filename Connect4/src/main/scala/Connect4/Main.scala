package Connect4
import mcts._

object Main {

  def main(args: Array[String]): Unit = {
    val analyzer = new Connect4StateAnalyzer()
    val state = new Connect4State(Connect4Board.init(), (AI(1), Human(2)), 1)
    state.disp()
    play(state, analyzer)
  }

  @scala.annotation.tailrec
  def play(s: Connect4State, analyzer: Connect4StateAnalyzer): Unit = {
    if(s.isTerminal){
      s.getWinner match{
        case None => println("Tie")
        case Some(winner) => println(winner.id + " wins!")
      }
    }
    else{
      s.getCurrentPlayer match{
        case Human(id) =>
          print(id + " where would you like to go (1...7): ")
          val column = scala.io.StdIn.readInt()
          println(" ")
          val new_state = s.perform(new Connect4Action(column - 1))
          new_state.disp()
          println(" ")
          play(new_state.asInstanceOf[Connect4State], analyzer)

        case AI(id) =>
          println(" ")
          val new_state = new AITurnTaker(s, analyzer, 5).takeTurn().asInstanceOf[Connect4State]
          new_state.disp()
          println(" ")
          play(new_state, analyzer)
      }
    }
  }

}
