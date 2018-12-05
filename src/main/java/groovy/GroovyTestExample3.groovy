
import groovy.json.JsonSlurper
import org.apache.commons.lang3.StringUtils
import org.junit.Rule
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentation
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.context.WebApplicationContext
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup

/**
 * Created by 502624833 on 6/30/16.
 */

@SpringApplicationConfiguration(classes = ConfigurationServiceApplication.class)
@WebIntegrationTest
@Title("Verifying UOM Controller")
class GroupTagTest extends Specification {
    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    private OAuthHelper helper;

    @Shared
    Logger logger = LoggerFactory.getLogger(GroupTagTest.class)


    @Rule
    RestDocumentation restDocumentation = new RestDocumentation("build/generated-snippets")

    @Shared
    UUID uuid = UUID.randomUUID()


    @Shared
            bearerToken

    @Shared
    MockMvc mockMvc

    @Shared
    MockMvc mockMvcWithDocumentation

    @Shared
    MockMvc mockMvcWithSecurity

    @Shared
    MockMvc mockMvcWithSecurityAndDocumentation

    def setup() {
        mockMvc = webAppContextSetup(webApplicationContext).build()
        mockMvcWithSecurity = webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        mockMvcWithSecurityAndDocumentation = webAppContextSetup(webApplicationContext).apply(springSecurity()).apply(documentationConfiguration(this.restDocumentation)).build()
        mockMvcWithDocumentation = webAppContextSetup(webApplicationContext).apply(documentationConfiguration(this.restDocumentation)).build()
        bearerToken = helper.bearerToken("acme")
    }


