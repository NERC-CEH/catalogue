<#import "skeleton.ftlh" as skeleton>
<@skeleton.master title="Publication" catalogue=catalogues.retrieve(catalogue)>
  <div id="publication" class="container">
    <h1 class="mt-5">Change the publication state of this record</h1>
    <div class="alert alert-primary text-uppercase fw-bold fs-4 state-${title}?lower_case">Current state: ${title}</div>
    <div>
      <#list transitions as transition>
        <form method="post" action="/${basePath}/${metadataId}/publication/${transition.id}">
          <p><button type="submit" class="btn btn-outline-secondary" title="${transition.helpText} title="${transition.cssClass}">${transition.title}</button></p>
        </form>
      </#list>
    </div>

    <div>
      <a href="/${basePath}/${metadataId}"><i class="fa-solid fa-arrow-left"></i>Return to the metadata record</a>
    </div>

  </div>
</@skeleton.master>
