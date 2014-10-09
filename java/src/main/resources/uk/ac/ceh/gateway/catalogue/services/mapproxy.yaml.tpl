services:
  tms:

layers:
<#list capabilities.layers as layer>
  - name: '${layer.name}'
    title: ${layer.title}
    sources: ['cache_${id}_${layer.name}']
</#list>

caches:
<#list capabilities.layers as layer>
  'cache_${id}_${layer.name}':
    grids: [webmercator]
    sources: ['layer_${id}_${layer.name}']
    <#-- 
        BUG: We are requesting only 1 meta tile per wms request to keep wms 
        request sizes below 2048x2048 pixels. Can we guarantee that requests 
        will always be less than 2048 pixels in this scenario?
    -->
    meta_size: [1, 1]
</#list>

sources:
<#list capabilities.layers as layer>
  'layer_${id}_${layer.name}':
    type: wms
    supported_srs: ['EPSG:4326']
    req:
      url: '${capabilities.directMap}'
      layers: '${layer.name}'
      transparent: true
</#list>

grids:
  webmercator:
    base: GLOBAL_MERCATOR
