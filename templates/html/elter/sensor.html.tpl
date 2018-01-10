<#import "../skeleton.html.tpl" as skeleton>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
    <section id='metadata'>
        <div class='container'>
            <h1>${title} <small>${shortName}</small></h1>
            <p class="lead">${description}</p>
            <div class="panel panel-default">
                <div class="panel-body">
                    <dl class="dl-horizontal">
                        <dt>Serial Number</dt>
                        <dd>${serialNumber}</dd>
                        <dt>Documentation</dt>
                        <dd><a href='${documentation}'>${documentation}</a></dd>
                        <dt>Process Type</dt>
                        <dd>${processType}</dd>
                        <dt>Manufacturer</dt>
                        <dd><a href='${manufacturer}'>${manufacturer}</a></dd>
                        <dt>Default Parameters</dt>
                        <dd>
                            <ul>
                            <#list defaultParameters as param>
                                <li>${param['value']}</li>
                            </#list>
                            </ul>
                        </dd>
                    </dl>
                </div>
            </div>
        </div>
        <#include "_admin.html.tpl">
    </section>
</@skeleton.master>