
<%@ page import="share.space.Share" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'share.label', default: 'Share')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#list-share" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="list-share" class="content scaffold-list" role="main">
			<h1><g:message code="default.list.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<table>
				<thead>
					<tr>
					
						<g:sortableColumn property="name" title="${message(code: 'share.name.label', default: 'Name')}" />
					
						<g:sortableColumn property="createdDate" title="${message(code: 'share.createdDate.label', default: 'Created Date')}" />
					
						<g:sortableColumn property="fileURL" title="${message(code: 'share.fileURL.label', default: 'File URL')}" />
					
						<g:sortableColumn property="filename" title="${message(code: 'share.filename.label', default: 'Filename')}" />
					
						<g:sortableColumn property="note" title="${message(code: 'share.note.label', default: 'Note')}" />
					
						<g:sortableColumn property="saveInWebServiceDirectory" title="${message(code: 'share.saveInWebServiceDirectory.label', default: 'Save In Web Service Directory')}" />
					
					</tr>
				</thead>
				<tbody>
				<g:each in="${shareInstanceList}" status="i" var="shareInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
					
						<td><g:link action="show" id="${shareInstance.id}">${fieldValue(bean: shareInstance, field: "name")}</g:link></td>
					
						<td><g:formatDate date="${shareInstance.createdDate}" /></td>
					
						<td>${fieldValue(bean: shareInstance, field: "fileURL")}</td>
					
						<td>${fieldValue(bean: shareInstance, field: "filename")}</td>
					
						<td>${fieldValue(bean: shareInstance, field: "note")}</td>
					
						<td><g:formatBoolean boolean="${shareInstance.saveInWebServiceDirectory}" /></td>
					
					</tr>
				</g:each>
				</tbody>
			</table>
			<div class="pagination">
				<g:paginate total="${Share.count()}" />
			</div>
		</div>
	</body>
</html>
