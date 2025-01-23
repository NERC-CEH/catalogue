import $ from 'jquery'
import { LegiloKeyword, LegiloVariable } from '../models'

export function fetchKeywordsFromLegilo (model) {
  const datasetId = model.get('id')
  const apiUrl = `/documents/${datasetId}/suggestKeywords`

  return $.getJSON(apiUrl)
    .then(data => data.map(keywordData => new LegiloKeyword({
      name: keywordData.name,
      confidence: keywordData.confidence,
      uri: keywordData.matched_url || ''
    })))
    .catch(error => {
      console.error('Error fetching keywords:', error)
      throw error
    })
}

export function fetchVariablesFromLegilo (model) {
  const datasetId = model.get('id')
  const apiUrl = `/documents/${datasetId}/suggestVariables`

  return $.getJSON(apiUrl)
    .then(data => data.map(variableData => new LegiloVariable({
      name: variableData.name,
      longName: variableData.longName,
      units: variableData.units,
      meaning: variableData.meaning,
      confidence: variableData.confidence
    })))
    .catch(error => {
      console.error('Error fetching variaqbles:', error)
      throw error
    })
}
