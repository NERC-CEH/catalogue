<#import "skeleton.ftlh" as skeleton>

<@skeleton.master "History">
    <div id="metadata">
        <div class="container" role="main">
            <h4>Edit history for Service Agreement: ${historyOf}</h4>
            <#if revisions?? && revisions?has_content>
                <#list revisions as revision>
                    <form action="${revision.href}" method="get">
                        <input type="submit" value="Version: ${revision.id}">
                    </form>
                    <br>
                </#list>
            </#if>
        </div>
    </div>

</@skeleton.master>