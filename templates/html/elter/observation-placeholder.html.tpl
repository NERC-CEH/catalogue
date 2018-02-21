<#import "../skeleton.html.tpl" as skeleton>
<#import "../new-form.html.tpl" as form>

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
            <@form.value name="routedTo" label="Routed To">
                <ul class="list-unstyled">
                    <li>
                        input string
                    </li>
                </ul>
            </@form.value>
            <@form.value name="usedBy" label="Used By">
                <ul class="list-unstyled">
                    <li>
                        input string
                    </li>
                </ul>
            </@form.value>
            <@form.value name="visibleThrough" label="Visible Through">
                <ul class="list-unstyled">
                    <li>
                        temporal procedure string
                    </li>
                </ul>
            </@form.value>
            <@form.value name="controlsFrequencyOf" label="Controls Frequency Of">
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