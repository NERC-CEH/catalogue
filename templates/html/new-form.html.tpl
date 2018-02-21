<#if permission.userCanEdit(id)>
    <#assign readonly></#assign>
    <#assign disabled></#assign>
<#else>
    <#assign readonly>readonly</#assign>
    <#assign disabled>disabled</#assign>
</#if>

<#macro master document>
    <div class="container">
        <h3 id="loading" class="alert alert-info static-message">
            <i class='fa fa-circle-o-notch fa-spin '></i> Loading ...
        </h3>
        <h3 id="saved" class="alert alert-info static-message" style="display: none;">
            <i class="fa fa-save"></i> Saved
        </h3>
        <form id="form" class="new-form ${readonly}" data-document="${document}" data-guid="${id}" novalidate>
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
<#macro title title="" class="">
    <div id="value-title" class='value ${class}'>
        <div class="value-children">
            <input ${readonly} name="title" type="text" class='title' placeholder="Title" value="${title}" required>
        </div>
        <div class="value-error">
            <span>Title is required</span>
        </div>
    </div>
</#macro>
<#macro description description="">
    <div id="value-title" class='value ${class}'>
        <div class="value-children">
            <textarea ${disabled} name="description" type="text" class='description' placeholder="Description">${description}</textarea>
        </div>
    </div>
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

<#macro input name type="text" class="" value="" placeholder="" id="" pattern="">
    <input ${readonly} id="${id}" name="${name}" type="${type}" class="${class}" placeholder="${placeholder}" value="${value}" <#if pattern != "">pattern="${pattern}"</#if>>
</#macro>
<#macro select name id="" class="">
    <select ${disabled} id="${id}" name="${name}" class="${class}">
        <#nested>
    </select>
</#macro>

<#macro value name label="" class="" hidden=false errorMessage="" href="">
    <div id="value-${name}" class='value ${class} <#if href != "">value-link</#if> <#if hidden>is-hidden</#if>' data-name="${name}">
        <#if label != "">
            <label class="value-label">
            <#if href != "">
                <a class='value-href' href="${href}">${label}</a>
            <#else>
                ${label}
            </#if>
            </label>
        </#if>
        <div class="value-children">
            <#nested>
        </div>
        <div class="value-error is-hidden <#if label != "">is-labelled</#if>">
            <span>${errorMessage}</span>
        </div>
    </div>
</#macro>

<#macro delete name>
    <#if readonly == "">
        <a id="" href='#' class="delete delete-${name}"><i class="fa fa-times"></i></a>
    </#if>
</#macro>

<#macro link name label href="#">
    <a id="" href="${href}" class="link link-${name}">${label}</a>
</#macro>
