<#import "skeleton.ftlh" as skeleton>

<@skeleton.master "History">
    <div id="metadata">
        <div class="container" role="main">
            <h2>Service Agreement History</h2>
            <#if revisions?? && revisions?has_content>
                <#list revisions as revision>
                    <form action="${revision.href}" method="get">
                        <input type="submit" value="${revision.title}">
                    </form>
                </#list>
            </#if>
        </div>
    </div>

</@skeleton.master>