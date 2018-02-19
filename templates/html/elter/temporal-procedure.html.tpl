<#import "../skeleton.html.tpl" as skeleton>
<#import "../new-form.html.tpl" as form>

<#if permission.userCanEdit(id)>
    <#assign readonly></#assign>
    <#assign disabled></#assign>
<#else>
    <#assign readonly>readonly</#assign>
    <#assign disabled>disabled</#assign>
</#if>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
    <@form.master document='temporal-procedure'>
        <@form.input name="type" type="hidden" value="dataset"></@form.input>
        <@form.head>
            <@form.title title=title></@form.title>
        </@form.head>
        <@form.body>
            <@form.value label="Replaced By">
                <ul class="list-unstyled">
                    <li>
                        temporal procedure string
                    </li>
                </ul>
            </@form.value>
            <@form.value label="Commit Code">
                input
            </@form.value>
            <@form.value label="Interpolation Type">
                select?
            </@form.value>
            <@form.value label="Intended Observation Spacing">
                input number (greater than 0?)
            </@form.value>
            <@form.value label="Maximum Gap">
                input number (greater than 0?)
            </@form.value>
            <@form.value label="Anchor Time">
                input number (greater than 0?)
            </@form.value>
            <@form.value label="Sample Medium">
                input
            </@form.value>
            <@form.value label="Logger Sensor Name">
                input
            </@form.value>
            <@form.value label="Historic Sensor Name">
                input
            </@form.value>
            <@form.value label="Logger Name">
                input
            </@form.value>
            <@form.value label="Historic Feature Name">
                input
            </@form.value>
            
        </@form.body>
    </@form.master>
    <#include "_admin.html.tpl">
</@skeleton.master>