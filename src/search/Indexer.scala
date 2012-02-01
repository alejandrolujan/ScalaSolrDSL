package search

import scala.collection.JavaConversions.asScalaBuffer

import org.apache.commons.httpclient.auth.AuthScope
import org.apache.commons.httpclient.{HttpClient, UsernamePasswordCredentials}
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer
import org.apache.solr.client.solrj.response.{FacetField => AFacetField}
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.client.solrj.SolrQuery

import com.weiglewilczek.slf4s.Logging

/** Indexes and searches documents on Solr.*/
private[search] object Indexer extends Logging {

	// Your solr instance should always be protected, and http authentication is the minimum you should do
	private val authScope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT)
	private val httpClient = new HttpClient
	httpClient.getParams.setAuthenticationPreemptive(true)
	httpClient.getState.setCredentials(authScope,new UsernamePasswordCredentials(Config("search.httpAuthUser"), Config("search.httpAuthPass")))
	private val server = new CommonsHttpSolrServer(Config("search.solr.url"), httpClient)
	
	/** Indexes a document in Solr, updating it if it already exists.
	  * If flush = true, a commit is issued.
	  * Let solr auto-commit unless you absolutely need this document to be available immediately. */
	def index[T <: Searchable](entity: T, translator: Option[Translator[T]] = None, flush: Boolean = false) = 
	synchronized {
		try{
			translator match {
			  	case Some(t) => server.add(t.toDocument(entity))
			  	case _ => server.addBean(entity)
			}
			
			if(flush) server.commit
		} catch {
			case t => throw new IndexingException(t)		// wrap exception and throw
		}
	}
	
	/** Deletes the document identified by this id */
	def deleteById(id: Any) = synchronized{
		try{
			server.deleteById(id.toString)
		} catch {
			case t => throw new IndexingException(t)		// wrap exception and throw
		}
	}

	/** Deletes all documents that match the query */
	def delete[T <: Searchable](query: SearchQuery[T]) = synchronized {
		try{
			server.deleteByQuery(query.toString)
		} catch {
			// wrap exception and throw
			case t => throw new IndexingException(t)
		}
	}

	private def order(asc: Boolean) = if(asc) SolrQuery.ORDER.asc else SolrQuery.ORDER.desc

	
	/** Fetches entities that match the constraints set on the Query object. 
	 * Entities are of the class clazz, and if a translator is provided, it is used
	 * to build entities from the returned objects. 
	 * If facet results are required, they will be wrapped in the Response object returned. */
	def search[T <: Searchable](clazz: Class[T], query: SearchQuery[T], translator: Option[Translator[T]] = None) = {
		
		// Build Solr query object
		val solrQuery = new SolrQuery().setQuery(query.toString)
		
		// Add filter queries
		query.filterTerms.foreach(t => solrQuery.addFilterQuery(t.toString))
	
		// Add sort clauses
		for((key,asc) <- query.sortClauses) 
			solrQuery.addSortField(key.name, order(asc))
		
		// Paging clauses
		solrQuery.setStart(query.fromIndex)
		solrQuery.setRows(query.fetchCount)
	
		// Facet clauses
		if(query.facetFields.size > 0){
			solrQuery.setFacet(true)
			solrQuery.setFacetLimit(query.facetLimit)
			solrQuery.setFacetMinCount(query.facetMinCount)
			solrQuery.setFacetMissing(query.facetMissing)
			solrQuery.setFacetSort(query.facetSort)
		
			query.facetFields.foreach(f => solrQuery.addFacetField(f.name))
		} 
		
		logger.debug("Solr query: " + solrQuery)
		
		
		try {
			// Send query to the server, wrap response and return
			var response: QueryResponse = null
			
			synchronized {
				response = server.query(solrQuery)
			}
			
			val facetFields = response.getFacetFields match {
				case f: java.util.List[AFacetField] => FacetFieldResult(f.toList)
				case _ => List[FacetFieldResult]()
			}
			
			// Translate solr documents to entities
			val beans = translator match {
			  	case Some(t) => t.toEntities(response.getResults)		// Use the document-bean translator
			  	case _ => response.getBeans(clazz).toList				// Assume they can be interpreted by QueryResponse
			}	
			
			new Response(beans, 
						response.getResults.getNumFound, 
						facetFields, 
						response.getQTime)
		} catch {
			case t => logger.error("Error while querying Solr", t)
					  throw new SearchException(t)
		}
	}
}
