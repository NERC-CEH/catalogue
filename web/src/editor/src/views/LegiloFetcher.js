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
      console.error('Error fetching variables:', error)
      throw error
    })
}

export function fetchKeywordsFromLegilo (id, location = 'eidc') {
  const apiUrl = `/documents/${id}/suggestKeywords?location=${location}`

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
