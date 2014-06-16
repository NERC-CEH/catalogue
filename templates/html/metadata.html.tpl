<#import "skeleton.html.tpl" as skeleton>
<#import "keywords.html.tpl" as keyword>
<#import "title.html.tpl" as t>
<#import "description.html.tpl" as d>
<@skeleton.master title=title>

<!-- InstanceBeginEditable name="MAIN" -->
<div>
  <@t.title/>
</div>

<div>
  <@d.description/>
</div>

<div>
  <#if topicCategories?has_content>
    <p>Topic category:</p>
    <#list topicCategories as topics>
      ${topics}
    </#list>
  </#if>
</div>

<div>
  <@keyword.keywords/>
</div>


<div>
  <#if alternateTitles?has_content>
    <p>Alternate title:</p>
    <#list alternateTitles as atitles>
      ${atitles}
    </#list>
  </#if>
</div>



</@skeleton.master>