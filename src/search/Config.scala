package search

import java.util.Properties
import com.weiglewilczek.slf4s.Logging

/** Utility class to access configuration items easily */
object Config extends Logging {
  
	val prop = new Properties
	val file = "app.config"
	  
	try {
		val in = getClass().getClassLoader().getResourceAsStream(file)
		prop.load(in)
		in.close
	} catch {
	  	case _ => logger.error("Could not load configuration file " + file + "!")
	}


	def apply(key: String)  = prop.getProperty(key)
	def int(key: String)    = apply(key).toInt
}
