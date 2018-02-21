<#import "../skeleton.html.tpl" as skeleton>
<#import "../new-form.html.tpl" as form>

<#assign manufacturers=elter.getManufacturers() >

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
    <@form.master document='sensor'>
        <@form.input name="type" type="hidden" value="dataset"></@form.input>
        <@form.head>
            <@form.title title=title></@form.title>
            <@form.value name="shortName">
                <@form.input name="shortName" class="subtitle" placeholder="Short Name" value="${shortName!''}"></@form.input>
            </@form.value>
            <@form.description description=description></@form.description>
        </@form.head>
        <@form.body>
            <@form.value name="serialNumber" label="Serial Number">
                <@form.input name="serialNumber" placeholder="Serial Number" value="${serialNumber!''}"></@form.input>
            </@form.value>
            <@form.value name="documentation" label="Documentation" href="${documentation!'/documents/${id}#'}" errorMessage="URL format http(s)://...">
                <@form.input name="documentation" placeholder="Documentation" value="${documentation!''}" pattern="^(https?|ftp):\\/\\/(-\\.)?([^\\s\\/?\\.#-]+\\.?)+(\\/[^\\s]*)?$"></@form.input>
            </@form.value>
            <@form.value name="processType" label="Process Type">
                <@form.select name="processType">
                    <#if !processType??>
                        <option value=""></option>
                    </#if>
                    <option <#if processType?? && processType == "Unknown"> selected="selected"</#if> value="Unknown">Unknown</option>
                    <option <#if processType?? && processType == "Simulation"> selected="selected"</#if> value="Simulation">Simulation</option>
                    <option <#if processType?? && processType == "Manual"> selected="selected"</#if> value="Manual">Manual</option>
                    <option <#if processType?? && processType == "Sensor"> selected="selected"</#if> value="Sensor">Sensor</option>
                    <option <#if processType?? && processType == "Algorithm"> selected="selected"</#if> value="Algorithm">Algorithm</option>
                </@form.select>
            </@form.value>
            <@form.value  name="manufacturer" label="Manufacturer" href="/documents/${manufacturer!'${id}#'}">
                <@form.select id="manufacturer" name="manufacturer">
                    <#if ! manufacturer??>
                        <option value=""></option>
                    </#if>
                    <#list manufacturers as m>
                        <option <#if manufacturer?? && manufacturer == m.id>selected</#if> value="${m.id}">${m.title}</option>
                    </#list>
                    <option value="other">Other</option>
                </@form.select>
            </@form.value>
            <@form.value name="manufacturerName" class="other-manufacturer" label="Manufacturer Name" hidden=true errorMessage="Name is required">
                <input disabled name="manufacturerName" placeholder="Manufacturer Name" required>
            </@form.value>
            <@form.value name="defaultParameters" label="Default Parameters">
                <ul <@form.ifReadonly>id="defaultParameters"</@form.ifReadonly> class="list-unstyled">
                    <#if defaultParameters??>
                    <#list defaultParameters as defaultParameter>
                        <li>
                            <@form.delete name="defaultParameter"></@form.delete>
                            <@form.input name="defaultParameters[${defaultParameter_index}]['value']" placeholder="Default Parameter" value="${defaultParameter['value']}"></@form.input>
                        </li>
                    </#list>
                        <@form.ifReadonly>
                        <li>
                            <@form.delete name="defaultParameter"></@form.delete>
                            <@form.input name="defaultParameters[${defaultParameters?size}]['value']" placeholder="Default Parameter" value=""></@form.input>
                        </li>
                        </@form.ifReadonly>
                    <#else>
                        <@form.ifReadonly>
                        <li>
                            <@form.delete name="defaultParameter"></@form.delete>
                            <@form.input name="defaultParameters[0]['value']" placeholder="Default Parameter" value=""></@form.input>
                        </li>
                        </@form.ifReadonly>
                    </#if>
                </ul>
            </@form.value>
        </@form.body>
    </@form.master>
    <#include "_admin.html.tpl">
</@skeleton.master>