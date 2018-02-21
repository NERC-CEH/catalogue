<#import "../skeleton.html.tpl" as skeleton>
<#import "../new-form.html.tpl" as form>

<#assign tps=elter.getTemporalProcedures()>


<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
    <@form.master document='temporal-procedure'>
        <@form.input name="type" type="hidden" value="dataset"></@form.input>
        <@form.head>
            <@form.title title=title></@form.title>
        </@form.head>
        <@form.body>
            <@form.value label="Replaced By">
                <@form.delete name="bacon"></@form.delete>
                <@form.select name="temporalProcedures">
                    <#if !(replacedBy??)>
                        <option value=""></option>
                    <#elseif replacedBy?? && replacedBy?size == 0>
                        <option value=""></option>
                    </#if>
                    <#list tps as temporalProcedure>
                        <#if temporalProcedure.id != id>
                            <option value="${temporalProcedure.id}">${temporalProcedure.title}</option>
                        </#if>
                    </#list>
                        <option value="other">Other</option>
                </@form.select>
            </@form.value>
            <@form.value label="Commit Code">
                <@form.input name="commitCode" placeholder="Commit Code" value="${commitCode!''}"></@form.input>
            </@form.value>
            <@form.value label="Interpolation Type">
                <@form.input name="interpolationType" placeholder="Interpolation Type" value="${interpolationType!''}"></@form.input>
            </@form.value>
            <@form.value label="Intended Observation Spacing">
                <@form.input name="intendedObservationSpacing" placeholder="Intended Observation Spacing" value="${intendedObservationSpacing!'0'}" pattern="^[0-9][0-9]*$" errorName="intendedObservationSpacing" errorMessage="A number greater than 0"></@form.input>
            </@form.value>
            <@form.value label="Maximum Gap">
                <@form.input name="maximumGap" placeholder="maximumGap" value="${maximumGap!'0'}" pattern="^[0-9][0-9]*$" errorName="maximumGap" errorMessage="A number greater than 0"></@form.input>
            </@form.value>
            <@form.value label="Anchor Time">
                <@form.input name="anchorTime" placeholder="anchorTime" value="${anchorTime!'0'}" pattern="^[0-9][0-9]*$" errorName="anchorTime" errorMessage="A number greater than 0"></@form.input>
            </@form.value>
            <@form.value label="Sample Medium">
                <@form.input name="sampleMedium" placeholder="Sample Medium" value="${sampleMedium!''}"></@form.input>
            </@form.value>
            <@form.value label="Logger Sensor Name">
                <@form.input name="loggerSensorName" placeholder="Logger Sensor Name" value="${loggerSensorName!''}"></@form.input>
            </@form.value>
            <@form.value label="Historic Sensor Name">
                <@form.input name="historicSensorName" placeholder="Historic Sensor Name" value="${historicSensorName!''}"></@form.input>
            </@form.value>
            <@form.value label="Logger Name">
                <@form.input name="loggerName" placeholder="Logger Name" value="${loggerName!''}"></@form.input>
            </@form.value>
            <@form.value label="Historic Feature Name">
                <@form.input name="historicFeatureName" placeholder="Historic Feature Name" value="${historicFeatureName!''}"></@form.input>
            </@form.value>
        </@form.body>
    </@form.master>
    <#include "_admin.html.tpl">
</@skeleton.master>