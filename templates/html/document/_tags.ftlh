<#macro keywordList list listTitle="Keywords" commaseparated=false>
  <dt>${listTitle}</dt>
  <dd>
    <#list list as item>
      <@displayLinkedKeyword item.value item.uri/>
      <#sep><#if commaseparated=true>,<#else><br></#if></#sep>
    </#list>
</#macro>

<#macro displayLinkedKeyword value="" uri="">
  <#if uri?has_content>
    <abbr title="${uri}">${value?trim}</abbr>
    <a href="${uri}" target="_blank" rel="noopener noreferrer" class="externalVocabularyLink"><sup class="fa-solid fa-xs fa-arrow-up-right-from-square"></sup></a>
  <#else>
    ${value?trim}
  </#if>
</#macro>

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

<#-- KEYWORDS -->
<#assign allKeywords= []>

<#if keywords_theme?has_content>
  <#list keywords_theme as keyword>
      <#assign allKeywords = allKeywords + [keyword]>
  </#list>
</#if>
<#if keywords_observedProperty?has_content>
  <#list keywords_observedProperty as keyword>
      <#assign allKeywords = allKeywords + [keyword]>
  </#list>
</#if>
<#if keywords_instrument?has_content>
  <#list keywords_instrument as keyword>
      <#assign allKeywords = allKeywords + [keyword]>
  </#list>
</#if>
<#if keywords_discipline?has_content>
  <#list keywords_discipline as keyword>
      <#assign allKeywords = allKeywords + [keyword]>
  </#list>
</#if>
<#if keywords_place?has_content>
  <#list keywords_place as keyword>
      <#assign allKeywords = allKeywords + [keyword]>
  </#list>
</#if>
<#if keywords_project?has_content>
  <#list keywords_project as keyword>
      <#assign allKeywords = allKeywords + [keyword]>
  </#list>
</#if>
<#if keywords_other?has_content>
  <#list keywords_other as keyword>
      <#assign allKeywords = allKeywords + [keyword]>
  </#list>
</#if>

<#--This can be deleted when descriptiveKeywords are removed -->
<#if descriptiveKeywords?has_content>
  <#list descriptiveKeywords as descriptiveKeyword>
    <#list descriptiveKeyword.keywords as keyword>
      <#assign allKeywords = allKeywords + [keyword]>
    </#list>
  </#list>
</#if>

<#if allKeywords?has_content>
  <@keywordList allKeywords "Keywords" true/>
</#if>