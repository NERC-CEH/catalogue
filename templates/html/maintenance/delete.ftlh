<#import "../skeleton.ftlh" as skeleton>
<@skeleton.master title="Delete a record" catalogue=catalogues.defaultCatalogue()>
  <div class="container">
    <h1 class="my-4">Delete a record</h1>

    <p class="text-body-secondary">
      Deletes any record in any catalogue, ignoring that record's own permissions. Intended for records
      orphaned by a retired document type or catalogue. Every deletion is recorded in the datastore's git
      history with the reason given below.
    </p>

    <#if error??>
      <div class="alert alert-danger">${error}</div>
    </#if>

    <#if messages?has_content>
      <div class="alert alert-success">
        <ol class="mb-0">
          <#list messages as message><li>${message}</li></#list>
        </ol>
      </div>
    </#if>

    <#-- STEP 1: look a record up. Always shown, so another record can be dealt with straight after one
         has been deleted. -->
    <form method="POST" action="/maintenance/documents/delete/preview" class="my-4">
      <input type="hidden" name="${csrfParameterName}" value="${csrfToken}">

      <div class="mb-3">
        <label class="form-label" for="location">Location</label>
        <select class="form-select" id="location" name="location">
          <#list locations as loc>
            <option value="${loc}"<#if location?? && location == loc> selected</#if>>${loc.label}</option>
          </#list>
        </select>
      </div>

      <div class="mb-3">
        <label class="form-label" for="id">Record id</label>
        <input class="form-control" id="id" name="id" value="${id!''}"
               placeholder="00000000-0000-0000-0000-000000000000" required>
      </div>

      <button type="submit" class="btn btn-outline-secondary">Look up</button>
    </form>

    <#-- STEP 2: confirm. Only reachable once a lookup has found something, so the operator has seen what
         they are about to destroy. -->
    <#if found>
      <hr class="my-4">

      <h2 class="mb-3">Review before deleting</h2>

      <#if published>
        <div class="alert alert-warning">
          <strong>This record is published.</strong> Deleting it removes it from the public catalogue.
          Type the catalogue id below to confirm you intend this.
        </div>
      </#if>

      <#if !documentTypeRegistered>
        <div class="alert alert-info">
          This record's document type is not registered, so the application cannot read it. This is the
          orphaned state that this page exists to clear up.
        </div>
      </#if>

      <dl class="row">
        <dt class="col-sm-3">Path</dt><dd class="col-sm-9"><code>${path}</code></dd>
        <dt class="col-sm-3">Document type</dt>
        <dd class="col-sm-9">
          ${documentType!'(none)'}
          <#if !documentTypeRegistered><span class="badge text-bg-warning">not registered</span></#if>
        </dd>
        <dt class="col-sm-3">State</dt><dd class="col-sm-9">${state!'(none)'}</dd>
        <dt class="col-sm-3">Catalogue</dt><dd class="col-sm-9">${catalogue!'(none)'}</dd>
        <dt class="col-sm-3">Body (.raw)</dt>
        <dd class="col-sm-9">
          <#if rawPresent>present (${rawSize} bytes)<#else><em>missing</em></#if>
        </dd>
        <dt class="col-sm-3">Permissions</dt><dd class="col-sm-9">${permissions!''}</dd>
      </dl>

      <form method="POST" action="/maintenance/documents/delete/confirm" class="my-4">
        <input type="hidden" name="${csrfParameterName}" value="${csrfToken}">
        <input type="hidden" name="location" value="${location}">
        <input type="hidden" name="id" value="${id}">

        <div class="mb-3">
          <label class="form-label" for="confirmId">Retype the record id to confirm</label>
          <input class="form-control" id="confirmId" name="confirmId" autocomplete="off" required>
        </div>

        <#if published>
          <div class="mb-3">
            <label class="form-label" for="confirmCatalogue">
              Type the catalogue id (<code>${catalogue}</code>) to confirm deleting a published record
            </label>
            <input class="form-control" id="confirmCatalogue" name="confirmCatalogue" autocomplete="off" required>
          </div>
        </#if>

        <div class="mb-3">
          <label class="form-label" for="reason">Reason (recorded in the git commit)</label>
          <input class="form-control" id="reason" name="reason" required
                 placeholder="e.g. orphaned by removal of the elter catalogue">
        </div>

        <button type="submit" class="btn btn-danger">Delete permanently</button>
        <a class="btn btn-link" href="/maintenance">Cancel</a>
      </form>
    </#if>

    <#if !found>
      <p><a href="/maintenance">Back to maintenance</a></p>
    </#if>
  </div>
</@skeleton.master>
