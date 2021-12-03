define [
  'backbone'
  'underscore'
  'cs!views/EditorView'
  'cs!views/editor/SingleObjectView'
  'cs!views/editor/InputView'
  'cs!views/editor/TextareaView'
  'cs!views/editor/ParentView'
  'cs!views/editor/PredefinedParentView'
  'cs!views/editor/AccessLimitationView'
  'cs!models/editor/AccessLimitation'
  'cs!models/editor/InspireTheme'
  'cs!views/service-agreement/CategoryView'
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
  'cs!views/editor/ReadOnlyView'
  'cs!views/editor/ParentStringView'
  'cs!models/editor/BoundingBox'
  'cs!views/editor/BoundingBoxView'
  'cs!views/service-agreement/TextOnlyView'
  'cs!views/service-agreement/AuthorView'
  'cs!views/service-agreement/FileView'
  'cs!views/service-agreement/EndUserLicenceView'
  'cs!views/editor/FundingView'
  'cs!models/editor/Funding'
], (Backbone, _, EditorView, SingleObjectView, InputView, TextareaView, ParentView, PredefinedParentView, AccessLimitationView, AccessLimitation, InspireTheme, CategoryView, ContactView, ResourceIdentifierView, DatasetReferenceDateView, Contact, OnlineResourceView, OnlineResource, ResourceConstraintView, DescriptiveKeywordView, DescriptiveKeyword, DistributionFormatView, DistributionFormat, MapDataSource, ReadOnlyView, ParentStringView, BoundingBox, BoundingBoxView, TextOnlyView, AuthorView, FileView, EndUserLicenceView, FundingView, Funding) -> EditorView.extend

  initialize: ->
    @delegate "click #exitWithoutSaving": "exit"
    @delegate "click #editorExit": "attemptExit"

    @sections = [
      label: 'General'
      title:  ''
      views: [

        new TextOnlyView
          model: @model
          text: """<p>Deposit Reference should be a valid EIDC Jira ticket i.e. EIDCHELP-{n}</p>"""

        new InputView
          model: @model
          modelAttribute: 'depositReference'
          label: 'Deposit Reference'

        new TextOnlyView
          model: @model
          text: """<p>Provide a title that best describes that data resource. Include references to the subject, spatial and temporal aspects of the data resource.</p>
                <p>Only the leading letter and proper nouns of the title should be capitalised.  If it's necessary to include acronyms in the title, then include both the acronym (in parentheses) and the phrase/word from which it was formed. Acronyms should not include full-stops between each letter.</p>
                <p>If there are multiple titles or translations of titles (e.g. in Welsh), these should be added as alternative titles.</p>
                """

        new InputView
          model: @model
          modelAttribute: 'title'
          label: 'Title'

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

        new ReadOnlyView
          model: @model
          modelAttribute: 'eidcContactDetails'
          label: 'For the EIDC: contact details'

      ]
    ,
      label: 'Data identification and citation'
      title: 'Data identification and citation'
      views: [

        new ReadOnlyView
          model: @model
          modelAttribute: 'id'
          label: 'Data identifier'

        new ParentView
          model: @model
          ModelType: Contact
          modelAttribute: 'authors'
          label: 'Authors'
          ObjectInputView: AuthorView
          multiline: true
      ]
    ,
      label: 'Policies & Legislation'
      title: 'Policies & Legislation'
      views: [

        new TextOnlyView
          model: @model
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
          text: """<p>The depositor may also wish to provide an image to accompany the dataset which may subsequently be used to advertise its availability on social media.  If no image is provided, the EIDC may source a suitable picture.</p>"""

        new TextOnlyView
          model: @model
          text: """<p>Include here details of any grants or awards that were used to generate this resource.</p>
                   <p>If you include funding information, the Funding body is MANDATORY, other fields are useful but optional.</p>
                   <p>Award URL is either the unique identifier for the award or sa link to the funder's  grant page (if it exists). It is <b>NOT</b> a link to a project website.</p>
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
      ]
    ,
      label: 'The Data'
      title: 'The Data'
      views: [

        new InputView
          model: @model
          modelAttribute: 'fileNumber'
          label: 'Number of data files'

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

        new TextareaView
          model: @model
          modelAttribute: 'relatedDataHoldings'
          label: 'Related Data Holdings'
          rows: 15

        new SingleObjectView
          model: @model
          modelAttribute: 'dataCategory'
          label: 'Data Category'
          ObjectInputView: CategoryView
      ]
    ,
      label: 'Supporting documentation'
      title: 'Supporting documentation'
      views: [

        new TextOnlyView
          model: @model
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
          text: """<p>The depositor may also wish to provide an image to accompany the dataset which may subsequently be used to advertise its availability on social media.  If no image is provided, the EIDC may source a suitable picture."""

      ]
    ,
      label: 'Data retention'
      title: 'Data retention'
      views: [

        new TextOnlyView
          model: @model
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
          text: """
          <p>Depositors may request that access to the data be restricted for an agreed period (embargoed).</p>
          <p>Approving embargoes and the negotiation of the duration of an embargo period are subject to funder requirements. For NERC-funded research, a reasonable embargo period is considered to be a maximum of two years <i><u>from the end of data collection.</u></i></p>
          <p>If the EIDC receives a request for access to data during the embargo period, it is treated as a request under the Environmental Information Regulations (EIR) and follows the designated NERC procedure for such requests.</p>
          """

        new TextareaView
          model: @model
          modelAttribute: 'availability'
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
          text: """
          <p>The EIDC recommends that the depositor seeks guidance from their own institution and/or funding agency as to the appropriate licence.</p>
          """

        new SingleObjectView
          model: @model
          modelAttribute: 'endUserLicence'
          label: 'End user license'
          ObjectInputView: EndUserLicenceView
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

        new TextOnlyView
          model: @model
          text: """
                <p>The names of Owner should be in the format <code>Surname, First Initial. Second Initial.</code> For example <i>Brown, A.B.</i></p>
                <p>Role and organisation name are mandatory.</p>
                <p>The preferred identifier for individuals is an ORCiD.  You must enter the identifier as a <i>fully qualified</i> ID (e.g.  <b>https://orcid.org/1234-5678-0123-987X</b> rather than <b>1234-5678-0123-987X</b>).</p>
                """

        new ParentView
          model: @model
          ModelType: Contact
          modelAttribute: 'ownersOfIpr'
          label: 'Owner of IPR'
          ObjectInputView: ContactView
          multiline: true

      ]
    ,
      label: 'Superseding existing data'
      title: 'Superseding existing data (if applicable)'
      views: [

        new TextOnlyView
          model: @model
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

        new TextOnlyView
          model: @model
          Text: """
                <p>A bounding box representing the limits of the data resource's study area.</p>
                <p>If you do not wish to reveal the exact location publicly (for example, if locations are sensitive) it is recommended that you generalise the location.</p>
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
      ]
    ]

    EditorView.prototype.initialize.apply @


  attemptExit: ->
    if @saveRequired
      @$('#confirmExit').modal 'show'
    else
      do @exit

  exit: ->
    @$('#confirmExit').modal 'hide'
    _.invoke @sections, 'remove'
    do @remove

    if Backbone.history.location.pathname == "/documents/#{@model.get 'id'}"
      Backbone.history.location.replace "/service-agreement/#{@model.get 'id'}"
    else
      do Backbone.history.location.reload
