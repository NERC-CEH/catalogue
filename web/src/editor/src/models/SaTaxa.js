import Backbone from 'backbone'

export default Backbone.Model.extend({

    defaults: {
        value: ''
    },

    uris: {
        Algae: 'http://vocab.ceh.ac.uk/esb#algae',
        Amphibian: 'http://vocab.ceh.ac.uk/esb#amphibian',
        Annelid: 'http://vocab.ceh.ac.uk/esb#annelid',
        Arthropod: 'http://vocab.ceh.ac.uk/esb#arthropod',
        Bacteria: 'http://vocab.ceh.ac.uk/esb#bacteria',
        Bird: 'http://vocab.ceh.ac.uk/esb#bird',
        Fern: 'http://vocab.ceh.ac.uk/esb#fern',
        Fish: 'http://vocab.ceh.ac.uk/esb#fish',
        Fungi: 'http://vocab.ceh.ac.uk/esb#fungi',
        Invertebrate: 'http://vocab.ceh.ac.uk/esb#invertebrate',
        Lichen: 'http://vocab.ceh.ac.uk/esb#lichen',
        Mammal: 'http://vocab.ceh.ac.uk/esb#mammal',
        Mollusc: 'http://vocab.ceh.ac.uk/esb#mollusc',
        'Moss or liverwort': 'http://vocab.ceh.ac.uk/esb#moss',
        Nematode: 'http://vocab.ceh.ac.uk/esb#nematode',
        Plankton: 'http://vocab.ceh.ac.uk/esb#plankton',
        Plant: 'http://vocab.ceh.ac.uk/esb#plant',
        Platyhelminthes: 'http://vocab.ceh.ac.uk/esb#platyhelminthes',
        Protozoa: 'http://vocab.ceh.ac.uk/esb#protozoa',
        Reptile: 'http://vocab.ceh.ac.uk/esb#reptile',
        Virus: 'http://vocab.ceh.ac.uk/esb#virus'
    },

    initialize () {
        this.on('change:value', this.updateUri)
    },

    updateUri (model, value) {
        this.set('uri', this.uris[value] ? this.uris[value] : '')
    }
})
