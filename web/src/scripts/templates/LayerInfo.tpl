<div class="layerInfo">
  <h2>Legend</h2>
  <div class="legendContainer">
    <div><%=onlineResource.collection.metadataDocument.get('title')%></div>
    <div><img src="<%=getLegend()%>"/></div>
  </div>
  
  <p><a href="<%=onlineResource.get('url')%>">Request</a></p>

  <p><a href="<%=onlineResource.collection.metadataDocument.url()%>">Back to metadata record</a></p>
</div>