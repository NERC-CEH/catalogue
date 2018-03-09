<#import "skeleton.html.tpl" as skeleton>

<#assign title=document.title>
<#assign id=document.id>
<#assign catalogue=catalogues.retrieve(document.metadata.catalogue)>

<@skeleton.master title=title catalogue=catalogue>
    <div class="container">
        <div class="row">
            <h1>${title}</h1>
        </div>
        <div class="row">
            <div class="col-md-0">
                <div class="menu"></div>
            </div>
            <div class="col-md-12">
                <div>
                    <button type="button" class="btn btn-sm btn-default" id="cy-zoom-in">
                        <i class="fa fa-search-plus"></i><span> Zoom In</span>
                    </button>
                    <button type="button" class="btn btn-sm btn-default" id="cy-zoom-out">
                        <i class="fa fa-search-minus"></i><span> Zoom Out</span>
                    </button>
                    <button type="button" class="btn btn-sm btn-default" id="cy-fit">
                        <i class="fa fa-search"></i><span> Fit</span>
                    </button>
                    <button type="button" class="btn btn-sm btn-default" id="cy-organise">
                        <i class="fa fa-random"></i><span> Organise Layout</span>
                    </button>
                    <button type="button" class="btn btn-sm btn-default" id="cy-screenshot">
                        <i class="fa fa-file-image-o"></i><span> Screenshot</span>
                    </button>
                </div>
                <div class="cy" id="cy" data-document=${id}></div>
            </div>
        </div>
        <div class="row">
            <a href="/elter/documents" class="btn btn-link">
                <i class="fa fa-files-o" aria-hidden="true"></i>
                <span>${catalogue.title} Documents</span>
            </a>
            <a href="/documents/${document.id}" class="btn btn-link">
                <i class="fa fa-file-o" aria-hidden="true"></i>
                <span>${title}</span>
            </a>
        </div>
    </div>
</@skeleton.master>