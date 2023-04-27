package dev.weidt.user

import com.fasterxml.jackson.databind.ObjectMapper
import io.quarkus.resteasy.reactive.links.InjectRestLinks
import io.quarkus.resteasy.reactive.links.RestLink
import io.quarkus.resteasy.reactive.links.RestLinkType
import io.quarkus.resteasy.reactive.links.RestLinksProvider
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.Uni
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.metrics.MetricUnits
import org.eclipse.microprofile.metrics.annotation.Timed
import org.jboss.resteasy.reactive.RestPath
import org.jboss.resteasy.reactive.common.util.RestMediaType
import org.neo4j.driver.Driver
import org.neo4j.driver.Record
import org.neo4j.driver.reactive.ReactiveResult
import org.neo4j.driver.reactive.ReactiveSession
import org.neo4j.ogm.session.SessionFactory


@Path("user")
class UserResource {
    @Inject
    lateinit var driver: Driver

    @Inject
    lateinit var linksProvider: RestLinksProvider

    // this is with plain neo driver, for ogm see below

    @Produces(RestMediaType.APPLICATION_HAL_JSON)
    @GET
//    @RestLink(rel = "list")
//    @InjectRestLinks(RestLinkType.TYPE)
    @Path("admins")
    @Timed(name = "adminsTimer", description = "A measure of how long it takes to perform the primality test.", unit = MetricUnits.MILLISECONDS)
    fun getAdmins(): Multi<User> {
        return Multi.createFrom().resource({ driver.session(ReactiveSession::class.java) }) { session ->
            session.executeRead { tx ->
                val result =
                    tx.run("MATCH (u:User)-[:isIn]->(g:Group {name: 'admins'}) OPTIONAL MATCH (u:User)-[:isIn]->(g2:Group) return u, collect(distinct g2.name) as groups;")
                Multi.createFrom().publisher(result).flatMap(ReactiveResult::records).map{ x ->
                    User.fromNode(x.get("u").asNode(), x.get("groups").asList().joinToString());
                }
            }
        }
            .withFinalizer(Companion::sessionFinalizer)
            .invoke { user -> println(linksProvider.getInstanceLinks(user)) }
    }

    @Produces(RestMediaType.APPLICATION_HAL_JSON)
    @GET
    @RestLink(rel = "list")
    @Timed(name = "usersTimer", description = "A measure of how long it takes to perform the primality test.", unit = MetricUnits.MILLISECONDS)
    fun getUsers(): List<User> {
        return Multi.createFrom().resource({ driver.session(ReactiveSession::class.java) }) { session ->
            session.executeRead { tx ->
                val result =
                    tx.run("MATCH (u:User)-[:isIn]->(g:Group {name: 'users'}) OPTIONAL MATCH (u:User)-[:isIn]->(g2:Group) return u, collect(distinct g2.name) as groups;")
                Multi.createFrom().publisher(result).flatMap(ReactiveResult::records).map{ x ->
                    User.fromNode(x.get("u").asNode(), x.get("groups").asList().joinToString());
                }
            }
        }
            .withFinalizer(Companion::sessionFinalizer)
            .invoke { user -> println(linksProvider.getInstanceLinks(user)) }
            .collect().asList().await().indefinitely()
    }

    @Produces(value = [RestMediaType.APPLICATION_HAL_JSON, MediaType.APPLICATION_JSON])
    @GET
    @Path("{id}")
    @RestLink(rel = "self")
//    @InjectRestLinks(RestLinkType.INSTANCE)
    @Timed(name = "singleUserTimer", description = "A measure of how long it takes to perform the primality test.", unit = MetricUnits.MILLISECONDS)
    fun getUser(@RestPath id: String): Uni<User> {
        return Multi.createFrom().resource({ driver.session(ReactiveSession::class.java) }) { session ->
            session.executeRead { tx ->
                val result = tx.run("MATCH (u:User)-[]->(g:Group) WHERE id(u) = $id return u, collect(g.name) as groups;")
                val flatMap: Multi<Record> = Multi.createFrom().publisher(result).flatMap(ReactiveResult::records)
                flatMap.map{ x ->
                    User.fromNode(x.get("u").asNode(), x.get("groups").asList().joinToString());
                }
            }
        }
            .withFinalizer(Companion::sessionFinalizer)
            .toUni()
    }

    // with ogm

    @Inject
    lateinit var neo: SessionFactory

    @Produces(value = [RestMediaType.APPLICATION_HAL_JSON, MediaType.APPLICATION_JSON])
    @GET
    @Path("ogm/users")
    fun getUsersWithOgm(): Collection<OGMUser> {
        return neo.openSession()
            .query(OGMUser::class.java, "MATCH (u:User)--(g:Group {name:'users'}) return u,g;", emptyMap<String, String>()) as Collection<OGMUser>
    }

    @Produces(value = [RestMediaType.APPLICATION_HAL_JSON, MediaType.APPLICATION_JSON])
    @GET
    @Path("ogm")
    fun getAllUsersWithOgm(): Collection<OGMUser> {
        return neo.openSession()
            .loadAll(OGMUser::class.java)
    }

    companion object {
        fun sessionFinalizer(session: ReactiveSession): Uni<Void> {
            return Uni.createFrom().publisher(session.close())
        }
    }

}
