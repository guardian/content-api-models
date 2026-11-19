# story-packages-model

This repository contains the Thrift schema required for displaying story packages.

## Downloading

To download you should be able to simply add a dependency similar to the following (with your desired version):

`"com.gu" %% "story-packages-model" % "x.y.z"`

## How to release

Ensure you have the following installed on your machine:
 - `tsc` (`brew install typescript`)
 - `npm` (not sure! there are so many ways to install it)
 
Ensure you have an NPM account, part of the [@guardian](https://www.npmjs.com/org/guardian) org with a [configured token](https://docs.npmjs.com/creating-and-viewing-authentication-tokens)

You'll also need a sonatype account to publish the library to Maven.

### New!
Beta releases from a WIP branch can be deployed to Maven & npm. To do this, start sbt with
`sbt -DRELEASE_TYPE=beta`
When you follow the remaining instructions, you'll see a couple of new prompts and version number formats. We leave it
up to the developer to keep track of the versions you've released this way, but you should always update the next
version to reflect the beta status of the code (i.e. don't just let it revert to -SNAPSHOT which it will want to do by
default). This is particularly important for npmRelease which you have to manually supply with a version number. Just
use the same version identifier as you released to Maven.

If you don't wish to make a beta release, just start sbt as normal (without the -DRELEASE_TYPE parameter) and continue
with the steps that follow.

```sbtshell
release // will release the scala / thrift projects
project typescriptClasses
releaseNpm 1.0.0 // you have to specify the version again
```
