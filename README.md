gdmc: Gradle Dependency Management Center
=========================================
An attempt to add some sanity to maven dependencies across multiple gradle projects

## Why
TODO

## Usage
Add gdmc to your buildscript dependencies...
```groovy
buildscript {
  repositories {
    jcenter()
  }
  dependencies {
    classpath 'com.episode6.hackit.gdmc:gdmc:0.0.9.8-SNAPSHOT'
  }
}
```

In your project's build.gradle, apply the gdmc plugin and define your dependencies without versions
```groovy
apply plugin: 'java'
apply plugin: 'com.episode6.hackit.gdmc'

dependencies {

  // Leave versions out of your dependency declarations
  compile 'net.sourceforge.findbugs:jsr305'
  compile 'com.episode6.hackit.chop:chop-core'

  // you can also use the map format without a version
  testCompile group: 'junit', name: 'junit'
}
```

Execute the `gdmcResolve` task and the latest release versions of your new / missing dependencies will be resolved into a `gdmc.json` file. This file should be added to your source control and will be the source of truth for what versions are used to build your project. It will look something like this...
```json
// auto generated via gdmcResolve task
{
    "com.episode6.hackit.chop:chop-core": {
        "groupId": "com.episode6.hackit.chop",
        "artifactId": "chop-core",
        "version": "0.1.8"
    },
    "junit:junit": {
        "groupId": "junit",
        "artifactId": "junit",
        "version": "4.12"
    },
    "net.sourceforge.findbugs:jsr305": {
        "groupId": "net.sourceforge.findbugs",
        "artifactId": "jsr305",
        "version": "1.3.7"
    }
}
```

If you're adding gdmc to an existing project with many dependencies, you can can execute the `gdmcImport` task before removing the versions from your build.gradle. This will import the fully-qualified dependencies from your project into the `gdmc.json` file instead of resolving their latest versions.


## gdmc file
gdmc will look for it's json file in the following locations in the following order
- If the property `gdmc.file` contains a valid file path, it will be used.
- `$rootDir/gdmc.json` will be used if it exists
- `$rootDir/gdmc/gdmc.json` will be used if it exists.

If no valid gdmc file is found, `$rootDir/gdmc.json` will be used.

## gdmc tasks
gdmc provides the following tasks to handle resolving, importing and upgrading dependencies.
- `gdmcResolve`: Find any unmapped dependencies in the project (i.e. version not defined in gradle, and key not mapped in gdmc), resolve the latest release version of said dependencies, and add them to gdmc.json
 - Note: some plugins cause dependencies to be resolved before this task gets a chance to execute (android gradle build tools is a known example). If you see an unmapped dependency error when executing this task, simply override the `gdmc.forceResolve` gradle property by running
 ```
 ./gradlew -Pgdmc.forceResolve=true gdmcResolve
 ```
- `gdmcImport`: Find fully-qualified dependencies in the project (i.e. versions are defined in-line in build.gradle) and add those explicit versions to gdmc.json
 - To overwrite existing entries in gdmc.json, override the `gdmc.overwrite` gradle property by running
 ```
 ./gradlew -Pgdmc.overwrite gdmcImport
 ```
- `gdmcImportTransitive`: Same as `gdmcImport` except it will also import all transitive dependencies to your gdmc.json
- `gdmcUpgrade`: Find all properly mapped gdmc dependencies in your project, and resolve the latest release versions of each of them. Then dump those new versions into gdmc.json, overwriting whatever was there.
- `gdmcUpgradeAll`: Upgrade all entries in your gdmc.json file, regardless of if they're defined in you project.
- `gdmcImportSelf`: Add/update an entry in gdmc.json for this project, using the `group`, `name`, and `version` defined in gradle.
- `gdmcValidateSelf`: A validation task that gets added as a dependant task to check and test. If the project's `version` does not include `SNAPSHOT`, ensures that current version referenced in gdmc.json
 - You should only need gdmc self validation if sharing a single gdmc file with multiple projects/repos. If you don't need this validation it can be disabled like so...
 ```groovy
 gdmcValidateSelf.required = {false}
 ```

## gdmc aliases
If you want to define groups of dependencies or even just shorten some dependency names, you can manually add aliases to the `gdmc.json` file.

- namespaced aliases contain a ':' (colon)

```json
    "e6:chop-core": {
        "alias": ["com.episode6.hackit.chop:chop-core"]
    },
    "e6:chop-android": {
        "alias": ["com.episode6.hackit.chop:chop-android"]
    }
```

- raw alias do not contain a ':' (colon)

```json
    "chop-all": {
        "alias": [
            "e6:chop-core",
            "e6:chop-android"
        ]
    }
```

- In your build.gradle, you can then reference the shorter aliases

```groovy
dependencies {
  // namespace aliases can be referenced directly
  compile 'e6:chop-core'

  // raw aliases must be wrapped by the gdmc method for gradle to handle them properly
  compile gdmc('chop-all')
}
```

## gdmc and spring's dependency management plugin
TODO


### License
MIT: https://github.com/episode6/gdmc/blob/master/LICENSE