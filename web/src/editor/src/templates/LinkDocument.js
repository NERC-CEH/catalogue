import _ from 'underscore'

export default _.template(`
<h2>
  <a href="/documents/<%=identifier%>" target="_new"><%=title%></a> <small><span class="fa-solid fa-external-link-alt" aria-hidden="true"></span></small>
</h2>
<div class="row">
  <div class="col-sm-10 col-lg-10"><%=shortenedDescription%></div>
  <div class="col-sm-2 col-lg-2">
   <button class="btn btn-success">Select</button>
  </div>
</div>
`)
