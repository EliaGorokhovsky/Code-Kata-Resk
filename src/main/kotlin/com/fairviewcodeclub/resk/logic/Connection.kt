package com.fairviewcodeclub.resk.logic

/**
 * A connection between two tiles
 * Signifies that travel between those two tiles is permitted
 * Connections are bidirectional (ordering of node1 and node2 doesn't matter)
 * Nodes are stored as indices, NOT NODE OBJECTS
 */
class Connection(val node1: Int, val node2: Int) {

	/**
	 * Returns the other node of the connection if the given index is connected by this connection
	 * Returns null if the given index isn't a part of this connection
	 */
	fun connects(index: Int): Int? {
		return if (this.node1 == index) this.node2 else if (this.node2 == index) this.node1 else null
	}

	/**
	 * Two connections are equal if they have the same nodes, regardless of order
	 */
	override fun equals(other: Any?): Boolean {
		return other is Connection &&
				(other.node1 == this.node2 && other.node2 == this.node1
				||
				other.node1 == this.node1 && other.node2 == this.node2)
	}

}