    @Unroll
    def "create new unit tags "(tenantId, request, status) {
        given:

        def createresponse = mockMvcWithSecurity.perform(post("/config")
                .with(bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .header("tenantId", tenantId)
                .content(request.toString())).andReturn().response
        def path = createresponse.getHeader("Location")
        def id = path.substring(path.lastIndexOf('/') + 1);
        when:
        def unitGroups = """["u8u", "Ttt", "rr","kkk"]"""

        def response = mockMvcWithSecurity.perform(post("/config/" + id + "/unitGroups")
                .with(bearerToken)
                .header("tenantId", tenantId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(unitGroups)).andReturn().response
        then:
        response.status == status
        where:
        tenantId                               | request     | status
        "2412ad81-0fd1-48b6-aad9-c947793bc18d" | """{
      "name": "Celsius",
      "expression": "(#value * (9 / 5)) + 32",
      "to": ["rkyz31"],
      "from": ["rabc32"],
      "testData": [{"inputValue": 0,"outputValue": 32}],
      "precision": 5,
      "rounding": "ceiling",
      "unitGroupTags": ["Temp"],
      "enabled": true
    }""" | HttpStatus.CREATED.value()

    }


    @Unroll
    def "patch existing unit tags "(tenantId, request, status) {
        given:

        def createresponse = mockMvcWithSecurity.perform(post("/config")
                .with(bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .header("tenantId", tenantId)
                .content(request.toString())).andReturn().response
        def path = createresponse.getHeader("Location")
        def id = path.substring(path.lastIndexOf('/') + 1);
        when:
        def unitGroups = """["ii78", "bob", "rr","kkk"]"""

        def response = mockMvcWithSecurity.perform(patch("/config/" + id + "/unitGroups")
                .with(bearerToken)
                .header("tenantId", tenantId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(unitGroups)).andReturn().response
        then:
        response.status == status
        where:
        tenantId                               | request     | status
        "2412ad81-0fd1-48b6-aad9-c947793bc18d" | """{
      "name": "Celsius",
      "expression": "(#value * (9 / 5)) + 32",
      "to": ["rkyz229"],
      "from": ["rabc230"],
      "testData": [{"inputValue": 0,"outputValue": 32}],
      "precision": 5,
      "rounding": "ceiling",
      "unitGroupTags": ["Temp"],
      "enabled": true
    }""" | HttpStatus.OK.value()

    }

    @Unroll
    def "Read unit tags for tenant "(tenantId, request, status) {
        given:
        mockMvcWithSecurity.perform(post("/config")
                .with(bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .header("tenantId", tenantId)
                .content(request.toString())).andReturn().response
        when:
        def response = mockMvcWithSecurity.perform(get("/config/unitGroups")
                .with(bearerToken)
                .header("tenantId", tenantId))
                .andReturn().response
        then:
        response.status == status
        where:
        tenantId   | request     | status
        "22222222" | """{
    "name": "Celsius",
    "expression": "(#value * (9 / 5)) + 32",
    "to": ["rkyz1934"],
    "from": ["rabc1211"],
    "testData": [{"inputValue": 0,"outputValue": 32}],
    "precision": 5,
    "rounding": "ceiling",
    "unitGroupTags": ["Temp"],
    "enabled": true
    }""" | HttpStatus.OK.value()

    }


    @Unroll
    def "get measurement units by Id and Unit group tags "(tenantId, request, status) {
        given:
        def postresponse = mockMvcWithSecurity.perform(post("/config")
                .with(bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .header("tenantId", tenantId)
                .content(request.toString())).andReturn().response

        def getresponse = mockMvcWithSecurity.perform(get("/config/unitGroups")
                .with(bearerToken)
                .header("tenantId", tenantId))
                .andReturn().response

        def jsonSlurper = new JsonSlurper();
        def res = jsonSlurper.parseText(getresponse.content.toString())
        System.out.println("url " + res.links[0].href[1])
        def url = res.links[0].href[1]

        def result = StringUtils.substringAfter(url, 'unitGroups/')
        def ugid = StringUtils.substringBefore(result, '/measurementUnits')
        System.out.println("ugid " + ugid)
        when:
        def response = mockMvcWithSecurity.perform(get("/config/unitGroups/" + ugid + "/measurementUnits")
                .with(bearerToken)
                .header("tenantId", tenantId))
                .andReturn().response
        then:
        response.status == status

        where:
        tenantId                               | request     | status
        "2412ad81-0fd1-48b6-aad9-c947793bc18d" | """{
    "name": "Celsius",
    "expression": "(#value * (9 / 5)) + 32",
    "to": ["rkyz30"],
    "from": ["rabc31"],
    "testData": [{"inputValue": 0,"outputValue": 32}],
    "precision": 5,
    "rounding": "ceiling",
    "unitGroupTags": ["Temp"],
    "enabled": true
    }""" | HttpStatus.OK.value()
    }

    @Unroll
    def "ASCII Docs get measurement units by Id and Unit group tags "() {
        given:
        def getresponse = mockMvcWithSecurityAndDocumentation.perform(get("/config/unitGroups")
                .with(bearerToken)
                .header("tenantId", "2412ad81-0fd1-48b6-aad9-c947793bc18d"))
                .andReturn().response
        def jsonSlurper = new JsonSlurper();
        def res = jsonSlurper.parseText(getresponse.content.toString())
        System.out.println("url " + res.links[0].href[1])
        def url = res.links[0].href[1]

        def result = StringUtils.substringAfter(url, 'unitGroups/')
        def ugid = StringUtils.substringBefore(result, '/measurementUnits')
        System.out.println("ugid " + ugid)
        when:
        def response = mockMvcWithSecurityAndDocumentation.perform(get("/config/unitGroups/" + ugid + "/measurementUnits")
                .with(bearerToken)
                .header("tenantId", "2412ad81-0fd1-48b6-aad9-c947793bc18d"))
        then:
        response.andDo(document("read-get-mu-from-ugt"))


    }

    @Unroll
    def "Ascii Doc - create new unit tags "(tenantId, request, status) {
        given:

        def createresponse = mockMvcWithSecurityAndDocumentation.perform(post("/config")
                .with(bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .header("tenantId", tenantId)
                .content(request.toString())).andReturn().response
        def path = createresponse.getHeader("Location")
        def id = path.substring(path.lastIndexOf('/') + 1);
        when:
        def unitGroups = """["u8u", "Ttt", "rr","kkk"]"""

        def response = mockMvcWithSecurityAndDocumentation.perform(post("/config/" + id + "/unitGroups")
                .with(bearerToken)
                .header("tenantId", tenantId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(unitGroups))
        then:
        response.andDo(document("create-unit-group-tags"))
        where:
        tenantId                               | request     | status
        "2412ad81-0fd1-48b6-aad9-c947793bc18d" | """{
      "name": "Celsius",
      "expression": "(#value * (9 / 5)) + 32",
      "to": ["rkyz33"],
      "from": ["rabc34"],
      "testData": [{"inputValue": 0,"outputValue": 32}],
      "precision": 5,
      "rounding": "ceiling",
      "unitGroupTags": ["Temp"],
      "enabled": true
    }""" | HttpStatus.CREATED.value()

    }

    @Unroll
    def "Ascii Doc - read new unit tags "(tenantId, request, status) {
        given:
        mockMvcWithSecurityAndDocumentation.perform(post("/config")
                .with(bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .header("tenantId", tenantId)
                .content(request.toString())).andReturn().response
        when:
        def response = mockMvcWithSecurityAndDocumentation.perform(get("/config/unitGroups")
                .with(bearerToken)
                .header("tenantId", tenantId))
        then:
        response.andDo(document("read-unit-group-tags"))
        where:
        tenantId   | request     | status
        "22222222" | """{
    "name": "Celsius",
    "expression": "(#value * (9 / 5)) + 32",
    "to": ["rkyz35"],
    "from": ["rabc36"],
    "testData": [{"inputValue": 0,"outputValue": 32}],
    "precision": 5,
    "rounding": "ceiling",
    "unitGroupTags": ["Temp"],
    "enabled": true
    }""" | HttpStatus.OK.value()


    }

    @Unroll
    def "Ascii Doc - patch existing unit tags "(tenantId, request, status) {
        given:

        def createresponse = mockMvcWithSecurityAndDocumentation.perform(post("/config")
                .with(bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .header("tenantId", tenantId)
                .content(request.toString())).andReturn().response
        def path = createresponse.getHeader("Location")
        def id = path.substring(path.lastIndexOf('/') + 1);
        when:
        def unitGroups = """["ii78", "bob", "rr","kkk"]"""

        def response = mockMvcWithSecurityAndDocumentation.perform(patch("/config/" + id + "/unitGroups")
                .with(bearerToken)
                .header("tenantId", tenantId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(unitGroups))
        then:
        response.andDo(document("patch-unit-group-tags"))
        where:
        tenantId                               | request     | status
        "2412ad81-0fd1-48b6-aad9-c947793bc18d" | """{
      "name": "Celsius",
      "expression": "(#value * (9 / 5)) + 32",
      "to": ["rkyz231"],
      "from": ["rabc234"],
      "testData": [{"inputValue": 0,"outputValue": 32}],
      "precision": 5,
      "rounding": "ceiling",
      "unitGroupTags": ["Temp"],
      "enabled": true
    }""" | HttpStatus.OK.value()

    }
}