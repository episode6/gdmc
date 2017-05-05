package com.episode6.hackit.gdmc

import com.episode6.hackit.gdmc.testutil.IntegrationTest
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import spock.lang.Specification

import static com.episode6.hackit.gdmc.testutil.TestDefinitions.*

/**
 *
 */
class GdmcAliasTest extends Specification {

  private static final String MISMATCHED_DEPENDENCIES = """
  "chop:core": {
      "groupId": "com.episode6.hackit.chop",
      "artifactId": "chop-core",
      "version": "0.1.7.2"
   },
   "spock:core": {
     "groupId": "org.spockframework",
     "artifactId": "spock-core",
     "version": "1.0-groovy-2.4"
   }
"""

  private static final String GROUP_ALIAS_DEPENDENCIES = """
  "test:mygroup": {
    "alias": [
      "chop:core",
      "spock:core"
    ]
  }
"""

  private static final String INDIVIDUAL_ALIAS_DEPENDENCIES = """
  "test:chop": {
    "alias": "chop:core"
  },
  "test:spock": {
    "alias": "spock:core"
  }
"""

  static String buildFile(String plugin, boolean aliasMethodWrap, String... deps) {
    String prefix = buildFilePrefix(plugin)
    prefix += """

dependencies {

"""
    for (String d : deps) {
      prefix += "\tcompile "
      prefix += aliasMethodWrap ? "gdmc('${d}')" : "'${d}'"
      prefix += "\n"
    }
    prefix += "}"
    return prefix
  }

  @Rule final IntegrationTest test = new IntegrationTest()

