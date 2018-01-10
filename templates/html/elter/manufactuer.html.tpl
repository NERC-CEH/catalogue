<#import "../skeleton.html.tpl" as skeleton>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
    <section id='metadata'>
        <div class='container'>
            <h1>${title} <small></small></h1>
            <div class="panel panel-default">
                <div class="panel-body">
                    <dl class="dl-horizontal">
                        <dt>Website</dt>
                        <dd><a href='${website!"#"}'>${website!"<b class='text-muted'>No Website</b>"}</a></dd>
                    </dl>
                </div>
            </div>
        </div>
        <#include "_admin.html.tpl">
    </section>
</@skeleton.master>