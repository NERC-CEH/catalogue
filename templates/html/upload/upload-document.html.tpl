<#import "../skeleton.html.tpl" as skeleton>

<#assign issues=jira.search("project=eidchelp and component='data transfer' and labels=" + parentId)>

<@skeleton.master title=title>
    <div id="documents-upload" class="container documents-upload" data-guid='${id}'>
        
    <section class="section">
        <#assign headText = "data">
        <#if issues[0].status == 'scheduled'>
            <#assign headText = "upload " + headText>
        <#elseif issues[0].status == 'in progress'>
            <#if permission.userCanUpload(parentId)>
                <#assign headText = "manage metadata/" + headText>
            </#if>
        <#else>
        </#if>

        <h1>
        ${headText?cap_first} for record <small><a href="/documents/${parentId}">${parentId}</a></small>
        </h1> 
    </section>

        <#if permission.userCanUpload(parentId)>
            <#if issues?size != 1 || issues[0].status == 'open' || issues[0].status == 'approved' || issues[0].status == 'on hold'>
                <#include "_read-only.html.tpl">
            <#elseif issues[0].status == 'scheduled'>
                <#include "_scheduled.html.tpl">
            <#else>
                <#include "_in-progress.html.tpl">
            </#if>
        <#elseif permission.userCanView(parentId)>
            <#include "_read-only.html.tpl">
        </#if>
    </div>
    <#include "_footer.html.tpl">
</@skeleton.master>
