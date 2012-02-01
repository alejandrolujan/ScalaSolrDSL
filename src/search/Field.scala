package search

import java.util.Date

import org.apache.solr.client.solrj.util.ClientUtils
import org.apache.solr.common.util.DateUtil

class Field[T <: Searchable](val name: String) {
	def eq(value: Any) = new Term[T](name + ":" + clean(value))
	def >=(value: Any) = between(clean(value), "*")				// Special case of between
	def <=(value: Any) = between("*", clean(value))				// Special case of between
	def isNotNull      = between("*", "*")							// Special case of between
	def isNull         = new Term[T]("-" + isNotNull.toString)		// Negation of isNotNull
	def between(lower: Any, upper: Any) 
	                   = new Term[T](name + ":[" + clean(lower) + " TO " + clean(upper) + "]")
	                   
	private def clean(value: Any) = {
		value match {
		  	case d:Date 	=> ClientUtils.escapeQueryChars(DateUtil.getThreadLocalDateFormat.format(d))
		  	case s:String 	=> '"' + ClientUtils.escapeQueryChars(s) + '"'
		  	case _ 			=> ClientUtils.escapeQueryChars(value.toString)
		}
	}
}