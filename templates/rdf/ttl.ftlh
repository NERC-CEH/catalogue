<#ftl encoding="UTF-8">
<#compress>
<#-- Create keyword list  -->
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