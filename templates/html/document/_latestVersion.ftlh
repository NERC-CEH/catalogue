<div id="latestVersion" class="text-end">
  <b>This is the most recent version of this ${recordType}</b>
  <span class="dropdown otherVersions">
    <span class="dropdown-toggle" id="dropdownVersions" data-bs-toggle="dropdown" aria-haspopup="true" aria-expanded="true">
       (view other versions)
    </span>
    <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="dropdownVersions">
      <#list rel_supersedes as item>
      <li><a class="dropdown-item" href="${item.href}">${item.title}</a></li>
      </#list>
    </ul>
  </span>
</div>
