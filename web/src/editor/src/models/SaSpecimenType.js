import Backbone from 'backbone'

export default Backbone.Model.extend({

    defaults: {
        value: ''
    },

    uris: {
        Air: 'http://vocab.ceh.ac.uk/esb#air',
        DNA: 'http://vocab.ceh.ac.uk/esb#dna',
        Ectoparasite: 'http://vocab.ceh.ac.uk/esb#ectoparasite',
        Endoparasite: 'http://vocab.ceh.ac.uk/esb#endoparasite',
        Fossil: 'http://vocab.ceh.ac.uk/esb#fossil',
        'Fresh water': 'http://vocab.ceh.ac.uk/esb#freshWater',
        Gas: 'http://vocab.ceh.ac.uk/esb#gas',
        'Ice core': 'http://vocab.ceh.ac.uk/esb#iceCore',
        Pathogen: 'http://vocab.ceh.ac.uk/esb#pathogen',
        'Rain water': 'http://vocab.ceh.ac.uk/esb#rain',
        RNA: 'http://vocab.ceh.ac.uk/esb#rna',
        Rock: 'http://vocab.ceh.ac.uk/esb#rock',
        'Sea water': 'http://vocab.ceh.ac.uk/esb#seaWater',
        Sediment: 'http://vocab.ceh.ac.uk/esb#sediment',
        Seed: 'http://vocab.ceh.ac.uk/esb#seed',
        Soil: 'http://vocab.ceh.ac.uk/esb#soil',
        'Surface water': 'http://vocab.ceh.ac.uk/esb#surfaceWater',
        Vegetation: 'http://vocab.ceh.ac.uk/esb#Vegetation'
    },

    initialize () {
        this.on('change:value', this.updateUri)
    },

    updateUri (model, value) {
        this.set('uri', this.uris[value] ? this.uris[value] : '')
    }
})
