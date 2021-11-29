<#import "../skeleton.ftlh" as skeleton>

<@skeleton.master "History">
    <div id="metadata">
        <div class="container" role="main">
            <h4>Edit history for Service Agreement: ${historyOf}</h4>
            <#list revisions>
                <ul>
                    <#items as revision>
                        <li><a href="${revision.href}">Version ${revision.version}</a></li>
                    </#items>
                </ul>
            </#list>
        </div>
    </div>
</@skeleton.master>
