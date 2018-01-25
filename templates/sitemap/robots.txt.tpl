User-agent: *
Disallow: /static/
Disallow: /datastore/
Disallow: /history/
Disallow: /maintenance/
Disallow: /maps/
Disallow: /upload/
<#list catalogues as catalogue>
Allow: /${catalogue}/
</#list>

<#list catalogues as catalogue>
Sitemap: ${baseUri}/${catalogue}/sitemap.txt
</#list>