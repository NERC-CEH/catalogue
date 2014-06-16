<#import "skeleton.html.tpl" as skeleton>
<#import "keywords.html.tpl" as keyword>
<#import "title.html.tpl" as t>
<#import "description.html.tpl" as d>
<#import "topiccategories.html.tpl" as tc>

<@skeleton.master title=title>

<!-- InstanceBeginEditable name="MAIN" -->
<div>
  <@t.title_tmpl title=title/>
</div>

<div>
  <@d.description_tmpl description=description/>
</div>

<div>
  <@tc.category/>
</div>

<div>
  <@keyword.keywords/>
</div>

</@skeleton.master>