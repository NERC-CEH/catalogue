<#import "../skeleton.html.tpl" as skeleton>
<#import "../new-form.html.tpl" as form>

<#assign allInputs=elter.getInputs()>
<#assign allTps=elter.getTemporalProcedures()>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
    <@form.master document='observation-placeholder'>
        <@form.input name="type" type="hidden" value="dataset"></@form.input>
        <@form.head>
            <@form.title title=title></@form.title>
        </@form.head>
        <@form.body>
            <@form.value name="signature" label="Signature">
                <ul class="list-unstyled">
                    <li>
                        temporal entity
                    </li>
                </ul>
            </@form.value>
            <@form.ifNotReadonly>
                <@form.value name="routedTo" label="Routed To">
                    <@form.selectList name="routedTo" documents=routedTo allDocuments=allInputs></@form.selectList>
                </@form.value>
            </@form.ifNotReadonly>
            <@form.value name="routedToName" class="other-routedTo" label="Routed To Name" hidden=true errorMessage="Name is required">
                <input disabled name="routedToName" placeholder="Routed To Name" required>
            </@form.value>

            <@form.ifNotReadonly>
                <@form.value name="usedBy" label="Used By">
                    <@form.selectList name="usedBy" documents=usedBy allDocuments=allInputs></@form.selectList>
                </@form.value>
            </@form.ifNotReadonly>
            <@form.value name="usedByName" class="other-usedBy" label="Used By Name" hidden=true errorMessage="Name is required">
                <input disabled name="usedByName" placeholder="Used By Name" required>
            </@form.value>

            <@form.ifNotReadonly>
                <@form.value name="controlsFrequencyOf" label="Controls Frequenct Of">
                    <@form.selectList name="controlsFrequencyOf" documents=controlsFrequencyOf allDocuments=allInputs></@form.selectList>
                </@form.value>
            </@form.ifNotReadonly>
            <@form.value name="controlsFrequencyOfName" class="other-controlsFrequencyOf" label="Controls Frequenct Of Name" hidden=true errorMessage="Name is required">
                <input disabled name="controlsFrequencyOfName" placeholder="Controls Frequenct Of Name" required>
            </@form.value>

            <@form.ifNotReadonly>
                <@form.value name="visibleThrough" label="Visible Through">
                    <@form.selectList name="visibleThrough" documents=visibleThrough allDocuments=allTps></@form.selectList>
                </@form.value>
            </@form.ifNotReadonly>
            <@form.value name="visibleThroughName" class="other-visibleThrough" label="Visible Through Name" hidden=true errorMessage="Name is required">
                <input disabled name="visibleThroughName" placeholder="Visible Through Name" required>
            </@form.value>

            
        </@form.body>
    </@form.master>
    <#include "_admin.html.tpl">
</@skeleton.master>