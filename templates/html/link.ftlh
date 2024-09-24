<#import "blocks.ftlh" as blocks>
<#import "skeleton.ftlh" as skeleton>

<@skeleton.master title="Link Document" catalogue=catalogues.retrieve(metadata.catalogue)>
  <div id="metadata" class="metadata">
   <div class="container">
    <div id="section-Top">
      <@blocks.title title="Link Document" type="" />
    </div>
      <div class="row">
        <div class="col-sm-4 col-sm-push-8 col-xs-12">
          <#include "link/_admin.ftlh">
        </div>
        <div class="col-sm-8 col-sm-pull-4 col-xs-12">
          <div class="alert alert-info" role="alert">This metadata record is not available</div>
        </div>
      </div>
    </div>
  </div>
</@skeleton.master>
