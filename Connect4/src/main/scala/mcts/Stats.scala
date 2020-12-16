package mcts

/**
 * Representation of the stats at a node. Note the number of wins corresponds to wins of the parent not the current
 * player in the state of the node
 */
protected class Stats(val wins_through: Int = 0, val plays_through: Int = 0)

/** Contains methods to generate stats assuming the game is a two player game */
object Stats{

  /** Returns new stats based on changes to the child stats and your current stats */
  def updateStats(child_original: Stats, child_updated: Stats, parent_original: Stats): Stats = {
    if(child_original.wins_through == child_updated.wins_through){
      new Stats(parent_original.wins_through + 1, parent_original.plays_through + 1)
    }
    else{
      new Stats(parent_original.wins_through, parent_original.plays_through + 1)
    }
  }

  /** Returns new stats based on the current terminal node. Requires: [term_node] is a terminal node */
  def updateStats(term_node: Node): Stats = {
    term_node.state.getWinner match{
      case None => new Stats(term_node.stats.wins_through, term_node.stats.plays_through + 1)
      case Some(player) =>
        if(term_node.state.getCurrentPlayer == player){
          new Stats(term_node.stats.wins_through, term_node.stats.plays_through + 1)
        }
        else{
          new Stats(term_node.stats.wins_through + 1, term_node.stats.plays_through + 1)
        }
    }
  }

  /** Returns initial stats based on the state of the expanded node and the simulated result */
  def makeStats(state: State, winner: Option[Player]): Stats = {
    winner match{
      case None => new Stats(0, 1)
      case Some(player) =>
        if (state.getCurrentPlayer == player){
          new Stats(0, 1)
        }
        else{
          new Stats(1, 1)
        }
    }
  }

  /** Returns an initial stats of 0/0 */
  def init: Stats = {
    new Stats()
  }
}