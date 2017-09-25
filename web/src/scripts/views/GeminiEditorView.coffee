define [
  'cs!views/EditorView'
  'cs!views/editor/SingleObjectView'
  'cs!views/editor/InputView'
  'cs!views/editor/TextareaView'
  'cs!views/editor/ParentView'
  'cs!views/editor/PredefinedParentView'
  'cs!views/editor/ParentStringView'
  'cs!views/editor/ResourceTypeView'
  'cs!models/editor/ResourceType'
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
  'cs!views/editor/ResourceStatusView'
  'cs!views/editor/ResourceMaintenanceView'
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
  'cs!views/editor/ConformanceResultView'
  'cs!models/editor/MapDataSource'
  'cs!views/editor/MapDataSourceView'
], (EditorView, SingleObjectView, InputView, TextareaView, ParentView, PredefinedParentView, ParentStringView, ResourceTypeView, ResourceType, TopicCategory, TopicCategoryView, ContactView, ResourceIdentifierView, DatasetReferenceDateView, MultipleDate, Contact, BoundingBox, BoundingBoxView, OnlineResourceView, OnlineResource, ResourceConstraintView, OtherConstraintView, TemporalExtentView, ResourceStatusView, ResourceMaintenanceView, SpatialReferenceSystemView, SpatialRepresentationTypeView, DescriptiveKeywordView, DescriptiveKeyword, DistributionFormatView, DistributionFormat, SpatialResolutionView, SpatialResolution, FundingView, Funding, SupplementalView, Supplemental, ServiceView, Service, ConformanceResultView, MapDataSource, MapDataSourceView) -> EditorView.extend

  initialize: ->

    @sections = [
      label: 'General'
      title:  ''
      views: [
        new SingleObjectView
          model: @model
          modelAttribute: 'resourceType'
          ModelType: ResourceType
          label: 'Resource Type'
          ObjectInputView: ResourceTypeView

        new InputView
          model: @model
          modelAttribute: 'title'
          label: 'Title'
          helpText: """
                    <p>Provide a title that best describes that data resource. Include references to the subject, spatial and temporal aspects of the data resource.</p>
                    <p>Only the leading letter and proper nouns of the title should be capitalised.  If it's necessary to include acronyms in the title, then include both the acronym (in parentheses) and the phrase/word from which it was formed. Acronyms should not include full-stops between each letter.</p>
                    <p>If there are multiple titles or translations of titles (e.g. in Welsh), these should be added as alternative titles.</p>
                    <p>Please refer to <a href="https://eip.ceh.ac.uk/catalogue/help/editing/metadataauthorguide" target="_blank">guidance for metadata authors</a></p>
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
          rows: 17
          helpText: """
                    <p>The description should describe the data resource in question, NOT the project/activity which produced it.</p>
                    <p>The description is an 'executive summary' that allows the reader to determine the relevance and usefulness of the resource.  The text should be concise but should contain sufficient detail to allow the reader to ascertain rapidly the scope and limitations of the resource.</p>
                    <p>Write in plain English; in other words, write complete sentences rather than fragments.  It is recommended that the abstract is organised using the "What, Where, When, How, Why, Who" structure - see <a href="https://eip.ceh.ac.uk/catalogue/help/editing/metadataauthorguide" target="_blank">guidance for metadata authors</a></p>
                    """

        new ParentView
          model: @model
          modelAttribute: 'resourceIdentifiers'
          label: 'Resource identifiers'
          ObjectInputView: ResourceIdentifierView
          helpText: """
                    <p>A unique string or number used to identify the data resource. The codespace identifies the context in which the code is unique.</p>
                    """

        new ResourceStatusView
          model: @model
          modelAttribute: 'resourceStatus'
          label: 'Resource status'
          helpText: """
                    <p>The current status of the data resource.  For data curated by the EIDC, <strong>Completed</strong> is the usual expected value.  <strong>Historical archive</strong> is acceptable if the data have been withdrawn or replaced (e.g. due to corrections/updated data published elsewhere).  <strong>Obsolete</strong> may be used in exceptional circumstances.</p>
                    """

        new SingleObjectView
          model: @model
          modelAttribute: 'datasetReferenceDate'
          ModelType: MultipleDate
          label: 'Reference dates'
          ObjectInputView: DatasetReferenceDateView,
          helpText: """
                    <p><u>Created</u> date is the date on which the data resource was originally created.</p>
                    <p><u>Published</u> date is the date when this metadata record is made available publicly.</p>
                    <p>For embargoed resources, the <u>Released</u> date is the date on which the embargo was lifted.</p>
                    <p><u>Superseded</u> date is the date on which the resource was superseded by another resource (where relevant).</p>
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
          predefined:
            'CEH Author':
              organisationName: 'Centre for Ecology & Hydrology'
              role: 'author'
              email: 'enquiries@ceh.ac.uk'
            'CEH Bangor':
              organisationName: 'Centre for Ecology & Hydrology'
              role: 'pointOfContact'
              email: 'enquiries@ceh.ac.uk'
              address:
                deliveryPoint: 'Environment Centre Wales, Deiniol Road'
                postalCode: 'LL57 2UW'
                city: 'Bangor'
                administrativeArea: 'Gwynedd'
                country: 'United Kingdom'
            'CEH Edinburgh':
              organisationName: 'Centre for Ecology & Hydrology'
              role: 'pointOfContact'
              email: 'enquiries@ceh.ac.uk'
              address:
                deliveryPoint: 'Bush Estate'
                postalCode: 'EH26 0QB'
                city: 'Penicuik'
                administrativeArea: 'Midlothian'
                country: 'United Kingdom'
            'CEH Lancaster':
              organisationName: 'Centre for Ecology & Hydrology'
              role: 'pointOfContact'
              email: 'enquiries@ceh.ac.uk'
              address:
                deliveryPoint: 'Lancaster Environment Centre, Library Avenue, Bailrigg'
                postalCode: 'LA1 4AP'
                city: 'Lancaster'
                administrativeArea: 'Lancashire'
                country: 'United Kingdom'
            'CEH Wallingford':
              organisationName: 'Centre for Ecology & Hydrology'
              role: 'pointOfContact'
              email: 'enquiries@ceh.ac.uk'
              address:
                deliveryPoint: 'Maclean Building, Benson Lane, Crowmarsh Gifford'
                postalCode: 'OX10 8BB'
                city: 'Wallingford'
                administrativeArea: 'Oxfordshire'
                country: 'United Kingdom'
            'EIDC Custodian':
              organisationName: 'Environmental Information Data Centre'
              role: 'custodian'
              email: 'eidc@ceh.ac.uk'
            'NERC Publisher':
              organisationName: 'NERC Environmental Information Data Centre'
              role: 'publisher'
              email: 'eidc@ceh.ac.uk'
          helpText: """
                    <p>The organisation or person responsible for the authorship and maintenance of the data resource.</p>
                    <p>A contact must include an email address, role and organisation name.  Other elements are optional.</p>
                    <p>The names of individuals should be included in the format <code>Surname, First Initial. Second Initial.</code> For example <strong>Brown, A.B.</strong></p>
                    <p>The preferred <b>identifier</b> for individuals is an ORCiD.  You must enter the identifier as a <i>fully qualified</i> ID.  For example <b>https://orcid.org/1234-5678-0123-987X</b> rather than <b>1234-5678-0123-987X</b>.</p>
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
                    <p>The main theme(s) of the data resource as defined by the INSPIRE Directive.</p>
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
            'INSPIRE Theme':
              type: 'INSPIRE Theme'
            'CEH Topic':
              type: 'CEH Topic'
          helpText: """
                    <p>Keywords (preferably taken from a controlled vocabulary) categorising and describing the data resource.</p>
                    <p>Good quality keywords help to improve the efficiency of search, making it easier to find relevant records.</p>
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
            'Order manager resource':
              url: 'https://catalogue.ceh.ac.uk/download?fileIdentifier={fileIdentifier}'
              name: 'Download the data'
              description: 'Download a copy of this data'
              function: 'order'
            'Supporting information':
              url: 'http://eidc.ceh.ac.uk/metadata/{fileIdentifier}/zip_export/'
              name: 'Supporting information'
              description: 'Supporting information available to assist in re-use of this dataset'
              function: 'information'
            'Embargoed data':
              url: 'http://eidc.ceh.ac.uk/administration-folder/tools/embargo'
              name: 'Online ordering'
              description: 'This resource is under embargo and will be made available by {dd/mm/yyyy} at the latest'
              function: 'order'
            'Embargoed documentation':
              url: 'http://eidc.ceh.ac.uk/administration-folder/tools/embargo'
              name: 'Supporting information'
              description: 'This resource is under embargo and will be made available by {dd/mm/yyyy} at the latest'
              function: 'information'
          helpText: """
                    <p>Include addresses of web services used to access the data (e.g. order manager) and supporting information.</p>
                    <p>Other links such as project websites or related journal articles should NOT be included here. You can add them to "Additional links"</p>
                    """
        
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
            'Open Government Licence':
              value: 'This resource is made available under the terms of the Open Government Licence'
              uri: 'http://eidc.ceh.ac.uk/administration-folder/tools/ceh-standard-licence-texts/cehOGL/plain'
              code: 'license'
            'Open Government Licence Non-CEH':
              value: 'This resource is made available under the terms of the Open Government Licence'
              uri: 'http://eidc.ceh.ac.uk/administration-folder/tools/ceh-standard-licence-texts/OGLnonceh/plain'
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

        new PredefinedParentView
          model: @model
          modelAttribute: 'accessConstraints'
          label: 'Limitations on access'
          ObjectInputView: ResourceConstraintView
          multiline: true
          predefined:
            'Registration required':
              value: 'Registration is required to access this data'
              uri: 'https://eip.ceh.ac.uk/catalogue/help/faq/registration'
              code: 'otherRestrictions'
            'no limitations':
              value: 'no limitations'
              code:  'otherRestrictions'
          helpText: """
                    <p>Any conditions that are in place to restrict a user's <strong>access</strong> to the data. These may include, for example, restrictions imposed for reasons of security or for licensing purposes.</p>
                    <p>You MUST enter something even if there are no access limitations. In the rare case that there are none, enter "no limitations".</p>
                    """
       
        new PredefinedParentView
          model: @model
          ModelType: Contact
          modelAttribute: 'distributorContacts'
          label: 'Distributor contact'
          ObjectInputView: ContactView
          multiline: true
          predefined:
            'EIDC':
              organisationName: 'Environmental Information Data Centre'
              role: 'distributor'
              email: 'eidc@ceh.ac.uk'
          helpText: """
                    <p>The organisation responsible for distributing the data resource</p>
                    <p>It <b>MUST</b> include an email address, role and an organisation name.  Other elements are optional.</p>
                    """
      ]
    ,
      label: 'Relationships'
      title: 'Links to other resources'
      views: [
        new InputView
          model: @model
          modelAttribute: 'parentIdentifier'
          label: 'Parent identifier'
          helpText: """
                    <p>File identifier of parent series.</p>
                    """
        
        new ParentStringView
          model: @model
          modelAttribute: 'partOfRepository'
          label: 'Repository membership'
          helpText: """
                    <p>File Identifier of repository.</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'revisionOfIdentifier'
          label: 'Revision of'
          helpText: """
                    <p>File Identifier of data resource being revised.</p>
                    """
          
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
          # These bounding box values were copied from terraCatalog
          predefined:
            England:
              northBoundLatitude: 55.812
              eastBoundLongitude: 1.7675
              southBoundLatitude: 49.864
              westBoundLongitude: -6.4526
            'Great Britain':
              northBoundLatitude: 60.861
              eastBoundLongitude: 1.768
              southBoundLatitude: 49.864
              westBoundLongitude: -8.648
            'Northern Ireland':
              northBoundLatitude: 55.313
              eastBoundLongitude: -5.432
              southBoundLatitude: 54.022
              westBoundLongitude: -8.178
            Scotland:
              northBoundLatitude: 60.861
              eastBoundLongitude: -0.728
              southBoundLatitude: 54.634
              westBoundLongitude: -8.648
            'United Kingdom':
              northBoundLatitude: 60.86099
              eastBoundLongitude: 1.767549
              southBoundLatitude: 49.86382
              westBoundLongitude: -8.648393
            Wales:
              northBoundLatitude: 53.434
              eastBoundLongitude: -2.654
              southBoundLatitude: 51.375
              westBoundLongitude: -5.473
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
            'OSGB 1936 / British National Grid':
              code: 27700
              codeSpace: 'urn:ogc:def:crs:EPSG'
            'TM65 / Irish National Grid':
              code: 29900
              codeSpace: 'urn:ogc:def:crs:EPSG'
            'OSNI 1952 / Irish National Grid':
              code: 29901
              codeSpace: 'urn:ogc:def:crs:EPSG'
            'TM65 / Irish Grid':
              code: 29902
              codeSpace: 'urn:ogc:def:crs:EPSG'
            'WGS 84':
              code: 4326
              codeSpace: 'urn:ogc:def:crs:EPSG'
            'WGS 84 longitude-latitude (CRS:84)':
              code: 'CRS:84'
              codeSpace: 'urn:ogc:def:crs:EPSG'
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
                    <p>For gridded data, this is the area of the ground (in metres) represented in each pixel.</p>
                    <p>For point data, the ground sample distance is the degree of confidence in the point's location (e.g. for a point expressed as a six-figure grid reference, SN666781, the resolution would be 100m)</p>
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
                    <p>See <a href="https://eip.ceh.ac.uk/catalogue/help/editing/metadataauthorguide" target="_blank">guidance for metadata authors</a>.</p>
                    """

        new ParentView
          model: @model
          modelAttribute: 'resourceMaintenance'
          label: 'Resource maintenance'
          ObjectInputView: ResourceMaintenanceView
          helpText: """
                    <p>This states how often the updated data resource is made available to the user.  For the vast majority of EIDC data, this value will be "not planned".</p>
                    """

        new PredefinedParentView
          model: @model
          modelAttribute: 'conformanceResults'
          label: 'Conformance results'
          multiline: true
          ObjectInputView: ConformanceResultView
          predefined:
            INSPIRE:
              title: 'COMMISSION REGULATION (EU) No 1089/2010 of 23 November 2010 implementing Directive 2007/2/EC of the European Parliament and of the Council as regards interoperability of spatial data sets and services'
              date: '2010-12-08'
              dateType: 'publication'
      ]
    ,
      label: 'Supplemental'
      title:  'Supplemental information and funding'
      views: [
        new ParentView
          model: @model
          modelAttribute: 'supplemental'
          ModelType: Supplemental
          multiline: true
          label: 'Supplemental information'
          ObjectInputView: SupplementalView
          helpText: """
                    <p>You can add information not documented elsewhere here. This includes links to related articles or websites.  For example:</p>
                    <ul><li>papers that cite this resource</li><li>papers that provide relevant supporting information but which do not cite this resource</li><li>project websites</li></ul>
                    <p>When linking to published articles, please use DOIs whenever possible.</p>
                    <p><small class='text-danger'><i class='fa fa-exclamation-triangle'> </i> Project websites may only be maintained for a limited period and may therefore soon become unavailable.</small></p>
                    """

        new PredefinedParentView
          model: @model
          modelAttribute: 'funding'
          ModelType: Funding
          multiline: true
          label: 'Funding'
          ObjectInputView: FundingView
          predefined:
            'NERC':
              funderName: 'Natural Environment Research Council'
              funderIdentifier: 'https://doi.org/10.13039/501100000270'
          helpText: """
                    <p>Include here details of any grants or awards that were used to generate this resource.</p>
                    <p>If you include funding information, the Funding body is MANDATORY, other fields are useful but optional.</p>
                    <p>Award URL is either the unique identifier for the award or sa link to the funder's  grant page (if it exists). It is <b>NOT</b> a link to a project website.</p>
                    """
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
      ]
    ,
      label: 'Metadata'
      title: 'Metadata about metadata'
      views: [
        new PredefinedParentView
          model: @model
          ModelType: Contact
          modelAttribute: 'metadataPointsOfContact'
          label: 'Metadata point of contact'
          ObjectInputView: ContactView
          multiline: true
          predefined:
            'CEH Point of Contact':
              organisationName: 'Centre for Ecology & Hydrology'
              role: 'pointOfContact'
              email: 'enquiries@ceh.ac.uk'
              address:
                deliveryPoint: 'Maclean Building, Benson Lane, Crowmarsh Gifford'
                postalCode: 'OX10 8BB'
                city: 'Wallingford'
                administrativeArea: 'Oxfordshire'
                country: 'United Kingdom'
            'EIDC Point of Contact':
              organisationName: 'Environmental Information Data Centre'
              role: 'pointOfContact'
              email: 'eidc@ceh.ac.uk'
              address:
                country: 'United Kingdom'
          helpText: """
                    <p>The organisation or person responsible for the authorship, maintenance and curation of the metadata resource.</p>
                    <p>A contact must include the contact's email address, role and an organisation name and/or individual's name.  Other elements are optional.</p>
                    <p>The names of individuals should be included in the format Surname, First Initial. Second Initial.  For example <strong>Brown, A.B.</strong></p>
                    """

        new InputView
          model: @model
          modelAttribute: 'id'
          label: 'File identifier'
          readonly: true
          helpText: """
                    <p>File identifier of metadata record.</p>
                    <p>For information only, not editable.</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'uri'
          label: 'URL'
          readonly: true
          helpText: """
                    <p>URL of metadata record.</p>
                    <p>For information only, not editable.</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'metadataDate'
          label: 'Metadata Date'
          readonly: true
          helpText: """
                    <p>Date and time metadata last updated.</p>
                    <p>For information only, not editable.</p>
                    """
      ]
    ]

    EditorView.prototype.initialize.apply @
