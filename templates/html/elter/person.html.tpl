<#import "../skeleton.html.tpl" as skeleton>
<#import "../new-form.html.tpl" as form>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
    <@form.master document='person'>
        <@form.input name="type" type="hidden" value="dataset"></@form.input>
        <@form.head>
            <@form.title title=title></@form.title>
        </@form.head>
        <@form.body>

        </@form.body>
    </@form.master>
    <#include "_admin.html.tpl">
</@skeleton.master>