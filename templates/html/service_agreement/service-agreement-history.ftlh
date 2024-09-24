<#import "../skeleton.ftlh" as skeleton>

<@skeleton.master title="Service Agreement History: " + historyOf catalogue=catalogues.defaultCatalogue()>
    <div id="metadata" class="metadata">
        <div class="container" role="main">
            <h4>Edit history for Service Agreement: ${historyOf}</h4>
            <ul>
                <li><a href="/service-agreement/${historyOf}">Current Version</a></li>
            <#list revisions as revision>
                <li><a href="${revision.href}">Version ${revision.version}</a></li>
            </#list>
            </ul>
        </div>
    </div>

</@skeleton.master>
