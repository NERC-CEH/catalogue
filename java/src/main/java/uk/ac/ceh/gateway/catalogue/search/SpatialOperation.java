package uk.ac.ceh.gateway.catalogue.search;

public enum SpatialOperation {
    INTERSECTS ("Intersects"),
    ISWITHIN ("IsWithin");
    
    private final String operation;
    
    SpatialOperation(String operation) {
        this.operation = operation;
    }
    
    public String getOperation() {
        return operation;
    }
}
