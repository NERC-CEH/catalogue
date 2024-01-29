<#ftl encoding="UTF-8">
<#compress>
<#-- Create keyword list -->
<#-- Single list at the moment but they may need to be separated in future-->
    <#assign allKeywords = []>
    <#if descriptiveKeywords?has_content>
        <#list descriptiveKeywords as descriptiveKeyword>
            <#assign allKeywords = allKeywords + descriptiveKeyword.keywords >
        </#list>
    </#if>

    <#if keywords?has_content>
    <#list keywords as keyword>
        <#assign allKeywords = allKeywords + [keyword]>
    </#list>
    </#if>

    <#if keywordsTheme?has_content>
    <#list keywordsTheme as keyword>
        <#assign allKeywords = allKeywords + [keyword]>
    </#list>
    </#if>
    <#if keywordsObservedProperty?has_content>
    <#list keywordsObservedProperty as keyword>
        <#assign allKeywords = allKeywords + [keyword]>
    </#list>
    </#if>
    <#if keywordsInstrument?has_content>
    <#list keywordsInstrument as keyword>
        <#assign allKeywords = allKeywords + [keyword]>
    </#list>
    </#if>
    <#if keywordsDiscipline?has_content>
    <#list keywordsDiscipline as keyword>
        <#assign allKeywords = allKeywords + [keyword]>
    </#list>
    </#if>
    <#if keywordsPlace?has_content>
    <#list keywordsPlace as keyword>
        <#assign allKeywords = allKeywords + [keyword]>
    </#list>
    </#if>
    <#if keywordsProject?has_content>
    <#list keywordsProject as keyword>
        <#assign allKeywords = allKeywords + [keyword]>
    </#list>
    </#if>
    <#if keywordsOther?has_content>
    <#list keywordsOther as keyword>
        <#assign allKeywords = allKeywords + [keyword]>
    </#list>
    </#if>

    <#--DE-DUPLICATE KEYWORD LIST -->
    <#assign deDup = [] />
    <#list allKeywords as originalList>
      <#if ! deDup?seq_contains(originalList)>
          <#assign deDup = deDup + [originalList] />
      </#if>
    </#list>
    <#assign allKeywords = deDup />
<#-- END keyword list -->

<#include "_macros.ftlh">
<#include "_prefixes.ftlh">
@base <${uri?replace(id,"")?replace("id/","")}> .
@prefix : <${uri?replace(id,"")}> .

<#include "_body.ftlh">

</#compress>
