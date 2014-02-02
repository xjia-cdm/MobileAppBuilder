
<%@ page import="share.space.Share" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'share.label', default: 'Share')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#show-share" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="show-share" class="content scaffold-show" role="main">
			<h1><g:message code="default.show.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<ol class="property-list share">
			
				<g:if test="${shareInstance?.name}">
				<li class="fieldcontain">
					<span id="name-label" class="property-label"><g:message code="share.name.label" default="Name" /></span>
					
						<span class="property-value" aria-labelledby="name-label"><g:fieldValue bean="${shareInstance}" field="name"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${shareInstance?.createdDate}">
				<li class="fieldcontain">
					<span id="createdDate-label" class="property-label"><g:message code="share.createdDate.label" default="Created Date" /></span>
					
						<span class="property-value" aria-labelledby="createdDate-label"><g:formatDate date="${shareInstance?.createdDate}" /></span>
					
				</li>
				</g:if>
			
				<g:if test="${shareInstance?.fileURL}">
				<li class="fieldcontain">
					<span id="fileURL-label" class="property-label"><g:message code="share.fileURL.label" default="File URL" /></span>
					
						<span class="property-value" aria-labelledby="fileURL-label"><g:fieldValue bean="${shareInstance}" field="fileURL"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${shareInstance?.filename}">
				<li class="fieldcontain">
					<span id="filename-label" class="property-label"><g:message code="share.filename.label" default="Filename" /></span>
					
						<span class="property-value" aria-labelledby="filename-label"><g:fieldValue bean="${shareInstance}" field="filename"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${shareInstance?.note}">
				<li class="fieldcontain">
					<span id="note-label" class="property-label"><g:message code="share.note.label" default="Note" /></span>
					
						<span class="property-value" aria-labelledby="note-label"><g:fieldValue bean="${shareInstance}" field="note"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${shareInstance?.saveInWebServiceDirectory}">
				<li class="fieldcontain">
					<span id="saveInWebServiceDirectory-label" class="property-label"><g:message code="share.saveInWebServiceDirectory.label" default="Save In Web Service Directory" /></span>
					
						<span class="property-value" aria-labelledby="saveInWebServiceDirectory-label"><g:formatBoolean boolean="${shareInstance?.saveInWebServiceDirectory}" /></span>
					
				</li>
				</g:if>
			
				<g:if test="${shareInstance?.saveInWebServiceDatabase}">
				<li class="fieldcontain">
					<span id="saveInWebServiceDatabase-label" class="property-label"><g:message code="share.saveInWebServiceDatabase.label" default="Save In Web Service Database" /></span>
					
						<span class="property-value" aria-labelledby="saveInWebServiceDatabase-label"><g:formatBoolean boolean="${shareInstance?.saveInWebServiceDatabase}" /></span>
					
				</li>
				</g:if>
			
				<g:if test="${shareInstance?.author}">
				<li class="fieldcontain">
					<span id="author-label" class="property-label"><g:message code="share.author.label" default="Author" /></span>
					
						<span class="property-value" aria-labelledby="author-label"><g:link controller="user" action="show" id="${shareInstance?.author?.id}">${shareInstance?.author?.encodeAsHTML()}</g:link></span>
					
				</li>
				</g:if>
			
			</ol>
			<g:form>
				<fieldset class="buttons">
					<g:hiddenField name="id" value="${shareInstance?.id}" />
					<g:link class="edit" action="edit" id="${shareInstance?.id}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
					<g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
				</fieldset>
			</g:form>
		</div>
	</body>
</html>
