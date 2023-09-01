<#import "blocks.ftlh" as blocks>
<#import "ef/blocks.ftlh" as ef_blocks>
<#import "skeleton.ftlh" as skeleton>

<#assign footerContent>
  <#include "ef/_footer.ftlh">
</#assign>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue) footer=footerContent>
  <div id="metadata" class="container">

    <@blocks.title title type />
    <@blocks.description description />
    <#include "ef/_extent.ftlh">

    <@ef_blocks.links "Parameters Measured" parametersMeasured/>
    <@ef_blocks.links "Keywords" keywords/>

    <#include "ef/_responsibleParties.ftlh">
    <#include "ef/_identifiers.ftlh">
    <@ef_blocks.links "Online Resources" onlineResources/>

  </div>
</@skeleton.master>
