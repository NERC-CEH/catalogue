import EditorView from '../EditorView'
import InputView from '../InputView'
import {
  DatasetReferenceDateView,
  DistributionFormatView, KeywordView,
  ParentView,
  PredefinedParentView, SingleObjectView, SpatialReferenceSystemView, SpatialRepresentationTypeView,
  SpatialResolutionView,
  SupplementalView, TemporalExtentView,
  TextareaView
} from '../views'
import { DistributionFormat, MultipleDate, SpatialResolution } from '../models'

export default EditorView.extend({

  initialize () {
    if (!this.model.has('type')) { this.model.set('type', 'dataset') }

    this.sections = [{
      label: 'Basic Info',
      title: 'Basic Info',
      views: [
        new InputView({
          model: this.model,
          modelAttribute: 'title',
          label: 'Title'
        }),
        new TextareaView({
          model: this.model,
          modelAttribute: 'description',
          label: 'Description',
          rows: 12
        }),
        new SingleObjectView({
          model: this.model,
          modelAttribute: 'datasetReferenceDate',
          ModelType: MultipleDate,
          label: 'Reference dates',
          ObjectInputView: DatasetReferenceDateView,
          helpText: `\
<p><b>Created</b> is the date on which the data resource was originally created.</p>
<p><b>Published</b> is the date when this metadata record is made available publicly.</p>
<p>For embargoed resources, <b>Release(d)</b> is the date on which the embargo was lifted <i class='text-red'><b>or is due to be lifted</b></i>.</p>
<p><b>Superseded</b> is the date on which the resource was superseded by another resource (where relevant).</p>\
`
        }),
        new ParentView({
          model: this.model,
          modelAttribute: 'keywords',
          label: 'Keywords',
          ObjectInputView: KeywordView
        })
      ]
    },
    {
      label: 'Time & Space',
      title: 'Time & Space',
      views: [
        new ParentView({
          model: this.model,
          modelAttribute: 'temporalExtents',
          ModelType: MultipleDate,
          label: 'Temporal extent',
          ObjectInputView: TemporalExtentView
        }),

        new PredefinedParentView({
          model: this.model,
          modelAttribute: 'spatialReferenceSystems',
          label: 'Spatial reference systems',
          ObjectInputView: SpatialReferenceSystemView,
          predefined: {
            'British National Grid (EPSG::27700)': {
              code: 'http://www.opengis.net/def/crs/EPSG/0/27700',
              title: 'OSGB 1936 / British National Grid'
            },
            'GB place names': {
              code: 'https://data.ordnancesurvey.co.uk/datasets/opennames',
              title: 'GB place names'
            },
            'GB postcodes': {
              code: 'https://data.ordnancesurvey.co.uk/datasets/os-linked-data',
              title: 'GB postcodes'
            },
            'Lat/long (WGS84) (EPSG::4326)': {
              code: 'http://www.opengis.net/def/crs/EPSG/0/4326',
              title: 'WGS 84'
            },
            'Web mercator (EPSG::3857)': {
              code: 'http://www.opengis.net/def/crs/EPSG/0/3857',
              title: 'WGS 84 / Pseudo-Mercator'
            }
          },
          helpText: `
<p>The spatial referencing system used within the data resource.  <strong>This is mandatory for datasets</strong>; if the dataset has no spatial component (e.g. if it is a laboratory study) the resource type ‘non-geographic data’ should be used instead.</p>
`
        }),

        new SpatialRepresentationTypeView({
          model: this.model,
          modelAttribute: 'spatialRepresentationTypes',
          label: 'Spatial Representation Types'
        }),

        new ParentView({
          model: this.model,
          modelAttribute: 'spatialResolutions',
          ModelType: SpatialResolution,
          label: 'Spatial resolution',
          ObjectInputView: SpatialResolutionView,
          helpText: `
<p>This is an indication of the level of spatial detail/accuracy. Enter a distance OR equivalent scale but not both. For most datasets, <i>distance</i> is more appropriate.</p>For gridded data, distance is the area of the ground (in metres) represented in each pixel. For point data, it is the degree of confidence in the point's location (e.g. for a point expressed as a six-figure grid reference, SN666781, the resolution would be 100m)</p>
`
        })
      ]
    },
    {
      label: 'Data formats',
      title: 'Data formats',
      views: [
        new PredefinedParentView({
          model: this.model,
          modelAttribute: 'distributionFormats',
          ModelType: DistributionFormat,
          label: 'Data files',
          ObjectInputView: DistributionFormatView,
          predefined: {
            CSV: {
              name: 'Comma-separated values (CSV)',
              type: 'text/csv',
              version: 'unknown'
            },
            'NetCDF v4': {
              name: 'NetCDF',
              type: 'application/netcdf',
              version: '4'
            }
          },
          helpText: `\
<p><b>Name</b> is the filename (including extension) and is mandatory.</p>
<p><b>Type</b> is the machine-readable media type.  If you do not know it, leave it blank.</p>
<p><b>Version</b> is mandatory; if it's not applicable, enter '<i>unknown</i>'</p>\
`
        }),
        new ParentView({
          model: this.model,
          modelAttribute: 'supplemental',
          multiline: true,
          label: 'Additional information',
          ObjectInputView: SupplementalView,
          helpText: `\
<p>You can add information not documented elsewhere here. This includes links to related papers, grey literature or websites.  For example:</p>
<ul><li>papers that cite this resource</li><li>papers/reports that provide relevant supporting information but which do not cite this resource</li><li>project websites</li></ul>
<p>When linking to published articles, please use DOIs whenever possible.</p>
<p><small class='text-danger'><i class='fa-solid fa-exclamation-triangle'> </i> NOTE: Some websites may be maintained for a limited period and may therefore soon become unavailable.</small></p>\
`
        }),
        new InputView({
          model: this.model,
          modelAttribute: 'units',
          label: 'Units'
        }),
        new TextareaView({
          model: this.model,
          modelAttribute: 'provenance',
          label: 'Provenance information',
          rows: 12
        })
      ]
    }
    ]

    return EditorView.prototype.initialize.apply(this)
  }
})
