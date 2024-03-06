import _ from 'underscore'

export default _.template(`

    <div class="form-operation">
      <p>Choose how to query records:</p>
      <div>
        <label>
          <input class="spatialOp" data-op="intersects" type="radio" name="operations" id="opIntersects" <% if (op === 'intersects') {%> checked<%}%>>
          Intersects
        </label>
        <label>
          <input class="spatialOp" data-op="iswithin" type="radio" name="operations" id="opWithin" <% if (op === 'iswithin') {%> checked<%}%>>
          Within
        </label>
      </div>
    </div>
    <div class="map"></div>
`)
