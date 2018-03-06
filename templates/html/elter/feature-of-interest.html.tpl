<#import "../skeleton.html.tpl" as skeleton>
<#import "../new-form.html.tpl" as form>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
    <@form.master document='feature-of-interest'>
        <@form.input name="type" type="hidden" value="dataset"></@form.input>
        <@form.head>
            <@form.title title=title></@form.title>
        </@form.head>
        <@form.body>
            <@form.value name="foiType" label="Feature Of Interest Type">
                <@form.select id="foi-type" name="foiType">
                    <#if !foiType??>
                        <option id="blank-option" value=""></option>
                    </#if>
                    <option <#if foiType?? && foiType == "Monitoring"> selected="selected"</#if> value="Monitoring">Monitoring</option>
                    <option <#if foiType?? && foiType == "VerticalMonitoring"> selected="selected"</#if> value="VerticalMonitoring">Vertical Monitoring</option>
                    <option <#if foiType?? && foiType == "Composite"> selected="selected"</#if> value="Composite">Composite</option>
                </@form.select>
            </@form.value>
            <div class='foi-data is-${(foiType!"notype")?lower_case}'>
                <#include "_monitoring.html.tpl">
                <#include "_veritcal-monitoring.html.tpl">
                <#include "_composite.html.tpl">
                <#if !foiType??>
                    <div id="no-type" class="alert alert-danger notype" role="alert">
                        This Feature Of Interest has no type
                    </div>
                </#if>
            </div>
        </@form.body>
    </@form.master>
    <#include "_admin.html.tpl">
</@skeleton.master>