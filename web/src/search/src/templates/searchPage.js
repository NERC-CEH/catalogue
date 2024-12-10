import _ from 'underscore'

/*
 * This underscore template generates the search results client side.
 *
 * IMPORTANT: If you change the structure of this, please update the
 * corresponding freemarker template /templates/search/_page.ftl
 */
export default _.template(`
<div class="row justify-content-between results__header">
    <div class="col-sm-4">
      <span id="num-records"><%=numFound%></span> records found
      <span class="mx-2">
        <i id="searchShareIcon" class="fas fa-share-square" data-bs-toggle="tooltip" data-bs-placement="right" role="button" title="copy to clipboard"></i>
      </span>
    </div>
    <div class="col-sm-4">
        <select class="form-select sort-search" aria-label="Select Dropdown for Sorting Search results">
            <option value="" <%= !sortField ? 'selected' : '' %>>Relevance</option>
            <option value="publicationDate-desc" <%= sortField === 'publicationDate' && order === 'desc' ? 'selected' : '' %>>Published Date (Newest First)</option>
            <option value="publicationDate-asc" <%= sortField === 'publicationDate' && order === 'asc' ? 'selected' : '' %>>Published Date (Oldest First)</option>
            <option value="title-asc" <%= sortField === 'title' && order === 'asc' ? 'selected' : '' %>>Title (A-Z)</option>
            <option value="title-desc" <%= sortField === 'title' && order === 'desc' ? 'selected' : '' %>>Title (Z-A)</option>
        </select>
    </div>
</div>

<div class="results__related_searches"></div>

<div class="results__list">
<% _.each(results, function(result) { %>

    <a class="result result--<%=result.state%> <% if (result.operationalStatus != '') { %>opstatus-<%=result.operationalStatus%><% } %> <% if (result.resourceStatus != '') { %>result--<%=result.resourceStatus%><% } %>" id="<%=result.identifier%>" href="/documents/<%=result.identifier%>">

        <div class="result__tags">
            <div class="state">
                <% if(result.state == 'draft') { %>
                    <span class="text-draft"><b>DRAFT</b></span>
                <% } else if(result.state == 'pending') { %>
                    <span class="text-pending"><b>PENDING PUBLICATION</b></span>
                <% } %>
            </div>

            <% if (result.operationalStatus != '') {  %>
                <div class="opstatus"><%=result.operationalStatus%></div>
            <% } %>

            <div class="recordType">
            <% if (result.documentType != '' && result.documentType == "LINK_DOCUMENT") {  %>
            <i class="fa-solid fa-link"></i> Linked
            <% } %>
                <span><%=result.recordType%></span>
            </div>

            <% if (result.resourceStatus != '') {  %>
                <div class="resourceStatus"><%=result.resourceStatus%></div>
            <% } %>

        </div>

        <div class="result__title">
          <%=result.title%>
        </div>

        <div class="result__description"><%=result.shortenedDescription%></div>

        <% if(result.incomingCitationCount != 0) { %>
            <div class="result__citationCount"><%=result.incomingCitationCount%> citation<% if(result.incomingCitationCount >1) { %>s<% } %></div>
        <% } %>

    </a>
<% }); %>
</div>

<div class="results__footer">
<ul class="pagination">
    <% if(prevPage) { %>
        <li class="page-item previous-item"><a class="page-link" href="<%=prevPage%>">&larr; Previous</a></li>
    <% } %>
    <li class="page-item center-item">Page <%=page%></li>
    <% if(nextPage) { %>
        <li class="page-item next-item"><a class="page-link" href="<%=nextPage%>">Next &rarr;</a></li>
    <% } %>
</ul>
</div>
`)
