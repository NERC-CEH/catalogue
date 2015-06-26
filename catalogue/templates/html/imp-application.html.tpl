<#import "blocks.html.tpl" as blocks>
<#import "skeleton.html.tpl" as skeleton>
<#import "../underscore.tpl" as _>

<@skeleton.master title=title>
  <div id="metadata" class="container">

    <@blocks.title title type />
    <@blocks.description description />
</@skeleton.master>
