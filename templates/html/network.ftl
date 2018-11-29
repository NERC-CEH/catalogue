<#import "skeleton.ftl" as skeleton>

<#assign title=document.title>
<#assign id=document.id>
<#assign catalogue=catalogues.retrieve(document.metadata.catalogue)>

<@skeleton.master title=title catalogue=catalogue>
    <div class="container">
        <div class="row">
            <h1>${title}</h1>
        </div>
        <div class="row">
            <div class="col-md-12">
                <div class="graph-buttons">
                    <button type="button" class="btn btn-sm btn-default btn-success" id="cy-explore">
                        <i class="far fa-dot-circle"></i>
                        <i class="far fa-circle"></i>
                        <i class="far fa-hand-pointer"></i>
                        <span> Explore</span>
                    </button>
                    <button type="button" class="btn btn-sm btn-default" id="cy-open">
                        <i class="far fa-dot-circle"></i>
                        <i class="far fa-circle"></i>
                        <i class="far fa-file"></i>
                        <span> Open</span>
                    </button>
                    <button type="button" class="btn btn-sm btn-default" id="cy-zoom-in">
                        <i class="fas fa-search-plus"></i><span> Zoom In</span>
                    </button>
                    <button type="button" class="btn btn-sm btn-default" id="cy-zoom-out">
                        <i class="fas fa-search-minus"></i><span> Zoom Out</span>
                    </button>
                    <button type="button" class="btn btn-sm btn-default" id="cy-fit">
                        <i class="fas fa-search"></i><span> Fit</span>
                    </button>
                    <button type="button" class="btn btn-sm btn-default" id="cy-organise">
                        <i class="fas fa-random"></i><span> Organise Layout</span>
                    </button>
                    <button type="button" class="btn btn-sm btn-default" id="cy-screenshot">
                        <i class="far fa-file-image"></i><span> Screenshot</span>
                    </button>
                </div>
                <div class="cy" id="cy" data-document=${id}></div>
            </div>
        </div>
        <div class="row">
            <a href="/documents/${document.id}" class="btn btn-link">
                <i class="far fa-file" aria-hidden="true"></i>
                <span>${title}</span>
            </a>
        </div>
    </div>
</@skeleton.master>