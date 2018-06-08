# API Documentation
A REST API to create, view, modify and delete metadata records.

Replace curly brackets {} with appropriate content.

### Defined Values
[CatalogueServiceConfig](../java/src/main/java/uk/ac/ceh/gateway/catalogue/config/CatalogueServiceConfig.java) for valid values of {catalogue identifier}

[WebConfig](../java/src/main/java/uk/ac/ceh/gateway/catalogue/config/WebConfig.java) for valid values of {document type}

### File Identifier
The jsonpath of {identifier} from the response payload is

    $.id

## Create

    POST /documents?catalogue={catalogue identifier}

    Accept: application/json
    Authorization: Basic {Base64 username:password}
    Content-Type: application/{document type}+json

    {JSON request payload}

## View

    GET /documents/{identifier}

    Accept: application/json
    Authorization: Basic {Base64 username:password}

## Modify

    PUT /documents/{identifier}

    Accept: application/json
    Authorization: Basic {Base64 username:password}
    Content-Type: application/{document type}+json

    {JSON request payload}

## Delete

    DELETE /documents/{identifier}
    Authorization: Basic {Base64 username:password}