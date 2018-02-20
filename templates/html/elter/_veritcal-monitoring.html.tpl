<#import "../new-form.html.tpl" as form>

<@form.value label="Lower Point" class="verticalmonitoring">
    <@form.input name="lowerPoint" placeholder="Lower Point" value="${lowerPoint!''}"></@form.input>
</@form.value>
<@form.value label="Upper Point" class="verticalmonitoring">
    <@form.input name="upperPoint" placeholder="Upper Point" value="${upperPoint!''}"></@form.input>
</@form.value>