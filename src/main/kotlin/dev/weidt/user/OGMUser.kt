package dev.weidt.user

import com.fasterxml.jackson.annotation.JsonManagedReference
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Relationship

@NodeEntity("User")
class OGMUser (var username: String = "") {
    @Id
    @GeneratedValue
    var id: Long? = null

    @Relationship(type = "isIn", direction = Relationship.Direction.OUTGOING)
    @JsonManagedReference
    var groups: HashSet<OGMGroup> = HashSet()
}
