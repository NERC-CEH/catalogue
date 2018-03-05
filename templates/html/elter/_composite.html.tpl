<#import "../new-form.html.tpl" as form>

<div class="value composite">
    <@form.ifNotReadonly>
        <@form.value name="originalStream" label="Original Stream">
            <@form.selectList name="originalStream" documents=originalStream allDocuments=allTps></@form.selectList>
        </@form.value>
    </@form.ifNotReadonly>
    <@form.value name="originalStreamName" class="other-originalStream" label="Original Stream Name" hidden=true errorMessage="Name is required">
        <input disabled name="originalStreamName" placeholder="Original Stream Name" required>
    </@form.value>
</div>