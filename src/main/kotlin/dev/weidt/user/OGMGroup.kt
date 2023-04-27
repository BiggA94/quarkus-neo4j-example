package dev.weidt.user

import com.fasterxml.jackson.annotation.JsonBackReference
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Relationship

@NodeEntity("Group")
class OGMGroup(var name: String = "") {
    @Id
    @GeneratedValue
    var id: Long? = null

    @Relationship(type = "isIn", direction = Relationship.Direction.INCOMING)
    @JsonBackReference
    var users: HashSet<OGMUser> = HashSet()
}
