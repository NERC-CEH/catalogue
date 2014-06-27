<#import "skeleton.html.tpl" as skeleton>

<@skeleton.master title=title>

<div class="row">
    <div class="col-md-12">
      <#include "_title.html.tpl">
    </div>
</div>

<div class="row">
    <div class="col-md-12">
      <#include "_description.html.tpl">
    </div>
</div>

<div class="row">
    <div class="col-md-8">
      <#include "_contacts.html.tpl">
    </div>
    <div class="col-md-4">
      <#include "_ordering.html.tpl">
      <#include "_citation.html.tpl">
    </div>
</div>

<div class="row">
    <div class="col-md-12">
      <#include "_keywords.html.tpl">
    </div>
</div>

</@skeleton.master>