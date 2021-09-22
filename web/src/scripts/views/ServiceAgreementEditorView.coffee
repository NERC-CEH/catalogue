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

        new InputView
          model: @model
          modelAttribute: 'title'
          label: 'Title'
          helpText: """
                    <p>Provide a title that best describes that data resource. Include references to the subject, spatial and temporal aspects of the data resource.</p>
                    <p>Only the leading letter and proper nouns of the title should be capitalised.  If it's necessary to include acronyms in the title, then include both the acronym (in parentheses) and the phrase/word from which it was formed. Acronyms should not include full-stops between each letter.</p>
                    <p>If there are multiple titles or translations of titles (e.g. in Welsh), these should be added as alternative titles.</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'depositReference'
          label: 'Deposit Reference'
          helpText: """"""

        new InputView
          model: @model
          modelAttribute: 'depositorName'
          label: 'Depositor Name'
          helpText: """"""

        new InputView
          model: @model
          modelAttribute: 'depositorContactDetails'
          label: 'Depositor Contact Details'
          helpText: """"""

        new InputView
          model: @model
          modelAttribute: 'forTheEIDCName'
          label: 'For the EIDC Name'
          helpText: """"""

        new InputView
          model: @model
          modelAttribute: 'forTheEIDCContactDetails'
          label: 'For the EIDC contact details'
          helpText: """"""

        new InputView
          model: @model
          modelAttribute: 'DOI'
          label: 'DOI'
          helpText: """"""

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
      label: 'Data identification and citation'
      title: 'Data identification and citation'
      views: [

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
      label: 'The Data'
      title: 'The Data'
      views: [

        new InputView
          model: @model
          modelAttribute: 'fileNames'
          label: 'File names'
          helpText: """
                    <p>Provide a title that best describes that data resource. Include references to the subject, spatial and temporal aspects of the data resource.</p>
                    <p>Only the leading letter and proper nouns of the title should be capitalised.  If it's necessary to include acronyms in the title, then include both the acronym (in parentheses) and the phrase/word from which it was formed. Acronyms should not include full-stops between each letter.</p>
                    <p>If there are multiple titles or translations of titles (e.g. in Welsh), these should be added as alternative titles.</p>
                    """

        new PredefinedParentView
          model: @model
          modelAttribute: 'fileFormats'
          ModelType: DistributionFormat
          label: 'File Formats'
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

        new InputView
          model: @model
          modelAttribute: 'fileSize'
          label: 'File Size'
          helpText: """  """

        new ParentView
          model: @model
          ModelType: TopicCategory
          modelAttribute: 'dataCategory'
          label: 'Data Category'
          ObjectInputView: TopicCategoryView
          helpText: """
                    <p>Please note these are very broad themes and should not be confused with EIDC science topics.</p>
                    <p>Multiple topic categories are allowed - please include all that are pertinent.  For example, "Estimates of topsoil invertebrates" = Biota AND Environment AND Geoscientific Information.</p>
                    """

      ]
    ,
      label: 'Availability and access'
      title: 'Availability and access'
      views: [
        new PredefinedParentView
          model: @model
          modelAttribute: 'Availability'
          ModelType: OnlineResource
          label: 'Availability'
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

        new ParentView
          model: @model
          modelAttribute: 'relatedRecords'
          label: 'Related records'
          ObjectInputView: RelatedRecordView
          multiline: true
      ]
    ,
      label: 'Licensing and IPR'
      title: 'Licensing and IPR'
      views: [

        new TextareaView
          model: @model
          modelAttribute: 'End user license'
          label: 'End user license'
          rows: 15
          helpText: """  """

        new PredefinedParentView
          model: @model
          ModelType: Contact
          modelAttribute: 'Owner of IPR'
          label: 'Owner of IPR'
          ObjectInputView: ContactView
          multiline: true
          helpText: """
                    <p>The names of Owner should be in the format <code>Surname, First Initial. Second Initial.</code> For example <i>Brown, A.B.</i></p>
                    <p>Role and organisation name are mandatory.</p>
                    <p>The preferred identifier for individuals is an ORCiD.  You must enter the identifier as a <i>fully qualified</i> ID (e.g.  <b>https://orcid.org/1234-5678-0123-987X</b> rather than <b>1234-5678-0123-987X</b>).</p>
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

      ]
    ,
      label: 'Supporting documentation'
      title: 'Supporting documentation'
      views: [
      ]
    ,
      label: 'Data retention'
      title: 'Data retention'
      views: [
      ]
    ,
      label: 'Superseding existing data'
      title: 'Superseding existing data (if applicable)'
      views: [

        new ParentView
          model: @model
          modelAttribute: 'superSeededMetadataId'
          label: 'Super seeded metadata Id'
          ObjectInputView: ResourceIdentifierView
          helpText: """
                    <p>A unique string or number used to identify the data resource. The codespace identifies the context in which the code is unique.</p>
                    """
          disabled: disabled

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
      label: 'Miscellaneous'
      title: 'Miscellaneous'
      views: [
      ]
    ,
      label: 'Discovery metadata'
      title: 'Discovery metadata'
      views: [

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

        new TextareaView
          model: @model
          modelAttribute: 'lineage'
          label: 'Lineage'
          rows: 15
          helpText: """
                    <p>Information about the source data used in the construction of this data resource.</p>
                    <p>Quality assessments and enhancement processes applied to the data resource can also be noted and summarised here.</p>
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
          ModelType: TopicCategory
          modelAttribute: 'areaOfStudy'
          label: 'Area of study'
          ObjectInputView: TopicCategoryView
          helpText: """
                    <p>Please note these are very broad themes and should not be confused with EIDC science topics.</p>
                    <p>Multiple topic categories are allowed - please include all that are pertinent.  For example, "Estimates of topsoil invertebrates" = Biota AND Environment AND Geoscientific Information.</p>
                    """
      ]
    ]

    EditorView.prototype.initialize.apply @
