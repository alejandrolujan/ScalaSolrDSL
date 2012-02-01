package search

import org.junit._
import org.junit.Assert._
import model.Product
import search.{ProductFields => P}

// TODO if you ever use these tests, make sure the fixtures do not clash with any other test data living in your system.
class TestSearch {
  
	private var query:SearchQuery[Product] = _
	
	@Before def createFixtures {
		val p = new Product
		p.id = 10
		p.name = "Elvish lembas"
		p.price = 10
		p.index()
		
		p.id = 11
		p.name = "Nazgul scales"
		p.price = 100
		p.index()
		
		p.id = 12
		p.name = "Shire tobacco"
		p.price = 5
		p.index(true)
		
		query = Product.filterBy(P.id between(10,12))
	}
	
	@After def destroyFixtures {
		Product.deleteById(10)
		Product.deleteById(11)
		Product.deleteById(12)
	}

	@Test def findAllProducts {
		val products = query.fetch
		assertNotNull(products)
		assertEquals(3, products.total)
		assertEquals(3, products.beans.size)
	}
	
	@Test def findById {
		val products = Product.filterBy(P.id eq 12).fetch
		assertNotNull(products)
		assertEquals(1, products.total)
		assertEquals(12, products.beans(0).id)
	}
	
	@Test def findByName {
		val products = query.and(P.name eq "Nazgul scales").fetch
		assertNotNull(products)
		assertEquals(1, products.total)
		assertEquals(11, products.beans(0).id)
	}
	
	@Test def findByNameBetween {
		val products = query.and(P.name between("E","F")).fetch
		assertNotNull(products)
		assertEquals(1, products.total)
		assertEquals(10, products.beans(0).id)
	}
	
	@Test def findByNameNotNull {
		val products = query.and(P.name isNotNull).fetch
		assertNotNull(products)
		assertEquals(3, products.total)
	}
	
	@Test def findByPriceExact {
		val products = query.and(P.price eq 10).fetch
		assertNotNull(products)
		assertEquals(1, products.total)
		assertEquals(10, products.beans(0).id)
	}
	
	@Test def findByPriceLessEqualThan {
		val products = query.and(P.price <= 100).fetch
		assertNotNull(products)
		assertEquals(3, products.total)
	}
	
	@Test def findByPriceGreaterEqualThan {
		val products = query.and(P.price >= 10).fetch
		assertNotNull(products)
		assertEquals(2, products.total)
	}
	
	@Test def findByPriceBetween {
		val products = query.and(P.price between(5,10)).fetch
		assertNotNull(products)
		assertEquals(2, products.total)
	}
	
	// TODO 
	@Test def findByPriceAndName {
	  
	}
	
	@Test def findByPriceOrName {
	  
	}
}