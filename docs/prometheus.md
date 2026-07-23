# Prometheus metrics

The catalogue exports on the management port (8081) the endpoint
`/actuator/prometheus` metrics for ingestion by a
[Prometheus](https://prometheus.io/) service.  As well as the basic
metrics provided by the [micrometer.io](https://micrometer.io/)
implementation, there are some custom ones, defined in
uk.ac.ceh.gateway.catalogue.prometheus.DocumentCountService:

- `catalogue_documents`: gauge for total number of documents.
  Currently not hugely sophisticated, but could be augmented to break
  published documents down by catalogue and state and categorise
  within the metric.
