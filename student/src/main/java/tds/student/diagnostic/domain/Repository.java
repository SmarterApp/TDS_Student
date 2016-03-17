package tds.student.diagnostic.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Repository extends AbstractStatus {

    private String Schema;
    private List<DatabaseOperation> databaseOperations;


    public Repository(Rating rating, String schema, List<DatabaseOperation> databaseOperations) {
        super(rating);
        Schema = schema;
        this.databaseOperations = databaseOperations;
        super.updateRating(databaseOperations);
    }

    public Repository(Rating rating, String schema) {
        super(rating);
        Schema = schema;
    }

    public Repository(String schema) {
        super(Rating.IDEAL);
        Schema = schema;
    }

    public Repository(Rating rating) {
        super(rating);
    }

    public List<DatabaseOperation> getDatabaseOperations() {
        return databaseOperations;
    }

    public void setDatabaseOperations(List<DatabaseOperation> databaseOperations) {
        this.databaseOperations = databaseOperations;
        this.updateRating(databaseOperations);
    }

    public String getSchema() {
        return Schema;
    }

    public void setSchema(String schema) {
        Schema = schema;
    }

}
