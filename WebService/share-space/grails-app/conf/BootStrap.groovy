import share.space.User

class BootStrap {
    def init = { servletContext ->
    	if(!User.count()) {
    	println "BOOTSTRAP HIT1"
    		new User(userName:"admin", password:"password", firstName:"Mick", lastName:"O'Dwyer", role:"admin").save()
    		new User(userName:"jdoe", password:"password", firstName:"John", lastName:"Doe", role:"user").save()
    		new User(userName:"jsmith", password:"wordpass", firstName:"Jane", lastName:"Smith", role:"user").save()
    	}
    	println "BOOTSTRAP HIT2"
    	
    }
    
    def destroy = {
    }
}
