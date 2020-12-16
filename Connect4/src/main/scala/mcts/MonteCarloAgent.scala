package mcts

import scala.concurrent.duration._
/**
 * An agent that does MCTS
 */
protected class MonteCarloAgent(val root: Node, val analyzer: StateAnalyzer,
                      val search_time: Double = 5, val ucb: (Node, Edge) => Double = MonteCarloAgent.ucb) {

  /** Performs a Monte Carlo Tree Search from [root] and returns the best mcts.Action*/
  @scala.annotation.tailrec
  final def search(): Action = {
    if(search_time <= 0){
      println(root.stats.plays_through)
      root.getMostPlayedEdge.action
    }
    else{
      val deadline = search_time.seconds.fromNow
      val updated_agent = new MonteCarloAgent(mcts(root), analyzer, deadline.timeLeft.length * Math.pow(10, -9), ucb)
      updated_agent.search()
    }
  }


  /** Returns an updated version of [node] after a single play through with MCTS with ucb*/
  def mcts(node: Node): Node = {
    if(node.isTerminal){
      new Node(node.state, node.untaken_actions, Stats.updateStats(node), node.taken_actions)
    }
    else if(node.isLeaf){
      val chosen_action = node.untaken_actions.head
      val expanded_state = node.state.perform(chosen_action)
      val simulated_winner = analyzer.simulate(expanded_state)
      val expanded_edge = new Edge(chosen_action, new Node(expanded_state, analyzer.generateAllActions(expanded_state),
        Stats.makeStats(expanded_state, simulated_winner)))
      val updated_stats = Stats.updateStats(Stats.init, expanded_edge.end_node.stats, node.stats)
      new Node(node.state, node.untaken_actions.tail, updated_stats, node.taken_actions + expanded_edge)
    }
    else{
      val original_best_edge = node.getBestEdge(ucb)
      val updated_best_edge = new Edge(original_best_edge.action, mcts(original_best_edge.end_node))
      val updated_stats = Stats.updateStats(original_best_edge.end_node.stats, updated_best_edge.end_node.stats, node.stats)
      new Node(node.state, node.untaken_actions, updated_stats, (node.taken_actions - original_best_edge) + updated_best_edge)
    }
  }
}

/** Contains a default UCB */
object MonteCarloAgent{
  def ucb(node: Node, edge: Edge): Double = {
    val avg_reward = edge.end_node.stats.wins_through.toDouble / edge.end_node.stats.plays_through
    val optimism = 2 * math.sqrt(math.log(node.stats.plays_through)/edge.end_node.stats.plays_through)
    avg_reward + optimism
  }
}

