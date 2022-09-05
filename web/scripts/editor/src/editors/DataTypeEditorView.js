import EditorView from '../EditorView'
import InputView from '../InputView'
import {
  DataTypeProvenanceView,
  DataTypeSchemaView,
  PredefinedParentLargeView,
  SingleObjectView,
  TextareaView
} from '../views'
import { DataTypeSchema } from '../models'

export default EditorView.extend({

  initialize () {
    this.sections = [{
      label: 'General',
      title: '',
      views: [
        new InputView({
          model: this.model,
          modelAttribute: 'title',
          label: 'Title'
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'description',
          rows: 5,
          label: 'Description'
        }),

        new PredefinedParentLargeView({
          model: this.model,
          ModelType: DataTypeSchema,
          modelAttribute: 'schema',
          multiline: true,
          label: 'Schema',
          ObjectInputView: DataTypeSchemaView,
          predefined: {
            'Boolean (true/false)': {
              type: 'boolean'
            },
            Date: {
              type: 'date',
              format: 'YYYY-MM-DD'
            },
            'Date & time': {
              type: 'datetime',
              format: 'YYYY-MM-DDThh:mm:ss'
            },
            'Decimal number': {
              type: 'number'
            },
            Email: {
              type: 'string',
              format: 'email'
            },
            'Geographic point': {
              type: 'geopoint',
              format: 'lon, lat'
            },
            Integer: {
              type: 'integer'
            },
            Text: {
              type: 'string'
            },
            Time: {
              type: 'time',
              format: 'hh:mm:ss'
            },
            URI: {
              type: 'string',
              format: 'uri'
            },
            UUID: {
              type: 'string',
              format: 'uuid'
            },
            Year: {
              type: 'year',
              format: 'YYYY'
            },
            'Year & month': {
              type: 'yearmonth',
              format: 'YYYY-MM'
            }
          }
        }),

        new SingleObjectView({
          model: this.model,
          modelAttribute: 'provenance',
          label: 'Provenance',
          ObjectInputView: DataTypeProvenanceView
        })
      ]
    }
    ]

    return EditorView.prototype.initialize.apply(this)
  }
})
