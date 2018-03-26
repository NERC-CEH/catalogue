<#import "../new-form.ftl" as form>

<div class="value verticalmonitoring">
    <@form.value name="lowerPoint" label="Lower Point">
        <@form.input name="lowerPoint" placeholder="Lower Point" value="${lowerPoint!''}"></@form.input>
    </@form.value>
    <@form.value name="upperPoint" label="Upper Point">
        <@form.input name="upperPoint" placeholder="Upper Point" value="${upperPoint!''}"></@form.input>
    </@form.value>
</div>