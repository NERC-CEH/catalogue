<#import "../skeleton.ftlh" as skeleton>

<@skeleton.master title="Metrics Report" catalogue=catalogues.defaultCatalogue()>
  <div id="metrics-report" class="container">
    <h1>${catalogue?upper_case} Metrics Report</h1>
    <form id="metrics-form" action="/${catalogue}/metrics">
      <h2>Search Criteria</h2>
      <fieldset>
        <div class="row">
          <div class="col-md-6">
            <div class="form-group">
              <label for="startDate">From</label>
              <input type="date" id="startDate" name="startDate" class="form-control"/>
            </div>
          </div>
          <div class="col-md-6">
            <div class="form-group">
              <label for="endDate">To</label>
              <input type="date" id="endDate" name="endDate" class="form-control"/>
            </div>
          </div>
        </div>
        <div class="row">
          <div class="col-md-6">
            <div class="form-group">
              <label for="orderBy">Order By</label>
              <select id="orderBy" name="orderBy" class="form-control">
                <option value="docId">Document ID</option>
                <option value="downloads" selected>Downloads</option>
                <option value="views">Views</option>
              </select>
            </div>
          </div>
          <div class="col-md-6">
            <div class="form-group">
              <label for="ordering">Ordering</label>
              <select id="ordering" name="ordering" class="form-control">
                <option value="ascending">Ascending</option>
                <option value="descending" selected>Descending</option>
              </select>
            </div>
          </div>
        </div>
        <div class="row">
          <div class="col-md-6">
            <div class="form-group">
              <label for="docId">Document ID</label>
              <input type="text" id="docId" name="docId" class="form-control"/>
            </div>
          </div>
          <div class="col-md-6">
            <div class="form-group">
              <label for="recordType">Type</label>
              <div class="checkbox">
                <label><input type="checkbox" name="recordType" value="dataset">dataset</label>
              </div>
              <div class="checkbox">
                <label><input type="checkbox" name="recordType" value="datasetNonGeographic">dataset (non geographic)</label>
              </div>
              <div class="checkbox">
                <label><input type="checkbox" name="recordType" value="modelCodeApplication">model code (application)</label>
              </div>
            </div>
          </div>
        </div>
        <div class="row">
          <div class="col-md-6">
            <div class="form-group">
              <label for="noOfRecords">Maximum number of documents to show</label>
              <input type="number" id="noOfRecords" name="noOfRecords" value="100" class="form-control"/>
            </div>
          </div>
        </div>
        <div class="row">
          <div class="col-md-10">
            <button type="submit" class="btn btn-primary">&nbsp; &nbsp; &nbsp;OK&nbsp; &nbsp; &nbsp;</button>
          </div>
        </div>
      </fieldset>
    </form>

    <div id="metrics-results-container">
      <h2>Results</h2>
      <div class="table-responsive">
        <table id="metrics-table" class="table table-bordered">
          <thead>
            <tr>
              <th>Document ID</th>
              <th>Title</th>
              <th>Type</th>
              <th>Views</th>
              <th>Downloads</th>
            </tr>
          </thead>
          <tbody>
            <#list metricsReport![] as record>
              <tr>
                <td>${record["document"]}</td>
                <td>${record["docTitle"]}</td>
                <td>${record["recordType"]}</td>
                <td>${record["views"]}</td>
                <td>${record["downloads"]}</td>
              </tr>
            </#list>
          </tbody>
        </table>
      </div>
  </div>
</div>
</@skeleton.master>
