import _ from 'underscore'

export default _.template(`
<div id="map-style-selector" class="input-group">
  <input id="colorCode" type="text" class="form-control" value="<%= data.type %>" <%= data.disabled%>/>
  <div class="input-group-btn">
    <button type="button" class="btn btn-outline-secondary dropdown-toggle" data-bs-toggle="dropdown" aria-haspopup="true" aria-expanded="false" <%= data.disabled%>>
      <span class="selected"></span>
    </button>
    <ul class="dropdown-menu dropdown-menu-end">
      <li><a class="dropdown-item" href="#" data-symbol="blank">Polygon</a></li>
      <li class="dropdown-divider"></li>
      <% _.chain(data.symbols).each(function(s, id){%>
        <li><a class="dropdown-item" href="#" data-symbol="<%=id%>"><span class="icon--margin-right"><%=s.icon%></span><%=s.label%></a></li>
      <%})%>
    </ul>
  </div>
</div>
<div class="input-group">
  <input type="color" id="picker" class="form-control form-control-color">
</div>
`)
