<#import "../skeleton.html.tpl" as skeleton>

<#if permission.userCanEdit(id)>
    <#assign readonly></#assign>
    <#assign disabled></#assign>
<#else>
    <#assign readonly>readonly</#assign>
    <#assign disabled>disabled</#assign>
</#if>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
<div class="container">
    <h3 id="loading" class="alert alert-info">
        <i class='fa fa-circle-o-notch fa-spin '></i> Loading ...
    </h3>
    <h3 id="saved" class="alert alert-info" style="display: none;">
        <i class="fa fa-save"></i> Saved
    </h3>
    <form id="form" class="new-form ${readonly}" data-document="sensor" data-guid="${id}">
        <input ${readonly} name="type" type="hidden" value="dataset">
        <div class='head'>
            <input ${readonly} name="title" type="text" class='title' value="${title}" placeholder="Title" required>
            <input ${readonly} name="shortName" type="text" class='subtitle' value="${shortName!''}" placeholder="Short Name">
            <textarea ${disabled} name="description" type="text" class='description' placeholder="Description">${description!''}</textarea>
        </div>
        <div class='body'>
            <div class='value'>
                <label>Serial Number</label>
                <input ${readonly} name="serialNumber" type="text" value="${serialNumber!''}" placeholder="Serial Number">
            </div>
            <div class='value value-link' data-name="documentation">
                <label><a href="${documentation!'#'}">Documentation</a></label>
                <input ${readonly} name="documentation" type="text" value="${documentation!''}" placeholder="Documentation">
            </div>
            <div class='value'>
                <label>Process Type</label>
                <select ${disabled} name="processType">
                    <#if !processType??>
                        <option value=""></option>
                    </#if>
                    <option <#if processType?? && processType == "Unknown"> selected="selected"</#if> value="Unknown">Unknown</option>
                    <option <#if processType?? && processType == "Simulation"> selected="selected"</#if> value="Simulation">Simulation</option>
                    <option <#if processType?? && processType == "Manual"> selected="selected"</#if> value="Manual">Manual</option>
                    <option <#if processType?? && processType == "Sensor"> selected="selected"</#if> value="Sensor">Sensor</option>
                    <option <#if processType?? && processType == "Algorithm"> selected="selected"</#if> value="Algorithm">Algorithm</option>
                </select>
            </div>
            <div class='value value-link' data-name="manufacturer" data-format="/documents/{manufacturer}">
                <label><a href="/documents/${manufacturer!'#'}">Manufacturer</a></label>
                <select ${disabled} id="manufacturer" name="manufacturer">
                    <#if manufacturer??>
                        <option value="${manufacturer}">${manufacturerName}</option>
                    <#else>
                        <option value=""></option>
                    </#if>
                </select>
            </div>
            <div class='value'>
                <label>Default Parameters</label>
                <ul <#if readonly == "">id="defaultParameters"</#if> class="list-unstyled">
                    <#if defaultParameters??>
                    <#list defaultParameters as defaultParameter>
                        <li>
                            <#if readonly == "">
                                <a href='#' class="delete delete-defaultParameter"><i class="fa fa-times"></i></a>
                            </#if>
                            <input ${readonly} name="defaultParameters[${defaultParameter_index}]['value']" type="text" value="${defaultParameter['value']}" placeholder="Default Parameter">
                        </li>
                    </#list>
                        <#if readonly == "">
                        <li>
                            <a href='#' class="delete delete-defaultParameter"><i class="fa fa-times"></i></a>
                            <input ${readonly} name="defaultParameters[${defaultParameters?size}]['value']" type="text" value="" placeholder="Default Parameter">
                        </li>
                        </#if>
                    <#else>
                        <#if readonly == "">
                        <li>
                            <a href='#' class="delete delete-defaultParameter"><i class="fa fa-times"></i></a>
                            <input ${readonly} name="defaultParameters[0]['value']" type="text" value="" placeholder="Default Parameter">
                        </li>
                        </#if>
                    </#if>
                </ul>
            </div>
        </div>
        <input type="submit" style="display: none;">
    </form>
    <#include "_admin.html.tpl">
</div>
</@skeleton.master>