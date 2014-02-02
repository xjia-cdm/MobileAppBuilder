package share.space

import grails.converters.*
import grails.util.*
import groovy.xml.MarkupBuilder
import groovy.json.*

class ShareController {

    //def index() { }
    def scaffold = Share
    
    def list = {
    	if(!params.max) params.max = 10
    	def list = Share.list(params)
    	request.withFormat{
    	  html{ [shareInstanceList: Share.list(params)] }
    		json{	render list as JSON	}  			
    	  xml{ render xmlOut }    	
    	}
    }
    
    /**
    * Create the XML format to display the users
    * without the password.
    */
    def xmlOut = {
      def list = Share.list()      
      def writer = new StringWriter()
      def builder = new MarkupBuilder(writer)
     
      builder.all() {
        files {
         list.each { item ->
            file(id: item.id) {
               shareName(item.name)
               filename(item.filename)
               fileURL(item.fileURL)
               saveInDirectory(item.saveInWebServiceDirectory)
               saveInDatabase(item.saveInWebServiceDatabase)
               createdDate(item.createdDate)
               author(item.author)
            }
          }
         }
      }
      [ sharesXml: writer.toString() ]
    }
     
    /**
    *	Save a new Share.     
    */
    def save = {   
    	def shareInstance
    	def saveDir = (params.saveInWebServiceDirectory == "true") ? true : false
			def saveDb = (params.saveInWebServiceDatabase == "true") ? true : false
			def hasFile = (params.payload != "") ? true : false
			def hasFileURL = (params.fileURL != "") ? true : false	
						
			// Leave if there is no file to upload or URL to save
			if(false == hasFile && false == hasFileURL) {
			  response.status = 500
			  render "<error>No file or fileURL submitted.  No action taken.</error>"
			  return
			}
			
    	// Create an instance that saves the file in the database or not. 
    	// Using binding, the file is automatically saved in db.  This is not
    	// wanted if the user doesn't want it, to put it plainly.
    	// The way around it is to place each property manually, thus
    	// avoiding the "payload" property from being set.
    	if(saveDb) { 
    		// Use binding.
    		shareInstance = new Share(params)
    	} 
    	if(saveDir) {	
    		// Set properties manually.
    		shareInstance = new Share()	
    		shareInstance.name = params.name
    		shareInstance.note = params.note
    		shareInstance.fileURL = params.fileURL
    		shareInstance.filename = params.filename
    		shareInstance.saveInWebServiceDirectory = params.saveInWebServiceDirectory
    		shareInstance.saveInWebServiceDatabase = params.saveInWebServiceDatabase
    		shareInstance.createdDate = new Date()	
    	}
    	//println "session.user.id: $session.user.id"
    	//if(null == shareInstance.author) { println "author is null" }
    	shareInstance.author = User.findById(session.user.id)			
			//if(!shareInstance.author) { println "no author" }
			
    	//handle uploaded file
    	def uploadedFile = request.getFile('payload')
    	if(!uploadedFile.empty){
      	if(params.saveInWebServiceDirectory) {
      		def webRootDir = servletContext.getRealPath("/")
      		def userDir = new File(webRootDir, "/userUploads/${session.user.userName}")
      		userDir.mkdirs()
      		uploadedFile.transferTo( new File( userDir, uploadedFile.originalFilename))
      		shareInstance.fileURL = "/userUploads/${session.user.userName}/" + uploadedFile.originalFilename
      	}
      	
      	shareInstance.filename = uploadedFile.originalFilename      	
    	}
			
			// Save share
    	if(!shareInstance.hasErrors() && shareInstance.save()) {
    		response.status = 200
        render "Entry ${shareInstance.id} created."
        redirect(action:show,id:shareInstance.id)
    	} else {
       	render "Could not create new Share due to errors:\n ${shareInstance.errors}"
    	}
		}
		    
    /**POST Handling**/   
    
    /** 
    * PUT doesn't really apply to Share.  There is no way
    * (at least with something like cURL, that I found) to send a new 
    * upload file and header paramters in a PUT request.
    * The solution I have come up with is to send updates as
    * a POST to /files/{id} which maps to this closure. 
    * POST is the only way I can see (as of now) to accomplish
    * a true PUT where all values for the Share can change.
    */
    def updateFile = {
    	def share = Share.findById(params.id)
    	if(!share) {
    	  response.status = 404
    	  render "<error>File not found.</error>"
    	}    
    	//if(share.saveInWebServiceDatabase) {
    	//	share = ShareDb.findById(params.id)
    	//}
    	
    	def saveDir = (params.saveInWebServiceDirectory == "true") ? true : false
			def saveDb = (params.saveInWebServiceDatabase == "true") ? true : false
			
			// Update/upload file.
			if(saveDb) {
				share.payload = params.payload.getBytes()
			}
			if(saveDir) {
				def uploadedFile = request.getFile('payload')
    		if(!uploadedFile.empty){
      		if(params.saveInWebServiceDirectory) {
      			def webRootDir = servletContext.getRealPath("/")
      			def userDir = new File(webRootDir, "/userUploads/${session.user.userName}")
      			userDir.mkdirs()
      			uploadedFile.transferTo( new File( userDir, uploadedFile.originalFilename))
      			share.fileURL = "/userUploads/${session.user.userName}/" + uploadedFile.originalFilename
      		}
      		share.filename = uploadedFile.originalFilename      	
    		}
			}			
			
    	// Update properties.
    	share.name = params.name
    	share.note = params.note
    	share.fileURL = params.fileURL
    	share.filename = params.filename
    	share.saveInWebServiceDirectory = params.saveInWebServiceDirectory
    	share.saveInWebServiceDatabase = params.saveInWebServiceDatabase
			
			// Save share
    	if(!share.hasErrors() && share.save()) {
    		response.status = 200
        render "Entry ${share.id} updated."
        redirect(action:show,id:share.id)
    	} else {
       	render "Could not create new Share due to errors:\n ${share.errors}"
    	}
    }
    
    /**
    * Delete a file from the database or directory.
    */
    def deleteFile = {
    	println "DELETE wants to delete a file"
    	println "params.id: ${params.id}"
    	
    	def share = Share.findById(params.id)
    	if(!share) {
    		response.status = 404
    		render "<error>File not found.</error>"
    		return
    	}
    	
    	// Only the author can delete their files.
    	if(share.author.userName != session.user.userName) {
    		response.status = 401
        render "<error>You do not have permission to complete this request.</error>"
        return
    	}
    	println "share.saveInWebServiceDirectory: ${share.saveInWebServiceDirectory}"
    	
    	// Delete the file.
    	if(share.validate()) {
    		// Delete from directory if exists.
    		if(share.saveInWebServiceDirectory) {
    			directoryDeleteFile(share)
    		}
 				// Delete shareInstance from database   	
    		share.delete()    		
    		response.status = 200
    		render "Shared file has been removed."
    	} else {
    	  response.status = 500
    		render "Could not delete file due to errors:\n ${share.errors}"
    	}
    }
    
    /**
    *  Delete the file from disk.
    */
    private def directoryDeleteFile(Share share) {
    	def webRootDir = servletContext.getRealPath("/")
    	def userDir = new File(webRootDir, "/userUploads/${session.user.userName}")
      new File( userDir, share.filename ).delete()
    }
    
    def downloadFile = {
    	println "downloadFile wants to GET a file"
    	//def file = new File(params.fileDir)    
			//response.setContentType("application/octet-stream")
			//response.setHeader("Content-disposition", "attachment;filename=${file.getName()}")

			//response.outputStream << file.newInputStream()
    }
}
