<#import "blocks.ftlh" as blocks>
<#import "skeleton.ftlh" as skeleton>

<#assign linkScripts>
    <script src="/static/scripts/editor-bundle.js"></script>
    <script data-main="/static/scripts/main-out" src="/static/vendor/requirejs/require.js"></script>
</#assign>

<@skeleton.master title="Link Document" catalogue=catalogues.retrieve(metadata.catalogue) scripts=linkScripts>
  <div id="metadata">
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
