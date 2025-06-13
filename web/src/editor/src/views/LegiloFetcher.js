import $ from 'jquery'
import { LegiloKeyword, LegiloVariable } from '../models'

export function fetchVariablesFromLegilo (id) {
  const apiUrl = `/documents/${id}/suggestVariables`

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

export function fetchKeywordsFromLegilo (id) {
  const apiUrl = `/documents/${id}/suggestKeywords`

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
