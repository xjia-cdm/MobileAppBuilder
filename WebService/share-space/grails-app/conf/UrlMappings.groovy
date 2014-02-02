class UrlMappings {

	static mappings = {
		///*
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}
		//*/
		/*
		"403"(controller: "errors", action: "forbidden")
    "404"(controller: "errors", action: "notFound")
    "500"(controller: "errors", action: "serverError")
		*/
		
		"/users"(controller: "user", parseRequest: true) {			
			action = [GET:"list", PUT:"updateUser", DELETE:"deleteUser", POST:"addUser"]
			//action = [GET:"list"]
		}
		"/xmlList"(controller: "user", action: "xmlList")
		///*
		"/users/$id"{
			controller = "user"
			//this should "SHOW" a list of the users files as links
			action = [GET:"show", DELETE:"deleteUser"]			
		}
		//*/
		"/files"{
			controller = "share"
			action = [GET:"list", POST:"save"]
		}
		"/files/$id"{
			controller = "share"
			action = [GET:"downloadFile", POST:"updateFile", DELETE:"deleteFile"]
		}
		"/login"{
			controller = "user"
			action = [GET:"login"]
		}	
		
		"/"(view:"/index")
		"500"(view:'/error')
	}
}
