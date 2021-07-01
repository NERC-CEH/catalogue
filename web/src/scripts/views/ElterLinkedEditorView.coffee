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
  'cs!models/editor/MapDataSource'
  'cs!views/editor/MapDataSourceView'
  'cs!views/editor/RelatedRecordView'
  'cs!views/deims/DeimsSiteView'
], (EditorView, SingleObjectView, InputView, CheckboxView, ReadOnlyView, TextareaView, ParentView, ParentLargeView, PredefinedParentView, PredefinedParentLargeView, ParentStringView, ResourceTypeView, ResourceType, AccessLimitationView, AccessLimitation, InspireTheme, InspireThemeView, TopicCategory, TopicCategoryView, ContactView, ResourceIdentifierView, DatasetReferenceDateView, MultipleDate, Contact, BoundingBox, BoundingBoxView, OnlineResourceView, OnlineResource, ResourceConstraintView, OtherConstraintView, TemporalExtentView,  ResourceMaintenanceView, SpatialReferenceSystemView, SpatialRepresentationTypeView, DescriptiveKeywordView, DescriptiveKeyword, DistributionFormatView, DistributionFormat, SpatialResolutionView, SpatialResolution, FundingView, Funding, SupplementalView, Supplemental, ServiceView, Service, MapDataSource, MapDataSourceView, RelatedRecordView, DeimsSiteView) -> EditorView.extend

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

        new InputView
          model: @model
          modelAttribute: 'title'
          label: 'Title'
          helpText: """
                    <p>Provide a title that best describes that data resource. Include references to the subject, spatial and temporal aspects of the data resource.</p>
                    <p>Only the leading letter and proper nouns of the title should be capitalised.  If it's necessary to include acronyms in the title, then include both the acronym (in parentheses) and the phrase/word from which it was formed. Acronyms should not include full-stops between each letter.</p>
                    <p>If there are multiple titles or translations of titles (e.g. in Welsh), these should be added as alternative titles.</p>
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

        new InputView
          model: @model
          modelAttribute: 'linkedDocumentUri'
          label: 'Linked document URL'
          helpText: """
                    <p>For creating linked documents, add the URL here.</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'linkedDocumentType'
          label: 'Linked document type'
          helpText: """
                    <p>Enter the type of the linked document.</p>
                    """
      ]
    ]

    EditorView.prototype.initialize.apply @
