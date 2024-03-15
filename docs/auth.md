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

Example for eLTER catalogue. In a production system these values would be provided as environment
variables. `.elter.` and `.elter-oauth-provider.` can be replaced with what ever values you want.

```properties
spring.security.oauth2.client.registration.elter.client-id=DnAHS5BRmrsdyHnQslzhNFdqk8jkp7R9
spring.security.oauth2.client.registration.elter.client-secret=<<from Auth0>>
spring.security.oauth2.client.registration.elter.client-name=eLTER Auth0 Client
spring.security.oauth2.client.registration.elter.provider=elter-oauth-provider
spring.security.oauth2.client.registration.elter.scope=openid,email
spring.security.oauth2.client.registration.elter.redirect-uri=https://catalogue.ceh.ac.uk/login/oauth2/code/elter
spring.security.oauth2.client.registration.elter.client-authentication-method=client_secret_basic
spring.security.oauth2.client.registration.elter.authorization-grant-type=authorization_code
spring.security.oauth2.client.provider.elter-oauth-provider.authorization-uri=https://elter-web.eu.auth0.com/authorize
spring.security.oauth2.client.provider.elter-oauth-provider.token-uri=https://elter-web.eu.auth0.com/oauth/token
spring.security.oauth2.client.provider.elter-oauth-provider.user-info-uri=https://elter-web.eu.auth0.com/userinfo
spring.security.oauth2.client.provider.elter-oauth-provider.user-info-authentication-method=header
spring.security.oauth2.client.provider.elter-oauth-provider.jwk-set-uri=https://elter-web.eu.auth0.com/.well-known/jwks.json
spring.security.oauth2.client.provider.elter-oauth-provider.user-name-attribute=email
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
    "admin1@example.com": ["ROLE_ELTER_EDITOR", "ROLE_ELTER_PUBLISHER", "ROLE_CIG_SYSTEM_ADMIN"],
    "publisher1@example.com": ["ROLE_ELTER_EDITOR", "ROLE_ELTER_PUBLISHER"],
    "publisher2@example.com": ["ROLE_ELTER_EDITOR", "ROLE_ELTER_PUBLISHER"],
    "editor1@example.com": ["ROLE_ELTER_EDITOR"],
    "editor2@example.com": ["ROLE_ELTER_EDITOR"]
}
```
