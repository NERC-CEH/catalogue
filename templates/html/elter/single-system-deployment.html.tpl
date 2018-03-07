<#import "../skeleton.html.tpl" as skeleton>
<#import "../new-form.html.tpl" as form>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
    <@form.master document='single-system-deployment'>
        <@form.input name="type" type="hidden" value="dataset"></@form.input>
        <@form.head>
            <@form.title title=title></@form.title>
        </@form.head>
        <@form.body>
            <@form.ifNotReadonly>
                <@form.value name="powers" label="Powers">
                    <@form.selectList name="powers" documents=powers></@form.selectList>
                </@form.value>
            </@form.ifNotReadonly>
            <@form.value name="powersName" class="other-powers" label="Powers Name" hidden=true errorMessage="Name is required">
                <input disabled name="powersName" placeholder="Powers Name" required>
            </@form.value>

            <@form.ifNotReadonly>
                <@form.value name="deploymentInstallation" label="Deployment Installation">
                    <@form.selectList name="deploymentInstallation" documents=deploymentInstallation></@form.selectList>
                </@form.value>
            </@form.ifNotReadonly>
            <@form.value name="deploymentInstallationName" class="other-deploymentInstallation" label="Deployment Installation Name" hidden=true errorMessage="Name is required">
                <input disabled name="deploymentInstallationName" placeholder="Deployment Installation Name" required>
            </@form.value>

            <@form.ifNotReadonly>
                <@form.value name="deploymentRemoval" label="Deployment Removal">
                    <@form.selectList name="deploymentRemoval" documents=deploymentRemoval></@form.selectList>
                </@form.value>
            </@form.ifNotReadonly>
            <@form.value name="deploymentRemovalName" class="other-deploymentRemoval" label="Deployment Removal Name" hidden=true errorMessage="Name is required">
                <input disabled name="deploymentRemovalName" placeholder="Deployment Removal Name" required>
            </@form.value>

            <@form.ifNotReadonly>
                <@form.value name="deploymentCleaning" label="Deployment Cleaning">
                    <@form.selectList name="deploymentCleaning" documents=deploymentCleaning></@form.selectList>
                </@form.value>
            </@form.ifNotReadonly>
            <@form.value name="deploymentCleaningName" class="other-deploymentCleaning" label="Deployment Cleaning Name" hidden=true errorMessage="Name is required">
                <input disabled name="deploymentCleaningName" placeholder="Deployment Cleaning Name" required>
            </@form.value>

            <@form.ifNotReadonly>
                <@form.value name="deploymentMaintenance" label="Deployment Maintenance">
                    <@form.selectList name="deploymentMaintenance" documents=deploymentMaintenance></@form.selectList>
                </@form.value>
            </@form.ifNotReadonly>
            <@form.value name="deploymentMaintenanceName" class="other-deploymentMaintenance" label="Deployment Maintenance Name" hidden=true errorMessage="Name is required">
                <input disabled name="deploymentMaintenanceName" placeholder="Deployment Maintenance Name" required>
            </@form.value>

            <@form.ifNotReadonly>
                <@form.value name="deploymentProgramUpdate" label="Deployment Program Update">
                    <@form.selectList name="deploymentProgramUpdate" documents=deploymentProgramUpdate></@form.selectList>
                </@form.value>
            </@form.ifNotReadonly>
            <@form.value name="deploymentProgramUpdateName" class="other-deploymentProgramUpdate" label="Deployment Program Update Name" hidden=true errorMessage="Name is required">
                <input disabled name="deploymentProgramUpdateName" placeholder="Deployment Program Update Name" required>
            </@form.value>
        </@form.body>
    </@form.master>
    <#include "_admin.html.tpl">
</@skeleton.master>