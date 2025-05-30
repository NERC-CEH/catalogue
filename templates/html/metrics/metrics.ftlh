<#import "../skeleton.ftlh" as skeleton>

<@skeleton.master title="Metrics Report" catalogue=catalogues.defaultCatalogue()>
  <div id="metrics-report" class="container">
    <h1 class="my-5">${catalogue?upper_case} downlaods and views</h1>
    <form id="metrics-form" action="/${catalogue}/metrics">
      <h2 class="m-3">Search criteria</h2>
        <div class="row p-3">
          <div class="col-md-6">
            <div class="form-floating">
              <input type="text" id="docId" name="docId" class="form-control"/>
              <label for="docId">Document ID</label>
            </div>
          </div>
          <div class="col-md-6">
            <label>Type</label>
            <div class="ms-3 mt-1">
              <div class="form-check">
                <input class="form-check-input" type="checkbox" value="dataset" id="recordTypeDataset" name="recordType">
                <label class="form-check-label" for="recordTypeDataset">Dataset</label>
              </div>
              <div class="form-check">
                <input class="form-check-input" type="checkbox" value="nonGeographicDataset" id="recordTypeNonGeographic" name="recordType">
                <label class="form-check-label" for="recordTypeNonGeographic">Dataset (non geographic)</label>
              </div>
              <div class="form-check">
                <input class="form-check-input" type="checkbox" value="application" id="recordTypeModel" name="recordType">
                <label class="form-check-label" for="recordTypeModel">Model code (application)</label>
              </div>
            </div>
          </div>
        </div>
        <div class="row p-3">
          <div class="col-md-6">
            <div class="form-floating">
              <input type="date" id="startDate" name="startDate" class="form-control"/>
              <label for="startDate">From</label>
            </div>
          </div>
          <div class="col-md-6">
            <div class="form-floating">
              <input type="date" id="endDate" name="endDate" class="form-control"/>
              <label for="endDate">To</label>
            </div>
          </div>
        </div>
        <div class="row p-3">
          <div class="col-md-6">
            <div class="form-floating">
              <select id="orderBy" name="orderBy" class="form-select">
                <option value="docId">Document ID</option>
                <option value="downloads" selected>Downloads</option>
                <option value="views">Views</option>
              </select>
              <label for="orderBy">Order By</label>
            </div>
          </div>
          <div class="col-md-6">
            <div class="form-floating">
              <select id="ordering" name="ordering" class="form-select">
                <option value="ascending">Ascending</option>
                <option value="descending" selected>Descending</option>
              </select>
              <label for="ordering">Ordering</label>
            </div>
          </div>
        </div>
        <div class="row p-3">
          <div class="col-md-6">
            <div class="form-floating">
              <input type="number" id="noOfRecords" name="noOfRecords" value="100" class="form-control"/>
              <label for="noOfRecords">Maximum number of documents to show</label>
            </div>
          </div>
        </div>
        <div class="row p-3">
          <div class="d-grid gap-2 mx-auto col-md-3 col-sm-6 col-xs-12">
            <button type="submit" class="btn btn-primary">OK</button>
          </div>
        </div>
        </div>
    </form>

    <div id="metrics-results-container" class="mt-5">
      <h2 class="my-3">Results</h2>
      <div class="table-responsive">
        <table id="metrics-table" class="table table-bordered table-hover">
          <thead>
            <tr class="table-light">
              <th class="text-nowrap">Document ID</th>
              <th class="text-nowrap">Title</th>
              <th class="text-nowrap">Type</th>
              <th class="text-nowrap">Views</th>
              <th class="text-nowrap">Downloads</th>
            </tr>
          </thead>
          <tbody  class="table-group-divider">
            <#list metricsReport![] as record>
              <tr>
                <td class="text-nowrap">${record["document"]}</td>
                <td>${record["docTitle"]}</td>
                <td class="text-nowrap">${record["recordType"]}</td>
                <td class="text-nowrap">${record["views"]}</td>
                <td class="text-nowrap">${record["downloads"]}</td>
              </tr>
            </#list>
          </tbody>
        </table>
      </div>
  </div>
</div>
</@skeleton.master>
