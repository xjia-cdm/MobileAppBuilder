package share.space

import grails.converters.*
import groovy.xml.MarkupBuilder
import groovy.json.*

class UserController {

    //def index() { }
    def scaffold = User
    
    def login = {}
    
    def list = {
    	if(!params.max) params.max = 10
    	def list = User.list(params)
    	request.withFormat{
    		
    	  html{ [userInstanceList: User.list(params)] }
    		json{	render list as JSON	}
    		//json{ render jsonOut }
    		//xml{ render list as XML	} 
    		xml{ render xmlOut }
    	} 
    }
    
    def show = {
    	if(!params.max) params.max = 10
    	def list = User.findById(params.id)
    	request.withFormat{
    		
    	  html{ [userInstanceList: User.findById(params.id)] }
    		json{	render list as JSON	}
    		//json{ render jsonOut }
    		//xml{ render list as XML	} 
    		xml{ render xmlOutShow }
    	} 
    }
    
    /**
    * Create the XML format to display the users
    */
    def xmlOut = {
      def list = User.list()
      def writer = new StringWriter()
      def builder = new MarkupBuilder(writer)
     
      builder.all() {
        users {
         list.each { item ->
            user(id: item.id) {
               firstName(item.firstName)
               lastName(item.lastName)
               userName(item.userName)
            }
          }
         }
      }
      [ usersXml: writer.toString() ]
    }
    
    
    /**
    * Create the XML format to display the users.
    */
    def xmlOutShow = {
      def list = User.findById(params.id)
      def writer = new StringWriter()
      def builder = new MarkupBuilder(writer)
     
      builder.all() {
        users {
         list.each { item ->
            user(id: item.id) {
               firstName(item.firstName)
               lastName(item.lastName)
               userName(item.userName)
            }
          }
         }
      }
      [ usersXml: writer.toString() ]
    }
    
    /**
    * Create the JSON format to display to the users
    * without the password.
    */
    def jsonOut = {
      // TODO: display the JSON without showing the password.
    }
    
    
    /**
    *	Handle the login for a user.
    */
    def handleLogin = {    	
    	//def user = User.findByUserName(params.userName) 
    	//def password = user.password
    	def user = User.findByUserNameAndPassword(params.userName, params.password)
    	
    	if(!user) {
    		flash.message = "User not found for userName: ${params.userName}"
    		redirect(action:'login')
    		return
    	}
    	session.user = user
    	//redirect(controller:'share')
    	redirect action:"list"
    	/*
    	if(password == params.password) {
    		session.user = user
    		//redirect(controller:'share')
    		redirect action:"list"
    	} else {
    		flash.message = "Incorrect password"
    		redirect action:'login'
    	}
    	*/
    }
    
    /**
    *	Handle the logout for a user.
    */
    def logout = {
    	if(session.user) {
    		session.user = null
    		//redirect(action:'login')
    		redirect(action:'')
    		return
    	}
    }
        
    /**
    *	Method to verify that a user is logged in.
    */
    private def loggedIn(id="id", Closure c) {
	    def user = User.get(params[id])
    	if(session.user) {
    		c.call user
    	} else {
    		flash.message = "You need to login to make changes."
    		redirect action:"list"
    	}
    }
    
    /**
    *  Method to verify the user only modifies their account
    *  or that the user is an admin.
    */    
    private def ownAccount (Closure c) {    	
    	loggedIn { user ->
    		def sessUser = User.findById(session.user.id)
    		def sameIds = (session.user.id.toString() == params.id) ? true : false;
    		def isAdmin = (sessUser.role.toString() == "admin") ? true : false;
    		
    		if(sameIds || isAdmin) {
    			c.call user
    		} else {
    		  flash.message = "You can only modify your own account."
    		  redirect action:"list"
    		  return
    		}
    	}    	
    }
    
    /*
    private def ownAccount (Closure c) {    	
    	loggedIn { user ->
    		def sessUser = User.findById(session.user.id)
    		if(session.user.id.toString() == params.id) {
    			c.call user
    		} else if(sessUser.usergroup.toString() == "admin") {
    			c.call user
    		} else {
    		  flash.message = "You can only modify your own account."
    		  redirect action:"list"
    		  return
    		}
    	}    	
    }
    */
        
