services:
  tms:

layers:
<#list layers as layer>
  - name: '${layer.name}'
    title: ${layer.title}
    sources: ['cache_${layer.name}']
</#list>

caches:
<#list layers as layer>
  'cache_${layer.name}':
    grids: [webmercator]
    sources: ['layer_${layer.name}']
    meta_size: [1, 1]
</#list>

sources:
<#list layers as layer>
  'layer_${layer.name}':
    type: wms
    supported_srs: ['EPSG:4326']
    req:
      url: '${directMap}'
      layers: '${layer.name}'
      transparent: true
</#list>

grids:
  webmercator:
    base: GLOBAL_MERCATOR
