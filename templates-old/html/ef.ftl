<#import "blocks.ftl" as blocks>
<#import "ef/blocks.ftl" as ef_blocks>
<#import "skeleton.ftl" as skeleton>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
  <div id="metadata" class="container">

    <@blocks.title title type />
    <@blocks.description description />
    <#include "ef/_extent.ftl">

    <@ef_blocks.links "Parameters Measured" parametersMeasured/>
    <@ef_blocks.links "Keywords" keywords/>

    <#include "ef/_responsibleParties.ftl">
    <#include "ef/_identifiers.ftl">
    <@ef_blocks.links "Online Resources" onlineResources/>

  </div>
  <div id="footer">
    <#include "ef/_footer.ftl">
  </div>
</@skeleton.master>
