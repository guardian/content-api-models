# Content API Thrift models

[![content-api-models-scala Scala version support](https://index.scala-lang.org/guardian/content-api-models/content-api-models-scala/latest-by-scala-version.svg?platform=jvm)](https://index.scala-lang.org/guardian/content-api-models/content-api-models-scala)
[![content-api-models-json Scala version support](https://index.scala-lang.org/guardian/content-api-models/content-api-models-json/latest-by-scala-version.svg?platform=jvm)](https://index.scala-lang.org/guardian/content-api-models/content-api-models-json)
[![CI](https://github.com/guardian/content-api-models/actions/workflows/ci.yml/badge.svg)](https://github.com/guardian/content-api-models/actions/workflows/ci.yml)

## Version Info

### 17.0.0 
* This release imports the `sbt-scrooge-typescript 1.4.0` sbt plugin which has the potential to introduce some [breaking changes](https://github.com/apache/thrift/blob/master/CHANGES.md#breaking-changes-2) for generated typescript mappings via thrift 0.13.0, specifically related to the [handling of `Int64`](https://issues.apache.org/jira/browse/THRIFT-4675) data.

# Publishing a new release

This repository has a Github Action that will create new releases for Sonatype and NPM when a new release is created in Github.

- Push the branch with the changes you want to release to Github.
- Begin creating a new release (here's a [quick link.](https://github.com/guardian/flexible-model/releases/new))
- Set the `Target` to your branch.
- Create a tag:
- - For a production release, the tag should be the new version number, e.g. `vX.X.X`. Beta releases are production releases – for example `v1.0.0-beta.0`.
- - For a snapshot release, the tag should ideally have the format `vX.X.X-SNAPSHOT`.
- **If you are intending to release a snapshot,** double-check that the "Set as pre-release" box is ticked.
- Click the "Publish release" button. The action will trigger, and your release should be on its way.

To release a package from your local machine, follow the instructions for [publishing a new version to Maven Central via Sonatype](https://docs.google.com/document/d/1rNXjoZDqZMsQblOVXPAIIOMWuwUKe3KzTCttuqS7AcY/edit#).

## Information about built bundles

The content-api-models project builds the following bundles: 

* content-api-models-scala - A jar containing Scrooge generated class files based on the Thrift definitions of the content api models found in the `content-api-models` dependency. 

* content-api-models-json - A jar containing Json serializers and deserializers. Used internally by the content api and also by the `content-api-scala-client` to convert from Elasticsearch returned json to the Scrooge-generated Thrift classes. As a client you should never need to depend on this explicitly, although you may have a transitive dependency on it if using the `content-api-scala-client`.

* content-api-models - A jar containing the Thrift definitions of the content api models only. As a client it is unlikely that you should ever need to depend on this but rather use the `content-api-models-scala` dependency instead.

* [@guardian/content-api-models](https://www.npmjs.com/package/@guardian/content-api-models) - An npm package containing the generated models and their type definitions.



## Publishing locally 

To publish a snapshot version locally.

```
sbt +publishLocal
```



