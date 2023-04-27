CREATE (a:User {username: 'admin'});
CREATE (b:User {username: 'user'});
CREATE (c:Group {name: 'admins'});
CREATE (d:Group {name: 'users'});

MATCH (u:User),(a:Group)
  WHERE (u.username = 'admin') AND a.name = 'admins'
CREATE (u)-[r:isIn]->(a);

MATCH (u:User),(a:Group)
  WHERE a.name = 'users'
CREATE (u)-[r:isIn]->(a);
