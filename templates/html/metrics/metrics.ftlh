<#import "../skeleton.ftlh" as skeleton>


<@skeleton.master title="Metrics Report" catalogue=catalogues.defaultCatalogue()>
  <div id="metrics class="section">
    <div class="container">
      <h1>${catalogue} metrics report</h1>
      <form action="/${catalogue}/metrics" method="post">
        <div>
          <label for="startDate">From</label>
          <input type="date" id="startDate" name="startDate"/>
        </div>
        <div>
          <label for="endDate">To</label>
          <input type="date" id="endDate" name="endDate"/>
        </div>
        <div>
          <label for="orderBy">Order By</label>
          <select id="orderBy" name="orderBy">
            <option value="docId">Doc Id</option>
            <option value="views">Views</option>
            <option value="downloads">Downloads</option>
          </select>
        </div>
        <div>
          <label for="ordering">Ordering</label>
          <select id="ordering" name="ordering">
            <option value="ascending">Ascending</option>
            <option value="descending">Descending</option>
          </select>
        </div>
        <div>
          <label for="docId">Doc ID</label>
          <input type="text" id="docId" name="docId"/>
        </div>
        <div>
          <label for="recordType">Record Type</label>
          <div>
            <label><input type="checkbox" name="recordType" value="dataset"/> Dataset</label>
            <label><input type="checkbox" name="recordType" value="datasetNonGeographic"/> Dataset (non geographic)</label>
            <label><input type="checkbox" name="recordType" value="modelCodeApplication"/> Model code (application)</label>
          </div>
        </div>
        <div>
          <label for="noOfRecords">No. of records</label>
          <input type="number" id="noOfRecords" name="noOfRecords" value=100>
        </div>
        <div>
          <input type="submit" value="Submit"/>
        </div>
      </form>

      <#if metricsReport??>
        <table>
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
          <#list metricsReport as record>
            <tr>
              <td>${record["document"]}</td>
              <td>${record["doc_title"]}</td>
              <td>${record["record_type"]}</td>
              <td>${record["views"]}</td>
              <td>${record["downloads"]}</td>
            </tr>
          </#list>
          </tbody>
        </table>
      </#if>
    </div>
  </div>
</@skeleton.master>

