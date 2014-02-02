<%@ page import="share.space.Share" %>



<div class="fieldcontain ${hasErrors(bean: shareInstance, field: 'name', 'error')} required">
	<label for="name">
		<g:message code="share.name.label" default="Name" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="name" required="" value="${shareInstance?.name}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: shareInstance, field: 'createdDate', 'error')} required">
	<label for="createdDate">
		<g:message code="share.createdDate.label" default="Created Date" />
		<span class="required-indicator">*</span>
	</label>
	<g:datePicker name="createdDate" precision="day"  value="${shareInstance?.createdDate}"  />
</div>

<div class="fieldcontain ${hasErrors(bean: shareInstance, field: 'fileURL', 'error')} ">
	<label for="fileURL">
		<g:message code="share.fileURL.label" default="File URL" />
		
	</label>
	<g:textField name="fileURL" value="${shareInstance?.fileURL}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: shareInstance, field: 'filename', 'error')} ">
	<label for="filename">
		<g:message code="share.filename.label" default="Filename" />
		
	</label>
	<g:textField name="filename" value="${shareInstance?.filename}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: shareInstance, field: 'note', 'error')} ">
	<label for="note">
		<g:message code="share.note.label" default="Note" />
		
	</label>
	<g:textArea name="note" cols="40" rows="5" maxlength="1000" value="${shareInstance?.note}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: shareInstance, field: 'saveInWebServiceDirectory', 'error')} ">
	<label for="saveInWebServiceDirectory">
		<g:message code="share.saveInWebServiceDirectory.label" default="Save In Web Service Directory" />
		
	</label>
	<g:checkBox name="saveInWebServiceDirectory" value="${shareInstance?.saveInWebServiceDirectory}" />
</div>

<div class="fieldcontain ${hasErrors(bean: shareInstance, field: 'saveInWebServiceDatabase', 'error')} ">
	<label for="saveInWebServiceDatabase">
		<g:message code="share.saveInWebServiceDatabase.label" default="Save In Web Service Database" />
		
	</label>
	<g:checkBox name="saveInWebServiceDatabase" value="${shareInstance?.saveInWebServiceDatabase}" />
</div>

<div class="fieldcontain ${hasErrors(bean: shareInstance, field: 'author', 'error')} required">
	<label for="author">
		<g:message code="share.author.label" default="Author" />
		<span class="required-indicator">*</span>
	</label>
	<g:select id="author" name="author.id" from="${share.space.User.list()}" optionKey="id" required="" value="${shareInstance?.author?.id}" class="many-to-one"/>
</div>

