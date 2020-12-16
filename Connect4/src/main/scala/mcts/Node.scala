package mcts

import scala.collection.immutable.HashSet

/**
 * Representation of a node used in MCTS
 */
protected class Node(val state: State, val untaken_actions: HashSet[Action],
           val stats: Stats = new Stats(), val taken_actions: HashSet[Edge] = new HashSet[Edge]()) {


  /** Returns true if the node is a leaf */
  def isLeaf: Boolean = {
    untaken_actions.nonEmpty
  }

  /** Returns true if the node is terminal  */
  def isTerminal: Boolean = {
    state.isTerminal
  }

  /** Returns the best edge from this node according to [ucb]. Requires: taken actions must be non empty*/
  def getBestEdge(ucb: (Node, Edge) => Double): Edge = {
    if(taken_actions.isEmpty){throw new Exception("Taken actions must be non-empty")}
    val compareEdges = (e1: Edge, e2: Edge) => if (ucb(this, e1) >= ucb(this, e2)) e1 else e2
    taken_actions.reduceLeft(compareEdges)
    }

  /** Returns the best edge from this node according to which edge has the most play through's. Requires:
   * taken actions must be non empty */
  def getMostPlayedEdge: Edge = {
    if(taken_actions.isEmpty){throw new Exception("Taken actions must be non-empty")}
    val compareEdges = (e1: Edge, e2: Edge) =>
      if (e1.end_node.stats.plays_through >= e2.end_node.stats.plays_through) e1 else e2
    taken_actions.reduceLeft(compareEdges)
  }

}

/**
 * Representation of an edge leaving a node
 */
class Edge(val action: Action, val end_node: Node)



