<#import "../skeleton.ftl" as skeleton>
<#import "../new-form.ftl" as form>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
    <@form.master document='deployment-related-process-duration'>
        <@form.input name="type" type="hidden" value="dataset"></@form.input>
        <@form.head>
            <@form.title title=title></@form.title>
        </@form.head>
        <@form.body>
            <@form.ifNotReadonly>
                <@form.value name="carriedOutBy" label="Carried Out By">
                    <@form.selectList name="carriedOutBy" documents=carriedOutBy></@form.selectList>
                </@form.value>
            </@form.ifNotReadonly>
            <@form.value name="carriedOutByName" class="other-carriedOutBy" label="Carried Out By Name" hidden=true errorMessage="Name is required">
                <input disabled name="carriedOutByName" placeholder="Carried Out By Name" required>
            </@form.value>
        </@form.body>
    </@form.master>
    <#include "_admin.ftl">
</@skeleton.master>