define [
  'cs!models/editor/BoundingBox'
  'cs!models/editor/Contact'
  'cs!models/editor/DistributionFormat'
  'cs!models/editor/MultipleDate'
  'cs!models/editor/SpatialResolution'
  'cs!models/editor/Supplemental'
  'cs!views/editor/BoundingBoxView'
  'cs!views/editor/ContactView'
  'cs!views/editor/DatasetReferenceDateView'
  'cs!views/editor/DistributionFormatView'
  'cs!views/editor/InputView'
  'cs!views/editor/ParentView'
  'cs!views/editor/PredefinedParentView'
  'cs!views/editor/SingleObjectView'
  'cs!views/editor/SpatialReferenceSystemView'
  'cs!views/editor/SpatialRepresentationTypeView'
  'cs!views/editor/SpatialResolutionView'
  'cs!views/editor/SupplementalView'
  'cs!views/editor/TemporalExtentView'
  'cs!views/editor/TextareaView'
  'cs!views/EditorView'
], (
  BoundingBox
  Contact
  DistributionFormat
  MultipleDate
  SpatialResolution
  Supplemental
  BoundingBoxView
  ContactView
  DatasetReferenceDateView
  DistributionFormatView
  InputView
  ParentView
  PredefinedParentView
  SingleObjectView
  SpatialReferenceSystemView
  SpatialRepresentationTypeView
  SpatialResolutionView
  SupplementalView
  TemporalExtentView
  TextareaView
  EditorView
) -> EditorView.extend

  initialize: ->
    @model.set 'type', 'dataset' unless @model.has 'type'

    @sections = [
      label: 'Basic Info'
      title: 'Basic Info'
      views: [
        new InputView
          model: @model
          modelAttribute: 'title'
          label: 'Title'
        new TextareaView
          model: @model
          modelAttribute: 'description'
          label: 'Description'
          rows: 12
        new ParentView
          model: @model
          modelAttribute: 'responsibleParties'
          ModelType: Contact
          label: 'Contacts'
          ObjectInputView: ContactView
        new SingleObjectView
          model: @model
          modelAttribute: 'datasetReferenceDate'
          ModelType: MultipleDate
          label: 'Reference dates'
          ObjectInputView: DatasetReferenceDateView,
          helpText: """
                    <p><b>Created</b> is the date on which the data resource was originally created.</p>
                    <p><b>Published</b> is the date when this metadata record is made available publicly.</p>
                    <p>For embargoed resources, <b>Release(d)</b> is the date on which the embargo was lifted <i class='text-red'><b>or is due to be lifted</b></i>.</p>
                    <p><b>Superseded</b> is the date on which the resource was superseded by another resource (where relevant).</p>
                    """

      ]
    ,
      label: 'Time & Space'
      title: 'Time & Space'
      views: [
        new ParentView
          model: @model
          modelAttribute: 'temporalExtents'
          ModelType: MultipleDate
          label: 'Temporal extent'
          ObjectInputView: TemporalExtentView
        new PredefinedParentView
          model: @model
          modelAttribute: 'boundingBoxes'
          ModelType: BoundingBox
          label: 'Spatial extent'
          ObjectInputView: BoundingBoxView
          multiline: true
          predefined:
            'England':
              northBoundLatitude: 55.812
              eastBoundLongitude: 1.768
              southBoundLatitude: 49.864
              westBoundLongitude: -6.452
              extentName: 'England'
              extentUri: 'http://sws.geonames.org/6269131'
            'Great Britain':
              northBoundLatitude: 60.861
              eastBoundLongitude: 1.768
              southBoundLatitude: 49.864
              westBoundLongitude: -8.648
              extentName: 'Great Britain'
            'Northern Ireland':
              northBoundLatitude: 55.313
              eastBoundLongitude: -5.432
              southBoundLatitude: 54.022
              westBoundLongitude: -8.178
              extentName: 'Northern Ireland'
              extentUri: 'http://sws.geonames.org/2641364'
            Scotland:
              northBoundLatitude: 60.861
              eastBoundLongitude: -0.728
              southBoundLatitude: 54.634
              westBoundLongitude: -8.648
              extentName: 'Scotland'
              extentUri: 'http://sws.geonames.org/2638360'
            'United Kingdom':
              northBoundLatitude: 60.861
              eastBoundLongitude: 1.768
              southBoundLatitude: 49.864
              westBoundLongitude: -8.648
              extentName: 'United Kingdom'
              extentUri: 'http://sws.geonames.org/2635167'
            Wales:
              northBoundLatitude: 53.434
              eastBoundLongitude: -2.654
              southBoundLatitude: 51.375
              westBoundLongitude: -5.473
              extentName: 'Wales'
              extentUri: 'http://sws.geonames.org/2634895'
            World:
              northBoundLatitude: 90.00
              eastBoundLongitude: 180.00
              southBoundLatitude: -90.00
              westBoundLongitude: -180.00
          helpText: """
                    <p>A bounding box representing the limits of the data resource's study area.</p>
                    <p>If you do not wish to reveal the exact location publicly (for example, if locations are sensitive) it is recommended that you generalise the location.</p>
                    """

        new PredefinedParentView
          model: @model
          modelAttribute: 'spatialReferenceSystems'
          label: 'Spatial reference systems'
          ObjectInputView: SpatialReferenceSystemView
          predefined:
            'British National Grid (EPSG::27700)':
              code: 'http://www.opengis.net/def/crs/EPSG/0/27700'
              title: 'OSGB 1936 / British National Grid'
            'GB place names' :
              code: 'https://data.ordnancesurvey.co.uk/datasets/opennames'
              title: 'GB place names'
            'GB postcodes' :
              code: 'https://data.ordnancesurvey.co.uk/datasets/os-linked-data'
              title: 'GB postcodes'
            'Lat/long (WGS84) (EPSG::4326)' :
              code: 'http://www.opengis.net/def/crs/EPSG/0/4326'
              title: 'WGS 84'
            'Web mercator (EPSG::3857)':
              code: 'http://www.opengis.net/def/crs/EPSG/0/3857'
              title: 'WGS 84 / Pseudo-Mercator'
          helpText: """
                    <p>The spatial referencing system used within the data resource.  <strong>This is mandatory for datasets</strong>; if the dataset has no spatial component (e.g. if it is a laboratory study) the resource type ‘non-geographic data’ should be used instead.</p>
                    """

        new SpatialRepresentationTypeView
          model: @model
          modelAttribute: 'spatialRepresentationTypes'
          label: 'Spatial Representation Types'

        new ParentView
          model: @model
          modelAttribute: 'spatialResolutions'
          ModelType: SpatialResolution
          label: 'Spatial resolution'
          ObjectInputView: SpatialResolutionView
          helpText: """
                    <p>This is an indication of the level of spatial detail/accuracy. Enter a distance OR equivalent scale but not both. For most datasets, <i>distance</i> is more appropriate.</p>For gridded data, distance is the area of the ground (in metres) represented in each pixel. For point data, it is the degree of confidence in the point's location (e.g. for a point expressed as a six-figure grid reference, SN666781, the resolution would be 100m)</p>
                    """
      ]
    ,
      label: 'Data formats'
      title: 'Data formats'
      views: [
        new PredefinedParentView
          model: @model
          modelAttribute: 'distributionFormats'
          ModelType: DistributionFormat
          label: 'Data files'
          ObjectInputView: DistributionFormatView
          predefined:
            'CSV':
              name: 'Comma-separated values (CSV)'
              type: 'text/csv'
              version: 'unknown'
            'NetCDF v4':
              name: 'NetCDF'
              type: 'application/netcdf'
              version: '4'
            'Shapefile':
              name: 'Shapefile'
              type: ''
              version: 'unknown'
            'TIFF':
              name: 'TIFF'
              type: 'image/tiff'
              version: 'unknown'
           helpText: """
                    <p><b>Name</b> is the filename (including extension) and is mandatory.</p>
                    <p><b>Type</b> is the machine-readable media type.  If you do not know it, leave it blank.</p>
                    <p><b>Version</b> is mandatory; if it's not applicable, enter '<i>unknown</i>'</p>
                    """
      ]
    ]

    EditorView.prototype.initialize.apply @
