<#import "skeleton.html.tpl" as skeleton>
<@skeleton.master title="Maintenance Page">
  <h1>Catalogue Maintenance</h1>

  <h2>Upload a terraCatalog export</h2>
  <form method="POST" enctype="multipart/form-data" action="/maintenance/terraCatalogImport">
    TerraCatalog Export: <input type="file" name="file">
    <br />
    <input type="submit" value="Import">
  </form>
</@skeleton.master>
