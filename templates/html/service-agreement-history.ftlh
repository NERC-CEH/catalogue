<#import "skeleton.ftlh" as skeleton>

<@skeleton.master "History">
    <div id="metadata">
        <div class="container" role="main">
            <h4>Edit history for Service Agreement: ${historyOf}</h4>
            <#list revisions>
                <ul>
                <#items as revision>
                    <a href="${revision.href}"><li>Version ${revision.version}</li><a/>
                </#items>
                </ul>
            </#list>
        </div>
    </div>

</@skeleton.master>