<#import "../skeleton.html.tpl" as skeleton>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
    <section id='metadata'>
        <div class='container'>
            <h1>${title} <small>${shortName!"<b class='text-muted'>No Short Name</b>"}</small></h1>
            <p class="lead">${description!"<b class='text-muted'>No Description</b>"}</p>
            <div class="panel panel-default">
                <div class="panel-body">
                    <dl class="dl-horizontal">
                        <dt>Serial Number</dt>
                        <dd>${serialNumber!"<b class='text-muted'>No Serial Number</b>"}</dd>
                        <dt>Documentation</dt>
                        <dd><a href='${documentation!"#"}'>${documentation!"<b class='text-muted'>No Documentation</b>"}</a></dd>
                        <dt>Process Type</dt>
                        <dd>${processType!"<b class='text-muted'>No Process Type</b>"}</dd>
                        <dt>Manufacturer</dt>
                        <#if manufacturerName??>
                            <dd><a href='/documents/${manufacturer!"#"}'>${manufacturerName}</a></dd>
                        <#elseif manufacturer??>
                            <dd><a href='/documents/${manufacturer!"#"}'>${manufacturer!""}</a></dd>
                        <#else>
                            <b class='text-muted'>No Manufacturer</b>
                        </#if>
                        
                        <dt>Default Parameters</dt>
                        <dd>
                        <#if defaultParameters??>
                            <ul class="list-unstyled">
                            <#list defaultParameters as param>
                                <li>${param['value']}</li>
                            </#list>
                            </ul>
                        <#else>
                            <b class='text-muted'>No Default Parameters</b>
                        </#if>
                        </dd>
                    </dl>
                </div>
            </div>
        </div>
        <#include "_admin.html.tpl">
    </section>
</@skeleton.master>