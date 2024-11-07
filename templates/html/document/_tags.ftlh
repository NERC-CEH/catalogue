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

<#if observedProperty?has_content>
  <@displayObservedProperties observedProperty />
</#if>

<#macro displayObservedProperties op>
  <dt>Observed properties</dt>
  <dd>
  <#list op?sort_by("value") as item>
    <#local opLabel="unknown">
    <#if item.title?has_content>
      <#local opLabel=item.title?trim>
    <#elseif item.value?has_content>
      <#local opLabel=item.value?trim>
    </#if>

    <#if item.uri?has_content>
      <span><abbr title="${item.uri?trim}">${opLabel}</abbr></span>
      <a href="${item.uri?trim}" target="_blank" rel="noopener noreferrer" class="externalVocabularyLink"><sup class="fa-solid fa-xs fa-arrow-up-right-from-square"></sup></a>
    <#else>
      <span>${opLabel}</span>
    </#if>
    <#sep><br></#sep>
  </#list>
  </dd>
</#macro>

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
    <span><abbr title="${uri?trim}">${value?trim}</abbr></span>
    <a href="${uri?trim}" target="_blank" rel="noopener noreferrer" class="externalVocabularyLink"><sup class="fa-solid fa-xs fa-arrow-up-right-from-square"></sup></a>
  <#else>
    <span>${value?trim}</span>
  </#if>
</#macro>
