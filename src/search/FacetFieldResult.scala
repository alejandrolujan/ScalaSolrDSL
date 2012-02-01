package search

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.mutable.LinkedHashMap

import org.apache.solr.client.solrj.response.{FacetField => AFacetField}

/** Represents a facet query result, such as:
 *  	Product color: {black:10, white:8, other:5} 
 */
class FacetFieldResult(var name: String, val values: LinkedHashMap[String, Long]) { }

object FacetFieldResult {

	def apply(solrFF: AFacetField) = {
        val values = LinkedHashMap[String, Long]()
        solrFF.getValues.foreach(v => values.put(v.getName, v.getCount))
        new FacetFieldResult(solrFF.getName, values)
    }
    
    def apply(solrFF: List[AFacetField]): List[FacetFieldResult] = {
    	var facetFields = List[FacetFieldResult]()
    	solrFF.foreach(f => facetFields = FacetFieldResult(f) :: facetFields)
	    facetFields.reverse
    }
}
