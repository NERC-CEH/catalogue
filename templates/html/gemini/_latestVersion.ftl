<div id="latestVersion" class="text-right">
  <b>This is the most recent version of this ${recordType}</b>
  <span class="dropdown otherVersions">
    <span class="dropdown-toggle" id="dropdownVersions" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">
       (other versions
      <span class="caret"></span>)
    </span>
    <ul class="dropdown-menu dropdown-menu-right" aria-labelledby="dropdownVersions">
      <#list rel_supersedes as item>
      <li><a href="${item.href?html}">${item.title?html}</a></li>
      </#list>
    </ul>
  </span>  
</div>
