define [
  'backbone'
], (Backbone) -> Backbone.Model.extend

  defaults:
    value: ''

  uris:
    biota: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/biota'
    boundaries: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/boundaries'
    climatologyMeteorologyAtmosphere: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/climatologyMeteorologyAtmosphere'
    economy: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/economy'
    elevation: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/elevation'
    environment: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/environment'
    farming: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/farming'
    geoscientificInformation: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/geoscientificInformation'
    health: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/health'
    imageryBaseMapsEarthCover: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/imageryBaseMapsEarthCover'
    inlandWaters: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/inlandWaters'
    intelligenceMilitary: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/intelligenceMilitary'
    location: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/location'
    oceans: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/oceans'
    planningCadastre: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/planningCadastre'
    society: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/society'
    structure: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/structure'
    transportation: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/transportation'
    utilitiesCommunication: 'http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/utilitiesCommunication'

  initialize: ->
    @on 'change:value', @updateUri

  updateUri: (model, value) ->
    @set 'uri', if @uris[value] then @uris[value] else ''