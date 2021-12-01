<#import "../skeleton.ftlh" as skeleton>

<@skeleton.master "History">
    <div id="metadata">
        <div class="container" role="main">
            <h4>Edit history for Service Agreement: ${historyOf}</h4>
            <ul>
                <a href="/service-agreement/${historyOf}"><li>Current Version</li><a/>
            <#list revisions>
                <#items as revision>
                    <a href="${revision.href}"><li>Version ${revision.version}</li><a/>
                </#items>
            </#list>
            </ul>
        </div>
    </div>

</@skeleton.master>
