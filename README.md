# Content API Thrift models

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.gu/content-api-models_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.gu/content-api-models_2.12)
[![Build Status](https://travis-ci.org/guardian/content-api-models.svg?branch=master)](https://travis-ci.org/guardian/content-api-models)

To release, in the SBT repl:

```sbtshell
release cross // will release the scala / thrift projects
project typescript
releaseNpm <version> // you have to specify the version again i.e releaseNpm 1.0.0
```

will publish to [Maven Central](http://search.maven.org/) via Sonatype. You will need Sonatype credentials and a PGP key. It can take up to 2hrs to show up in search.

It will also release to NPM, ensure you have an NPM account, part of the [@guardian](https://www.npmjs.com/org/guardian) org with a [configured token](https://docs.npmjs.com/creating-and-viewing-authentication-tokens)

## Information about built bundles

The content-api-models project builds the following bundles: 

* content-api-models-scala - A jar containing Scrooge generated class files based on the Thrift definitions of the content api models found in the `content-api-models` dependency. 

* content-api-models-json - A jar containing Json serializers and deserializers. Used internally by the content api and also by the `content-api-scala-client` to convert from Elasticsearch returned json to the Scrooge-generated Thrift classes. As a client you should never need to depend on this explicitly, although you may have a transitive dependency on it if using the `content-api-scala-client`.

* content-api-models - A jar containing the Thrift definitions of the content api models only. As a client it is unlikely that you should ever need to depend on this but rather use the `content-api-models-scala` dependency instead.

* [@guardian/content-api-models](https://www.npmjs.com/package/@guardian/content-api-models) - An npm package containing the generated models and their type definitions.

