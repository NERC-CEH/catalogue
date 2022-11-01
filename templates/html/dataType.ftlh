<#import "skeleton.ftlh" as skeleton>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>

  <div id="metadata" class="datatype">
    <div class="section section-grey">
      <div class="container">

        <#if permission.userCanEdit(id)>
          <div class="admin hidden-print">
            <div class="text-right">
              <div class="btn-group">
                <a type="button" class="btn btn-default edit-control" href="#" data-document-type="${metadata.documentType}">Edit</a>
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

        <h1>${title}</h1>
        <#if description?? && description?has_content>
          <div class="description">${description}</div>
        </#if>

      </div>
    </div>

    <div class="section">
      <div class="container">
        <#if schema??>
          <div class="section--schema">
            <h2 class="section-heading">Schema</h2>
            <div class="section-content">
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
            </div>
          </div>
          </#if>

          <#if provenance??>
          <div class="section--provenance">
            <#if provenance.creationDate?has_content || provenance.modificationDate?has_content || provenance.contributors?has_content >
              <h2 class="section-heading">Provenance</h2>
              <div class="section-content">
              <#if provenance.creationDate?? && provenance.creationDate?has_content>
                <div>
                  <span>Created</span>
                  <span title="${provenance.creationDate?date?string['dd MMMM yyyy']} ${provenance.creationDate?datetime.iso?time?string['HH:mm']}">${provenance.creationDate?date?string['dd MMMM yyyy']}</span>
                </div>
              </#if>
              <#if provenance.modificationDate?? && provenance.modificationDate?has_content>
                <div>
                  <span>Modified</span>
                  <span title="${provenance.modificationDate?date?string['dd MMMM yyyy']} ${provenance.modificationDate?datetime.iso?time?string['HH:mm']}">${provenance.modificationDate?date?string['dd MMMM yyyy']}</span>
                </div>
              </#if>
              <#if provenance.contributors?? && provenance.contributors?has_content>
                <div>
                  <span>Contributors</span>
                  <span>${provenance.contributors?join(", ")}</span>
                </div>
              </#if>
              </div>
            </#if>
          </div>
          </#if>

      </div>
    </div>

  </div>
</@skeleton.master>