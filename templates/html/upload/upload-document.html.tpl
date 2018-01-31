<#import "../skeleton.html.tpl" as skeleton>

<@skeleton.master title=title>
    <div id="documents-upload" class="container documents-upload">
        <#include "_title.html.tpl">
        <#if permission.userCanUpload(parentId)>
            <h1>upload</h1>
        <#elseif permission.userCanView(parentId)>
            <#include "_read-only.html.tpl">
        </#if>

<#--  <#list uploadFiles['documents'].documents?values as document>
    <p>${document.name}</p>
</#list>
          -->
        <#--  <#if (canUpload && isScheduled)>
            <#include "_scheduled.html.tpl">
        <#elseif (canUpload && isInProgress)>
            <#include "_in-progress.html.tpl">
        <#else>
            <#include "_read-only.html.tpl">
        </#if>  -->
    </div>
    <#include "_footer.html.tpl">
</@skeleton.master>
