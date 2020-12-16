package mcts

/**
 * An abstract representation of a game state
 */
abstract class State{

  /** Prints out the game state to the terminal */
  def disp(): Unit

  /** Returns the state that results from taking action [a] */
  def perform(a: Action): State

  /** Returns the current player */
  def getCurrentPlayer: Player

  /** Returns true if the game is over, false otherwise */
  def isTerminal: Boolean

  /** Returns None if there is no winner. Some winner otherwise. Note the game could be over or ongoing if None */
  def getWinner: Option[Player]
}

/**
 * An abstract representation of a player. Different players must have different id's if they are of the same subtype
 */
abstract class Player {
  /** The id of the player */
  val id: Int
}

/**
 * An abstract representation of an mcts.Action
 */
abstract class Action