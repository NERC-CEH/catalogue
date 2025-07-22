  <#if lcmUrl??>
    <div class="d-flex justify-content-end mt-2">
      <a href="${lcmUrl}" target="_blank" class="btn btn-sm btn-light border text-decoration-none">
        <span class="me-1">Open in new tab</span>
        <i class="fa-solid fa-up-right-from-square"></i>
      </a>
    </div>
    <iframe src="${lcmUrl}" style="width: 100%; height: 700px;" title="Spatial Explorer" scrolling="no"></iframe>
  </#if>

