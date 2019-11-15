package at.htl.formula1.boundary;

import at.htl.formula1.entity.Driver;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;


@Path("results")
public class ResultsEndpoint {

    @PersistenceContext
    EntityManager em;

    /**
     * @param name als QueryParam einzulesen
     * @return JsonObject
     */
    @GET
    public JsonObject getPointsSumOfDriver(
            @QueryParam("name") String name
    ) {
        if (name == null) {
            return null;
        }

        Long sum = this.em
                .createNamedQuery("Result.getDriverPointSum", Long.class)
                .setParameter("NAME", name)
                .getSingleResult();

        if (sum == null) {
            return null;
        }

        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        JsonObject jsonObject = objectBuilder
                .add("driver", name)
                .add("points", sum)
                .build();

        return jsonObject;
    }

    /**
     * @param id des Rennens
     * @return
     */
    public Response findWinnerOfRace(long id) {
        return null;
    }


    // Ergänzen Sie Ihre eigenen Methoden ...

}
