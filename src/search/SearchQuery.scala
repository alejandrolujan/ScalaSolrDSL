package search

import scala.collection.mutable.LinkedHashMap

class SearchQuery[T <: Searchable] private[search] (private val targetClass: Class[T], translator: Option[Translator[T]]) {
	
	/** Search terms, such as id=5 or yearCreated=2010 */
    private var searchTerms: List[Term[T]] = Nil
    
    /** Search terms that are optional*/
    private var searchTermsOr: List[Term[T]] = Nil
    
    /** Filtering terms, similar to searchTerms */
    private[search] var filterTerms: List[Term[T]] = Nil
    
    /** First result returned, used for pagination. Default is zero. */
    private[search] var fromIndex = 0
    
    /** Max number of results to retrieve. Default is search.page.defaultSize */
    private[search] var fetchCount = Config.int("search.fetchCount")
    
    /** Max number of facet items to retrieve */
    private[search] var facetLimit = Config.int("search.facet.limit")
    
    /** Min number of facet counts - facet hits with less counts than this will not be included in facet results */
    private[search] var facetMinCount = Config.int("search.facet.mincount")
    
    /** Determines if facet search will include a <missing> category for fields with a null value on the facet field */
    private[search] var facetMissing = false
    
    /** Facet information requested as part of the query result */
    private[search] var facetFields = Set[Field[T]]()
    
    /** Facets are sorted by count by default. Set to false to sort alphabetically by index. */
    private[search] var facetSort = true
    
    /** Results will be sorted by these fields, in the order they are added */
    private[search] val sortClauses = LinkedHashMap[Field[T], Boolean]()
    
    /** Adds term to the filtering criteria */
    def filterBy(term: Term[T]) = {
      	filterTerms = term :: filterTerms
      	this
    }

    /** Adds term to the searching criteria */
    def and(term:Term[T]) = {
      	searchTerms = term :: searchTerms
      	this
    }
    
     /** Adds term to the searching criteria */
    def or(term:Term[T]) = {
      	searchTermsOr = term :: searchTermsOr
      	this
    }
    
    /** Sets the index of the first result, for pagination purposes */
    def from(index: Int) = {
		fromIndex = index
		this
    }
    
    /** Adds field to the list of fields to facet on this search query */
    def facet(field: Field[T]) = {
		facetFields += field
		this
    }
    
    /** Sets the maximum number of facet counts to return. Defaults to search.facet.limit */
    def limitFacet(limit: Int) = {
		this.facetLimit = limit
		this
    }
    
    /** Sets the minimum count that a facet must have in order to be returned. Defaults to search.facet.mincount */
    def facetMin(count: Int) = {
		this.facetMinCount = count
		this
    }
    
    def facetSortCount(count: Boolean) = {
    	this.facetSort = count
    	this
    }
    
    /** Sets facetMissing, which determines if facet search will include a <missing> category for fields 
     *  with a null value on the facet field */
    def includeMissing(include: Boolean = true) = {
		this.facetMissing = include
		this
    }

    /** Adds field to the sorting criteria. Ascending defaults to true */
    def sortBy(field: Field[T], ascending: Boolean = true) = {
    	this.sortClauses(field) = ascending
    	this
    }
    
    /** Executes the Search Query and returns the corresponding Response object */
    def fetch: Response[T] = Indexer.search(targetClass, this, translator)
    
    /** Sets the number of objects to fetch and executes the Search Query. Defaults to search.fetchCount */
    def fetch(count: Int): Response[T] = {
      	this.fetchCount = count
      	fetch
    }
    
    /** Fetches the first object that matches this query */
    def first = {
    	val beans = fetch(1).beans
    	beans.size match {
    	    case 1 => Some(beans(0))
    	    case 0 => None
    	    case _ => throw new SearchException("More than one object found where one expected")
    	}
    }
    
    /** Returns the number of objects that match this query */
    def count = fetch(0).total
    
    override def toString = {
    	searchTerms.size + searchTermsOr.size match {
    	  	case 0 => "*:*"
    	  	case _ if( searchTermsOr.size > 0 ) => {
    	  	  searchTermsOr.mkString(" OR ")
    	  	}
    	  	case _ => {
    	  	  searchTerms.mkString(" +")
    	  	}
    	}
	}
}

