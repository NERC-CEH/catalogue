import Backbone from 'backbone'
export default Backbone.Model.extend({

  defaults: {
    value: ''
  },

  uris: {
    Agriculture: 'http://onto.nerc.ac.uk/CEHMD/topic/1',
    'Animal behaviour': 'http://onto.nerc.ac.uk/CEHMD/topic/20',
    Biodiversity: 'http://onto.nerc.ac.uk/CEHMD/topic/2',
    'Climate and climate change': 'http://onto.nerc.ac.uk/CEHMD/topic/4',
    'Ecosystem services': 'http://onto.nerc.ac.uk/CEHMD/topic/6',
    'Environmental risk': 'http://onto.nerc.ac.uk/CEHMD/topic/5',
    'Environmental survey': 'http://onto.nerc.ac.uk/CEHMD/topic/7',
    'Evolutionary ecology': 'http://onto.nerc.ac.uk/CEHMD/topic/19',
    Hydrology: 'http://onto.nerc.ac.uk/CEHMD/topic/9',
    'Invasive species': 'http://onto.nerc.ac.uk/CEHMD/topic/10',
    'Land cover': 'http://onto.nerc.ac.uk/CEHMD/topic/11',
    'Land use': 'http://onto.nerc.ac.uk/CEHMD/topic/12',
    Mapping: 'http://onto.nerc.ac.uk/CEHMD/topic/18',
    Modelling: 'http://onto.nerc.ac.uk/CEHMD/topic/13',
    Phenology: 'http://onto.nerc.ac.uk/CEHMD/topic/3',
    Pollinators: 'http://onto.nerc.ac.uk/CEHMD/topic/14',
    Pollution: 'http://onto.nerc.ac.uk/CEHMD/topic/15',
    Radioecology: 'http://onto.nerc.ac.uk/CEHMD/topic/8',
    Soil: 'http://onto.nerc.ac.uk/CEHMD/topic/17',
    'Water quality': 'http://onto.nerc.ac.uk/CEHMD/topic/16'
  },

  initialize () {
    this.on('change:value', this.updateUri)
  },

  updateUri (model, value) {
    this.set('uri', this.uris[value] ? this.uris[value] : '')
  }
})
