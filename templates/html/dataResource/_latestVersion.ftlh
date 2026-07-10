<div id="latestVersion" class="alert alert-secondary">
  <div class="d-flex justify-content-between">
    <div>
      <b>This is the most recent version of this ${recordType}</b>
    </div>
    <div>
      <span class="otherVersions">
        <span  class="dropdown-toggle" data-bs-toggle="dropdown" aria-expanded="false">
          (view other versions)
        </span>
        <ul class="dropdown-menu dropdown-menu-end">
            <#list relReplaces as item>
              <li><a class="dropdown-item" href="${item.href}">${item.title}</a></li>
            </#list>
        </ul>
      </span>
    </div>
  </div>
</div>
