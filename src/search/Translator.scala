package search

import org.apache.solr.common.{SolrInputDocument, SolrDocumentList, SolrDocument}
import scala.collection.JavaConversions.{asScalaMap, asScalaBuffer, asJavaList}

/** Works as a bridge between Solr documents and your Entity objects.
 *  Implementations of this trait are capable of creating entities from
 *  Solr documents and viceversa. */
trait Translator[T <: Searchable] {
	def toEntities(docs: SolrDocumentList) = docs.map(toEntity(_)).toList
	def toEntity(docs: SolrDocument): T
	def toDocument(entity: T): SolrInputDocument
}