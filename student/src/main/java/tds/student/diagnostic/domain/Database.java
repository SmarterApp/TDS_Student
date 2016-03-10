package tds.student.diagnostic.domain;

import java.util.List;

public class Database extends AbstractStatus {

    private List<Repository> repositories;

    public Database(Rating rating, List<Repository> repositories) {
        super(rating);
        this.repositories = repositories;
        super.updateRating(repositories);
    }

    public List<Repository> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<Repository> repositories) {
        this.repositories = repositories;
    }

}
