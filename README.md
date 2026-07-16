# Content API Thrift models

[![content-api-models-scala Scala version support](https://index.scala-lang.org/guardian/content-api-models/content-api-models-scala/latest-by-scala-version.svg?platform=jvm)](https://index.scala-lang.org/guardian/content-api-models/content-api-models-scala)
[![content-api-models-json Scala version support](https://index.scala-lang.org/guardian/content-api-models/content-api-models-json/latest-by-scala-version.svg?platform=jvm)](https://index.scala-lang.org/guardian/content-api-models/content-api-models-json)
[![CI](https://github.com/guardian/content-api-models/actions/workflows/ci.yml/badge.svg)](https://github.com/guardian/content-api-models/actions/workflows/ci.yml)
[![Release](https://github.com/guardian/content-api-models/actions/workflows/release.yml/badge.svg)](https://github.com/guardian/content-api-models/actions/workflows/release.yml)

## Version Info

### 17.0.0 
* This release imports the `sbt-scrooge-typescript 1.4.0` sbt plugin which has the potential to introduce some [breaking changes](https://github.com/apache/thrift/blob/master/CHANGES.md#breaking-changes-2) for generated typescript mappings via thrift 0.13.0, specifically related to the [handling of `Int64`](https://issues.apache.org/jira/browse/THRIFT-4675) data.

## Publishing a new release

This repo uses [`gha-scala-library-release-workflow`](https://github.com/guardian/gha-scala-library-release-workflow)
to automate publishing releases (both full & preview releases) - see
[**Making a Release**](https://github.com/guardian/gha-scala-library-release-workflow/blob/main/docs/making-a-release.md).


## Information about built bundles

The content-api-models project builds the following bundles: 

* content-api-models-scala - A jar containing Scrooge generated class files based on the Thrift definitions of the content api models found in the `content-api-models` dependency. 

* content-api-models-json - A jar containing JSON serializers and deserializers. Used internally by the content api and also by the `content-api-scala-client` to convert from Elasticsearch returned json to the Scrooge-generated Thrift classes. As a client you should never need to depend on this explicitly, although you may have a transitive dependency on it if using the `content-api-scala-client`.

* content-api-models - A jar containing the Thrift definitions of the content api models only. As a client it is unlikely that you should ever need to depend on this but rather use the `content-api-models-scala` dependency instead.

* [@guardian/content-api-models](https://www.npmjs.com/package/@guardian/content-api-models) - An npm package containing the generated models and their type definitions.

## Publishing locally 

To publish a snapshot version locally.

```
sbt +publishLocal
```

## Tests

There are two sets of tests in this repository: tests in [CirceRoundTripSpec.scala](./json/src/test/scala/com/gu/contentapi/json/CirceRoundTripSpec.scala) which test the JSON encoders and decoders, and tests in [ThriftRoundTripSpec.scala](scala/src/test/scala/com.gu.contentapi.scala/ThriftRoundTripSpec.scala) which test the thrift serialisation and deserialisation.

These tests verify that output from old versions of this library can still be decoded by the current version, and that the resulting output from the current version is the same. If these tests pass on a PR, you can be reasonably confident that the changes in the PR won’t break consumers of `content-api-models-scala` or `content-api-models-json`. (One limitation of these tests is that they don’t cover the entirety of the model. For example, if a PR changes a field from optional to required in a struct which is not covered by the tests, the tests will still pass.)

There aren’t any tests here of `content-api-models-typescript`, but that functionality is tested [in scrooge-extras](https://github.com/guardian/scrooge-extras/blob/main/scrooge-generator-typescript/).

### Running tests

This repository is cross-compiled for scala 2.12 and 2.13, so the CI workflow uses `sbt +test` to run the tests against all versions in `crossScalaVersions`.

You can also run the tests for a specific Scala version if necessary:

```
sbt ++2.12.11 test
```



