<#import "../skeleton.html.tpl" as skeleton>
<#import "../new-form.html.tpl" as form>

<#assign sensors=elter.getSensors(id) >

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
    <@form.master document='manufacturer'>
        <@form.input name="type" type="hidden" value="dataset"></@form.input>
        <@form.head>
            <@form.title title=title></@form.title>
        </@form.head>
        <@form.body>
            <@form.value href="${website!'/documents/${id}#'}" name="website" label="Website" name="website" errorMessage="Not a valid url, needs to be http(s)://url">
                <@form.input name="website" placeholder="Website" value="${website!''}" pattern="^(https?|ftp):\\/\\/(-\\.)?([^\\s\\/?\\.#-]+\\.?)+(\\/[^\\s]*)?$" readonlyValue="${website!'No Website'}"></@form.input>
            </@form.value>
            <@form.value name="manufactured" label="Manufactured">
                <ul class="list-unstyled">
                    <#if sensors?size == 0>
                        <li><span class="static-value">No Sensors</span></li>
                    <#else>
                        <#list sensors as sensor>
                            <li><a class="static-value" href="/documents/${sensor.id}">${sensor.title}</a></li>
                        </#list>
                    </#if>
                </ul>
            </@form.value>
        </@form.body>
    </@form.master>
    <#include "_admin.html.tpl">
</@skeleton.master>