<#import "blocks.html.tpl" as blocks>
<#import "ef/blocks.html.tpl" as ef_blocks>
<#import "skeleton.html.tpl" as skeleton>

<@skeleton.master title=title>
  <div id="metadata" class="container">

    <@blocks.title title type />
    <@blocks.description description />
    <#include "ef/_extent.html.tpl">

    <@ef_blocks.links "Parameters Measured" parametersMeasured/>
    <@ef_blocks.links "Keywords" keywords/>

    <#include "ef/_responsibleParties.html.tpl">
    <#include "ef/_identifiers.html.tpl">
    <@ef_blocks.links "Online Resources" onlineResources/>
    
  </div>
  <div id="footer">
    <#include "ef/_footer.html.tpl">
  </div>
</@skeleton.master>
