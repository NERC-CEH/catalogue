import _ from 'underscore'

export default _.template(`
    <div class="map"></div>
    <div class="form-operation">
      <div>
        <input class="spatialOp" data-op="intersects" type="radio" name="operations" id="opIntersects" <% if (op == 'intersects') {%> checked<%}%>>
        <label>Intersects</label>
      </div>
      <div>
        <input class="spatialOp" data-op="iswithin" type="radio" name="operations" id="opWithin" <% if (op == 'iswithin') {%> checked<%}%>>
        <label>Within</label>
      </div>
    </div>
`)
