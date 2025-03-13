# Authentication & Authorization Options in Catalogue

## Options available:

- Crowd
- DataLabs
- Development
- OIDC

### Crowd

Uses the UKCEH Crowd server for authentication and authorization.

Activate by adding "auth:crowd" to Spring profiles

#### application.properties

- crowd.address
- crowd.username
- crowd.password

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

[Spring Boot security configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/web.html#web.security.oauth2)

#### auth.env

Example for `example` catalogue. In a production system these values would be provided as environment
variables. `.example.` and `.example-oauth-provider.` can be replaced with what ever values you want.

```properties
spring.security.oauth2.client.registration.example.client-id=DnAHS5BRmrsdyHnQslzhNFdqk8jkp7R9
spring.security.oauth2.client.registration.example.client-secret=<<from Auth0>>
spring.security.oauth2.client.registration.example.client-name=Example Auth0 Client
spring.security.oauth2.client.registration.example.provider=example-oauth-provider
spring.security.oauth2.client.registration.example.scope=openid,email
spring.security.oauth2.client.registration.example.redirect-uri=https://catalogue.ceh.ac.uk/login/oauth2/code/example
spring.security.oauth2.client.registration.example.client-authentication-method=client_secret_basic
spring.security.oauth2.client.registration.example.authorization-grant-type=authorization_code
spring.security.oauth2.client.provider.example-oauth-provider.authorization-uri=https://example-web.eu.auth0.com/authorize
spring.security.oauth2.client.provider.example-oauth-provider.token-uri=https://example-web.eu.auth0.com/oauth/token
spring.security.oauth2.client.provider.example-oauth-provider.user-info-uri=https://example-web.eu.auth0.com/userinfo
spring.security.oauth2.client.provider.example-oauth-provider.user-info-authentication-method=header
spring.security.oauth2.client.provider.example-oauth-provider.jwk-set-uri=https://example-web.eu.auth0.com/.well-known/jwks.json
spring.security.oauth2.client.provider.example-oauth-provider.user-name-attribute=email
```

#### docker-compose.yml

For development can be added to the docker compose file

```yaml
services:
  web:
    env_file:
      - auth.env
```

#### Authorization

Roles are looked up from a local json file. This file needs to be mounted in to the Docker container at `/var/ceh-catalogue/oidc/roles.json`.
This location can be changed in the `auth.oidc.roles.location` property.

The key is the authenticated user's email address and then a list of roles.


```json
{
    "admin1@example.com": ["ROLE_EXAMPLE_EDITOR", "ROLE_EXAMPLE_PUBLISHER", "ROLE_CIG_SYSTEM_ADMIN"],
    "publisher1@example.com": ["ROLE_EXAMPLE_EDITOR", "ROLE_EXAMPLE_PUBLISHER"],
    "publisher2@example.com": ["ROLE_EXAMPLE_EDITOR", "ROLE_EXAMPLE_PUBLISHER"],
    "editor1@example.com": ["ROLE_EXAMPLE_EDITOR"],
    "editor2@example.com": ["ROLE_EXAMPLE_EDITOR"]
}
```
