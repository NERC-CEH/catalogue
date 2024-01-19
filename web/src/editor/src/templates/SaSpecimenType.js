import _ from 'underscore'

export default _.template(`
<select data-name="value" class="editor-input">
    <option value="" selected >- Add specimen type -</option>
    <option value="Air">Air</option>
    <option value="DNA">DNA</option>
    <option value="Ectoparasite">Ectoparasite</option>
    <option value="Endoparasite">Endoparasite</option>
    <option value="Fossil">Fossil</option>
    <option value="Fresh water">Fresh water</option>
    <option value="Gas">Gas</option>
    <option value="Ice core">Ice core</option>
    <option value="Pathogen">Pathogen</option>
    <option value="Rain water">Rain water</option>
    <option value="RNA">RNA</option>
    <option value="Rock">Rock</option>
    <option value="Sea water">Sea water</option>
    <option value="Sediment">Sediment</option>
    <option value="Seed">Seed</option>
    <option value="Soil">Soil</option>
    <option value="Surface water">Surface water</option>
    <option value="Vegetation">Vegetation</option>
</select>
`)
