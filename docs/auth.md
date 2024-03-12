# Authentication & Authorization Options in Catalogue

## Options available:

- Crowd
- DataLabs
- Development
- OIDC

### Crowd

Uses the UKCEH Crowd server for authentication and authorization.

Activate by adding "auth:crowd" to Spring profiles

### DataLabs

Used for the deployment of the catalogue into DataLabs.

Activate by adding "auth:datalabs" to Spring profiles

### Development

For local development

Activate by adding "auth:crowd, development" to Spring profiles

### OIDC

OIDC OAuth2 authentication and a local json file for authorization.
Catalogue can be deployed independently of the UKCEH Crowd server.

Activate by adding "auth:oidc" to Spring profiles
