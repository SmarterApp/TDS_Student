package tds.student.diagnostic.domain;

public interface Rated {

    void setRating(Rating rating);

    Rating getRating();

    Integer getStatusRating();

}
