import _ from 'underscore'

export default _.template(`
<div class="input-group">
  <input type="text" class="form-control" value="<%= data.type %>" <%= data.disabled%>/>
  <div class="input-group-btn">
    <button type="button" class="btn btn-light border dropdown-toggle" data-bs-toggle="dropdown" aria-haspopup="true" aria-expanded="false" <%= data.disabled%>>
      <span class="selected icon"></span>
    </span></button>
    <ul class="dropdown-menu dropdown-menu-end">
      <li><a class="dropdown-item" href="#" data-symbol="blank">Polygon</a></li>
      <li class="dropdown-divider"></li>
      <% _.chain(data.symbols).each(function(s, id){%>
        <li><a class="dropdown-item" href="#" data-symbol="<%=id%>"><span class="icon"><%=s.icon%></span> <%=s.label%></a></li>
      <%})%>
    </ul>
  </div>
</div>
<div class="input-group">
  <input type="color" id="picker" style="background-color: white;float: right;width: 150px;">
</div>
`)
