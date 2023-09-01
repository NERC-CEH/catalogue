    <h2>Access the data</h2>
    <div class="distribution-signpost">
      <#if resourceType.value == 'nercSignpost' >
        <#include "_nercSignpost.ftlh">
      <#elseif resourceType.value == 'signpost'  && metadata.catalogue != "eidc">
        <#include "_signpost.ftlh">
      </#if>
    </div>