    /**
    *  Method to verify the user exists.
    */
    private def withUser(id="id", Closure c) {
      def user = User.get(params[id])
	    if(user) {
	      c.call user
	    } else {
	    	flash.message = "The user was not found."
	  	  redirect action:"list"
		  }
    }
    
    /**
    *	 Make sure the logged in user is an admin.
    */
    private def isAdmin(Closure c) {    	
    	def user = User.findById(session.user.id)    	
    	if(user.role.toString() == "admin") {
    		c.call session.user
    	} else {
    		flash.message = "Only administrators can add new users."
    		redirect action:"list"
    	}    	
    }
    
    /**
    *	Block users from editing other accounts.
    */
    def edit = {
    	ownAccount { usr ->
    		withUser { user ->   
    			[ user : user ]
    	 	}
    	}
    }
    
    /**
    *	Block users from deleting other accounts.
    */
    def delete = {
    	ownAccount { usr ->
    		withUser { user ->   
    			[ user : user ]
    	 	}
    	}
    }
    
    /**
    *	Block users from updating other accounts.
    * This is an extra precaution really since they
    * won't be able to enter edit screen.
    */
    def update = {
    	ownAccount { usr ->
    		withUser { user ->   
    			[ user : user ]
    	 	}
    	}
    }
    
    
    def create = {
    
    	def all = User.list()
    	if(0 == all.size()) { return }
    	/*
    	isAdmin { sessUser ->
      			[ sessUser : sessUser ]
      		}
      */		
    
    	//flash.message = "create hit"
    	///*
    	loggedIn { usr ->
      	//withUser { user ->
      		isAdmin { sessUser ->
      			[ sessUser : sessUser ]
      		}
      	//}
      }
      //*/
    }
    
    /**POST Handling**/
       
    def addUser = {
    	//TODO: maybe put login "withFormat" in that
    	// will redirect up to create if this is being
    	// accessed from the web pages.  Don't want to 
    	// lose all of that.
    	
    	println "POST wants to add a new user"
    	println "session.user: ${session.user}"
    	
    	if(session.user.role.toString() == "admin") {
    	  def xmlUser = request.XML
    	
    		def newUser = new User(userName:xmlUser.userName.text(), password:xmlUser.password.text(), firstName:xmlUser.firstName.text(), lastName:xmlUser.lastName.text(), role:xmlUser.role.text())
    		if(newUser.save()){
        	response.status = 201 // Created
        	render newUser as XML
      	} else {
        	response.status = 500 //Internal Server Error
        	render "Could not create new User due to errors:\n ${newUser.errors}"
      	}
    	} else {
      	response.status = 401
        render "<error>You do not have permission to complete this request.</error>"
      }
    }
    
    def updateUser = {
    	println "PUT wants to update a user"

    	def xmlUser = request.XML
    	def user = User.findById(xmlUser.id.text())
    	
    	def isAdmin = (session.user.role.toString() == "admin") ? true : false;
    	def isUser = (session.user.id.toString() == xmlUser.id.text()) ? true : false;
    	
    	if(isAdmin || isUser) {
    		user.userName = xmlUser.userName.text()
    		user.password = xmlUser.password.text()
    		user.firstName = xmlUser.firstName.text()
    		user.lastName = xmlUser.lastName.text()
    		user.role = xmlUser.role.text()
    	
    		if(user.validate()) {
    			user.save()
    			response.status = 201
    			render "User updated successfully."
    		} else {
    			response.status = 500 
        	render "Could not update User due to errors:\n ${user.errors}"
    		}
    	} else {
    		response.status = 401
        render "<error>You do not have permission to complete this request.</error>"
    	}
    }
    
    def deleteUser = {
    	println "DELETE wants to delete a user"
    	
    	def user = User.findById(params.id)
    	println "params.id: ${params.id}"
    	
    	// Only allow admins to delete accounts.
    	def isAdmin = (session.user.role.toString() == "admin") ? true : false;
    	
    	if(isAdmin) {
    		if(!user) {
    			response.status = 500 
    			render "Unable to delete user.  User not found."
    		} else {
    			user.delete();
    			response.status = 200
    			render "User has been removed."
    		}
    	} else {
    		response.status = 401
        render "<error>You do not have permission to complete this request.</error>"
    	}
    	
    }
    
    
    
    
}
