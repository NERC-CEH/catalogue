import {
  CheckboxView,
  InspireThemeView,
  KeywordThemeView,
  KeywordVocabularyView,
  ParentView,
  TopicCategoryView
} from './views'
import { InspireTheme, KeywordTheme, TopicCategory } from './models'

export const createServiceAgreementKeywordView = (model) => {
  return [
    new ParentView({
      model,
      ModelType: TopicCategory,
      modelAttribute: 'topicCategories',
      label: 'ISO 19115 topic categories',
      ObjectInputView: TopicCategoryView,
      helpText: `\
<p>Please note these are very broad themes required by the metadata standard and should not be confused with science topics.</p>
<p>Multiple topic categories are allowed - please include all that are pertinent.  For example, "<i>Estimates of topsoil invertebrates</i>" = Biota <strong>and</strong> Environment <strong>and</strong> Geoscientific Information.</p>\
`
    }),
    new ParentView({
      model,
      ModelType: KeywordTheme,
      modelAttribute: 'keywordsTheme',
      label: 'Science topic',
      ObjectInputView: KeywordThemeView,
      multiline: false,
      helpText: 'These are used to populate the topic facet in the search interface - try to include at least one'
    }),
    new ParentView({
      model,
      modelAttribute: 'keywordsObservedProperty',
      label: 'Observed properties',
      ObjectInputView: KeywordVocabularyView,
      multiline: true,
      helpText: 'Controlled keywords describing the observed properties/variables contained in this data resource'
    }),
    new ParentView({
      model,
      modelAttribute: 'keywordsPlace',
      label: 'Places',
      ObjectInputView: KeywordVocabularyView,
      multiline: true,
      helpText: `\
        Controlled keywords describing geographic places pertinent to this resource.
        For example, named countries/regions in which the research was conducted.
        `
    }),
    new ParentView({
      model,
      modelAttribute: 'keywordsProject',
      label: 'Projects',
      ObjectInputView: KeywordVocabularyView,
      multiline: true,
      helpText: 'Controlled keywords describing projects that fund/support the creation of this resource'
    }),
    new ParentView({
      model,
      modelAttribute: 'keywordsInstrument',
      label: 'Instruments',
      ObjectInputView: KeywordVocabularyView,
      multiline: true,
      helpText: 'Controlled keywords describing instruments/sensors used to generate this data'
    }),
    new ParentView({
      model,
      modelAttribute: 'keywordsOther',
      label: 'Other keywords',
      ObjectInputView: KeywordVocabularyView,
      multiline: true,
      helpText: 'All other keywords not described elsewhere'
    })
  ]
}

export const createGeminiKeywordView = (model) => {
  return createServiceAgreementKeywordView(model).concat([
    new ParentView({
      model,
      ModelType: InspireTheme,
      modelAttribute: 'inspireThemes',
      label: 'INSPIRE theme',
      ObjectInputView: InspireThemeView,
      helpText: `\
<p>If the resource falls within the scope of an INSPIRE theme it must be declared here.</p>
<p>Conformity is the degree to which the <i class='text-red'>data</i> conforms to the relevant INSPIRE data specification.</p>\
`
    }),
    new CheckboxView({
      model,
      modelAttribute: 'notGEMINI',
      label: 'Exclude from GEMINI obligations',
      helpText: `
<p>Tick this box to exclude this resource from GEMINI/INSPIRE obligations.</p><p <b class='text-red'><span class='fa-solid fa-exclamation-triangle'>&nbsp;</span> WARNING.  This should only be ticked if the data DOES NOT relate to an area where an EU Member State exercises jurisdictional rights</b>.</p>
`
    })
  ])
}
