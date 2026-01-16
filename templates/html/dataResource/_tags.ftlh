<#if topicCategories?has_content>
  <@keywordList topicCategories "Topic categories"/>
</#if>

<#if inspireThemes??>
<div class="row key-value">
  <div class="col-sm-3 key">INSPIRE theme<#if inspireThemes?size gt 1>s</#if></div>
  <div class="col-sm-9 value">
    <#list inspireThemes?sort_by("theme") as inspireTheme>
      <@displayLinkedKeyword inspireTheme.theme inspireTheme.uri?trim/>
      <#sep><br></#sep>
    </#list>
  </div>
</div>
</#if>

<#if allKeywords?has_content>
  <@keywordList allKeywords "Keywords" true/>
</#if>

<#if fileset?? && fileset?has_content>
  <@displayObservedProperties fileset />
</#if>

<#macro displayObservedProperties filesetList>

  <#--Combine the observed Properties lists and de-duplicate -->
  <#assign deDuplicatedList = [] />
  <#list filesetList as fileset>
    <#if fileset.observedProperty?has_content>
      <#list fileset.observedProperty as op>
        <#if ! deDuplicatedList?seq_contains(op)>
          <#assign deDuplicatedList = deDuplicatedList + [ op ] />
        </#if>
      </#list>
    </#if>
  </#list>

  <div class="row key-value">
    <div class="col-sm-3 key">Observed properties</div>
    <div class="col-sm-9 value">
    <#list deDuplicatedList?sort_by("title") as op>
      <#local opLabel="unknown">
      <#if op.title?has_content>
        <#local opLabel=op.title?trim>
      <#elseif op.value?has_content>
        <#local opLabel=op.value?trim>
      </#if>

      <#if op.uri?has_content>
        <span><abbr title="${op.uri?trim}">${opLabel}</abbr></span>
        <a href="${op.uri?trim}" target="_blank" rel="noopener noreferrer" class="externalVocabularyLink" aria-label="link to external vocabulary ${op.uri?trim}" title="link to external vocabulary"><sup class="fa-solid fa-xs fa-arrow-up-right-from-square"></sup></a>
      <#else>
        <span>${opLabel}</span>
      </#if>
      <#sep><br></#sep>
    </#list>
    </div>
  </div>
</#macro>

<#macro keywordList list listTitle="Keywords" commaseparated=false>
  <div class="row key-value">
    <div class="col-sm-3 key">${listTitle}</div>
    <div class="col-sm-9 value">
      <#list list?sort_by("value") as item>
        <@displayLinkedKeyword item.value item.uri/>
        <#sep><#if commaseparated=true>,<#else><br></#if></#sep>
      </#list>
    </div>
  </div>
</#macro>

<#macro displayLinkedKeyword value="" uri="">
  <#if uri?has_content>
    <span><abbr title="${uri?trim}">${value?trim}</abbr></span>
    <a href="${uri?trim}" target="_blank" rel="noopener noreferrer"  aria-label="link to external vocabulary ${uri?trim}" class="externalVocabularyLink" title="link to external vocabulary"><sup class="fa-solid fa-xs fa-arrow-up-right-from-square"></sup></a>
  <#else>
    <span>${value?trim}</span>
  </#if>
</#macro>
