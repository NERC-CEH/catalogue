<#import "../new-form.html.tpl" as form>

<@form.value name="lowerPoint" label="Lower Point" class="verticalmonitoring">
    <@form.input name="lowerPoint" placeholder="Lower Point" value="${lowerPoint!''}"></@form.input>
</@form.value>
<@form.value name="upperPoint" label="Upper Point" class="verticalmonitoring">
    <@form.input name="upperPoint" placeholder="Upper Point" value="${upperPoint!''}"></@form.input>
</@form.value>