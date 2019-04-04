# ENDPOINTS
## How are all the endpoints wired up?

Proxy server endpoint management is wider than just catalogue endpoints, it manages access to THREDDS, apps (e.g. NFMPP, Atmospheric Portals) and any other service that needs protecting.

### {eip}/datastore/**

Goes to `:7102` the datastore Apache vhost on proxy server.

### {catalogue}/robots.txt, {catalogue}/**/sitemap.txt

Goes to `cig-prod:7100` (this catalogue), for search indexing

### {catalogue}/id/**

Goes to `cig-prod:7100` (this catalogue), metadata record id redirect

### {catalogue}/static/**

Goes to `cig-prod:7100` (this catalogue), static resources (css, js)

### {catalogue}/history/**

Goes to `cig-prod:7100` (this catalogue), view older versions of metadata record

### {catalogue}/*/documents/**

Goes to `cig-prod:7100` (this catalogue), search page of each catalogue

### {catalogue}/documents/** 

Goes to `cig-prod:7100` (this catalogue), metadata record

### {catalogue}/maintenance/**

Goes to `cig-prod:7100` (this catalogue), maintenance operations e.g. reindex

### {catalogue}/maps/**

Goes to `cig-prod:7100` (this catalogue), WMS services

### {catalogue}/upload/**

Goes to `cig-prod:7100` (this catalogue), upload datasets

### {catalogue}/*/quality/**

Goes to `cig-prod:7100` (this catalogue), quality

### {catalogue}/catalogues

Goes to `cig-prod:7100` (this catalogue), list of catalogues

### {catalogue}/**

Goes to `cig-prod:7200` (order manager), everthing to do with dataset ordering

### /**

Everthing else goes to the EIP Portal