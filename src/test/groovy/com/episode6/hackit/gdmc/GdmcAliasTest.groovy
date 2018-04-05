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
    "mockspresso:mockito": {
        "groupId": "com.episode6.hackit.mockspresso",
        "artifactId": "mockspresso-mockito",
        "inheritVersion": "mockspresso:core"
    },
    "mockspresso:core": {
        "groupId": "com.episode6.hackit.mockspresso",
        "artifactId": "mockspresso-core",
        "version": "0.0.11"
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
      "mockspresso:mockito",
      "mockspresso:core",
      "spock:core"
    ]
  }
"""

  private static final String INDIVIDUAL_ALIAS_DEPENDENCIES = """
  "test:mockspresso-mockito": {
    "alias": "mockspresso:mockito"
  },
  "test:mockspresso": {
    "alias": "mockspresso:core"
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
    test.gradleBuildFile << buildFile(plugin, aliasMethodWrap, "mockspresso:mockito", "spock:core", "mockspresso:core")
    test.createJavaFile(packageName: "com.episode6.testproject", imports: MOCKSPRESSO_MOCKITO_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest", imports: SPOCK_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest2", imports: MOCKSPRESSO_CORE_IMPORT)

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
    test.gradleBuildFile << buildFile(plugin, aliasMethodWrap, "mockspresso:mockito", "spock:core", "mockspresso:core")
    test.createJavaFile(packageName: "com.episode6.testproject", imports: MOCKSPRESSO_MOCKITO_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest", imports: SPOCK_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest2", imports: MOCKSPRESSO_CORE_IMPORT)

    when:
    def result = test.build("gdmcUpgrade")

    then:
    result.task(":gdmcUpgrade").outcome == TaskOutcome.SUCCESS
    with(test.gdmcJsonFile.asJson()) {
      with(get("mockspresso:core")) {
        groupId == "com.episode6.hackit.mockspresso"
        artifactId == "mockspresso-core"
        !version.contains("-SNAPSHOT")
        version.asVersion().isGreaterThan("0.0.11")
        get("locked") == null
      }
      with(get("mockspresso:mockito")) {
        groupId == "com.episode6.hackit.mockspresso"
        artifactId == "mockspresso-mockito"
        inheritVersion == "mockspresso:core"
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
    test.createJavaFile(packageName: "com.episode6.testproject", imports: MOCKSPRESSO_MOCKITO_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest", imports: SPOCK_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest2", imports: MOCKSPRESSO_CORE_IMPORT)

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
    test.createJavaFile(packageName: "com.episode6.testproject", imports: MOCKSPRESSO_MOCKITO_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest", imports: SPOCK_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest2", imports: MOCKSPRESSO_CORE_IMPORT)

    when:
    def result = test.build("gdmcUpgrade")

    then:
    result.task(":gdmcUpgrade").outcome == TaskOutcome.SUCCESS
    with(test.gdmcJsonFile.asJson()) {
      with(get("mockspresso:core")) {
        groupId == "com.episode6.hackit.mockspresso"
        artifactId == "mockspresso-core"
        !version.contains("-SNAPSHOT")
        version.asVersion().isGreaterThan("0.0.11")
        get("locked") == null
      }
      with(get("mockspresso:mockito")) {
        groupId == "com.episode6.hackit.mockspresso"
        artifactId == "mockspresso-mockito"
        inheritVersion == "mockspresso:core"
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
    test.gradleBuildFile << buildFile(plugin, aliasMethodWrap, "test:mockspresso", "test:spock", "test:mockspresso-mockito")
    test.createJavaFile(packageName: "com.episode6.testproject", imports: MOCKSPRESSO_MOCKITO_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest", imports: SPOCK_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest2", imports: MOCKSPRESSO_CORE_IMPORT)

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
    test.gradleBuildFile << buildFile(plugin, aliasMethodWrap, "test:mockspresso", "test:spock", "test:mockspresso-mockito")
    test.createJavaFile(packageName: "com.episode6.testproject", imports: MOCKSPRESSO_MOCKITO_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest", imports: SPOCK_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest2", imports: MOCKSPRESSO_CORE_IMPORT)

    when:
    def result = test.build("gdmcUpgrade")

    then:
    result.task(":gdmcUpgrade").outcome == TaskOutcome.SUCCESS
    with(test.gdmcJsonFile.asJson()) {
      with(get("mockspresso:core")) {
        groupId == "com.episode6.hackit.mockspresso"
        artifactId == "mockspresso-core"
        !version.contains("-SNAPSHOT")
        version.asVersion().isGreaterThan("0.0.11")
        get("locked") == null
      }
      with(get("mockspresso:mockito")) {
        groupId == "com.episode6.hackit.mockspresso"
        artifactId == "mockspresso-mockito"
        inheritVersion == "mockspresso:core"
        get("version") == null
        get("locked") == null
      }
      with(get("spock:core")) {
        groupId == "org.spockframework"
        artifactId == "spock-core"
        !version.contains("-SNAPSHOT")
        version.asVersion().isGreaterThan("1.0-groovy-2.4")
      }
      with(get("test:mockspresso-mockito")) {
        alias == "mockspresso:mockito"
      }
      with(get("test:mockspresso")) {
        alias == "mockspresso:core"
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
