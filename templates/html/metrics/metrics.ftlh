<#import "../skeleton.ftlh" as skeleton>

<@skeleton.master title="Metrics Reports" catalogue=catalogues.defaultCatalogue()>
  <div class="container">
    <h1 class="text-center">${catalogue?upper_case} Metrics Reports</h1>
    <form id="metricsForm" action="/${catalogue}/metrics" method="post" style="border: 1px solid black; padding: 1rem">
      <fieldset>
        <legend>Search Criteria</legend>
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
                <option value="docId">Doc Id</option>
                <option value="views">Views</option>
                <option value="downloads">Downloads</option>
              </select>
            </div>
          </div>
          <div class="col-md-6">
            <div class="form-group">
              <label for="ordering">Ordering</label>
              <select id="ordering" name="ordering" class="form-control">
                <option value="ascending">Ascending</option>
                <option value="descending">Descending</option>
              </select>
            </div>
          </div>
        </div>
        <div class="row">
          <div class="col-md-6">
            <div class="form-group">
              <label for="docId">Doc ID</label>
              <input type="text" id="docId" name="docId" class="form-control"/>
            </div>
          </div>
          <div class="col-md-6">
            <div class="form-group">
              <label for="recordType">Type</label>
              <div class="checkbox">
                <label><input type="checkbox" name="recordType" value="dataset"> dataset</label>
              </div>
              <div class="checkbox">
                <label><input type="checkbox" name="recordType" value="datasetNonGeographic"> dataset (non geographic)</label>
              </div>
              <div class="checkbox">
                <label><input type="checkbox" name="recordType" value="modelCodeApplication"> model code (application)</label>
              </div>
            </div>
          </div>
        </div>
        <div class="row">
          <div class="col-md-6">
            <div class="form-group">
              <label for="noOfRecords">No. of records</label>
              <input type="number" id="noOfRecords" name="noOfRecords" value="100" class="form-control"/>
            </div>
          </div>
          <div class="col-md-6">
            <div class="form-group" style="text-align: center; margin-top: 25px;">
              <button type="submit" class="btn btn-primary">Submit</button>
            </div>
          </div>
        </div>
      </fieldset>
    </form>


    <div id="metricsResultsContainer" style="display: none;">
      <fieldset id="metricsResult" style="margin-top: 2rem">
        <legend>Search Result</legend>
        <div class="table-responsive">
          <table id="metricsTable" class="table table-striped table-bordered">
            <thead>
              <tr>
                <th>Doc Id</th>
                <th>Doc Title</th>
                <th>Type</th>
                <th>Views</th>
                <th>Downloads</th>
              </tr>
            </thead>
            <tbody>

            </tbody>
          </table>
        </div>
      </fieldset>
    </div>

  </div>
</@skeleton.master>
