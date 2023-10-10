# Profiles used in catalogue

### Configuration

For Docker set environment variable `spring.profiles.active` to comma-separated
list of profiles e.g. `spring.profiles.active=auth:crowd,server:eidc,upload:hubbub`

## Authentication
Authentication and user/group authorisation

Choice of:
* `auth:crowd` CROWD authentication
* `auth:datalabs` DataLabs OIDC authentication

## Server
Which server the catalogue is deployed on

Choice of:
* `server:datalabs`
* `server:eidc`
* `server:elter`
* `server:inms`
* `server:pimfe`
* `server:ukeof`

## Development and testing
* `development` for local development, including setting up test users
* `test` to indicate we are running tests - not needed manually

## Cache
* `cache` enables ehcache components, for caching user details and other communications with Crowd

## Service Agreements
Enables Online Service Agreements functionality
* `service-agreement`

## Uploads
Dataset upload functionality

Choice of:
* `upload:hubbub` Upload managed through EIDC Hubbub server
* `upload:simple` Upload to directory

## Imports and Exports
* `imports` Enable import of records from external services (see [imports documentation](imports.md))
* `exports` Enable exporting records to a Fuseki instance

## Search
Configures how search works

Choice of:
* `search:basic` for standard text search
* `search:enhanced` for extra semantic search capabilities, e.g. related keywords
