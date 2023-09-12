import Backbone from 'backbone'

export default Backbone.Model.extend({

  defaults: {
    value: ''
  },

  uris: {
    Bone: 'http://vocab.ceh.ac.uk/esb#bone',
    Brain: 'http://vocab.ceh.ac.uk/esb#brain',
    Egg: 'http://vocab.ceh.ac.uk/esb#egg',
    Fat: 'http://vocab.ceh.ac.uk/esb#fat ',
    Feather: 'http://vocab.ceh.ac.uk/esb#feather',
    Fur: 'http://vocab.ceh.ac.uk/esb#fur',
    'Gut contents': 'http://vocab.ceh.ac.uk/esb#stomachContents',
    Heart: 'http://vocab.ceh.ac.uk/esb#heart',
    'Homogenised whole sample': 'http://vocab.ceh.ac.uk/esb#Homogenised',
    Kidney: 'http://vocab.ceh.ac.uk/esb#kidney',
    Liver: 'http://vocab.ceh.ac.uk/esb#liver',
    Lung: 'http://vocab.ceh.ac.uk/esb#lung',
    'Lymph node': 'http://vocab.ceh.ac.uk/esb#lymphNode',
    Muscle: 'http://vocab.ceh.ac.uk/esb#muscle',
    'Nerve/spinal cord': 'http://vocab.ceh.ac.uk/esb#nerve-spinalcord',
    Plasma: 'http://vocab.ceh.ac.uk/esb#plasma',
    Serum: 'http://vocab.ceh.ac.uk/esb#serum',
    'Skin ': 'http://vocab.ceh.ac.uk/esb#skin',
    Spleen: 'http://vocab.ceh.ac.uk/esb#spleen',
    Trachea: 'http://vocab.ceh.ac.uk/esb#trachea',
    'Whole blood': 'http://vocab.ceh.ac.uk/esb#blood',
    'Whole body': 'http://vocab.ceh.ac.uk/esb#carcase'
  },

  initialize () {
    this.on('change:value', this.updateUri)
  },

  updateUri (model, value) {
    this.set('uri', this.uris[value] ? this.uris[value] : '')
  }
})
