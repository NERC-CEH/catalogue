<#import "../skeleton.html.tpl" as skeleton>
<#import "../new-form.html.tpl" as form>

<#assign manufacturers=elter.getManufacturers() >

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
    <@form.master document='sensor'>
        <@form.input name="type" type="hidden" value="dataset" class="is-hidden"></@form.input>
        <@form.head>
            <@form.title title=title></@form.title>
            <@form.value name="shortName">
                <@form.input name="shortName" class="subtitle" placeholder="Short Name" value="${shortName!''}" readonlyValue="${shortName!'No Short Name'}"></@form.input>
            </@form.value>
            <@form.description description=description></@form.description>
        </@form.head>
        <@form.body>
            <@form.value name="serialNumber" label="Serial Number">
                <@form.input name="serialNumber" placeholder="Serial Number" value="${serialNumber!''}" readonlyValue="${serialNumber!'No Serial Number'}"></@form.input>
            </@form.value>
            <@form.value name="documentation" label="Documentation" href="${documentation!'/documents/${id}#'}" errorMessage="Not a valid url, needs to be http(s)://url">
                <@form.input name="documentation" placeholder="Documentation" value="${documentation!''}" pattern="^(https?|ftp):\\/\\/(-\\.)?([^\\s\\/?\\.#-]+\\.?)+(\\/[^\\s]*)?$" readonlyValue="${documentation!'No Documentation'}"></@form.input>
            </@form.value>
            <@form.value name="processType" label="Process Type">
                <@form.select name="processType" value=processType!"" readonlyValue="${processType!'No Process Type'}">
                    <#if !processType??>
                        <option selected="selected" value="Unknown">Unknown</option>
                    <#else>
                        <option <#if processType?? && processType == "Unknown"> selected="selected"</#if> value="Unknown">Unknown</option>
                    </#if>
                    <option <#if processType?? && processType == "Simulation"> selected="selected"</#if> value="Simulation">Simulation</option>
                    <option <#if processType?? && processType == "Manual"> selected="selected"</#if> value="Manual">Manual</option>
                    <option <#if processType?? && processType == "Sensor"> selected="selected"</#if> value="Sensor">Sensor</option>
                    <option <#if processType?? && processType == "Algorithm"> selected="selected"</#if> value="Algorithm">Algorithm</option>
                </@form.select>
            </@form.value>
            <@form.value name="manufacturer" label="Manufacturer" href="/documents/${manufacturer!'${id}#'}">
                <div id="manufacturer">
                    <#if manufacturer??>
                        <@form.select name="manufacturer" value=manufacturer!"">
                            <option value=""></option>
                            <#list manufacturers as m>
                                <option <#if manufacturer?? && manufacturer == m.id>selected</#if> value="${m.id}">${m.title}</option>
                            </#list>
                            <option value="other">Other</option>
                        </@form.select>
                    <#else>
                        <@form.select name="manufacturer" value=manufacturer!"">
                            <option value=""></option>
                            <#list manufacturers as m>
                                <option <#if manufacturer?? && manufacturer == m.id>selected</#if> value="${m.id}">${m.title}</option>
                            </#list>
                            <option value="other">Other</option>
                        </@form.select>
                    </#if>
                </div>
            </@form.value>
            <@form.value name="manufacturerName" class="other-manufacturer" label="Manufacturer Name" hidden=true errorMessage="Name is required">
                <input disabled name="manufacturerName" placeholder="Manufacturer Name" required>
            </@form.value>
            <@form.value name="defaultParameters" label="Default Parameters">
                <@form.ifReadonly>
                    <#if defaultParameters??>
                        <ul class="list-unstyled">
                            <#list defaultParameters as defaultParameter>
                                <li><@form.static>${defaultParameter['value']}</@form.static></li>
                            </#list>
                        </ul>
                    <#else>
                        <@form.static>No Default Parameters</@form.static>
                    </#if>
                </@form.ifReadonly>
                <@form.ifNotReadonly>
                    <ul id="defaultParameters" class="list-unstyled">
                        <#if defaultParameters??>
                            <#list defaultParameters as defaultParameter>
                                <li>
                                    <@form.delete name="defaultParameter"></@form.delete>
                                    <@form.input name="defaultParameters[${defaultParameter_index}]['value']" placeholder="Default Parameter" value="${defaultParameter['value']}"></@form.input>
                                </li>
                            </#list>
                            <li>
                                <@form.delete name="defaultParameter"></@form.delete>
                                <@form.input name="defaultParameters[${defaultParameters?size}]['value']" placeholder="Default Parameter" value=""></@form.input>
                            </li>
                        <#else>
                            <li>
                                <@form.delete name="defaultParameter"></@form.delete>
                                <@form.input name="defaultParameters[0]['value']" placeholder="Default Parameter" value=""></@form.input>
                            </li>
                        </#if>
                    </ul>
                </@form.ifNotReadonly>
            </@form.value>
        </@form.body>
    </@form.master>
    <#include "_admin.html.tpl">
</@skeleton.master>