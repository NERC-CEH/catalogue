<#macro keywordList list listTitle="Keywords" commaseparated=false>
  <dt>${listTitle}</dt>
  <dd>
    <#list list?sort_by("value") as item>
      <@displayLinkedKeyword item.value item.uri/>
      <#sep><#if commaseparated=true>,<#else><br></#if></#sep>
    </#list>
</#macro>

<#macro displayLinkedKeyword value="" uri="">
  <#if uri?has_content>
    <span class="bob"><abbr title="${uri?trim}">${value?trim}</abbr></span>
    <a href="${uri?trim}" target="_blank" rel="noopener noreferrer" class="externalVocabularyLink"><sup class="fa-solid fa-xs fa-arrow-up-right-from-square"></sup></a>
  <#else>
    <span class="bob">${value?trim}</span>
  </#if>
</#macro>

<#if topicCategories?has_content>
  <@keywordList topicCategories "Topic categories"/>
</#if>

<#if inspireThemes??>
  <dt>INSPIRE theme<#if inspireThemes?size gt 1>s</#if></dt>
  <dd >
  <#list inspireThemes?sort_by("theme") as inspireTheme>
    <@displayLinkedKeyword inspireTheme.theme inspireTheme.uri?trim/>
    <#sep><br></#sep>
  </#list>
</dd>
</#if>

<#if allKeywords?has_content>
  <@keywordList allKeywords "Keywords" true/>
</#if>
