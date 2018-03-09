<#import "skeleton.html.tpl" as skeleton>

<#assign title=document.title>
<#assign id=document.id>
<#assign catalogue=catalogues.retrieve(document.metadata.catalogue)>

<@skeleton.master title=title catalogue=catalogue>
    
    <div class="container">
        <a href="/elter/documents" class="btn btn-link">
            <i class="fa fa-files-o" aria-hidden="true"></i>
            <span>${catalogue.title} Documents</span>
        </a>
        <a href="/documents/${document.id}" class="btn btn-link">
            <i class="fa fa-file-o" aria-hidden="true"></i>
            <span>${title}</span>
        </a>
    </div>
</@skeleton.master>