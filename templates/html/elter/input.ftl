<#import "../skeleton.ftl" as skeleton>
<#import "../new-form.ftl" as form>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
    <@form.master document='input'>
        <@form.input name="type" type="hidden" value="dataset"></@form.input>
        <@form.head>
            <@form.title title=title></@form.title>
        </@form.head>
        <@form.body>
            <@form.value label="Parameter Value" name="parameterValue">
                <@form.input name="parameterValue" value=parameterValue></@form.input>
            </@form.value>
            <@form.value label="Temporal Anchor" name="temporalAnchor">
                <div class="value-block">
                    <label for="temporalAnchorYes">Yes</label>
                    <@form.input id="temporalAnchorYes" type="radio" name="temporalAnchor" value="true" checked="${temporalAnchor?string('true', 'false')}"></@form.input>
                    <label for="temporalAnchorNo">No</label>
                    <@form.input id="temporalAnchorNo" type="radio" name="temporalAnchor" value="false" checked="${temporalAnchor?string('false', 'true')}"></@form.input>
                </div>
            </@form.value>
        </@form.body>
    </@form.master>
    <#include "_admin.ftl">
</@skeleton.master>