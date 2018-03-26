<#if id??>
    <#if permission.userCanEdit(id)>
        <#assign readonly></#assign>
        <#assign disabled></#assign>
    <#else>
        <#assign readonly>readonly</#assign>
        <#assign disabled>disabled</#assign>
    </#if>
<#else>
    <#assign readonly>readonly</#assign>
    <#assign disabled>disabled</#assign>
</#if>

<#macro master document>
    <div class="container beta-form">
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

<#macro form document name overrideReadOnly=false>
    <form id="form" class="new-form new-${name} <#if overrideReadOnly>override<#else>${readonly}</#if>" data-document="${document}" <#if id??>data-guid="${id}"</#if> novalidate>
        <#nested>
        <input type="submit" style="display: none;">
    </form>
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
    <div id="value-title" class='value'>
        <#if readonly == "readonly">
            <div class="value-children">
                <span class="static-value is-blank">No Description</span>
            </div>
        <#else>
            <div class="value-children">
                <textarea ${disabled} name="description" type="text" class='description' placeholder="Description">${description}</textarea>
            </div>
        </#if>
    </div>
</#macro>

<#macro ifReadonly>
    <#if readonly == "readonly">
        <#nested>
    </#if>
</#macro>
<#macro ifNotReadonly>
    <#if readonly == "">
        <#nested>
    </#if>
</#macro>
<#macro ifDisabled>
    <#if disabled == "disabled">
        <#nested>
    </#if>
</#macro>
<#macro ifNotDisabled>
    <#if disabled == "disabled">
        <#nested>
    </#if>
</#macro>

<#macro input name type="text" class="" value="" placeholder="" id="" pattern="" readonlyValue="" checked="false">
    <#if readonly == "readonly">
        <span class="static-value <#if value == "">is-blank</#if>">${readonlyValue}</span>
    <#else>
        <input ${readonly} id="${id}" name="${name}" type="${type}" class="${class}" placeholder="${placeholder}" value="${value}" <#if pattern != "">pattern="${pattern}"</#if> <#if checked == "true">checked</#if>>
    </#if>
</#macro>
<#macro select name id="" class="" value="" readonlyValue="">
    <#if readonly == "readonly">
        <span class="static-value <#if value == "">is-blank</#if>">${readonlyValue}</span>
    <#else>
        <select ${disabled} id="${id}" name="${name}" class="${class}">
            <#nested>
        </select>
    </#if>
</#macro>
<#macro static class="">
    <span class="static-value ${class}">
        <#nested>
    </span>
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
            <#if readonly == "">
                <div class="value-error">
                    <span>${errorMessage}</span>
                </div>
            </#if>
        </div>

    </div>
</#macro>

<#macro delete>
    <#if readonly == "">
        <a id="" href='#' class="delete"><i class="fa fa-times"></i></a>
    </#if>
</#macro>
<#macro selectList name documents=[] allDocuments=[]>
<ul id="${name}" class="list-unstyled">
    <#if documents??>
        <#list documents as doc>
            <li class='delete-parent'>
                <div class='value-block'>
                <@delete></@delete>
                    <div class='value-block-value'>
                        <@select name="documents[${doc_index}]">
                            <#list allDocuments as allDoc>
                                <#if id != allDoc.id>
                                    <option <#if doc == allDoc.id>selected</#if> value="${allDoc.id}">${allDoc.title}</option>
                                </#if>
                            </#list>
                                <option value="other">Other</option>
                        </@select>
                        <div>
                            <a class="static-value" href="/documents/${doc}">${doc}</a>
                        </div>
                    </div>
                </div>
            </li>
        </#list>
        <li class='delete-parent'>
            <div class='value-block'>
                <@delete></@delete>
                <div class='value-block-value'>
                    <@select name="${name}[${documents?size}]">
                        <option value=""></option>
                        <#list allDocuments as allDoc>
                            <#if id != allDoc.id>
                                <option value="${allDoc.id}">${allDoc.title}</option>
                            </#if>
                        </#list>
                            <option value="other">Other</option>
                    </@select>
                </div>
            </div>
        </li>
    <#else>
        <li class='delete-parent'>
            <div class='value-block'>
                <@delete></@delete>
                <div class='value-block-value'>
                    <@select name="documents[0]">
                        <option value=""></option>
                        <#list allDocuments as allDoc>
                            <#if id != allDoc.id>
                                <option value="${allDoc.id}">${allDoc.title}</option>
                            </#if>
                        </#list>
                            <option value="other">Other</option>
                    </@select>
                </div>
            </div>
        </li>
    </#if>
</ul>
</#macro>