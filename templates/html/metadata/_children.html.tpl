<#if resourceType == 'series'>
  <div class="panel panel-default" id="document-children">
    <div class="panel-body">
      <h4>This data series comprises the following datasets:</h4>
      <#list children as child>
        <p><a href="${child.href?html}">${child.title?html}</a></p>
      </#list>
    </div>
  </div>
</#if>