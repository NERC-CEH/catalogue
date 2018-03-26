<#import "../skeleton.ftl" as skeleton>
<#assign issues=jira.search("project=eidchelp and component='data transfer' and labels=" + parentId)>

<@skeleton.master title=title>
    <div id="documents-upload" class="container documents-upload" data-guid='${id}'>
    
    <div class="header">
        <#assign headText = "data">
        <#if permission.userCanUpload(parentId)>
            <#if issues[0].status == 'scheduled'>
                <#assign headText = "upload " + headText>
            <#elseif issues[0].status == 'in progress'>
                <#assign headText = "manage metadata/" + headText>
            </#if>
        <#else>
        </#if>
        
        <div>
            <a class="btn btn-default btn-sm" href="/documents/${parentId}">&laquo; Return to metadata</a></small>
        </div>
        <div>
            <h1>
            ${headText?cap_first} for record <small><a href="/documents/${parentId}">${parentId}</a></small>
            </h1>
        </div>
    </div>

    <#if issues?size != 1 >
        <#if permission.userInGroup("ROLE_CIG_SYSTEM_ADMIN")>
            <div class="alert alert-danger"><b>ERROR</b><br>There is no Jira issue for this deposit</div>
        </#if>
    <#else>
        <#if issues[0].status == 'scheduled'>
            <#if permission.userCanUpload(parentId)>
                 <!--CAN UPLOAD -->
                <#include "_scheduled.ftl">
            <#elseif permission.userInGroup("ROLE_CIG_SYSTEM_ADMIN")>
                <#include "_read-only.ftl">
            <#else>
            </#if>
        <#elseif permission.userInGroup("ROLE_CIG_SYSTEM_ADMIN") && permission.userCanUpload(parentId)>
            <!--MANAGE FILES -->
            <#include "_in-progress.ftl">
        <#else>
            <#include "_read-only.ftl">
        </#if>
    </#if>

    </div>
</@skeleton.master>