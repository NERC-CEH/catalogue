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
    <@form.master document='feature-of-interest'>
        <@form.input name="type" type="hidden" value="dataset"></@form.input>
        <@form.head>
            <#if !foiType??>
                <div id="no-type" class="alert alert-danger" role="alert">
                    This Feature Of Interest has no type
                </div>
            </#if>
            <@form.title title=title></@form.title>
        </@form.head>
        <@form.body>
            <@form.value label="Feature Of Interest Type">
                <@form.select name="foiType">
                    <#if !foiType??>
                        <option value=""></option>
                    </#if>
                    <option <#if foiType?? && foiType == "Monitoring"> selected="selected"</#if> value="Monitoring">Monitoring</option>
                    <option <#if foiType?? && foiType == "VerticalMonitoring"> selected="selected"</#if> value="VerticalMonitoring">Vertical Monitoring</option>
                    <option <#if foiType?? && foiType == "Composite"> selected="selected"</#if> value="Composite">Composite</option>
                </@form.select>
            </@form.value>
            <#if foiType?? && foiType == "Monitoring">
                <@form.value label="Property Attributes">
                <ul <@form.ifReadonly>id="propertyAttributes"</@form.ifReadonly> class="list-unstyled">
                    <li>
                        <@form.delete name="propertyAttribute"></@form.delete>
                        <@form.input name="propertyAttributes[0]" placeholder="Property Attribute (temporal entity)" value=""></@form.input>
                    </li>
                </ul>
                </@form.value>
            </#if>
            <#if foiType?? && foiType == "VerticalMonitoring">
                <@form.value label="Lower Point">
                    <@form.input name="lowerPoint" placeholder="Lower Point" value="${lowerPoint!''}"></@form.input>
                </@form.value>
                <@form.value label="Upper Point">
                    <@form.input name="upperPoint" placeholder="Upper Point" value="${upperPoint!''}"></@form.input>
                </@form.value>
            </#if>
            <#if foiType?? && foiType == "Composite">
             <@form.value label="Observation Placeholders">
                <ul class="list-unstyled">
                    <li>
                        <@form.link name="linky" label="9699d939-a9e0-4ca5-816a-c2581b1c0298" href="http://google.com"></@form.link>
                        <@form.select name="foiType" class="observation-placeholder">
                            <option value="id2">Second</option>
                        </@form.select>
                    </li>
                    <li>
                        <@form.delete name="observationPlacholder"></@form.delete>
                        <@form.link name="linky" label="9699d939-a9e0-4ca5-816a-c2581b1c0298" href="http://google.com"></@form.link>
                        <@form.select name="foiType" class="observation-placeholder">
                            <option value="id1">First</option>
                        </@form.select>
                    </li>
                    <li>
                        <@form.delete name="observationPlacholder"></@form.delete>
                        <@form.link name="linky" label="" href="#"></@form.link>
                        <@form.select name="foiType" class="observation-placeholder">
                            <option value="idx"></option>
                            <option value="id1">First</option>
                            <option value="id2">Second</option>
                            <option value="other-observation-placeholder">Other</option>
                        </@form.select>
                    </li>
                </ul>
             </@form.value>
            </#if>
        </@form.body>
    </@form.master>
    <#include "_admin.html.tpl">
</@skeleton.master>