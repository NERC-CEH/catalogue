<#import "skeleton.ftl" as skeleton>
<#import "new-form.ftl" as form>
<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)><#escape x as x?html?replace("\n", "<br>")>

<style>
/* temporary obvs */
.admin {margin-top:1em;}
.schemaItem { border-bottom: 1px solid #eee; margin:0.5em; padding:0.5em;}
</style>

  <div id="metadata" class="datatype">
    <div class="container">
      
     <#if permission.userCanEdit(id)>
      <div class="admin hidden-print">
        <div class="text-right">
          <div class="btn-group">
            <a type="button" class="btn btn-default edit-control" href="#" data-document-type="${metadata.documentType}">Edit</a></button>
            <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
              <span class="caret"></span>
              <span class="sr-only">Toggle Dropdown</span>
            </button>
            <ul class="dropdown-menu dropdown-menu-right">
              <li><a href="/documents/${id}/permission" ><i class="fas fa-users"></i> Permissions</a></li>
              <li><a href="/documents/${id}/publication" ><i class="fas fa-eye"></i> Publication status</a></li>
              <li role="separator" class="divider"></li>
              <li><a href="/documents/${id}/catalogue" class="catalogue-control"><i class="fas fa-sign-out-alt"></i> Move to a different catalogue</a></li>
            </ul>
          </div>
        </div>
      </div>
      </#if>

      <h1 class="section-heading">${title}</h1>
      <#if description?? && description?has_content>
        <div class="description">${description}</div>
      </#if>

      <#if schema??>
      <h1 class="section-heading">Schema</h1>
      <table class="table table-schema">
      <thead>
        <tr>
          <th>Field</th>
          <th>Type</th>
          <th>Description</th>
        </tr>
      </thead>
      <tbody>
      <#list schema as schemaItem>
      <tr>
        <td nowrap="nowrap">
          <#if schemaItem.name?? && schemaItem.name?has_content>
          ${schemaItem.name}
          </#if>
        </td>
        <td nowrap="nowrap">
          <#if schemaItem.type?? && schemaItem.type?has_content>
              ${schemaItem.type}
              <#if schemaItem.format?? && schemaItem.format?has_content>
                <span class="schema-format">(${schemaItem.format})</span>
              </#if>          
          </#if>
        </td>
        <td>
          <#if schemaItem.description?? && schemaItem.description?has_content>
            <span class="decript">${schemaItem.description}</span>
          <#else>
            <span class="nodata" />
          </#if>
        </td>
      </tr>    
      </#list>
      </tbody></table>
      </#if>

      <#if schema??>
        <h1 class="section-heading">More schema stuff for tidying up</h1>
        <#list schema as schemaItem>
        <div class="schemaItem">
          <#if schemaItem.name?? && schemaItem.name?has_content>
            <div>name = ${schemaItem.name}</div>
          </#if>
          <#if schemaItem.title?? && schemaItem.title?has_content>
            <div>title = ${schemaItem.title}</div>
          </#if>
          <#if schemaItem.type?? && schemaItem.type?has_content>
            <div>
              type = ${schemaItem.type}
              <#if schemaItem.format?? && schemaItem.format?has_content>
                <span>(${schemaItem.format})</span>
              </#if>
            </div>
          </#if>
          <#if schemaItem.description?? && schemaItem.description?has_content>
            <div>Description = ${schemaItem.description}</div>
          </#if>
          <#--<#if schemaItem.primaryKey==true>
            <div>primaryKey = Yes</div>
          </#if>-->
          <#if schemaItem.constraints?? && schemaItem.constraints?has_content>
            <div>constraints = ${schemaItem.constraints}</div>
          </#if>
          <#if schemaItem.units?? && schemaItem.units?has_content>
            <div>units = ${schemaItem.units}</div>
          </#if>
        </div>    
        </#list>
      </#if> 
      
      <#if provenance?? && (provenance.creationDate?? || provenance.modificationDate??)>
        <h1 class="section-heading">Provenance</h1>
        <#if provenance.creationDate?? && provenance.creationDate?has_content>
          <div>${provenance.creationDate}</div>
        </#if>
        <#if provenance.modificationDate?? && provenance.modificationDate?has_content>
          <div>${provenance.modificationDate}</div>
        </#if>
        <#if provenance.contributors?? && provenance.contributors?has_content>
          <div>${provenance.contributors?join(", ")}</div>
        </#if>
      </#if>
      
      <#if id?? && id?has_content>
        <div>${id}</div>
      </#if>

    </div>
  </div>
</#escape>
</@skeleton.master>