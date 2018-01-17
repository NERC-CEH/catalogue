<#import "../skeleton.html.tpl" as skeleton>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
<div class="container">
    <span id="loading">LOADING ...</span>
    <span id="saved" style="display: none;">SAVED</span>
    <form id="form" class="new-form" data-document="sensor" data-guid="${id}">
        <input name="type" type="hidden" value="dataset">
        <div class='head'>
            <input name="title" type="text" class='title' value="${title}" placeholder="Title" required>
            <input name="shortName" type="text" class='subtitle' value="${shortName!''}" placeholder="Short Name">
            <textarea name="description" type="text" class='description' placeholder="Description">${description!''}</textarea>
        </div>
        <div class='body'>
            <div class='value'>
                <label>Serial Number</label>
                <input name="serialNumber" type="text" value="${serialNumber!''}" placeholder="Serial Number">
            </div>
            <div class='value value-link' data-name="documentation">
                <label><a href="${documentation!'#'}">Documentation</a></label>
                <input name="documentation" type="text" value="${documentation!''}" placeholder="Documentation">
            </div>
            <div class='value'>
                <label>Process Type</label>
                <select name="processType">
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
                <select id="manufacturer" name="manufacturer">
                    <#if manufacturer??>
                        <option value="${manufacturer}">${manufacturerName}</option>
                    <#else>
                        <option value=""></option>
                    </#if>
                </select>
            </div>
            <div class='value'>
                <label>Default Parameters</label>
                <ul id="defaultParameters" class="list-unstyled">
                    <#if defaultParameters??>
                    <#list defaultParameters as defaultParameter>
                        <li>
                            <a href='#' class="delete"><i class="fa fa-times"></i></a>
                            <input name="defaultParameters[${defaultParameter_index}]['value']" type="text" value="${defaultParameter['value']}" placeholder="Default Parameter">
                        </li>
                    </#list>
                        <li>
                            <a href='#' class="delete"><i class="fa fa-times"></i></a>
                            <input name="defaultParameters[${defaultParameters?size}]['value']" type="text" value="" placeholder="Default Parameter">
                        </li>
                    <#else>
                        <li>
                            <a href='#' class="delete"><i class="fa fa-times"></i></a>
                            <input name="defaultParameters[0]['value']" type="text" value="" placeholder="Default Parameter">
                        </li>
                    </#if>
                </ul>
            </div>
        </div>
        <input type="submit" style="display: none;">
    </form>
</div>
</@skeleton.master>