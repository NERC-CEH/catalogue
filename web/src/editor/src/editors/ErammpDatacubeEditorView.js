import {
  DataLocationView,
  ObservedPropertyView,
  KeywordView,
  OnlineResourceSimpleView,
  ParentView, PointOfContactView, PredefinedParentLargeView,
  PredefinedParentView, ProcessingStepView,
  ReadOnlyView, ResourceConstraintView,
  SpatialReferenceSystemView, TextareaView
} from '../views'
import EditorView from '../EditorView'
import InputView from '../InputView'
import SelectView from '../SelectView'
import { ObservedProperty, PointOfContact } from '../models'
import BoundingBoxView from '../geometryMap/BoundingBoxView'
import BoundingBox from '../geometryMap/BoundingBox'

export default EditorView.extend({

  initialize () {
    if (!this.model.has('type')) { this.model.set('type', 'erammpDatacube') }

    this.sections = [{
      label: ' General ',
      title: 'General',
      views: [
        new InputView({
          model: this.model,
          modelAttribute: 'title',
          label: 'Data name'
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'version',
          label: 'Version'
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'description',
          label: 'Description',
          rows: 3
        }),

        new SelectView({
          model: this.model,
          modelAttribute: 'condition',
          label: 'Status',
          options: [
            { value: '', label: '' },
            { value: 'Current', label: 'Current' },
            { value: 'Draft', label: 'Draft' },
            { value: 'Obsolete', label: 'Obsolete (DO NOT USE)' }
          ]
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'dataFormat',
          label: 'Data format',
          placeholderAttribute: 'e.g. NetCDF, dbf, csv, shp'
        }),

        new PredefinedParentView({
          model: this.model,
          ModelType: PointOfContact,
          modelAttribute: 'provider',
          label: 'Data provider',
          ObjectInputView: PointOfContactView,
          helpText: `
<p>The contact(s) responsible for this model and who can be contacted if there are questions about it.  A <b>named</b> person is recommended</p>
`,
          predefined: {
            ADAS: {
              organisationName: 'ADAS'
            },
            UKCEH: {
              organisationName: 'UK Centre for Ecology & Hydrology'
            },
            Cranfield: {
              organisationName: 'Cranfield'
            },
            'Forest Research': {
              organisationName: 'Forest Research'
            }
          }
        })
      ]
    },
    {
      label: 'Data access',
      title: 'Data access',
      views: [
        new PredefinedParentView({
          model: this.model,
          modelAttribute: 'accessConstraints',
          label: 'Access constraints',
          ObjectInputView: ResourceConstraintView,
          multiline: true,
          predefined: {
            'Restricted to ERAMMP team': {
              value: 'Access to this data is restricted to members of the ERAMMP project team',
              code: 'otherRestrictions'
            },
            'Restricted to named individuals': {
              value: 'Access to this data is restricted to the following named individuals: ',
              code: 'otherRestrictions'
            }
          },
          helpText: `
<p>Describe any restrictions and legal prerequisites placed on <strong>access</strong> to this  data</p>
`
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'useConstraints',
          label: 'Use constraints',
          ObjectInputView: ResourceConstraintView,
          multiline: true,
          helpText: `
<p>Describe any restrictions and legal prerequisites placed on the <strong>use</strong> of a data resource once it has been accessed.</p>
`
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'dataLocations',
          label: 'Data location',
          ObjectInputView: DataLocationView,
          multiline: true,
          helpText: `
<p>Where is the data stored (eg SharePoint, SAN).  A direct path to the data is preferred.</p>
`
        })
      ]
    },
    {
      label: 'Data structure',
      title: 'Data structure',
      views: [

        new PredefinedParentLargeView({
          model: this.model,
          ModelType: ObservedProperty,
          modelAttribute: 'schema',
          multiline: true,
          label: 'Schema',
          ObjectInputView: ObservedPropertyView,
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
        })
      ]
    },
    {
      label: 'Spatial',
      title: 'Spatial',
      views: [
        new PredefinedParentView({
          model: this.model,
          modelAttribute: 'spatialReferenceSystems',
          label: 'Spatial reference systems',
          ObjectInputView: SpatialReferenceSystemView,
          predefined: {
            'British National Grid': {
              code: 27700,
              codeSpace: 'urn:ogc:def:crs:EPSG'
            },
            'Latitude/longitude (WGS84)': {
              code: 4326,
              codeSpace: 'urn:ogc:def:crs:EPSG'
            },
            'Spherical mercator': {
              code: 3857,
              codeSpace: 'urn:ogc:def:crs:EPSG'
            }
          },
          // eslint-disable-next-line no-multi-str
          helpText: '\
<p>The spatial referencing system used by the data resource.</p>\
'
        }),

        new PredefinedParentView({
          model: this.model,
          modelAttribute: 'boundingBoxes',
          ModelType: BoundingBox,
          label: 'Spatial coverage',
          ObjectInputView: BoundingBoxView,
          multiline: true,
          predefined: {
            Wales: {
              northBoundLatitude: 53.434,
              eastBoundLongitude: -2.654,
              southBoundLatitude: 51.375,
              westBoundLongitude: -5.473
            },
            'England & Wales': {
              northBoundLatitude: 55.812,
              eastBoundLongitude: 1.768,
              southBoundLatitude: 49.864,
              westBoundLongitude: -6.452
            },
            'UK (or Great Britain)': {
              northBoundLatitude: 60.861,
              eastBoundLongitude: 1.768,
              southBoundLatitude: 49.864,
              westBoundLongitude: -8.648
            },
            World: {
              northBoundLatitude: 90.00,
              eastBoundLongitude: 180.00,
              southBoundLatitude: -90.00,
              westBoundLongitude: -180.00
            }
          },
          helpText: `\
                <p>A bounding box showing the area that the archive covers. It will encompass the remit of the archive, which may be larger than that represented by the samples actually in the archive. It is represented by north, south, east and west in decimal degrees (WGS84).</p>
                <p>Enter the values, or click on the map to draw a  rectangle at the approximate location.</p>\
                `
        }),

        new SelectView({
          model: this.model,
          modelAttribute: 'spatialRepresentationType',
          label: 'Spatial type',
          options: [
            { value: 'grid', label: 'Raster (grid)' },
            { value: 'vector', label: 'Vector' },
            { value: 'textTable', label: 'Tabular (e.g. spreadsheet, database table)' }
          ]
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'spatialResolution',
          label: 'Spatial resolution'
        })
      ]
    },
    {
      label: 'Provenance',
      title: 'Provenance',
      views: [
        new ParentView({
          model: this.model,
          modelAttribute: 'processingSteps',
          label: 'Processing steps',
          ObjectInputView: ProcessingStepView
        })
      ]
    },
    {
      label: 'Metadata',
      title: 'Metadata',
      views: [
        new ParentView({
          model: this.model,
          modelAttribute: 'keywords',
          label: 'Keywords',
          ObjectInputView: KeywordView,
          helpText: `
<p>A list of keywords that help to identify and describe the model - used to improve search results and filtering. A keyword may be an entry from a vocabulary (with a uri) or just plain text.</p>
`
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'onlineResources',
          label: 'Additional links',
          ObjectInputView: OnlineResourceSimpleView,
          helpText: `
<p>A list of links to additional resources that may be of use to the user.</p>
`
        }),

        new ReadOnlyView({
          model: this.model,
          modelAttribute: 'id',
          label: 'Identifier'
        }),

        new ReadOnlyView({
          model: this.model,
          modelAttribute: 'uri',
          label: 'URL'
        })

      ]
    }
    ]

    return EditorView.prototype.initialize.apply(this)
  }
})
