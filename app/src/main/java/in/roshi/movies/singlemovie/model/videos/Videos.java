package in.roshi.movies.singlemovie.model.videos;

/**
 * Created by roshi on 03/07/17.
 */

public class Videos {
    private String id;

    private Results[] results;

    public String getId ()
    {
        return id;
    }

    public void setId (String id)
    {
        this.id = id;
    }

    public Results[] getResults ()
    {
        return results;
    }

    public void setResults (Results[] results)
    {
        this.results = results;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [id = "+id+", results = "+results+"]";
    }

}
