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
      "version": "0.1.9"
   },
   "chop:junit": {
      "groupId": "com.episode6.hackit.chop",
      "artifactId": "chop-junit",
      "inheritVersion": "chop:core"
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
      "chop:junit",
      "spock:core"
    ]
  }
"""

  private static final String INDIVIDUAL_ALIAS_DEPENDENCIES = """
  "test:chop": {
    "alias": "chop:core"
  },
  "test:chop-junit": {
    "alias": "chop:junit"
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
      prefix += "\timplementation "
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
    test.gradleBuildFile << buildFile(plugin, aliasMethodWrap, "chop:core", "spock:core", "chop:junit")
    test.createJavaFile(packageName: "com.episode6.testproject", imports: CHOP_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest", imports: SPOCK_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest2", imports: CHOP_RULE_IMPORT)

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
    test.gradleBuildFile << buildFile(plugin, aliasMethodWrap, "chop:core", "spock:core", "chop:junit")
    test.createJavaFile(packageName: "com.episode6.testproject", imports: CHOP_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest", imports: SPOCK_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest2", imports: CHOP_RULE_IMPORT)

    when:
    def result = test.build("gdmcUpgrade")

    then:
    result.task(":gdmcUpgrade").outcome == TaskOutcome.SUCCESS
    with(test.gdmcJsonFile.asJson()) {
      with(get("chop:core")) {
        groupId == "com.episode6.hackit.chop"
        artifactId == "chop-core"
        !version.contains("-SNAPSHOT")
        version.asVersion().isGreaterThanEquals("0.1.9")
        get("locked") == null
      }
      with(get("chop:junit")) {
        groupId == "com.episode6.hackit.chop"
        artifactId == "chop-junit"
        inheritVersion == "chop:core"
        get("version") == null
        get("locked") == null
      }
      with(get("spock:core")) {
        groupId == "org.spockframework"
        artifactId == "spock-core"
        !version.contains("-SNAPSHOT")
        version.asVersion().isGreaterThan("1.0-groovy-2.4")
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

  def "test build mismatched group aliases"(String plugin, boolean aliasMethodWrap) {
    given:
    test.gdmcJsonFile << "{${MISMATCHED_DEPENDENCIES}, ${GROUP_ALIAS_DEPENDENCIES}}"
    test.gradleBuildFile << buildFile(plugin, aliasMethodWrap, "test:mygroup")
    test.createJavaFile(packageName: "com.episode6.testproject", imports: CHOP_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest", imports: SPOCK_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest2", imports: CHOP_RULE_IMPORT)

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
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest2", imports: CHOP_RULE_IMPORT)

    when:
    def result = test.build("gdmcUpgrade")

    then:
    result.task(":gdmcUpgrade").outcome == TaskOutcome.SUCCESS
    with(test.gdmcJsonFile.asJson()) {
      with(get("chop:core")) {
        groupId == "com.episode6.hackit.chop"
        artifactId == "chop-core"
        !version.contains("-SNAPSHOT")
        version.asVersion().isGreaterThanEquals("0.1.9")
        get("locked") == null
      }
      with(get("chop:junit")) {
        groupId == "com.episode6.hackit.chop"
        artifactId == "chop-junit"
        inheritVersion == "chop:core"
        get("version") == null
        get("locked") == null
      }
      with(get("spock:core")) {
        groupId == "org.spockframework"
        artifactId == "spock-core"
        !version.contains("-SNAPSHOT")
        version.asVersion().isGreaterThan("1.0-groovy-2.4")
      }
      with(get("test:mygroup")) {
        alias.size() == 3
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

  def "test build mismatched singular aliases"(String plugin, boolean aliasMethodWrap) {
    given:
    test.gdmcJsonFile << "{${MISMATCHED_DEPENDENCIES}, ${INDIVIDUAL_ALIAS_DEPENDENCIES}}"
    test.gradleBuildFile << buildFile(plugin, aliasMethodWrap, "test:chop", "test:spock", "test:chop-junit")
    test.createJavaFile(packageName: "com.episode6.testproject", imports: CHOP_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest", imports: SPOCK_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest2", imports: CHOP_RULE_IMPORT)

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
    test.gradleBuildFile << buildFile(plugin, aliasMethodWrap, "test:chop", "test:spock", "test:chop-junit")
    test.createJavaFile(packageName: "com.episode6.testproject", imports: CHOP_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest", imports: SPOCK_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest2", imports: CHOP_RULE_IMPORT)

    when:
    def result = test.build("gdmcUpgrade")

    then:
    result.task(":gdmcUpgrade").outcome == TaskOutcome.SUCCESS
    with(test.gdmcJsonFile.asJson()) {
      with(get("chop:core")) {
        groupId == "com.episode6.hackit.chop"
        artifactId == "chop-core"
        !version.contains("-SNAPSHOT")
        version.asVersion().isGreaterThanEquals("0.1.9")
        get("locked") == null
      }
      with(get("chop:junit")) {
        groupId == "com.episode6.hackit.chop"
        artifactId == "chop-junit"
        inheritVersion == "chop:core"
        get("version") == null
        get("locked") == null
      }
      with(get("spock:core")) {
        groupId == "org.spockframework"
        artifactId == "spock-core"
        !version.contains("-SNAPSHOT")
        version.asVersion().isGreaterThan("1.0-groovy-2.4")
      }
      with(get("test:chop")) {
        alias == "chop:core"
      }
      with(get("test:chop-junit")) {
        alias == "chop:junit"
      }
      with(get("test:spock")) {
        alias == "spock:core"
      }
      size() == 6
    }

    where:
    plugin                      | aliasMethodWrap
    GDMC_PLUGIN                 | true
    GDMC_SPRINGS_COMPAT_PLUGIN  | true
    GDMC_PLUGIN                 | false
    GDMC_SPRINGS_COMPAT_PLUGIN  | false
  }
}
