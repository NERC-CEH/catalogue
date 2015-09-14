<#import "blocks.html.tpl" as blocks>
<#import "skeleton.html.tpl" as skeleton>

<@skeleton.master title=title>
  <div id="metadata" class="container">

    <@blocks.title title type />
    <@blocks.description description />
    <div id="studyarea-map">
      <span content="${geometry?html}" datatype="geo:wktLiteral"/>
    </div>
  </div>
</@skeleton.master>
