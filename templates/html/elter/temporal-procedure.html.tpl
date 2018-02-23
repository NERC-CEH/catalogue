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
            <@form.ifNotReadonly>
                <@form.value name="replacedBy" label="Replaced By">
                    <ul id="replacedBy" class="list-unstyled">
                        <#if replacedBy??>
                            <#list replacedBy as temporalProcedure>
                                <li>
                                    <div class='value-block'>
                                    <@form.delete name="deleteReplacedBy"></@form.delete>
                                        <div class='value-block-value'>
                                            <@form.select name="replacedBy[${temporalProcedure_index}]">
                                                <#list tps as tp>
                                                    <#if id != tp.id>
                                                        <option <#if temporalProcedure == tp.id>selected</#if> value="${tp.id}">${tp.title}</option>
                                                    </#if>
                                                </#list>
                                                    <option value="other">Other</option>
                                            </@form.select>
                                            <div>
                                                <a class="static-value" href="/documents/${temporalProcedure}">${elter.getTemporalProcedure(temporalProcedure).title}</a>
                                            </div>
                                        </div>
                                    </div>
                                </li>
                            </#list>
                            <li>
                                <div class='value-block'>
                                <@form.delete name="deleteReplacedBy"></@form.delete>
                                    <div class='value-block-value'>
                                        <@form.select name="replacedBy[${replacedBy?size}]">
                                            <option value=""></option>
                                            <#list tps as tp>
                                                <#if id != tp.id>
                                                    <option value="${tp.id}">${tp.title}</option>
                                                </#if>
                                            </#list>
                                                <option value="other">Other</option>
                                        </@form.select>
                                    </div>
                                </div>
                            </li>
                        <#else>
                            <li>
                                <div class='value-block'>
                                <@form.delete name="deleteReplacedBy"></@form.delete>
                                    <div class='value-block-value'>
                                        <@form.select name="replacedBy[0]">
                                            <option value=""></option>
                                            <#list tps as tp>
                                                <#if id != tp.id>
                                                    <option value="${tp.id}">${tp.title}</option>
                                                </#if>
                                            </#list>
                                                <option value="other">Other</option>
                                        </@form.select>
                                    </div>
                                </div>
                            </li>
                        </#if>
                    </ul>
                </@form.value>
            </@form.ifNotReadonly>
            <@form.ifReadonly>
                <@form.value name="replacedBy" label="Replaced By">
                    <ul class="list-unstyled">
                        <#if replacedBy??>
                            <#list replacedBy as temporalProcedure>
                                <li>
                                    <a class="static-value" href="/documents/${temporalProcedure}">${elter.getTemporalProcedure(temporalProcedure).title}</a>
                                </li>
                            </#list>
                        </#if>
                    </ul>
                </@form.value>
            </@form.ifReadonly>
            <@form.value name="replacedByName" class="other-replacedBy" label="Replace By Name" hidden=true errorMessage="Name is required">
                <input disabled name="replacedByName" placeholder="Replace By Name" required>
            </@form.value>
            <@form.value name="commitCode" label="Commit Code">
                <@form.input name="commitCode" placeholder="Commit Code" value=commitCode!'' readonlyValue=commitCode!'No Commit Code'></@form.input>
            </@form.value>
            <@form.value name="interpolationType" label="Interpolation Type">
                <@form.input name="interpolationType" placeholder="Interpolation Type" value=interpolationType!'' readonlyValue=interpolationType!'No Interpolation Type'></@form.input>
            </@form.value>
            <@form.value name="intendedObservationSpacing" label="Intended Observation Spacing" errorMessage="A number greater than 0">
                <@form.input name="intendedObservationSpacing" placeholder="Intended Observation Spacing" value="${intendedObservationSpacing!'0'}" readonlyValue=intendedObservationSpacing!'0' pattern="^[0-9][0-9]*$"></@form.input>
            </@form.value>
            <@form.value name="maximumGap" label="Maximum Gap" errorMessage="A number greater than 0">
                <@form.input name="maximumGap" placeholder="maximumGap" value="${maximumGap!'0'}" readonlyValue=maximumGap!'0' pattern="^[0-9][0-9]*$"></@form.input>
            </@form.value>
            <@form.value name="anchorTime" label="Anchor Time" errorMessage="A number greater than 0">
                <@form.input name="anchorTime" placeholder="anchorTime" value="${anchorTime!'0'}" readonlyValue=anchorTime!'0' pattern="^[0-9][0-9]*$"></@form.input>
            </@form.value>
            <@form.value name="sampleMedium" label="Sample Medium">
                <@form.input name="sampleMedium" placeholder="Sample Medium" value="${sampleMedium!''}" readonlyValue=sampleMedium!'No Sample Medium'></@form.input>
            </@form.value>
            <@form.value name="loggerSensorName" label="Logger Sensor Name">
                <@form.input name="loggerSensorName" placeholder="Logger Sensor Name" value="${loggerSensorName!''}" readonlyValue=sampleMedium!'No Logger Sensor Name'></@form.input>
            </@form.value>
            <@form.value name="historicSensorName" label="Historic Sensor Name">
                <@form.input name="historicSensorName" placeholder="Historic Sensor Name" value="${historicSensorName!''}" readonlyValue=historicSensorName!'No Historic Sensor Name'></@form.input>
            </@form.value>
            <@form.value name="loggerName" label="Logger Name">
                <@form.input name="loggerName" placeholder="Logger Name" value="${loggerName!''}" readonlyValue=loggerName!'No Logger Name'></@form.input>
            </@form.value>
            <@form.value name="historicFeatureName" label="Historic Feature Name">
                <@form.input name="historicFeatureName" placeholder="Historic Feature Name" value="${historicFeatureName!''}" readonlyValue=historicFeatureName!'No Historic Feature Name'></@form.input>
            </@form.value>
        </@form.body>
    </@form.master>
    <#include "_admin.html.tpl">
</@skeleton.master>