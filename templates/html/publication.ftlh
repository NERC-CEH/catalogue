<#import "skeleton.ftlh" as skeleton>
<@skeleton.master title="Publication" catalogue=catalogues.retrieve(catalogue)>
  <div id="publication" class="container">
    <h2>This record is currently <b>${title}</b></h2>
    <div>
    <p><b>Change the publication status of this record</b></p>

      <#list transitions as transition>
        <form method="post" action="/${basePath}/${metadataId}/publication/${transition.id}">
          <p><button type="submit" class="btn btn-light border" title="${transition.helpText}">${transition.title}</button></p>
        </form>
      </#list>

      <a href="/${basePath}/${metadataId}" class="btn btn-light border" title="Take no action and return to the metadata record">Cancel</a>
    </div>
  </div>
</@skeleton.master>
