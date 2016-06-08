<#import "skeleton.html.tpl" as skeleton>

<@skeleton.master title="Publication state for ${metadataTitle}"><#escape x as x?html>
  <div id="metadata" class="container">
  <div id="publicationActions">
    <h1>${metadataTitle}</h1>
	<h2><span class="label label-${title?replace(" ", "")}">This record is currently ${title}</span></h2>
   	<div class="list-group">
	<div class="list-group-item list-group-item-info">
      <h4 class="active list-group-item-heading">Actions available</h4>
	</div>
    <#list transitions as transition>
      <div class="list-group-item">
          <form method="post" action="${transition.href}">
            <p><button type="submit" class="btn btn-default">${transition.title}</button></p>
          </form>
		  <p class="list-group-item-text">${transition.helpText}</p>
      </div>
    </#list>
      <div class="list-group-item">
        <p><a class="btn btn-default" href="${metadataHref}">Cancel</a></p>
        <p class="list-group-item-text">Take no action and return to the metadata record</p>
      </div>
	</div> 
  </div>
  </div>
</#escape></@skeleton.master>


<#import "skeleton.html.tpl" as skeleton>