
<div class="container">
  <#include "_admin.ftlh">

  <div class="clearfix">
    <#if citation?has_content>
      ${citation.authors?join(', ')} (${citation.year?string["0000"]}).
      <b>${citation.title}</b>.  https://doi.org/${citation.doi}
    <#else>
      ${title}
    </#if>
  </div>

  <div class="notice notice--${resourceStatus?lower_case} clearfix" role="alert">
    <#include "_status.ftlh">
  </div>

</div>
