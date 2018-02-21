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
            <@form.value name="replaceBy" label="Replaced By">
                <@form.delete name="deleteTemporalProcedure"></@form.delete>
                <@form.select id="replaceBy" name="replaceBy">
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
            <@form.value name="replaceByName" class="other-replaceBy" label="Replace By Name" hidden=true errorMessage="Name is required">
                <input disabled name="replaceByName" placeholder="Replace By Name" required>
            </@form.value>
            <@form.value name="commitCode" label="Commit Code">
                <@form.input name="commitCode" placeholder="Commit Code" value="${commitCode!''}"></@form.input>
            </@form.value>
            <@form.value name="interpolationType" label="Interpolation Type">
                <@form.input name="interpolationType" placeholder="Interpolation Type" value="${interpolationType!''}"></@form.input>
            </@form.value>
            <@form.value name="intendedObservationSpacing" label="Intended Observation Spacing" errorMessage="A number greater than 0">
                <@form.input name="intendedObservationSpacing" placeholder="Intended Observation Spacing" value="${intendedObservationSpacing!'0'}" pattern="^[0-9][0-9]*$"></@form.input>
            </@form.value>
            <@form.value name="maximumGap" label="Maximum Gap" errorMessage="A number greater than 0">
                <@form.input name="maximumGap" placeholder="maximumGap" value="${maximumGap!'0'}" pattern="^[0-9][0-9]*$"></@form.input>
            </@form.value>
            <@form.value name="anchorTime" label="Anchor Time" errorMessage="A number greater than 0">
                <@form.input name="anchorTime" placeholder="anchorTime" value="${anchorTime!'0'}" pattern="^[0-9][0-9]*$"></@form.input>
            </@form.value>
            <@form.value name="sampleMedium" label="Sample Medium">
                <@form.input name="sampleMedium" placeholder="Sample Medium" value="${sampleMedium!''}"></@form.input>
            </@form.value>
            <@form.value name="loggerSensorName" label="Logger Sensor Name">
                <@form.input name="loggerSensorName" placeholder="Logger Sensor Name" value="${loggerSensorName!''}"></@form.input>
            </@form.value>
            <@form.value name="historicSensorName" label="Historic Sensor Name">
                <@form.input name="historicSensorName" placeholder="Historic Sensor Name" value="${historicSensorName!''}"></@form.input>
            </@form.value>
            <@form.value name="loggerName" label="Logger Name">
                <@form.input name="loggerName" placeholder="Logger Name" value="${loggerName!''}"></@form.input>
            </@form.value>
            <@form.value name="historicFeatureName" label="Historic Feature Name">
                <@form.input name="historicFeatureName" placeholder="Historic Feature Name" value="${historicFeatureName!''}"></@form.input>
            </@form.value>
        </@form.body>
    </@form.master>
    <#include "_admin.html.tpl">
</@skeleton.master>