  def "test build mismatched direct aliases"(String plugin, boolean aliasMethodWrap) {
    given:
    test.gdmcJsonFile << "{${MISMATCHED_DEPENDENCIES}}"
    test.gradleBuildFile << buildFile(plugin, aliasMethodWrap, "chop:core", "spock:core")
    test.createJavaFile(packageName: "com.episode6.testproject", imports: CHOP_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest", imports: SPOCK_IMPORT)

    when:
    def result = test.build("build")

    then:
    result.task(":build").outcome == TaskOutcome.SUCCESS

    where:
    plugin                      | aliasMethodWrap
    GDMC_PLUGIN                 | true // fail
    GDMC_SPRINGS_COMPAT_PLUGIN  | true
    GDMC_PLUGIN                 | false
    GDMC_SPRINGS_COMPAT_PLUGIN  | false // fail

  }

  def "test upgrade mismatched direct aliases"(String plugin, boolean aliasMethodWrap) {
    given:
    test.gdmcJsonFile << "{${MISMATCHED_DEPENDENCIES}}"
    test.gradleBuildFile << buildFile(plugin, aliasMethodWrap, "chop:core", "spock:core")
    test.createJavaFile(packageName: "com.episode6.testproject", imports: CHOP_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest", imports: SPOCK_IMPORT)

    when:
    def result = test.build("gdmcUpgrade")

    then:
    result.task(":gdmcUpgrade").outcome == TaskOutcome.SUCCESS
//    println "useAlias: ${aliasMethodWrap}"
//    println test.gdmcJsonFile.text
    with(test.gdmcJsonFile.asJson()) {
      with(get("chop:core")) {
        groupId == "com.episode6.hackit.chop"
        artifactId == "chop-core"
        !version.contains("-SNAPSHOT")
        version.asVersion().isGreaterThan("0.1.7.2")
        get("locked") == null
      }
      with(get("org.spockframework:spock-core")) {
        groupId == "org.spockframework"
        artifactId == "spock-core"
        !version.contains("-SNAPSHOT")
        version.asVersion().isGreaterThan("1.0-groovy-2.4")
      }
      size() == 2
    }

    where:
    plugin                      | aliasMethodWrap
    GDMC_PLUGIN                 | true
    GDMC_SPRINGS_COMPAT_PLUGIN  | true
    GDMC_PLUGIN                 | false
    GDMC_SPRINGS_COMPAT_PLUGIN  | false
  }

  def "test build mismatched group aliases"(String plugin, boolean aliasMethodWrap) {
    given:
    test.gdmcJsonFile << "{${MISMATCHED_DEPENDENCIES}, ${GROUP_ALIAS_DEPENDENCIES}}"
    test.gradleBuildFile << buildFile(plugin, aliasMethodWrap, "test:mygroup")
    test.createJavaFile(packageName: "com.episode6.testproject", imports: CHOP_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest", imports: SPOCK_IMPORT)

    when:
    def result = test.build("build")

    then:
    result.task(":build").outcome == TaskOutcome.SUCCESS

    where:
    plugin                      | aliasMethodWrap
    GDMC_PLUGIN                 | true // fail
    GDMC_SPRINGS_COMPAT_PLUGIN  | true
    GDMC_PLUGIN                 | false
    GDMC_SPRINGS_COMPAT_PLUGIN  | false

  }

  def "test upgrade mismatched group aliases"(String plugin, boolean aliasMethodWrap) {
    given:
    test.gdmcJsonFile << "{${MISMATCHED_DEPENDENCIES}, ${GROUP_ALIAS_DEPENDENCIES}}"
    test.gradleBuildFile << buildFile(plugin, aliasMethodWrap, "test:mygroup")
    test.createJavaFile(packageName: "com.episode6.testproject", imports: CHOP_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest", imports: SPOCK_IMPORT)

    when:
    def result = test.build("gdmcUpgrade")

    then:
    result.task(":gdmcUpgrade").outcome == TaskOutcome.SUCCESS
//    println "useAlias: ${aliasMethodWrap}"
//    println test.gdmcJsonFile.text
    with(test.gdmcJsonFile.asJson()) {
      with(get("chop:core")) {
        groupId == "com.episode6.hackit.chop"
        artifactId == "chop-core"
        !version.contains("-SNAPSHOT")
        version.asVersion().isGreaterThan("0.1.7.2")
        get("locked") == null
      }
      with(get("org.spockframework:spock-core")) {
        groupId == "org.spockframework"
        artifactId == "spock-core"
        !version.contains("-SNAPSHOT")
        version.asVersion().isGreaterThan("1.0-groovy-2.4")
      }
      with(get("test:mygroup")) {
        alias.size() == 2
      }
      size() == 3
    }

    where:
    plugin                      | aliasMethodWrap
    GDMC_PLUGIN                 | true
    GDMC_SPRINGS_COMPAT_PLUGIN  | true
    GDMC_PLUGIN                 | false
    GDMC_SPRINGS_COMPAT_PLUGIN  | false
  }

  def "test build mismatched singular aliases"(String plugin, boolean aliasMethodWrap) {
    given:
    test.gdmcJsonFile << "{${MISMATCHED_DEPENDENCIES}, ${INDIVIDUAL_ALIAS_DEPENDENCIES}}"
    test.gradleBuildFile << buildFile(plugin, aliasMethodWrap, "test:chop", "test:spock")
    test.createJavaFile(packageName: "com.episode6.testproject", imports: CHOP_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest", imports: SPOCK_IMPORT)

    when:
    def result = test.build("build")

    then:
    result.task(":build").outcome == TaskOutcome.SUCCESS

    where:
    plugin                      | aliasMethodWrap
    GDMC_PLUGIN                 | true // fail
    GDMC_SPRINGS_COMPAT_PLUGIN  | true
    GDMC_PLUGIN                 | false
    GDMC_SPRINGS_COMPAT_PLUGIN  | false

  }

  def "test upgrade mismatched signular aliases"(String plugin, boolean aliasMethodWrap) {
    given:
    test.gdmcJsonFile << "{${MISMATCHED_DEPENDENCIES}, ${INDIVIDUAL_ALIAS_DEPENDENCIES}}"
    test.gradleBuildFile << buildFile(plugin, aliasMethodWrap, "test:chop", "test:spock")
    test.createJavaFile(packageName: "com.episode6.testproject", imports: CHOP_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest", imports: SPOCK_IMPORT)

    when:
    def result = test.build("gdmcUpgrade")

    then:
    result.task(":gdmcUpgrade").outcome == TaskOutcome.SUCCESS
//    println "useAlias: ${aliasMethodWrap}"
//    println test.gdmcJsonFile.text
    with(test.gdmcJsonFile.asJson()) {
      with(get("chop:core")) {
        groupId == "com.episode6.hackit.chop"
        artifactId == "chop-core"
        !version.contains("-SNAPSHOT")
        version.asVersion().isGreaterThan("0.1.7.2")
        get("locked") == null
      }
      with(get("org.spockframework:spock-core")) {
        groupId == "org.spockframework"
        artifactId == "spock-core"
        !version.contains("-SNAPSHOT")
        version.asVersion().isGreaterThan("1.0-groovy-2.4")
      }
      with(get("test:chop")) {
        alias == "chop:core"
      }
      with(get("test:spock")) {
        alias == "spock:core"
      }
      size() == 4
    }

    where:
    plugin                      | aliasMethodWrap
    GDMC_PLUGIN                 | true
    GDMC_SPRINGS_COMPAT_PLUGIN  | true
    GDMC_PLUGIN                 | false
    GDMC_SPRINGS_COMPAT_PLUGIN  | false
  }
}
