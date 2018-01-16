<#import "../skeleton.html.tpl" as skeleton>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>    
<div class='container'>
    <form id="form" class="form-inline new-form" data-document="sensor" data-guid="${id}" method="post" action="/documents/${id}/submit">
        <input name="id" type="hidden" value="${id}">
        <input name="type" type="hidden" value="dataset">
        <h1>
            <div id="title" class="sensor-title-block sensor-title form-editable" data-field="title" data-value="${title}" data-type="text" data-is-required="required">
                <div id="form-value-title">
                    <i class="fa fa-pencil"></i>
                    <span id="title-value">${title}</span>
                    <input name="title" type="hidden" value="${title}">
                </div>
            </div>
            <div id="shortName" class="sensor-title-block sensor-title-small form-editable" data-field="shortName" data-value="${shortName!''}" data-type="text" data-default-value="<b class='text-muted'>No Short Name</b>">
                <div id="form-value-shortName">
                    <i class="fa fa-pencil"></i>
                    <small id="shortName-value">${shortName!"<b class='text-muted'>No Short Name</b>"}</small>
                    <input name="shortName" type="hidden" value="${shortName!''}">
                </div>
            </div>
        </h1>
        <div id="description" class="lead form-editable" data-field="description" data-value="${description!''}" data-type="textarea" data-default-value="<b class='text-muted'>No Description</b>">
            <div id="form-value-description">
                <p style="white-space: pre-line"><i class="fa fa-pencil"></i> <span id="description-value">${description!"<b class='text-muted'>No Description</b>"}</span></p>
                <input name="description" type="hidden" value="${description!''}">
            </div>
        </div>
        <dl class="dl-horizontal sensor-rows">
            <div class="form-editable" data-field="serialNumber" data-value="${serialNumber!''}" data-type="text" data-default-value="<b class='text-muted'>No Serial Number</b>">
                <input name="serialNumber" type="hidden" value="${serialNumber!''}">
                <dt class="sensor-row sensor-row-key">
                    <i class="fa fa-pencil"></i>
                    <span>Serial Number</span>
                </dt>
            </div>
            <div id="form-value-serialNumber">
                <dd class="sensor-row sensor-row-value">
                    <span id="serialNumber-value">${serialNumber!"<b class='text-muted'>No Serial Number</b>"}</span>
                </dd>
            </div>

            <div class="form-editable" data-field="documentation" data-value="${documentation!''}" data-type="link" data-default-value="<b class='text-muted'>No Documentation</b>">
                <input name="documentation" type="hidden" value="${documentation!''}">
                <dt class="sensor-row sensor-row-key">
                    <i class="fa fa-pencil"></i>
                    <span>Documentation</span>
                </dt>
            </div>
            <div id="form-value-documentation">
                <dd class="sensor-row sensor-row-value">
                    <a id="documentation-value" href="${documentation!'#'}">${documentation!"<b class='text-muted'>No Documentation</b>"}</a>
                </dd>
            </div>

            <div class="form-editable" data-field="processType" data-value="${processType!''}" data-type="processType" data-default-value="<b class='text-muted'>No Process Type</b>">
                <input name="processType" type="hidden" value="${processType!''}">
                <dt class="sensor-row sensor-row-key">
                    <i class="fa fa-pencil"></i>
                    <span>Process Type</span>
                </dt>
            </div>
            <div id="form-value-processType">
                <dd class="sensor-row sensor-row-value">
                    <span id="processType-value">${processType!"<b class='text-muted'>No Process Type</b>"}</span>
                </dd>
            </div>
        </dl>
    </form>
</div>
<#include "_admin.html.tpl">
</@skeleton.master>