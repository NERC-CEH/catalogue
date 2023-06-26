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

## Develop & Testing
Development profiles

* `develop`
* `test`

## Service Agreements
Enables Online Service Agreements functionality
* `service-agreement`

## Uploads
Dataset upload functionality

Choice of:
* `upload:hubbub` Upload managed through EIDC Hubbub server
* `upload:simple` Upload to directory

## Imports
* `imports` Enable import of records from external services (e.g. SITES metadata catalogue, in combination with `server:elter`)
