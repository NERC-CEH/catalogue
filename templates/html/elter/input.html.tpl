<#import "../skeleton.html.tpl" as skeleton>
<#import "../new-form.html.tpl" as form>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
    <@form.master document='input'>
        <@form.input name="type" type="hidden" value="dataset"></@form.input>
        <@form.head>
            <@form.title title=title></@form.title>
        </@form.head>
        <@form.body>
            <@form.value label="ID">
                input
            </@form.value>
            <@form.value label="Parameter Value">
                input
            </@form.value>
            <@form.value label="Temporal Anchor">
                radio
            </@form.value>
        </@form.body>
    </@form.master>
    <#include "_admin.html.tpl">
</@skeleton.master>