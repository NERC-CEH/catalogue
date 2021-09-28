define [
  'cs!views/EditorView'
  'cs!views/editor/SingleObjectView'
  'cs!views/editor/InputView'
  'cs!views/editor/TextareaView'
  'cs!views/editor/ParentView'
  'cs!views/editor/PredefinedParentView'
  'cs!views/editor/AccessLimitationView'
  'cs!models/editor/AccessLimitation'
  'cs!models/editor/InspireTheme'
  'cs!models/editor/TopicCategory'
  'cs!views/editor/TopicCategoryView'
  'cs!views/editor/ContactView'
  'cs!views/editor/ResourceIdentifierView'
  'cs!views/editor/DatasetReferenceDateView'
  'cs!models/editor/Contact'
  'cs!views/editor/OnlineResourceView'
  'cs!models/editor/OnlineResource'
  'cs!views/editor/ResourceConstraintView'
  'cs!views/editor/DescriptiveKeywordView'
  'cs!models/editor/DescriptiveKeyword'
  'cs!views/editor/DistributionFormatView'
  'cs!models/editor/DistributionFormat'
  'cs!models/editor/MapDataSource'
  'cs!views/editor/RelatedRecordView'
  'cs!views/editor/ReadOnlyView'
  'cs!views/editor/ParentStringView'
  'cs!views/editor/BoundingBoxView'
], (EditorView, SingleObjectView, InputView, TextareaView, ParentView, PredefinedParentView, AccessLimitationView, AccessLimitation, InspireTheme, TopicCategory, TopicCategoryView, ContactView, ResourceIdentifierView, DatasetReferenceDateView, Contact, OnlineResourceView, OnlineResource, ResourceConstraintView, DescriptiveKeywordView, DescriptiveKeyword, DistributionFormatView, DistributionFormat, MapDataSource, RelatedRecordView, ReadOnlyView, ParentStringView, BoundingBoxView) -> EditorView.extend

  initialize: ->

    @sections = [
      label: 'General'
      title:  ''
      views: [

        new InputView
          model: @model
          modelAttribute: 'depositReference'
          label: 'Deposit Reference'

        new ReadOnlyView
          model: @model
          modelAttribute: 'id'
          label: 'Data identifier'

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
          modelAttribute: 'depositorName'
          label: 'Depositor Name'

        new InputView
          model: @model
          modelAttribute: 'depositorContactDetails'
          label: 'Depositor Contact Details'

        new InputView
          model: @model
          modelAttribute: 'eidcName'
          label: 'For the EIDC: Name'

        new InputView
          model: @model
          modelAttribute: 'eidcContactDetails'
          label: 'For the EIDC: contact details'

      ]
    ,
      label: 'Data identification and citation'
      title: 'Data identification and citation'
      views: [

        new ParentView
          model: @model
          ModelType: Contact
          modelAttribute: 'authors'
          label: 'Authors'
          ObjectInputView: ContactView
          multiline: true
          helpText: """
                    <p>The names of authors should be in the format <code>Surname, First Initial. Second Initial.</code> For example <i>Brown, A.B.</i></p>
                    <p>Role and organisation name are mandatory.</p>
                    <p>The preferred identifier for individuals is an ORCiD.  You must enter the identifier as a <i>fully qualified</i> ID (e.g.  <b>https://orcid.org/1234-5678-0123-987X</b> rather than <b>1234-5678-0123-987X</b>).</p>
                    """
      ]
    ,
      label: 'Policies & Legislation'
      title: 'Policies & Legislation'
      views: [
        new TextareaView
          model: @model
          modelAttribute: 'otherPoliciesOrLegislation'
          label: 'Other Policies or Legislation'
          rows: 15
      ]
    ,
      label: 'The Data'
      title: 'The Data'
      views: [

        new InputView
          model: @model
          modelAttribute: 'dataFiles'
          label: 'Number of data files'

        new ParentStringView
          model: @model
          modelAttribute: 'fileNames'
          label: 'File names'

        new ParentStringView
          model: @model
          modelAttribute: 'fileFormats'
          label: 'File Formats'

        new InputView
          model: @model
          modelAttribute: 'fileSize'
          label: 'Size of data files'

        new InputView
          model: @model
          modelAttribute: 'transferMethod'
          label: 'Transfer Method'

        new ParentView
          model: @model
          modelAttribute: 'relatedRecords'
          label: 'Related records'
          ObjectInputView: RelatedRecordView
          multiline: true

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
      label: 'Supporting documentation'
      title: 'Supporting documentation'
      views: [

        new ParentStringView
          model: @model
          modelAttribute: 'supportingDocumentNames'
          label: 'Supporting Document names'

        new TextareaView
          model: @model
          modelAttribute: 'contentIncluded'
          label: 'Content Included'
          rows: 15
      ]
    ,
      label: 'Data retention'
      title: 'Data retention'
      views: [
        new TextareaView
          model: @model
          modelAttribute: 'policyExceptions'
          label: 'Policy Exceptions'
          rows: 15
      ]
    ,
      label: 'Availability and access'
      title: 'Availability and access'
      views: [
        new TextareaView
          model: @model
          modelAttribute: 'Availability'
          ModelType: OnlineResource
          label: 'Availability'
          multiline: true
          helpText: """ """

        new TextareaView
          model: @model
          modelAttribute: 'specificRequirements'
          label: 'Specific Requirements'
          rows: 15

        new TextareaView
          model: @model
          modelAttribute: 'otherServicesRequired'
          label: 'Other Services Required'
          rows: 15
      ]
    ,
      label: 'Licensing and IPR'
      title: 'Licensing and IPR'
      views: [

        new PredefinedParentView
          model: @model
          modelAttribute: 'End user license'
          label: 'End user license'
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

        new TextareaView
          model: @model
          modelAttribute: 'useConstraints'
          label: 'Additional Use Constraints'
          rows: 15

        new ParentView
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


      ]
    ,
      label: 'Superseding existing data'
      title: 'Superseding existing data (if applicable)'
      views: [

        new InputView
          model: @model
          modelAttribute: 'supersededMetadataId'
          label: 'Superseded Metadata Id'

        new TextareaView
          model: @model
          modelAttribute: 'supersededReason'
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
        new TextareaView
          model: @model
          modelAttribute: 'otherInfo'
          label: 'Other Info'
          rows: 7
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

        new PredefinedParentView
          model: @model
          modelAttribute: 'areaOfStudy'
          ModelType: BoundingBox
          label: 'Area of Study'
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
      ]
    ]

    EditorView.prototype.initialize.apply @
