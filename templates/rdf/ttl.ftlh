<#ftl encoding="UTF-8">
<#compress>
<#-- Create keyword list -->
<#-- Single list at the moment but they may need to be separated in future-->
    <#assign keywordList = []>
    <#if descriptiveKeywords?has_content>
        <#list descriptiveKeywords as descriptiveKeyword>
            <#assign keywordList = keywordList + descriptiveKeyword.keywords >
        </#list>
    </#if>

    <#if keywords?has_content>
    <#list keywords as keyword>
        <#assign keywordList = keywordList + [keyword]>
    </#list>
    </#if>

    <#if allKeywords?has_content>
      <#list allKeywords as keyword>
          <#assign keywordList = keywordList + [keyword]>
      </#list>
    </#if>

    <#--DE-DUPLICATE KEYWORD LIST -->
    <#assign deDup = [] />
    <#list keywordList as originalList>
      <#if ! deDup?seq_contains(originalList)>
          <#assign deDup = deDup + [originalList] />
      </#if>
    </#list>
    <#assign keywordList = deDup />
<#-- END keyword list -->

<#include "_macros.ftlh">
<#include "_prefixes.ftlh">
@base <${uri?replace(id,"")?replace("id/","")}> .
@prefix : <${uri?replace(id,"")}> .

<#include "_body.ftlh">

</#compress>
