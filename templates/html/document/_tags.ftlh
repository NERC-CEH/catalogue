<#macro keywordList list listTitle="Keywords" commaseparated=false>
  <dt>${listTitle}</dt>
  <dd>
    <#list list as item>
      <@displayLinkedKeyword item.value item.uri/>
      <#sep><#if commaseparated=true>,<#else><br></#if></#sep>
    </#list>
  </dd>
</#macro>

<#macro displayLinkedKeyword value="" uri="">
  <#if uri?has_content>
    <abbr title="${uri}">${value?trim}</abbr>
    <a href="${uri}" target="_blank" rel="noopener noreferrer" class="externalVocabularyLink"><sup class="fa-solid fa-xs fa-arrow-up-right-from-square"></sup></a>
  <#else>
    ${value?trim}
  </#if>
</#macro>

<#if observedProperties?has_content>
  <@keywordList observedProperties "Observed properties" true/>
</#if>

<#if topicCategories?has_content>
  <@keywordList topicCategories "Topic categories"/>
</#if>

<#if inspireThemes??>
  <dt>INSPIRE theme<#if inspireThemes?size gt 1>s</#if></dt>
  <dd >
  <#list inspireThemes?sort_by("theme") as inspireTheme>
    <@displayLinkedKeyword inspireTheme.theme inspireTheme.uri/>
    <#sep><br></#sep>
  </#list>
</dd>
</#if>

<#assign allKeywords= []>
<#if descriptiveKeywords?has_content>
  <#list descriptiveKeywords as descriptiveKeyword>
    <#list descriptiveKeyword.keywords as keyword>
      <#assign allKeywords = allKeywords + [keyword]>
    </#list>
  </#list>
</#if>
<#if keywords?has_content>
  <#list keywords as keyword>
      <#assign allKeywords = allKeywords + [keyword]>
  </#list>
</#if>

<#if allKeywords?has_content>
  <@keywordList allKeywords "Other keywords" true/>
</#if>

