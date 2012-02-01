package search

class Response[T](
	val beans: List[T],
	val total: Long,
	val facetFields: List[FacetFieldResult],
	val queryTime: Int) {
}