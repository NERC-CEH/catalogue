<div class="layerInfo">
  <h2>Legend</h2>
  <div class="legendContainer">
    <img src="<%=getLegend()%>"/>
  </div>
  
  <h2>Online Resource</h2>
  <a href="<%=onlineResource.get('url')%>"><%=onlineResource.get('name')%></a>

  <h2>Metadata Record</h2>
  <a href="<%=onlineResource.collection.metadataDocument.url()%>">
    <%=onlineResource.collection.metadataDocument.get('title')%>
  </a>
</div>