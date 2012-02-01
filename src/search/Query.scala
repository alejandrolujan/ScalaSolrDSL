package search

import scala.collection.mutable.{LinkedHashMap, ListBuffer}
import util.DateRange
import org.apache.solr.client.solrj.SolrQuery
import Query._

/**
 * Our abstraction over a Solr query.
 */
class Query[T <: Searchable] {

    /** Max number of results to retrieve. Default is search.page.defaultSize */
    var pageSize = Config.int("search.page.defaultSize")
    
    /** Max number of facet results to retrieve */
    var facetLimit = Config.int("search.facet.limit")
    
    /** Min number of facet result count - only facet results with a count greater or equal than this will be returned */
    var facetMinCount = Config.int("search.facet.mincount")
    
    /** Include a category for facet counts of documents with no value on the facet field */
    var facetMissing = false
    
    /** Result set starts on this item, used for pagination. Defaults to zero. */
    var start = 0
	    
    /** Search terms, such as id=5 or yearCreated=2010 */
    val searchTerms = LinkedHashMap[Field[T], Any]()
    
    /** Filtering terms, similar to searchTerms */
    val filterTerms = LinkedHashMap[Field[T], Any]()
    
    /** Facet information requested as part of the query result */
    var facetFields = Set[Field[T]]()
    
    /** Results will be sorted by these fields, in the order they are added */
    val sortClauses = LinkedHashMap[Field[T], Boolean]()
	
    
    /**
     * Adds a query term, such as source:Mac to this query.
     * Example: addSearchTerm(PictureIndexedField.source, "Mac") to search all pictures with source "Mac" */
    def addSearchTerm(field: Field[T], value: Any) {
        searchTerms.put(field, value)
    }
    
    /**
     * Adds a filter term on this query.
     * Using filters can improve cache performance if used correctly. 
     * For more details on filtering see http://wiki.apache.org/solr/CommonQueryParameters#fq
     * Example: addFilterTerm(PictureIndexedField.userId, 1) to filter pictures based on userId = 1 */
    def addFilterTerm(field: Field[T], value: Any) {
        filterTerms.put(field, value)
    }
    
    /**
     * Add a sort clause to this query. More than one field can be used for sorting.
     * Existing sort clauses for the same field will be overwritten.  */
    def addSortClause(field:Field[T], ascending: Boolean) {
        sortClauses.put(field, ascending)
    }
    
    /**
     * Returns the order of the sort clause, if it exists on this query. */
    def getSortClause(field:Field[T]) = {
        sortClauses.get(field) match {
            case Some(true) => Some(SolrQuery.ORDER.asc)
            case Some(false) => Some(SolrQuery.ORDER.desc)
            case _ => None
        }
    }
	 
	/**
	 * Builds a Solr query string based on the query terms, sort and facet fields.
	 * See queryTerm for term value support and format */
	def getQuery = {
		var builder = new StringBuilder
		  
		// Append query terms for each term field
		for(field <- searchTerms) 
		    builder.append(queryTerm(field._1, field._2))
		
		// Return query string, default to wildcard
		builder.length match {
			case 0 => "*:*"
		    case _ => builder.toString
		}
	}
	
	/**
	 * Builds a Solr filter query list based on the filter terms.
	 * See queryTerm for term value support and format */
	def getFilterQueries = {
	    val filterQueries = ListBuffer[String]()
	   
	    for(field <- filterTerms)
	        filterQueries += queryTerm(field._1, field._2)
	    
	    filterQueries.toList
	}
	
	/** Generates a query term string from an IndexedField and value 
	 * Single terms generate a query of the form "field:value", 
	 * while ranged terms generate a query of the form "[from TO to]"
	 * Supported ranged values: DateRange. */
	private def queryTerm(field: Field[T], value: Any) = {
		val term = value match {
		  	case v:DateRange => rangeToString(v.from, v.to)
		  	case s:String    => phrase(s)
		  	case _ 			 => value 
		}
		
		and + field.name + fieldValueSeparator + term
	}
	
	private def rangeToString(from: Any, to: Any) = "[" + from + " TO " + to + "]"
	
	private def phrase(s: String) = doubleQuotes + s + doubleQuotes
}

object Query {
    private val fieldValueSeparator = ":"
	private val doubleQuotes = "\""
	private val and = " +"
}