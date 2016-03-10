package tds.student.diagnostic.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class AbstractStatus implements Rated {

    private Rating rating;
    private String error;
    private String warning;

    public AbstractStatus(Rating rating) {
        this.rating = rating;
        this.error = null;
        this.warning = null;
    }

    public AbstractStatus(Rating rating, String error) {
        this.error = error;
        this.rating = rating;
        this.warning = null;
    }

    public void setRating(Rating rating) {
        this.rating = rating;
    }

    @JsonIgnore
    public Rating getRating() {
        return rating;
    }

    public Integer getStatusRating() {
        return rating.getStatusRating();
    }

    public String getStatusText() {
        return rating.getStatusText();
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }

    /**
     * When member objects are added to a rated object, this method updates the overall rating to the lowest level.
     * The rating integer value is used for comparison.  Rating is set with the enumeration.
     *
     * @param ratedList New member rated objects added to evaluate to determine this objects overall rating.
     */
    protected void updateRating(List<? extends Rated> ratedList) {

        Rated minimumRating = Iterables.getFirst(
                Ordering.natural()
                        .nullsLast()
                        .onResultOf(new Function<Rated, Integer>() {
                            public Integer apply(Rated item) {
                                return item.getStatusRating();
                            }
                        }).sortedCopy(ratedList)
                , null);

        if ( minimumRating != null && minimumRating.getStatusRating() < this.getStatusRating() ) {
            this.setRating(minimumRating.getRating());
        }
    }

    protected void updateRating(Rated rated) {

        if ( rated != null && rated.getStatusRating() < this.getStatusRating() ) {
            this.setRating(rated.getRating());
        }

    }


}
