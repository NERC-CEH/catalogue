<label for="input<%= id %>" class="control-label <%= required %>"><%= name %>
<% if (help !== '') { %>
  <a data-toggle="collapse" title="Click for help" href="#help<%= id %>" data-parent="#editor"><i class="glyphicon glyphicon-question-sign"></i></a>
<% } %>
</label>
<textarea rows="<%= rows %>" class="form-control" id="input<%= id %>"><%= value %></textarea>
<% if (help !== '') { %>
  <div id="help<%= id %>" class="help-block hidden-print collapse"><%= help %></div>
<% } %>