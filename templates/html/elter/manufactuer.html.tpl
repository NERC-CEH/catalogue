<#import "../skeleton.html.tpl" as skeleton>

<#if permission.userCanEdit(id)>
    <#assign readonly></#assign>
    <#assign disabled></#assign>
<#else>
    <#assign readonly>readonly</#assign>
    <#assign disabled>disabled</#assign>
</#if>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
    <section id='metadata'>
        <div class='container'>
        <h3 id="loading" class="alert alert-info">
            <i class='fa fa-circle-o-notch fa-spin '></i> Loading ...
        </h3>
        <h3 id="saved" class="alert alert-info" style="display: none;">
            <i class="fa fa-save"></i> Saved
        </h3>
        <form id="form" class="new-form ${readonly}" data-document="manufacturer" data-guid="${id}">
            <input ${readonly} name="type" type="hidden" value="dataset">
            <div class='head'>
                <input ${readonly} name="title" type="text" class='title' value="${title}" placeholder="Title" required>
            </div>
            <div class='body'>
                <div class='value value-link' data-name="website">
                    <label><a href="${website!'#'}">Website</a></label>
                    <input name="website" placeholder="Manufacturer Website">
                </div>
                <div class='value'>
                    <label>Sensors</label>
                    <ul id="sensors" class="list-unstyled">
                        <li>Retriving</li>
                    </ul>
                </div>
            </div>
        </form>
        <#include "_admin.html.tpl">
    </section>
</@skeleton.master>