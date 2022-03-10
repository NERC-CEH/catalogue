define [
  'cs!views/EditorView'
  'cs!views/editor/SingleObjectView'
  'cs!views/editor/InputView'
  'cs!views/editor/CheckboxView'
  'cs!views/editor/ReadOnlyView'
  'cs!views/editor/TextareaView'
  'cs!views/editor/ParentView'
  'cs!views/editor/ParentLargeView'
  'cs!views/editor/PredefinedParentView'
  'cs!views/editor/PredefinedParentLargeView'
  'cs!views/editor/ParentStringView'
  'cs!views/editor/ResourceTypeView'
  'cs!models/editor/ResourceType'
  'cs!views/editor/AccessLimitationView'
  'cs!models/editor/AccessLimitation'
  'cs!models/editor/InspireTheme'
  'cs!views/editor/InspireThemeView'
  'cs!models/editor/TopicCategory'
  'cs!views/editor/TopicCategoryView'
  'cs!views/editor/ContactView'
  'cs!views/editor/ResourceIdentifierView'
  'cs!views/editor/DatasetReferenceDateView'
  'cs!models/editor/MultipleDate'
  'cs!models/editor/Contact'
  'cs!models/editor/BoundingBox'
  'cs!views/editor/BoundingBoxView'
  'cs!views/editor/OnlineResourceView'
  'cs!models/editor/OnlineResource'
  'cs!views/editor/ResourceConstraintView'
  'cs!views/editor/OtherConstraintView'
  'cs!views/editor/TemporalExtentView'
  'cs!views/editor/SpatialReferenceSystemView'
  'cs!views/editor/SpatialRepresentationTypeView'
  'cs!views/editor/DescriptiveKeywordView'
  'cs!models/editor/DescriptiveKeyword'
  'cs!views/editor/DistributionFormatView'
  'cs!models/editor/DistributionFormat'
  'cs!views/editor/SpatialResolutionView'
  'cs!models/editor/SpatialResolution'
  'cs!views/editor/FundingView'
  'cs!models/editor/Funding'
  'cs!views/editor/SupplementalView'
  'cs!models/editor/Supplemental'
  'cs!views/editor/ServiceView'
  'cs!models/editor/Service'
  'cs!models/editor/MapDataSource'
  'cs!views/editor/MapDataSourceView'
  'cs!views/editor/RelatedRecordView'
  'cs!views/editor/DeimsSiteView'
], (EditorView, SingleObjectView, InputView, CheckboxView, ReadOnlyView, TextareaView, ParentView, ParentLargeView, PredefinedParentView, PredefinedParentLargeView, ParentStringView, ResourceTypeView, ResourceType, AccessLimitationView, AccessLimitation, InspireTheme, InspireThemeView, TopicCategory, TopicCategoryView, ContactView, ResourceIdentifierView, DatasetReferenceDateView, MultipleDate, Contact, BoundingBox, BoundingBoxView, OnlineResourceView, OnlineResource, ResourceConstraintView, OtherConstraintView, TemporalExtentView, SpatialReferenceSystemView, SpatialRepresentationTypeView, DescriptiveKeywordView, DescriptiveKeyword, DistributionFormatView, DistributionFormat, SpatialResolutionView, SpatialResolution, FundingView, Funding, SupplementalView, Supplemental, ServiceView, Service, MapDataSource, MapDataSourceView, RelatedRecordView, DeimsSiteView) -> EditorView.extend

  initialize: ->

    disabled = $($('body')[0]).data('edit-restricted')

    @sections = [
      label: 'General'
      title:  ''
      views: [
        new ReadOnlyView
          model: @model
          modelAttribute: 'id'
          label: 'File identifier'
          
        new SingleObjectView
          model: @model
          modelAttribute: 'resourceType'
          ModelType: ResourceType
          label: 'Resource Type'
          ObjectInputView: ResourceTypeView

        new ParentView
          model: @model
          modelAttribute: 'deimsSites'
          label: 'DEIMS sites'
          ObjectInputView: DeimsSiteView
          multiline: true
          helpText: """
                    <p>DEIMS sites that have contributed to the dataset.</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'title'
          label: 'Title'
          helpText: """
                    <p>Provide a title that best describes that data resource. Include references to the subject, spatial and temporal aspects of the data resource.</p>
                    <p>Only the leading letter and proper nouns of the title should be capitalised.  If it's necessary to include acronyms in the title, then include both the acronym (in parentheses) and the phrase/word from which it was formed. Acronyms should not include full-stops between each letter.</p>
                    <p>If there are multiple titles or translations of titles (e.g. in Welsh), these should be added as alternative titles.</p>
                    """

        new ParentStringView
          model: @model
          modelAttribute: 'alternateTitles'
          label: 'Alternative titles'
          helpText: """
                    <p>Alternative titles allow you to add multiple titles and non-English translations of titles (e.g. Welsh).</p>
                    <p>Only the leading letter and proper nouns of titles should be capitalised. If the title includes acronyms, include both the acronym (in parentheses) and its definition. Acronyms should not include full-stops between each letter.</p>
                    """

        new TextareaView
          model: @model
          modelAttribute: 'description'
          label: 'Description'
          rows: 12
          helpText: """
                    <p>The description should describe the data resource in question, NOT the project/activity which produced it.</p>
                    <p>The description is an 'executive summary' that allows the reader to determine the relevance and usefulness of the resource.  The text should be concise but should contain sufficient detail to allow the reader to ascertain rapidly the scope and limitations of the resource.</p>
                    <p>Write in plain English; in other words, write complete sentences rather than fragments.  It is recommended that the abstract is organised using the "What, Where, When, How, Why, Who" structure.</p>
                    """
                    
        new SingleObjectView
          model: @model
          modelAttribute: 'accessLimitation'
          ModelType: AccessLimitation
          label: 'Resource status'
          ObjectInputView: AccessLimitationView
          helpText: """
                    <p>Access status of resource.  For example, is the resource embargoed or are restrictions imposed for reasons of confidentiality or security.</p>
                    <p><b>NOTE</b>: if access is Embargoed, you must also complete the <i>Release date</i>.</p>
                    """

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
        
        new ParentView
          model: @model
          modelAttribute: 'temporalExtents'
          ModelType: MultipleDate
          label: 'Temporal extent'
          ObjectInputView: TemporalExtentView
          helpText: """
                    <p>The time period(s) the data resource covers.  This is often the same as the data capture period but it need not be so.</p>
                    """
      ]
    ,
      label: 'Authors & contacts'
      title: 'Authors and other contacts'
      views: [
        new PredefinedParentView
          model: @model
          ModelType: Contact
          modelAttribute: 'responsibleParties'
          label: 'Contacts'
          ObjectInputView: ContactView
          multiline: true
          helpText: """
                    <p>The names of authors should be in the format <code>Surname, First Initial. Second Initial.</code> For example <i>Brown, A.B.</i></p>
                    <p>Role and organisation name are mandatory.</p>
                    <p>The preferred identifier for individuals is an ORCiD.  You must enter the identifier as a <i>fully qualified</i> ID (e.g.  <b>https://orcid.org/1234-5678-0123-987X</b> rather than <b>1234-5678-0123-987X</b>).</p>
                    """
      ]
    ,
      label: 'Classification'
      title:  'Categories and keywords'
      views: [
        new ParentView
          model: @model
          ModelType: TopicCategory
          modelAttribute: 'topicCategories'
          label: 'Topic categories'
          ObjectInputView: TopicCategoryView
          helpText: """
                    <p>Please note these are very broad themes and should not be confused with EIDC science topics.</p>
                    <p>Multiple topic categories are allowed - please include all that are pertinent.  For example, "Estimates of topsoil invertebrates" = Biota AND Environment AND Geoscientific Information.</p>
                    """

        new PredefinedParentView
          model: @model
          ModelType: DescriptiveKeyword
          modelAttribute: 'descriptiveKeywords'
          label: 'Keywords'
          ObjectInputView: DescriptiveKeywordView
          multiline: true
          predefined:
            'Catalogue topic':
              type: 'Catalogue topic'
          helpText: """
                    <p>Keywords (preferably taken from a controlled vocabulary) categorising and describing the data resource.</p>
                    <p>Good quality keywords help to improve the efficiency of search, making it easier to find relevant records.</p>
                    """

        new ParentView
          model: @model
          ModelType: InspireTheme
          modelAttribute: 'inspireThemes'
          label: 'INSPIRE theme'
          ObjectInputView: InspireThemeView
          helpText: """
                     <p>If the resource falls within the scope of an INSPIRE theme it must be declared here.</p>
                     <p>Conformity is the degree to which the <i class='text-red'>data</i> conforms to the relevant INSPIRE data specification.</p>
                    """

        new CheckboxView
          model: @model
          modelAttribute: 'notGEMINI'
          label: 'Exclude from GEMINI obligations'
          helpText: """
              <p>Tick this box to exclude this resource from GEMINI/INSPIRE obligations.</p><p <b class='text-red'><span class='fas fa-exclamation-triangle'>&nbsp;</span> WARNING.  This should only be ticked if the data DOES NOT relate to an area where an EU Member State exercises jurisdictional rights</b>.</p>
              """
      ]
    ,    
      label: 'Distribution'
      title: 'Distribution ,licensing and constraints'
      views: [
        new PredefinedParentView
          model: @model
          modelAttribute: 'onlineResources'
          ModelType: OnlineResource
          label: 'Online availability'
          ObjectInputView: OnlineResourceView
          multiline: true
          predefined:
            'Data package':
              url: 'https://data-package.ceh.ac.uk/data/{fileIdentifier}'
              name: 'Download the data'
              description: 'Download a copy of this data'
              function: 'download'
            'Order manager data':
              url: 'https://order-eidc.ceh.ac.uk/resources/{ORDER_REF}}/order'
              name: 'Download the data'
              description: 'Download a copy of this data'
              function: 'order'
            'Direct access data':
              url: 'https://catalogue.ceh.ac.uk/datastore/eidchub/{fileIdentifier}'
              name: 'Download the data'
              description: 'Download a copy of this data'
              function: 'download'
            'Supporting documents':
              url: 'https://data-package.ceh.ac.uk/sd/{fileIdentifier}.zip'
              name: 'Supporting information'
              description: 'Supporting information available to assist in re-use of this dataset'
              function: 'information'
          helpText: """
                    <p>Include addresses of web services used to access the data and supporting information.</p>
                    <p>Other links such as project websites or papers should <b>NOT</b> be included here. You can add them to "Additional information"</p>
                    """
          disabled: disabled
        
        new PredefinedParentView
          model: @model
          modelAttribute: 'distributionFormats'
          ModelType: DistributionFormat
          label: 'Formats'
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
                    <p><b>Type</b> is the machine-readable media type.  If you do not know it, leave it blank.</p>
                    <p><b>Version</b> is mandatory; if it's not applicable, enter '<i>unknown</i>'</p>
                    """

        new PredefinedParentView
          model: @model
          modelAttribute: 'useConstraints'
          label: 'Use constraints'
          ObjectInputView: ResourceConstraintView
          multiline: true
          predefined:
            'Licence - OGL':
              value: 'This resource is available under the terms of the Open Government Licence'
              uri: 'https://eidc.ceh.ac.uk/licences/OGL/plain'
              code: 'license'
          helpText: """
                    <p>Describe any restrictions and legal prerequisites placed on the <strong>use</strong> of a data resource once it has been accessed. For example:</p>
                    <ul class="list-unstyled">
                      <li>"Licence conditions apply"</li>
                      <li>"If you reuse this data you must cite …"</li>
                      <li>"Do not use for navigation purposes"</li>
                    </ul>
                    <p>Where possible include a link to a document describing the terms and conditions.</p>
                    <p>You MUST enter something even if there are no constraints. In the rare case that there are none, enter "no conditions apply".</p>
                    """
      ]
    ,
      label: 'ID & relationships'
      title: 'Identifiers and links to related resources'
      views: [
        new ParentView
          model: @model
          modelAttribute: 'resourceIdentifiers'
          label: 'Resource identifiers'
          ObjectInputView: ResourceIdentifierView
          helpText: """
                    <p>A unique string or number used to identify the data resource. The codespace identifies the context in which the code is unique.</p>
                    """
          disabled: disabled

        new ParentView
          model: @model
          modelAttribute: 'relatedRecords'
          label: 'Related records'
          ObjectInputView: RelatedRecordView
          multiline: true
      ]
    ,
      label: 'Spatial'
      title:  'Spatial characteristics'
      views: [
        new PredefinedParentView
          model: @model
          modelAttribute: 'boundingBoxes'
          ModelType: BoundingBox
          label: 'Spatial extent'
          ObjectInputView: BoundingBoxView
          multiline: true
          predefined:
            Austria:
              northBoundLatitude: 49.021
              eastBoundLongitude: 17.161
              southBoundLatitude: 46.372
              westBoundLongitude: 9.531
              extentName: 'Austria'
            Belgium:
              northBoundLatitude: 51.505
              eastBoundLongitude: 6.407
              southBoundLatitude: 49.497
              westBoundLongitude: 2.546
              extentName: 'Belgium'
            Bulgaria:
              northBoundLatitude: 44.216
              eastBoundLongitude: 28.607
              southBoundLatitude: 41.236
              westBoundLongitude: 22.357
              extentName: 'Bulgaria'
            'Czech Republic':
              northBoundLatitude: 51.055
              eastBoundLongitude: 18.859
              southBoundLatitude: 48.552
              westBoundLongitude: 12.092
              extentName: 'Czech Republic'
            Denmark:
              northBoundLatitude: 57.752
              eastBoundLongitude: 15.193
              southBoundLatitude: 54.560
              westBoundLongitude: 8.076
              extentName: 'Denmark'
            Finland:
              northBoundLatitude: 70.092
              eastBoundLongitude: 31.586
              southBoundLatitude: 59.766
              westBoundLongitude: 19.312
              extentName: 'Finland'
            France:
              northBoundLatitude: 51.089
              eastBoundLongitude: 8.233
              southBoundLatitude: 42.333
              westBoundLongitude: -4.795
              extentName: 'France'
            Germany:
              northBoundLatitude: 55.058
              eastBoundLongitude: 15.041
              southBoundLatitude: 47.270
              westBoundLongitude: 5.868
              extentName: 'Germany'
            Greece:
              northBoundLatitude: 41.749
              eastBoundLongitude: 29.645
              southBoundLatitude: 34.802
              westBoundLongitude: 19.374
              extentName: 'Greece'
            Hungary:
              northBoundLatitude: 48.585
              eastBoundLongitude: 22.896
              southBoundLatitude: 45.738
              westBoundLongitude: 16.114
              extentName: 'Hungary'
            Israel:
              northBoundLatitude: 33.290
              eastBoundLongitude: 35.684
              southBoundLatitude: 29.493
              westBoundLongitude: 34.269
              extentName: 'Israel'
            Italy:
              northBoundLatitude: 47.92
              eastBoundLongitude: 18.519
              southBoundLatitude: 35.493
              westBoundLongitude: 6.627
              extentName: 'Italy'
            Latvia:
              northBoundLatitude: 58.084
              eastBoundLongitude: 28.241
              southBoundLatitude: 55.675
              westBoundLongitude: 20.971
              extentName: 'Latvia'
            Norway:
              northBoundLatitude: 71.184
              eastBoundLongitude: 31.168
              southBoundLatitude: 57.960
              westBoundLongitude: 4.503
              extentName: 'Norway'
            Poland:
              northBoundLatitude: 54.836
              eastBoundLongitude: 24.145
              southBoundLatitude: 49.003
              westBoundLongitude: 14.123
              extentName: 'Poland'
            Portugal:
              northBoundLatitude: 42.154
              eastBoundLongitude: -6.189
              southBoundLatitude: 36.970
              westBoundLongitude: -9.500
              extentName: 'Portugal'
            Romania:
              northBoundLatitude: 48.264
              eastBoundLongitude: 29.713
              southBoundLatitude: 43.620
              westBoundLongitude: 20.264
              extentName: 'Romania'
            Serbia:
              northBoundLatitude: 46.189
              eastBoundLongitude: 23.006
              southBoundLatitude: 41.858
              westBoundLongitude: 18.849
              extentName: 'Serbia'
            Slovakia:
              northBoundLatitude: 49.614
              eastBoundLongitude: 22.567
              southBoundLatitude: 47.731
              westBoundLongitude: 16.834
              extentName: 'Slovakia'
            Slovenia:
              northBoundLatitude: 46.876
              eastBoundLongitude: 16.597
              southBoundLatitude: 45.422
              westBoundLongitude: 13.375
              extentName: 'Slovenia'
            Spain:
              northBoundLatitude: 43.788
              eastBoundLongitude: 3.321
              southBoundLatitude: 36.008
              westBoundLongitude: -9.298
              extentName: 'Spain'
            Sweden:
              northBoundLatitude: 69.060
              eastBoundLongitude: 24.167
              southBoundLatitude: 55.338
              westBoundLongitude: 10.966
              extentName: 'Sweden'
            Switzerland:
              northBoundLatitude: 47.807
              eastBoundLongitude: 10.492
              southBoundLatitude: 45.818
              westBoundLongitude: 5.956
              extentName: 'Switzerland'
            'United Kingdom':
              northBoundLatitude: 60.86
              eastBoundLongitude: 1.77
              southBoundLatitude: 49.86
              westBoundLongitude: -8.65
              extentName: 'United Kingdom'
              extentUri: 'http://sws.geonames.org/2635167'
            World:
              northBoundLatitude: 90.0
              eastBoundLongitude: 180.0
              southBoundLatitude: -90.0
              westBoundLongitude: -180.0
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
      label: 'Quality'
      title: 'Quality'
      views: [
        new TextareaView
          model: @model
          modelAttribute: 'lineage'
          label: 'Lineage'
          rows: 15
          helpText: """
                    <p>Information about the source data used in the construction of this data resource.</p>
                    <p>Quality assessments and enhancement processes applied to the data resource can also be noted and summarised here.</p>
                    """

        new TextareaView
          model: @model
          modelAttribute: 'reasonChanged'
          label: 'Reason for change'
          rows: 7
          helpText: """
                    <p>If this record is being retracted, the reasons for withdrawal or replacement should be explained here.</p>
                    """
      ]
    ,
      label: 'Supplemental'
      title:  'Additional information and funding'
      views: [
        new ParentView
          model: @model
          modelAttribute: 'supplemental'
          ModelType: Supplemental
          multiline: true
          label: 'Additional information'
          ObjectInputView: SupplementalView
          helpText: """
                    <p>You can add information not documented elsewhere here. This includes links to related papers, grey literature or websites.  For example:</p>
                    <ul><li>papers that cite this resource</li><li>papers/reports that provide relevant supporting information but which do not cite this resource</li><li>project websites</li></ul>
                    <p>When linking to published articles, please use DOIs whenever possible.</p>
                    <p><small class='text-danger'><i class='fas fa-exclamation-triangle'> </i> NOTE: Some websites may be maintained for a limited period and may therefore soon become unavailable.</small></p>
                    """

        new PredefinedParentView
          model: @model
          modelAttribute: 'funding'
          ModelType: Funding
          multiline: true
          label: 'Funding'
          ObjectInputView: FundingView
          predefined:
            'BBSRC':
              funderName: 'Biotechnology and Biological Sciences Research Council'
              funderIdentifier: 'https://ror.org/00cwqg982'
            'Defra':
              funderName: 'Department for Environment Food and Rural Affairs'
              funderIdentifier: 'https://ror.org/00tnppw48'
            'EPSRC':
              funderName: 'Engineering and Physical Sciences Research Council'
              funderIdentifier: 'https://ror.org/0439y7842'
            'ESRC':
              funderName: 'Economic and Social Research Council'
              funderIdentifier: 'https://ror.org/03n0ht308'
            'Innovate UK':
              funderName: 'Innovate UK'
              funderIdentifier: 'https://ror.org/05ar5fy68'
            'MRC':
              funderName: 'Medical Research Council'
              funderIdentifier: 'https://ror.org/03x94j517'
            'NERC':
              funderName: 'Natural Environment Research Council'
              funderIdentifier: 'https://ror.org/02b5d8509'
            'STFC':
              funderName: 'Science and Technology Facilities Council'
              funderIdentifier: 'https://ror.org/057g20z61'
          helpText: """
                    <p>Include here details of any grants or awards that were used to generate this resource.</p>
                    <p>If you include funding information, the Funding body is MANDATORY, other fields are useful but optional.</p>
                    <p>Award URL is either the unique identifier for the award or sa link to the funder's  grant page (if it exists). It is <b>NOT</b> a link to a project website.</p>
                    """
          disabled: disabled
      ]
    ,
      label: 'Web service'
      title: 'Web service details'
      views: [
        new ServiceView
          model: @model
          modelAttribute: 'service'
          ModelType: Service
          label: 'Service'
          disabled: disabled

        new ParentView
          model: @model
          modelAttribute: 'mapDataDefinition.data'
          ModelType: MapDataSource
          multiline: true
          label: 'Web map service'
          ObjectInputView: MapDataSourceView
          helpText: """
                    <p>Link this metadata record to an ingested geospatial file and create a WMS (<strong>https://catalogue.ceh.ac.uk/maps/{METADATA_ID}?request=getCapabilities&service=WMS</strong>). The supported formats are:</p>
                    <ul>
                      <li>Shapefiles - Vector (ignore the .shp extension when specifying the path) </li>
                      <li>GeoTiff - Raster</li>
                    </ul>
                    <p>To maximise performance, it is generally best to provide reprojected variants of data sources in common EPSG codes.</p>
                    <p>Vector datasets should be spatially indexed (using <a href="http://mapserver.org/utilities/shptree.html">shptree</a>)</p>
                    <p>Raster datasets should be provided with <a href="http://www.gdal.org/gdaladdo.html">overviews</a>. GeoTiff supports internal overviews.</p>
                    <p>The 'Byte?' option that appears for raster (GeoTiff) datasets is used to indicate whether the GeoTiff is a 'byte' or 'non-byte' datatype.
                    This is only needed if you configure 'Stylying=Classification' for your GeoTiff.</p>
                    <p>Paths should be specified relative to the base of the datastore. e.g. <strong>5b3fcf9f-19d4-4ad3-a8bb-0a5ea02c857e/my_shapefile</strong></p>
                    """
          disabled: disabled
      ]
    ]

    EditorView.prototype.initialize.apply @