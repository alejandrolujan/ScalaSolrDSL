package search

import org.apache.solr.common.{SolrDocument, SolrInputDocument}

import model.Product
import ProductFields._

object ProductTranslator extends Translator[Product] {
	
	def toEntity(doc: SolrDocument) = {
		val p = new Product
		p.id = doc.get(id.name).asInstanceOf[Long]
		p.name = doc.get(name.name).asInstanceOf[String]
		p.price = doc.get(price.name).asInstanceOf[Double]
		p
	}
	
	def toDocument(p: Product) = {
		val doc = new SolrInputDocument
		doc.addField(id.name, p.id)
		doc.addField(name.name, p.name)
		doc.addField(price.name, p.price)
		doc
	}
}