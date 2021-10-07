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
  'cs!models/editor/BoundingBox'
  'cs!views/editor/BoundingBoxView'
  'cs!views/editor/TextOnlyView'
  'cs!views/editor/ServiceAgreementAuthorView'
  'cs!views/editor/FileView'
], (EditorView, SingleObjectView, InputView, TextareaView, ParentView, PredefinedParentView, AccessLimitationView, AccessLimitation, InspireTheme, TopicCategory, TopicCategoryView, ContactView, ResourceIdentifierView, DatasetReferenceDateView, Contact, OnlineResourceView, OnlineResource, ResourceConstraintView, DescriptiveKeywordView, DescriptiveKeyword, DistributionFormatView, DistributionFormat, MapDataSource, RelatedRecordView, ReadOnlyView, ParentStringView, BoundingBox, BoundingBoxView, TextOnlyView, ServiceAgreementAuthorView, FileView) -> EditorView.extend

  initialize: ->

    @sections = [
      label: 'General'
      title:  ''
      views: [

        new TextOnlyView
          model: @model
          modelAttribute: 'TextOnlyView'
          text: """"""

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

        new TextOnlyView
          model: @model
          modelAttribute: 'TextOnlyView'
          text: """
                   <h2>Authors</h2>
                   <ul>
                   <li>List authors below in the order in which they will appear in the citation.</li>
                   <li>Author's names must be in the format Surname <comma> Initial(s). For example, Smith, K.P. not Kim P. Smith</li>
                   <li>UK law requires us to inform all individuals listed below that they are being proposed as an author.  We therefore require a current, valid email address (or phone number) for all living authors - those without valid contact details are not eligible for authorship.</li>
                   <li>Authors' details will be published in a public data catalogue and held in EIDC systems. Please see our Privacy Notice for further information.</li>
                   </ul>
                """

        new ParentView
          model: @model
          ModelType: Contact
          modelAttribute: 'authors'
          label: 'Authors'
          ObjectInputView: ServiceAgreementAuthorView
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
        new TextOnlyView
          model: @model
          modelAttribute: 'TextOnlyView'
          text: """<p>All environmental data deposited into the EIDC are subject to the requirements of the <a href="https://nerc.ukri.org/research/sites/environmental-data-service-eds/policy/">NERC Data Policy.</a></p>
                   <p>By depositing data, you confirm that the data is compliant with the provisions of UK data protection laws.</p>
                   <p>Data and supporting documentation should not contain names, addresses or other personal information relating to 'identifiable natural persons'.  Discovery metadata (the catalogue record) may contain names and contact details of the authors of this data (<a href="https://eidc.ac.uk/policies/retentionPersonalData">see our policy on retention and use of personal data</a>).</p>
                   <p>If other policies/legislation applies (e.g. <a href="https://inspire.ec.europa.eu/">INSPIRE</a>), please specify below.</p>
                   """
        new TextareaView
          model: @model
          modelAttribute: 'otherPoliciesOrLegislation'
          label: 'Other Policies or Legislation'
          rows: 15

        new TextOnlyView
          model: @model
          modelAttribute: 'TextOnlyView'
          text: """<p>The depositor may also wish to provide an image to accompany the dataset which may subsequently be used to advertise its availability on social media.  If no image is provided, the EIDC may source a suitable picture.</p>"""
      ]
    ,
      label: 'The Data'
      title: 'The Data'
      views: [

        new ParentView
          model: @model
          modelAttribute: 'files'
          label: 'Files'
          ObjectInputView: FileView
          multiline: true

        new InputView
          model: @model
          modelAttribute: 'transferMethod'
          label: 'Transfer Method'

        new ParentView
          model: @model
          modelAttribute: 'relatedDataHoldings'
          label: 'Related Data Holdings'
          ObjectInputView: RelatedRecordView
          multiline: true

        new ParentView
          model: @model
          ModelType: TopicCategory
          modelAttribute: 'topicCategories'
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

        new TextOnlyView
          model: @model
          modelAttribute: 'TextOnlyView'
          text: """<p>Please provide the title and file extension of document(s) you will provide to enable re-use of the data (see <a href="https://eidc.ac.uk/deposit/supportingDocumentation">https://eidc.ac.uk/deposit/supportingDocumentation</a>).</a>"""

        new ParentStringView
          model: @model
          modelAttribute: 'supportingDocumentNames'
          label: 'Supporting Documents'

        new TextareaView
          model: @model
          modelAttribute: 'contentIncluded'
          label: 'Content Included'
          rows: 15

        new TextOnlyView
          model: @model
          modelAttribute: 'TextOnlyView'
          text: """<p>The depositor may also wish to provide an image to accompany the dataset which may subsequently be used to advertise its availability on social media.  If no image is provided, the EIDC may source a suitable picture."""

      ]
    ,
      label: 'Data retention'
      title: 'Data retention'
      views: [

        new TextOnlyView
          model: @model
          modelAttribute: 'TextOnlyView'
          text: """
          <h2>Data given a DOI will be kept in perpetuity.</h2>
          <p>For data not given a DOI, the period for which the EIDC guarantees to curate data is ten years, after which it will be periodically reviewed and may be discarded. Please note below any exceptions to this policy.</p>
          """

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

        new TextOnlyView
          model: @model
          modelAttribute: 'TextOnlyView'
          text: """
          <p>Depositors may request that access to the data be restricted for an agreed period (embargoed).</p>
          <p>Approving embargoes and the negotiation of the duration of an embargo period are subject to funder requirements. For NERC-funded research, a reasonable embargo period is considered to be a maximum of two years <i><u>from the end of data collection.</u></i></p>
          <p>If the EIDC receives a request for access to data during the embargo period, it is treated as a request under the Environmental Information Regulations (EIR) and follows the designated NERC procedure for such requests.</p>
          """

        new TextareaView
          model: @model
          modelAttribute: 'availability'
          ModelType: OnlineResource
          label: 'Availability'
          multiline: true

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

        new TextOnlyView
          model: @model
          modelAttribute: 'TextOnlyView'
          text: """
          <p>The EIDC recommends that the depositor seeks guidance from their own institution and/or funding agency as to the appropriate licence.</p>
          """

        new PredefinedParentView
          model: @model
          modelAttribute: 'endUserLicence'
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
          modelAttribute: 'ownerOfIpr'
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

        new TextOnlyView
          model: @model
          modelAttribute: 'TextOnlyView'
          text: """
          <p>If the data is intended to supersede an existing dataset held by the EIDC, the depositor should explain why it is to be replaced, including details of any errors found.</p>
          """

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
        new TextOnlyView
          model: @model
          modelAttribute: 'TextOnlyView'
          text: """
          <p>If there is any other information you wish to provide, please include it below.</p>
          """

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

        new TextOnlyView
          model: @model
          modelAttribute: 'TextOnlyView'
          text: """
          <p>Data resources deposited with the EIDC have an entry in the EIDC data catalogue, enabling users to find and access them. Please provide the following information to help complete the catalogue record. Further details on discovery metadata are available from our website.</p>
          <p><em>Please note, this information is not fixed and may be subject to change and improvement over time</em></p>
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
