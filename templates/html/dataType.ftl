<#import "skeleton.ftl" as skeleton>

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
          <th>Constraints</th>
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
            <span class="schema-description">${schemaItem.description}</span>
          <#else>
            <span class="nodata" />
          </#if>
        </td>
        <td>
          <#if schemaItem.constraints?? && schemaItem.constraints?has_content>
            <span class="schema-constraints">
              <#if schemaItem.constraints.minimum?has_content>Minimum : ${schemaItem.constraints.minimum}<br></#if>
              <#if schemaItem.constraints.maximum?has_content>Maximum : ${schemaItem.constraints.maximum}<br></#if>
              <#if schemaItem.constraints.minLength?has_content>Minimum length : ${schemaItem.constraints.minLength}<br></#if>
              <#if schemaItem.constraints.maxLength?has_content>Maximum length : ${schemaItem.constraints.maxLength}<br></#if>
              <#if schemaItem.constraints.unique=true><small class="far fa-check-circle text-success"></small> Is unique</#if>
            </span>
          <#else>
            <span class="nodata" />
          </#if>
        </td>
      </tr>    
      </#list>
      </tbody></table>
      </#if>

      
      <#if provenance.creationDate?has_content || provenance.modificationDate?has_content || provenance.contributors?has_content >
        <h1 class="section-heading">Provenance</h1>
        <#if provenance.creationDate?? && provenance.creationDate?has_content>
          <div><p>Created: ${provenance.creationDate}</p></div>
        </#if>
        <#if provenance.modificationDate?? && provenance.modificationDate?has_content>
          <div><p>Modified: ${provenance.modificationDate}</p></div>
        </#if>
        <#if provenance.contributors?? && provenance.contributors?has_content>
          <div><p>Contributors: ${provenance.contributors?join(", ")}</p></div>
        </#if>
      </#if>
      
    </div>
  </div>
</#escape>
</@skeleton.master>