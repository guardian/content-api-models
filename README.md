# Content API Thrift models

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.gu/content-api-models/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.gu/content-api-models)
[![Build Status](https://travis-ci.org/guardian/content-api-models.svg?branch=master)](https://travis-ci.org/guardian/content-api-models)

## Version Info

### 17.0.0 
* This release imports the `sbt-scrooge-typescript 1.4.0` sbt plugin which has the potential to introduce some [breaking changes](https://github.com/apache/thrift/blob/master/CHANGES.md#breaking-changes-2) for generated typescript mappings via thrift 0.13.0, specifically related to the [handling of `Int64`](https://issues.apache.org/jira/browse/THRIFT-4675) data.

## Releasing

Ensure the version is composed of three parts (`1.2.3`) as NPM doesn't accept shorter versions such as `1.2`.

The `release cross` command will publish to [Maven Central](http://search.maven.org/) via Sonatype. You will need Sonatype credentials and a PGP key. It can take up to 2hrs to show up in search.

`release NPM` will release the typescript package to NPM. Ensure you have an NPM account, part of the [@guardian](https://www.npmjs.com/org/guardian) org with a [configured token](https://docs.npmjs.com/creating-and-viewing-authentication-tokens)

To release, in the SBT repl:
```sbtshell
release cross // will release the scala / thrift projects
project typescript
releaseNpm <version> // you have to specify the version again i.e releaseNpm 1.0.0
```

If you see the message `Cannot run program "tsc"` you will need to install TypeScript:
```
npm install -g typescript
```

It is worth noting that different teams follow different practices for model releases. In CAPI, we always release from master/main, so when you are happy with your local release, you can go ahead and make your PR. Once that is merged into main, you can publish your release to Maven. Two consecutive commits will automatically be made to master/main updating the next version number.

### Releasing SNAPSHOT or release candidate versions

It's also possible to release a snapshot build to Sonatype's snapshot repo with no promotion to Maven Central. This can be useful for trialling a test or upgraded dependency internally.

To do this, start sbt with a RELEASE_TYPE variable;

`sbt -DRELEASE_TYPE=snapshot`

Then, when you run `release cross`, you'll be asked to confirm that you intend to make a SNAPSHOT release, and if you proceed will be prompted to complete the snapshot version details. Whatever you specify here will be written back to the `version.sbt` but this won't be automatically committed back to github.

You are able to re-release the same snapshot version repeatedly (which is handy if you're having GPG-related issues etc.)

Making a release candidate is also possible by using the appropriate RELEASE_TYPE variable;

`sbt -DRELEASE_TYPE=candidate`

Here, the main differences are that the version number is expected to be of the format 1.2.3-RC1, and the release will be promoted to Maven Central. 

As with the snapshot release process you'll be prompted to confirm and specify the version number you need to use, and changes applied to `version.sbt` will not be automatically committed to github etc.

Unlike a production release, these alternatives are useful for testing/developing with another team or application and can be executed from a branch so there's no need to have everything merged into main/master branches prior to making your changes available.

**Note:** `releaseNpm` also appears happy to accept non-production version numbers but may be less forgiving with re-publishing the same version number. This may require more effort if it becomes a problem.

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



