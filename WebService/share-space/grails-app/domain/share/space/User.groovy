package share.space

class User {
	static constraints = {
  	userName(blank:false,unique:true)
  	password(password:true, blank:false)
  	firstName(blank:false)
  	lastName(blank:false)
  	role(inList:["user", "admin"])
  }
  
	String userName
	String password
	String firstName
	String lastName
	String role = "user"
	//Usergroup usergroup
	//Set shares = []
	
	//static hasMany = [shares: Share]	
  
  String toString() {
  	"$lastName, $firstName"
  }
}
