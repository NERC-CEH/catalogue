import _ from 'underscore'

/*
 * This underscore template generates the search results client side.
 *
 * IMPORTANT: If you change the structure of this, please update the
 * corresponding freemarker template /templates/search/_page.ftl
 */
export default _.template(`

<div class="d-flex p-1 mx-3 results__header align-items-center justify-content-between">
   <% if(numFound > 0) { %>
    <div class="small">
      <span id="num-records"><%=numFound%></span> record<% if(numFound > 1) { %>s<% } %> found
      <i id="searchShareIcon" class="fa-solid fa-fw fa-share-square mx-1" data-bs-toggle="tooltip" data-bs-placement="right" role="button" title="Email search results"></i>
    </div>
    <div class="small">
      <label for="sort-search">Sort by</label>
        <select class="sort-search" id="sort-search" aria-label="Select dropdown for sorting serch results">
            <option value="" <%= !sortField ? 'selected' : '' %>>Relevance</option>
            <option class="option-eidc" value="publicationDate-desc" <%= sortField === 'publicationDate' && order === 'desc' ? 'selected' : '' %>>Published date (newest first)</option>
            <option class="option-eidc" value="publicationDate-asc" <%= sortField === 'publicationDate' && order === 'asc' ? 'selected' : '' %>>Published date (oldest first)</option>
            <option class="option-eidc" value="incomingCitationCount-desc" <%= sortField === 'incomingCitationCount' && order === 'asc' ? 'selected' : '' %>>Number of citatons</option>
            <option value="title-asc" <%= sortField === 'title' && order === 'asc' ? 'selected' : '' %>>Title (A-Z)</option>
            <option value="title-desc" <%= sortField === 'title' && order === 'desc' ? 'selected' : '' %>>Title (Z-A)</option>
        </select>
    </div>
  <% } else { %>
    <div>No results found. Try <a href="./documents">clearing all search filters</a></div>
  <% } %>
</div>


<div class="results__related_searches"></div>

<div class="results__list">
<% _.each(results, function(result) { %>

    <a class="result result--<%=result.state%> <% if (result.operationalStatus != '') { %>opstatus-<%=result.operationalStatus%><% } %> <% if (result.resourceStatus != '') { %>result--<%=result.resourceStatus%><% } %>" id="<%=result.identifier%>" href="/documents/<%=result.identifier%>">

        <div class="result__state">
            <% if(result.state == 'draft') { %>
                <span>DRAFT</span>
            <% } else if(result.state == 'pending') { %>
                <span>PENDING PUBLICATION</span>
            <% } %>
        </div>

        <div>

          <div>
            <% if (result.operationalStatus != '') {  %>
              <span class="opstatus"><%=result.operationalStatus%></span>
            <% } %>

            <span class="recordType text-body-tertiary">
              <% if (result.documentType != '' && result.documentType == "LINK_DOCUMENT") {  %>
                <i class="fa-solid fa-link"></i> Linked
              <% } %>
              <%=result.recordType %>
            </span>

            <% if (result.resourceStatus != '') {  %>
              <span class="resourceStatus resourceStatus-<%=result.resourceStatus %>"><%=result.resourceStatus %></span>
            <% } %>
          </div>
          <div class="result__title"><%=result.title%></div>
          <div class="result__description"><%=result.shortenedDescription%></div>
        </div>

        <% if(result.incomingCitationCount != 0) { %>
            <div class="result__citationCount"><%=result.incomingCitationCount%> citation<% if(result.incomingCitationCount >1) { %>s<% } %></div>
        <% } %>

    </a>
<% }); %>
</div>

<div class="results__footer">
 <% if(prevPage || nextPage) { %>
  <ul class="pagination">
      <% if(prevPage) { %>
          <li class="page-item previous-item"><a class="page-link" href="<%=prevPage%>">&larr; Previous</a></li>
      <% } %>
      <li class="page-item center-item">Page <%=page%></li>
      <% if(nextPage) { %>
          <li class="page-item next-item"><a class="page-link" href="<%=nextPage%>">Next &rarr;</a></li>
      <% } %>
  </ul>
<% } %>
</div>
`)
