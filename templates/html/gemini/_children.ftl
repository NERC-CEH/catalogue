<#assign childRecords = jena.inverseRelationships(uri, "http://vocabs.ceh.ac.uk/eidc#partOf")  >
<#assign parentRecords = jena.relationships(uri, "http://vocabs.ceh.ac.uk/eidc#partOf")  >

<#if childRecords?has_content && childRecords?size gt 0>
  <div class="panel panel-default" id="document-children">
    <div class="panel-heading">
      <p class="panel-title">This ${recordType} includes the following resources</p>
    </div>
    <div class="panel-body">
      <#list childRecords?sort_by("title")>
        <#items as child>
          <p><a href="${child.href?html}">${child.title?html}</a></p>
        </#items>
      </#list>
    </div>
  </div>
</#if>

<#if parentRecords?has_content && parentRecords?size gt 0>
  <div class="panel panel-default" id="document-parent">
    <div class="panel-heading">
      <p class="panel-title">This ${recordType} is part of the following</p>
    </div>
    <div class="panel-body">
      <#list parentRecords?sort_by("title")>
        <#items as parent>
          <p><a href="${parent.href?html}">${parent.title?html}</a></p>
        </#items>
       </#list>
    </div>
  </div>
</#if>