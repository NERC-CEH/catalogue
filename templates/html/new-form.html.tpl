<#if permission.userCanEdit(id)>
    <#assign readonly></#assign>
    <#assign disabled></#assign>
<#else>
    <#assign readonly>readonly</#assign>
    <#assign disabled>disabled</#assign>
</#if>

<#macro master document>
    <div class="container">
        <h3 id="loading" class="alert alert-info">
            <i class='fa fa-circle-o-notch fa-spin '></i> Loading ...
        </h3>
        <h3 id="saved" class="alert alert-info" style="display: none;">
            <i class="fa fa-save"></i> Saved
        </h3>
        <form id="form" class="new-form ${readonly}" data-document="${document}" data-guid="${id}">
            <#nested>
            <input type="submit" style="display: none;">
        </form>
    </div>
</#macro>

<#macro head>
    <div class='head'>
        <#nested>
    </div>
</#macro>
<#macro body>
    <div class='body'>
        <#nested>
    </div>
</#macro>
<#macro title title="">
    <input ${readonly} name="title" type="text" class='title' placeholder="Title" value="${title}" required>
</#macro>
<#macro description description="">
    <textarea ${disabled} name="description" type="text" class='description' placeholder="Description">${description}</textarea>
</#macro>

<#macro ifReadonly>
    <#if readonly == "">
        <#nested>
    </#if>
</#macro>
<#macro ifDisabled>
    <#if disabled == "">
        <#nested>
    </#if>
</#macro>

<#macro input name type="text" class="" value="" placeholder="" id="">
    <input ${readonly} id="${id}" name="${name}" type="${type}" class="${class}" placeholder="${placeholder}" value="${value}">
</#macro>
<#macro select name id="">
    <select ${disabled} id="${id}" name="${name}">
        <#nested>
    </select>
</#macro>

<#macro value label>
    <div class='value'>
        <label>${label}</label>
        <#nested>
    </div>
</#macro>
<#macro valueLink label name format="#" href="#">
    <div class='value value-link' data-name="${name}" data-format="${format}">
        <label><a href="${href}">${label}</a></label>
        <#nested>
    </div>
</#macro>

<#macro delete name>
    <#if readonly == "">
        <a id="" href='#' class="delete delete-${name}"><i class="fa fa-times"></i></a>
    </#if>
</#macro>