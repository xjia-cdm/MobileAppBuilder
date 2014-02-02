package share.space

class SecurityFilters {
	def filters = {
		basicAuth(controller:'*', action:'*') {
		  before = {
		  	def authString = request.getHeader('Authorization')
		  	println "authString: ${authString}"
		  	//if(!authString) {
		  	//	redirect uri:"500"
		  	//}
		  	
		  	//def encodedPair = authString - "Basic "
		  	//println "encodedPair: ${encodedPair}"
		  	//def decodedPair = new String(new sun.misc.BASE64Decoder().decodeBuffer(encodedPair))
		  	//def credentials = decodedPair.split(":")
		  	//def user = User.findByNameAndPassword(credentials[0],credentials[1])
		  	def credentials = authString.split(':')
		  	println "credentials[0]: ${credentials[0]} and credentials[1]: ${credentials[1]}"
		  	def user = User.findByUserNameAndPassword(credentials[0],credentials[1])
		  	println "User.count(): ${User.count()}"
		  	if(user) {
		  	  session.user = user
		  	  println "session.user = user"
		  	} else {
		  	  redirect uri:"500"
		  	}
		  }
		}
	}
}