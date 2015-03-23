resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"

resolvers += "Maven Central Server" at "http://repo1.maven.org/maven2"

resolvers += "Neo4j Scala Repo" at "http://m2.neo4j.org/content/repositories/releases"

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.8")

// web plugins

addSbtPlugin("com.typesafe.sbt" % "sbt-coffeescript" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-jshint" % "1.0.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-rjs" % "1.0.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-mocha" % "1.0.0")
