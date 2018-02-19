<#import "../skeleton.html.tpl" as skeleton>
<#import "../new-form.html.tpl" as form>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
    <@form.master document='manufacturer'>
        <@form.input name="type" type="hidden" value="dataset"></@form.input>
        <@form.head>
            <@form.title title=title></@form.title>
        </@form.head>
        <@form.body>
            <@form.valueLink label="Website" name="website">
                <@form.input name="website" placeholder="Website" value="${website!''}" pattern="^(https?|ftp):\\/\\/(-\\.)?([^\\s\\/?\\.#-]+\\.?)+(\\/[^\\s]*)?$" errorName="website" errorMessage="Not a valid url, needs to be http(s)://url"></@form.input>
            </@form.valueLink>
            <@form.value label="Manufactured">
                <ul id="sensors" class="list-unstyled">
                    <li>Retriving</li>
                </ul>
            </@form.value>
        </@form.body>
    </@form.master>
    <#include "_admin.html.tpl">
</@skeleton.master>