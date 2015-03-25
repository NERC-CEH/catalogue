<#import "skeleton.html.tpl" as skeleton>

<@skeleton.master title="Publication state for: ${metadataTitle}"><#escape x as x?html>
  <div id="metadata" class="container">
    <h1>${metadataTitle}</h1>
    <h2>Publication actions available from: <span class="label label-primary">${title}</span></h2>
    <#list transitions as transition>
      <h3>${transition.title}</h3>
      <div class="row">
        <div class="col-sm-7 col-md-7">${transition.helpText}</div>
        <div class="col-sm-5 col-md-5">
          <form method="post" action="${transition.href}">
            <button type="submit" class="btn btn-default">OK</button>
          </form>
        </div>
      </div>
    </#list>
    <h3>Take no action</h3>
      <div class="row">
        <div class="col-sm-7 col-md-7">Take no action and return to the metadata record</div>
        <div class="col-sm-5 col-md-5">
          <a class="btn btn-default" href="${metadataHref}">OK</a>
        </div>
      </div>
  </div>
</#escape></@skeleton.master>