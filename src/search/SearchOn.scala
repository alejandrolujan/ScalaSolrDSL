package search

trait SearchOn[T <: Searchable] {

	// A rather long way to get the original class from the companion object's class
	private val clazz = Class.forName(this.getClass.getName.replace("$","")).asInstanceOf[Class[T]]

	/** Returns a new SearchQuery object on T */
	def search = new SearchQuery[T](clazz.asInstanceOf[Class[T]], translator)
	
	/** Convenience method that fetches all objects */
	def all = search.fetch

	/** Returns a new SearchQuery object on T that includes the provided search term */
	def search(term: Term[T]):SearchQuery[T] = search.and(term)

	/** Counts the objects that match this query */
	def count(term:Term[T]) = search(term).count

	/** Returns a new SearchQuery object on T that includes the provided filter term */
	def filterBy(term: Term[T]) = search.filterBy(term)

	/** Saves the object to the index */
	def index(obj: T, flush: Boolean = false) = Indexer.index(obj, translator, flush)
	
	/** Removes all objects from the index that match the provided query */
	def deIndex(query: SearchQuery[T]) = Indexer.delete(query)
	
	/** Removes an object from the index by its ID */
	def deleteById(id: Long) = Indexer.deleteById(id)
	
	/** Provides a translation mechanism between objects and indexable documents */
	def translator: Option[Translator[T]] = None
}