
<div class="container">

  <div class="clearfix">
    <#if citation?has_content>
      ${citation.authors?join(', ')} (${citation.year?string["0000"]}).
      <b>${citation.title}</b>.  https://doi.org/${citation.doi}
    <#else>
      ${title}
    </#if>
  </div>

  <div class="alert alert-danger" role="alert">
    <#include "_status.ftlh">
  </div>

</div>
