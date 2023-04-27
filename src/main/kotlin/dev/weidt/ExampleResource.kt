package dev.weidt

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType

@Path("/example")
class ExampleResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    fun hello() = "Hello from RESTEasy Reactive"

    // MATCH (u:User)-[:isIn]->(g:Group) WHERE g.name = 'admins' return g,u;
}
