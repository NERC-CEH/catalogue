<#import "skeleton.html.tpl" as skeleton>
<#import "keywords.html.tpl" as keyword>
<#import "title.html.tpl" as t>
<#import "description.html.tpl" as d>
<#import "topiccategories.html.tpl" as tc>

<@skeleton.master title=title>

<!-- InstanceBeginEditable name="MAIN" -->
<div class="row">
	<div class="col-md-12">
  		<@t.title_tmpl title=title/>
  	</div>
</div>

<div class="row">
	<div class="col-md-9">
  		<@d.description_tmpl description=description/>
  	</div>
  	<div class="col-md-3">
  		<p>Get the data</p>
  	</div>
</div>

<div class="row">
	<div class="col-md-12">
 		<@tc.category/>
  	</div>
</div>

<div class="row">
	<div class="col-md-12">
 		<@keyword.keywords/>
  	</div>
</div>

</@skeleton.master>