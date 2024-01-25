import Backbone from 'backbone'

export default Backbone.Model.extend({

  defaults: {
    theme: ''
  },

  uris: {
    Addresses: 'http://inspire.ec.europa.eu/theme/ad',
    'Administrative Units': 'http://inspire.ec.europa.eu/theme/au',
    'Agricultural and Aquaculture Facilities': 'http://inspire.ec.europa.eu/theme/af',
    'Area Management Restriction Regulation Zones and Reporting units': 'http://inspire.ec.europa.eu/theme/am',
    'Atmospheric Conditions': 'http://inspire.ec.europa.eu/theme/ac',
    'Bio-geographical Regions': 'http://inspire.ec.europa.eu/theme/br',
    Buildings: 'http://inspire.ec.europa.eu/theme/bu',
    'Cadastral Parcels': 'http://inspire.ec.europa.eu/theme/cp',
    'Coordinate reference systems': 'http://inspire.ec.europa.eu/theme/rs',
    Elevation: 'http://inspire.ec.europa.eu/theme/el',
    'Energy Resources': 'http://inspire.ec.europa.eu/theme/er',
    'Environmental Monitoring Facilities': 'http://inspire.ec.europa.eu/theme/ef',
    'Geographical grid systems': 'http://inspire.ec.europa.eu/theme/gg',
    'Geographical Names': 'http://inspire.ec.europa.eu/theme/gn',
    Geology: 'http://inspire.ec.europa.eu/theme/ge',
    'Habitats and Biotopes': 'http://inspire.ec.europa.eu/theme/hb',
    'Human Health and Safety': 'http://inspire.ec.europa.eu/theme/hh',
    Hydrography: 'http://inspire.ec.europa.eu/theme/hy',
    'Land Cover': 'http://inspire.ec.europa.eu/theme/lc',
    'Land Use': 'http://inspire.ec.europa.eu/theme/lu',
    'Meteorological geographical features': 'http://inspire.ec.europa.eu/theme/mf',
    'Mineral Resources': 'http://inspire.ec.europa.eu/theme/mr',
    'Natural Risk Zones': 'http://inspire.ec.europa.eu/theme/nz',
    'Oceanographic Geographical Features': 'http://inspire.ec.europa.eu/theme/of',
    Orthoimagery: 'http://inspire.ec.europa.eu/theme/oi',
    'Population Distribution - Demography': 'http://inspire.ec.europa.eu/theme/pd',
    'Production and Industrial Facilities': 'http://inspire.ec.europa.eu/theme/pf',
    'Protected Sites': 'http://inspire.ec.europa.eu/theme/ps',
    'Sea Regions': 'http://inspire.ec.europa.eu/theme/sr',
    Soil: 'http://inspire.ec.europa.eu/theme/so',
    'Species Distribution': 'http://inspire.ec.europa.eu/theme/sd',
    'Statistical Units': 'http://inspire.ec.europa.eu/theme/su',
    'Transport Networks': 'http://inspire.ec.europa.eu/theme/tn',
    'Utility and Governmental Services': 'http://inspire.ec.europa.eu/theme/us'
  },

  initialize () {
    this.on('change:theme', this.updateUri)
  },

  updateUri (model, theme) {
    this.set('uri', this.uris[theme] ? this.uris[theme] : '')
  }
})
