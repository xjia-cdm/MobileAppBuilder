<!doctype html>
<!--[if lt IE 7 ]> <html lang="en" class="no-js ie6"> <![endif]-->
<!--[if IE 7 ]>    <html lang="en" class="no-js ie7"> <![endif]-->
<!--[if IE 8 ]>    <html lang="en" class="no-js ie8"> <![endif]-->
<!--[if IE 9 ]>    <html lang="en" class="no-js ie9"> <![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--> <html lang="en" class="no-js"><!--<![endif]-->
	<head>
  	<title><g:layoutTitle default="CSC699" /></title>
    <link rel="stylesheet" href="${createLinkTo(dir: 'css', file: 'main.css')}" type="text/css">
		<link rel="stylesheet" href="${createLinkTo(dir: 'css', file: 'mobile.css')}" type="text/css">
    <link rel="shortcut icon" href="${createLinkTo(dir:'images',
          file:'favicon.ico')}" type="image/x-icon" />
    <g:layoutHead />
    
  </head>
  <body>
  	<div id="spinner" class="spinner" style="display:none;">
    	<img src="${createLinkTo(dir:'images',file:'spinner.gif')}"
           alt="Spinner" />
		</div>
    <div id="topbar">
    	<g:render template="/common/topbar" />
		</div>    
    <g:layoutBody />
    <div class="footer">
      <g:render template="/common/footer" />
		</div>
		
  </body>
</html>