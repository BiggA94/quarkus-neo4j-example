package dev.weidt.user

import org.neo4j.driver.types.Node;

data class User(
    val id: String = "",
    val userName: String = "",
    val groups: String = ""
){
    companion object {
        fun fromNode(node: Node, groups: String = ""): User {
            return User(
                id = node.elementId(),
                userName = node.get("username").asString(),
                groups = groups
            )
        }
    }
}
