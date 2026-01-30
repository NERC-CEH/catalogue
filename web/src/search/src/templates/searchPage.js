import _ from 'underscore'

/*
 * This underscore template generates the search results client side.
 *
 * IMPORTANT: If you change the structure of this, please update the
 * corresponding freemarker template /templates/search/_page.ftl
 */
export default _.template(`

<div class="d-flex p-1 m-3 mt-0 results__header align-items-center justify-content-between">
   <% if(numFound > 0) { %>
    <div class="small">
      <%
        const startItem = (page - 1) * rows + 1;
        const endItem = Math.min(page * rows, numFound);
      %>
      <span id="num-records" class="recordCount">
        Showing <span class="recordCount-start"><%=startItem%></span> - <span class="recordCount-end"><%=endItem%></span>  of <span class="recordCount-total"><%=numFound%></span> records
      </span>
      <i id="searchShareIcon" class="ms-3 fa-solid fa-fw fa-share-square mx-1" data-bs-toggle="tooltip" data-bs-placement="right" role="button" title="Email search results"></i>
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

  <%
    const statusValue = result.availability || result.operationalStatus;
    const status = (typeof statusValue === 'string' && statusValue.trim() !== '')
      ? statusValue.toLowerCase()
      : 'unknown';
  %>

    <a class="result result--<%=result.state%> result--<%=status%>" id="<%=result.identifier%>" href="/documents/<%=result.identifier%>">

        <div class="result__publicationState">
            <% if(result.state == 'draft') { %>
                DRAFT
            <% } else if(result.state == 'pending') { %>
                PENDING PUBLICATION
            <% } %>
        </div>

        <div>

          <div class="result__tags">
            <% if (result.operationalStatus != '') {  %>
              <span class="opstatus"><%=result.operationalStatus%></span>
            <% } %>

            <span class="recordType small text-body-tertiary">
              <% if (result.documentType != '' && result.documentType == "LINK_DOCUMENT") {  %>
                <i class="fa-solid fa-link"></i> Linked
              <% } %>
              <%=result.recordType %>
            </span>

            <% if (result.availability != '') {  %>
              <span class="availability availability-<%=result.availability %>"><%=result.availability %></span>
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
  <% const totalPage = Math.ceil(numFound / rows); %>
  <% if(totalPage > 1) { %>
    <%
      const pageToShow = 5;
      const halfPageToShow = Math.floor(pageToShow / 2);
      let startPage = (page - halfPageToShow > 0) ? page - halfPageToShow : 1;
      let endPage = (startPage + pageToShow - 1 < totalPage) ? startPage + pageToShow - 1 : totalPage;
      if (endPage - startPage + 1 < pageToShow && startPage > 1) {
        startPage = Math.max(1, endPage - pageToShow + 1);
      }
    %>

    <nav aria-label="Search results pagination">
      <ul class="pagination mb-0">
        <% if(page > 1) { %>
          <li class="page-item">
            <a class="page-link" href="<%=prevPage%>" aria-label="Previous">
              <span aria-hidden="true">&laquo;</span>
            </a>
          </li>
        <% } else { %>
          <li class="page-item disabled">
            <span class="page-link" aria-hidden="true">&laquo;</span>
          </li>
        <% } %>

        <% for (let i = startPage; i <= endPage; i++) { %>
          <% if (i === page) { %>
            <li class="page-item active " aria-current="page">
              <span class="page-link"><%=i%></span>
            </li>
          <% } else { %>
            <li class="page-item">
              <a class="page-link" href="<%= buildPageUrl(url, page, i) %>"><%=i%></a>
            </li>
          <% } %>
        <% } %>

        <% if(page < totalPage) { %>
          <li class="page-item">
            <a class="page-link" href="<%=nextPage%>" aria-label="Next">
              <span aria-hidden="true">&raquo;</span>
            </a>
          </li>
        <% } else { %>
          <li class="page-item disabled">
            <span class="page-link" aria-hidden="true">&raquo;</span>
          </li>
        <% } %>
      </ul>
    </nav>
  <% } %>
</div>

<%
  function buildPageUrl(url, currentPage, pageNum) {
    if (url.indexOf('page=') !== -1) {
      return url.replace('page=' + currentPage, 'page=' + pageNum);
    } else {
      return (url.indexOf('?') !== -1) ? url + '&page=' + pageNum : url + '?page=' + pageNum;
    }
  }
%>
`)
