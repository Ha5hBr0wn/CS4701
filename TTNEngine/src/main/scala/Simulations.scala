import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import io.circe.generic.auto._, io.circe.syntax._
import java.io._


case class Stat (pts: Double, attempts: Int)
case class Move (simulations: Int, stats: List[Stat])
case class GameResult(winner: Double, p1: Double, p2: Double, moves: List[Move])

object Simulations {
    def print_board(state: TicTacState): Unit = {
      print(state.last_move + "\n")
      for (row <- state.board){
        for(entry <- row){
          print(entry + " ")
        }
        print("\n")
      }
    }

    def computer_vs_computer(state: TicTacState, player_1: StandardMCTSAgent, player_2: StandardMCTSAgent): GameResult = {
      @tailrec
      def turn(curr_state: TicTacState, curr_agent: StandardMCTSAgent, other_agent: StandardMCTSAgent, simulation_list: List[Int],
               moves_list: List[List[(Double, Int)]]): (TicTacState, List[Int], List[List[(Double, Int)]]) = {
        if (curr_state.winner.isEmpty) {
          val turn_data = curr_agent.make_move_data(curr_state)
          val (next_state, simulation_number, move_data) = turn_data
          turn(next_state.asInstanceOf[TicTacState], other_agent, curr_agent, simulation_list :+ simulation_number,
            moves_list :+ move_data)
        } else (curr_state, simulation_list, moves_list)
      }
      val turn_result = turn(state, player_1, player_2, List.empty, List.empty)
      val (final_state, simulation_list, moves_list) = turn_result
      create_game_result(final_state, player_1, player_2, simulation_list, moves_list)
    }
    def create_game_result(final_state: TicTacState, player_1: StandardMCTSAgent, player_2: StandardMCTSAgent,
                           simulation_list: List[Int], moves_data: List[List[(Double, Int)]]): GameResult = {
      def create_stats(moves_list: List[(Double, Int)]): List[Stat] = {
        moves_list.foldLeft(List.empty[Stat])((left_sol, ele) => Stat(ele._1, ele._2) :: left_sol).reverse
      }
      @tailrec
      def create_moves(simulation_list: List[Int], moves_data: List[List[(Double, Int)]], moves_list: List[Move]):
        List[Move] = {
        if (simulation_list.isEmpty) moves_list.reverse
        else create_moves(simulation_list.tail, moves_data.tail, Move(simulation_list.head,
          create_stats(moves_data.head)) +: moves_list)
      }
      val winner: Double = final_state.winner.get match {
          case 'r' => player_1.thinking_time
          case 'b' => player_2.thinking_time
          case _ => 0
        }
      GameResult(winner, player_1.thinking_time, player_2.thinking_time, create_moves(simulation_list, moves_data,
        List.empty))
    }
    def write_result(result: GameResult, file_path: String){
      val file = new File(file_path)
      val bw = new BufferedWriter(new FileWriter(file))
      bw.write(result.asJson.toString())
      bw.close()
    }

    def run_simulation(state: TicTacState, agent_1: StandardMCTSAgent, agent_2: StandardMCTSAgent, num_game: Int,
                       futures: List[Future[Unit]], file_path: String): List[Future[Unit]]= {
        val gameResult = Future{
          val result = computer_vs_computer(state, agent_1, agent_2)
          //write_result(result, file_path = s"${file_path}_${num_game-1}.json")
          write_result(result, file_path = s"${file_path}_${num_game-1+10}.json")
        }
        if (num_game > 1) run_simulation(state, agent_2, agent_1, num_game - 1, gameResult +: futures, file_path)
        else futures
    }

    def run_experiment(dir_name: String, dim: Int, time_1: Double, time_2: Double, num_games: Int, connect_num: Int = 4)
      : Unit = {
      val agent_1 = StandardMCTSAgent(time_1, Math.pow(2, 0.5))
      val agent_2 = StandardMCTSAgent(time_2, Math.pow(2, 0.5))
      val state = TicTacState.create_new_game_state(dim, connect_num)
      val file_path = s"$dir_name/${math.max(time_1, time_2).toInt}_${dim}"
      val futures = run_simulation(state, agent_1, agent_2, num_game = num_games, List.empty, file_path)
      Await.ready(Future.sequence(futures), Duration.Inf)
    }

    def run_2xSimulations(): Unit ={
      for (d <- 4 to 8){
        for(t <- 0 to 4) {
          val longer_time: Double = Math.pow(2, t)
          val shorter_time: Double = longer_time / 2
          run_experiment("2xSimulations", dim = d, longer_time, shorter_time, num_games = 10)
        }
      }
    }

    def main(args: Array[String]): Unit = {
      run_2xSimulations()
    }
}
