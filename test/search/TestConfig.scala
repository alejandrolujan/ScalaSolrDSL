package search

import org.junit._
import org.junit.Assert._

class TestConfig {

	@Test def ConfigHasSolrUrl {
		val url = Config("search.solr.url")
		assertNotNull(url)
	}
}