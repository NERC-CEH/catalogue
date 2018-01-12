<#import "../skeleton.html.tpl" as skeleton>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
    <section id='metadata'>
        <div class='container'>
        <form id="form">
            <h1>
                <div id="title" class="sensor-title-block sensor-title form-editable" data-field="title" data-value="${title}" data-type="text" data-required>
                    <i class="fa fa-pencil"></i>
                    <span>${title}</span>
                </div>
                <div id="shortName" class="sensor-title-block sensor-title-small form-editable" data-field="shortName" data-value="${shortName!''}" data-type="text">
                    <i class="fa fa-pencil"></i>
                    <small>${shortName!"<b class='text-muted'>No Short Name</b>"}</small>
                </div>
            </h1>
            <p id="description" class="lead form-editable" data-field="description" data-value="${description!''}" data-type="textarea">
                <i class="fa fa-pencil"></i>
                <span>${description!"<b class='text-muted'>No Description</b>"}</span>
            </p>
            <dl class="dl-horizontal sensor-rows">
                <dt class="form-editable sensor-row sensor-row-key" data-field="serialNumber" data-value="${serialNumber!''}" data-type="text">
                    <i class="fa fa-pencil"></i>
                    <span>Serial Number</span>
                </dt>
                <dd class="sensor-row sensor-row-value" id="serialNumber">
                    <span>${serialNumber!"<b class='text-muted'>No Serial Number</b>"}</span>
                </dd>
                <dt class="form-editable sensor-row sensor-row-key" data-field="documentation" data-value="${documentation!''}" data-type="text">
                    <i class="fa fa-pencil"></i>
                    <span>Documentation</span>
                </dt>
                <dd class="sensor-row sensor-row-value" id="documentation">
                    <span>${documentation!"<b class='text-muted'>No Documentation</b>"}</span>
                </dd>
                <dt class="form-editable sensor-row sensor-row-key" data-field="processType" data-value="${processType!''}" data-type="processType">
                    <i class="fa fa-pencil"></i>
                    <span>Process Type</span>
                </dt>
                <dd class="sensor-row sensor-row-value" id="processType">
                    <span>${processType!"<b class='text-muted'>No Process Type</b>"}</span>
                </dd>
                <dt class="form-editable sensor-row sensor-row-key" data-field="manufacturer" data-value="${manufacturer!''}" data-type="manufacturer">
                    <i class="fa fa-pencil"></i>
                    <span>Manufacturer</span>
                </dt>
                <dd class="sensor-row sensor-row-value" id="manufacturer">
                    <#if manufacturer??>
                        <a href='/documents/${manufacturer!"#"}'>${manufacturerName}</a>
                    <#else>
                        <b class='text-muted'>No Manufacturer</b>
                    </#if>
                </dd>
                <dt class="form-editable sensor-row sensor-row-key" data-field="defualtParameters" data-value="${defualtParameters!''}" data-type="defualtParameters">
                    <i class="fa fa-pencil"></i>
                    <span>Default Parameters</span>
                </dt>
                <dd class="sensor-row sensor-row-value" id="defualtParameters">
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
        </form>
        </div>
        <#include "_admin.html.tpl">
    </section>
</@skeleton.master>