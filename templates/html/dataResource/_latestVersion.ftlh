<div id="latestVersion" class="text-end">
  <b>This is the most recent version of this ${recordType}</b>

  <span class="otherVersions">
    <span  class="dropdown-toggle" data-bs-toggle="dropdown" aria-expanded="false">
      (view other versions)
    </span>
    <ul class="dropdown-menu dropdown-menu-end">
        <#list rel_supersedes as item>
          <li><a class="dropdown-item" href="${item.href}">${item.title}</a></li>
        </#list>
    </ul>
  </span>

</div>
