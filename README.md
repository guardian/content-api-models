# Content API Thrift models

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.gu/content-api-models/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.gu/content-api-models)
[![Build Status](https://travis-ci.org/guardian/content-api-models.svg?branch=master)](https://travis-ci.org/guardian/content-api-models)

To release:

```
$ sbt release
```

will publish to [Maven Central](http://search.maven.org/) via Sonatype. You will need Sonatype credentials and a PGP key. It can take up to 2hrs to show up in search.

## Information about built jars

The content-api-models project builds the following jar files: 

* content-api-models-scala - Scrooge generated class files based on the Thrift definitions of the content api models found in the `content-api-models` dependency. 

* content-api-models-json - Json parsers and deserializers. Used internally by the content api and also by the `content-api-scala-client` to convert from Elasticsearch returned json to the Scrooge-generated Thrift classes. As a client you should never need to depend on this explicitly, although you may have a transitive dependency on it if using the `content-api-scala-client`.

* content-api-models - Contains the Thrift definitions of the content api models only. As a client it is unlikely that you should ever need to depend on this but rather use the `content-api-models-scala` dependency instead.

