package search
import model.Product

// WARNING: This file is generated by SchemaParser.scala, if you need to modify it, 
// make sure your changes won't be overwritten during the build process.
object ProductFields {
    val id = new Field[Product]("id")
    val name = new Field[Product]("name")
    val price = new Field[Product]("price")
}