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
    <@form.master document='observation-placeholder'>
        <@form.input name="type" type="hidden" value="dataset"></@form.input>
        <@form.head>
            <@form.title title=title></@form.title>
        </@form.head>
        <@form.body>
            <@form.value label="Signature">
                <ul class="list-unstyled">
                    <li>
                        temporal entity
                    </li>
                </ul>
            </@form.value>
            <@form.value label="Routed To">
                <ul class="list-unstyled">
                    <li>
                        input string
                    </li>
                </ul>
            </@form.value>
            <@form.value label="User By">
                <ul class="list-unstyled">
                    <li>
                        input string
                    </li>
                </ul>
            </@form.value>
            <@form.value label="Visible Through">
                <ul class="list-unstyled">
                    <li>
                        temporal procedure string
                    </li>
                </ul>
            </@form.value>
            <@form.value label="Controls Frequency Of">
                <ul class="list-unstyled">
                    <li>
                        input string
                    </li>
                </ul>
            </@form.value>
        </@form.body>
    </@form.master>
    <#include "_admin.html.tpl">
</@skeleton.master>