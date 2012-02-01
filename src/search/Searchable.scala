package search

trait Searchable {
	var id: Long
	def deIndex = Indexer.deleteById(this.id)
}