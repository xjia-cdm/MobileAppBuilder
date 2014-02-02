package share.space

class Share {
	private static final int TEN_MEG_IN_BYTES = 1024*1024*10
	byte[] payload
	String name
	String note
	String fileURL
	String filename
	Boolean saveInWebServiceDirectory = true
	Boolean saveInWebServiceDatabase
	Date createdDate
	//User author
	static belongsTo = [author:User]
	//Category category
	
	//static belongsTo = [User, Category]
	//static belongsTo = User
	
	static constraints = {
		payload( nullable: true, minSize: 1, maxSize: TEN_MEG_IN_BYTES )
  	name( blank:false, unique:true )
  	createdDate()
  	fileURL( blank:true, nullable:true )
  	filename( blank:true, nullable:true )
  	note( maxSize:1000, nullable:true )
  	saveInWebServiceDirectory( nullable:true )
		saveInWebServiceDatabase( nullable:true )
  	
  }
  
  String toString() {
  	name
  }
}


