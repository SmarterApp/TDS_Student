package tds.student.diagnostic.domain;

public enum Rating {

    IDEAL(4, "Ideal"),
    RECOVERING(3, "Recovering"),
    WARNING(2, "Warning"),
    DEGRADED(1,"Degraded"),
    FAILED(0,"Failed")
    ;

    private final Integer rating;
    private final String text;

    Rating(final Integer rating, final String text) {
        this.rating = rating;
        this.text = text;
    }

    public Integer getStatusRating() {
        return this.rating;
    }

    public String getStatusText() {
        return this.text;
    }



}
