<h2>Identifiers</h3>
<ul class="list-unstyled">
  <li>${uri}</li>
  <#list identifiers as identifier>
    <li>${identifier.namespace!''}:${identifier.localIdentifier!''}</li>
  </#list>
</ul>
