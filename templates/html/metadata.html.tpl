<#import "skeleton.html.tpl" as skeleton>

<@skeleton.master title=title>

<!-- InstanceBeginEditable name="MAIN" -->
<div class="row">
    <div class="col-md-12">
      <#include "_title.html.tpl">
    </div>
</div>

<div class="row">
    <div class="col-md-9">
      <#include "_description.html.tpl">
    </div>
    <div class="col-md-3">
      <p>Get Data</p>
    </div>
</div>

<div class="row">
    <div class="col-md-12">
      <#include "_topiccategories.html.tpl">
    </div>    
</div>

<div class="row">
    <div class="col-md-12">
      <#include "_keywords.html.tpl">
    </div>
</div>

</@skeleton.master>