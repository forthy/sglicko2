/*
 * Copyright (c) 2015, Andreas Flierl <andreas@flierl.eu>
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package sglicko2

import scala.collection.breakOut

class Leaderboard[A] private (val playersByIdInNoParticularOrder: Map[A, Player[A]]) extends Serializable {
  lazy val idsByRank: Vector[Set[A]] = playersByIdInNoParticularOrder.values.groupBy(_ rating).toVector.sortBy(e => - e._1).map { case (_, ps) => ps.map(_ id)(breakOut): Set[A] }
  lazy val rankedPlayers: Vector[RankedPlayer[A]] = idsByRank.zipWithIndex.flatMap { case (ids, idx) => ids.map(id => RankedPlayer(idx + 1, playersByIdInNoParticularOrder(id))) }
  lazy val playersInRankOrder: Vector[Player[A]] = idsByRank.flatMap(_ map playersByIdInNoParticularOrder)

  def playerIdentifiedBy(id: A): Either[A, Player[A]] = playersByIdInNoParticularOrder.get(id).toRight(id)

  def updatedWith(players: Traversable[Player[A]]): Leaderboard[A] =
    new Leaderboard(players.foldLeft(playersByIdInNoParticularOrder)((accu, el) => accu.updated(el id, el)))

  def rankOf(id: A): Option[Int] = idsByRank.indexWhere(_ contains id) match {
    case -1 => None
    case other => Some(other + 1)
  }

  override def equals(any: Any): Boolean = any match {
    case other: Leaderboard[_] => other.playersByIdInNoParticularOrder == playersByIdInNoParticularOrder
    case _ => false
  }

  override def hashCode: Int = playersByIdInNoParticularOrder##

  override def toString = s"Leaderboard(${rankedPlayers mkString ", "})"
}

object Leaderboard {
  def empty[A] = new Leaderboard[A](Map())
  def fromPlayers[A](players: Traversable[Player[A]]) = new Leaderboard[A](players.map(p => p.id -> p)(breakOut))
}
