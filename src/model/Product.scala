package model

import search.Searchable
import search.SearchOn
import search.ProductTranslator

class Product extends Searchable {
	var id: Long = _
	var name: String = _
	var price: Double = _
	
	// Convenience method
	def index(flush: Boolean = false) = Product.index(this, flush)
	
	// TODO make this a trait?
	override def toString = "Product: " + id + "," + name + "," + price
}

object Product extends SearchOn[Product] {
	// The document-to-object translator for Solr searches
	override def translator = Some(ProductTranslator)
}