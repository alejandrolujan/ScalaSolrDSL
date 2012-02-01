package search

private[search] class Term[T<:Searchable] (private val cond: String) {
	override def toString = cond
}
