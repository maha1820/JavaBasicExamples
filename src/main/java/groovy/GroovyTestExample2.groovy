
import groovy.json.JsonSlurper
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
 * Created by 212393921 on 6/16/16.
 */
@SpringApplicationConfiguration(classes = UoMConfigurationServiceApplication.class)
@WebIntegrationTest
@Title("Verifying UOM Controller")
class UoMMeasurementUnitControllerTest extends Specification {
    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    private OAuthHelper helper;

    @Shared
    Logger logger = LoggerFactory.getLogger(UoMMeasurementUnitControllerTest.class)


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

    @Autowired
    MeasurementUnitRepository measurementUnitRepository

    def setup() {
        mockMvc = webAppContextSetup(webApplicationContext).build()
        mockMvcWithSecurity = webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        mockMvcWithSecurityAndDocumentation = webAppContextSetup(webApplicationContext).apply(springSecurity()).apply(documentationConfiguration(this.restDocumentation)).build()
        mockMvcWithDocumentation = webAppContextSetup(webApplicationContext).apply(documentationConfiguration(this.restDocumentation)).build()
        bearerToken = helper.bearerToken("acme")
    }

    @Unroll
    def "Ascii Doc - read from configuration"(tenantId, request, status) {
        given:
        def createresponse = mockMvcWithSecurityAndDocumentation.perform(post("/config")
                .with(bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .header("tenantId", tenantId.toString())
                .content(request.toString())).andReturn().response
        def path = createresponse.getHeader("Location")
        def id = path.substring(path.lastIndexOf('/') + 1);
        when:

        def response =
                mockMvcWithSecurityAndDocumentation.perform(get("/config/" + id + "/from")
                        .with(bearerToken)
                        .header("tenantId", tenantId.toString()))
        logger.info("Response Object Type : {}", response);
        then:
        response.andDo(document("read-from-config"))
        where:
        tenantId                               | request     | status
        "2412ad81-0fd1-48b6-aad9-c947793bc18d" | """{
      "name": "degc to degfah for asciidoc",
      "expression": "(#value * (9 / 5)) + 32",
      "to": ["degcel1"],
      "from": ["degfahrenheit"],
      "testData": [{"inputValue": 0,"outputValue": 32}],
      "precision": 5,
      "rounding": "ceiling",
      "unitGroupTags": ["Temp"],
      "enabled": true
    }""" | HttpStatus.OK.value()
    }


    @Unroll
    def "patch from "(tenantId, request, status) {
        given:

        def createresponse = mockMvcWithSecurity.perform(post("/config")
                .with(bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .header("tenantId", tenantId)
                .content(request)).andReturn().response
        def path = createresponse.getHeader("Location")
        def id = path.substring(path.lastIndexOf('/') + 1);
        when:
        when:
        def from = """["c", "cel", "cels"]"""
        def response = mockMvcWithSecurity.perform(patch("/config/" + id + "/from")
                .with(bearerToken)
                .header("tenantId", tenantId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(from)).andReturn().response
        then:
        response.status == status
        where:
        tenantId                               | request     | status
        "2412ad81-0fd1-48b6-aad9-c947793bc18d" | """{
      "name": "Celsius",
      "expression": "(#value * (9 / 5)) + 32",
      "to": ["rkyz5"],
      "from": ["rabc6"],
      "testData": [{"inputValue": 0,"outputValue": 32}],
      "precision": 5,
      "rounding": "ceiling",
      "unitGroupTags": ["Temp"],
      "enabled": true
    }""" | HttpStatus.OK.value()
    }

    @Unroll
    def "post from "(tenantId, request, status) {
        given:
        def createresponse = mockMvcWithSecurity.perform(post("/config")
                .with(bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .header("tenantId", tenantId)
                .content(request)).andReturn().response
        def path = createresponse.getHeader("Location")
        def id = path.substring(path.lastIndexOf('/') + 1);
        when:
        def from = """["cel1", "cel2"]"""
        def response = mockMvcWithSecurity.perform(post("/config/" + id + "/from")
                .with(bearerToken)
                .header("tenantId", tenantId.toString())
                .param("lang", "en")
                .contentType(MediaType.APPLICATION_JSON)
                .content(from)).andReturn().response
        then:
        response.status == status
        where:
        tenantId                               | request     | status
        "2412ad81-0fd1-48b6-aad9-c947793bc18d" | """{
      "name": "Celsius",
      "expression": "(#value * (9 / 5)) + 32",
      "to": ["rkyz7"],
      "from": ["rabc8"],
      "testData": [{"inputValue": 0,"outputValue": 32}],
      "precision": 5,
      "rounding": "ceiling",
      "unitGroupTags": ["Temp"],
      "enabled": true
    }""" | HttpStatus.CREATED.value()
    }


    @Unroll
    def "read to configuration"(tenantId, request, status) {
        given:

        def createresponse = mockMvcWithSecurity.perform(post("/config")
                .with(bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .header("tenantId", tenantId.toString())
                .content(request.toString())).andReturn().response
        def path = createresponse.getHeader("Location")
        def id = path.substring(path.lastIndexOf('/') + 1);
        when:
        def response;
        def jsonSlurper = new JsonSlurper()

        response = mockMvcWithSecurity.perform(get("/config/" + id + "/to")
                .with(bearerToken)
                .header("tenantId", tenantId.toString()))
                .andReturn().response
        then:
        def res = jsonSlurper.parseText(response.content.toString())
        res.unit == ['celsius']
        response.status == status
        where:
        tenantId                               | request     | status
        "2412ad81-0fd1-48b6-aad9-c947793bc18d" | """{
      "name": "celsius to fahrenheit",
      "expression": "(#value * (9 / 5)) + 32",
      "to": ["celsius"],
      "from": ["fahrenheit"],
      "testData": [{"inputValue": 0,"outputValue": 32}],
      "precision": 5,
      "rounding": "ceiling",
      "unitGroupTags": ["Temp"],
      "enabled": true
    }""" | HttpStatus.OK.value()

    }


    @Unroll
    def "patch to "(tenantId, request, status) {
        given:

        def createresponse = mockMvcWithSecurity.perform(post("/config")
                .with(bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .header("tenantId", tenantId)
                .content(request)).andReturn().response
        def path = createresponse.getHeader("Location")
        def id = path.substring(path.lastIndexOf('/') + 1);
        when:
        when:
        def from = """["c", "cel", "cels"]"""
        def response = mockMvcWithSecurity.perform(patch("/config/" + id + "/to")
                .with(bearerToken)
                .header("tenantId", tenantId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(from)).andReturn().response
        then:
        response.status == status
        where:
        tenantId                               | request     | status
        "2412ad81-0fd1-48b6-aad9-c947793bc18d" | """{
      "name": "Celsius",
      "expression": "(#value * (9 / 5)) + 32",
      "to": ["rkyz11"],
      "from": ["rabc12"],
      "testData": [{"inputValue": 0,"outputValue": 32}],
      "precision": 5,
      "rounding": "ceiling",
      "unitGroupTags": ["Temp"],
      "enabled": true
    }""" | HttpStatus.OK.value()
    }


    @Unroll

    def "post to configuration"(tenantId, request, status) {
        given:
        def createresponse = mockMvcWithSecurity.perform(post("/config")
                .with(bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .header("tenantId", tenantId)
                .content(request)).andReturn().response
        def path = createresponse.getHeader("Location")
        def id = path.substring(path.lastIndexOf('/') + 1);
        when:
        def from = """["c"]"""
        def response = mockMvcWithSecurity.perform(post("/config/" + id + "/to")
                .with(bearerToken)
                .header("tenantId", tenantId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(from)).andReturn().response
        then:
        response.status == status
        where:
        tenantId                               | request     | status
        "2412ad81-0fd1-48b6-aad9-c947793bc18d" | """{
      "name": "cel to fah",
      "expression": "(#value * (9 / 5)) + 32",
      "to": ["cel13"],
      "from": ["fah14"],
      "testData": [{"inputValue": 0,"outputValue": 32}],
      "precision": 5,
      "rounding": "ceiling",
      "unitGroupTags": ["Temp"],
      "enabled": true
    }""" | HttpStatus.CREATED.value()
    }


    @Unroll
    def "Ascii Doc - post to configuration"(tenantId, request, status) {
        given:
        def createresponse = mockMvcWithSecurityAndDocumentation.perform(post("/config")
                .with(bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .header("tenantId", tenantId)
                .content(request)).andReturn().response
        def path = createresponse.getHeader("Location")
        def id = path.substring(path.lastIndexOf('/') + 1);
        when:
        def to = """["c", "cel"]"""
        def response = mockMvcWithSecurityAndDocumentation.perform(post("/config/" + id + "/to")
                .with(bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .header("tenantId", tenantId)
                .content(to))
        then:
        response.andDo(document("post-to-config"))
        where:
        tenantId                               | request     | status
        "2412ad81-0fd1-48b6-aad9-c947793bc18d" | """{
      "name": "cel to fah",
      "expression": "(#value * (9 / 5)) + 32",
      "to": ["cel15"],
      "from": ["fah16"],
      "testData": [{"inputValue": 0,"outputValue": 32}],
      "precision": 5,
      "rounding": "ceiling",
      "unitGroupTags": ["Temp"],
      "enabled": true
    }""" | HttpStatus.CREATED.value()

    }


    @Unroll
    def "Ascii Doc - post from configuration"(tenantId, request, status) {
        given:
        def createresponse = mockMvcWithSecurityAndDocumentation.perform(post("/config")
                .with(bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .header("tenantId", tenantId)
                .content(request)).andReturn().response
        def path = createresponse.getHeader("Location")
        def id = path.substring(path.lastIndexOf('/') + 1);
        when:
        def from = """["c", "cel"]"""
        def response = mockMvcWithSecurityAndDocumentation.perform(post("/config/" + id + "/from")
                .with(bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .header("tenantId", tenantId)
                .content(from))
        then:
        response.andDo(document("post-from-config"))
        where:
        tenantId                               | request     | status
        "2412ad81-0fd1-48b6-aad9-c947793bc18d" | """{
      "name": "cel to fah",
      "expression": "(#value * (9 / 5)) + 32",
      "to": ["cel17"],
      "from": ["fah18"],
      "testData": [{"inputValue": 0,"outputValue": 32}],
      "precision": 5,
      "rounding": "ceiling",
      "unitGroupTags": ["Temp"],
      "enabled": true
    }""" | HttpStatus.CREATED.value()

    }


    @Unroll
    def "Ascii Doc - patch to configuration"(tenantId, request, status) {
        given:
        def createresponse = mockMvcWithSecurityAndDocumentation.perform(post("/config")
                .with(bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .header("tenantId", tenantId)
                .content(request)).andReturn().response
        def path = createresponse.getHeader("Location")
        def id = path.substring(path.lastIndexOf('/') + 1);
        when:
        def to = """["c", "cel"]"""
        def response = mockMvcWithSecurityAndDocumentation.perform(patch("/config/" + id + "/to")
                .with(bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .header("tenantId", tenantId)
                .content(to))
        then:
        response.andDo(document("patch-to-config"))
        where:
        tenantId                               | request     | status
        "2412ad81-0fd1-48b6-aad9-c947793bc18d" | """{
      "name": "cel to fah",
      "expression": "(#value * (9 / 5)) + 32",
      "to": ["cel19"],
      "from": ["fah20"],
      "testData": [{"inputValue": 0,"outputValue": 32}],
      "precision": 5,
      "rounding": "ceiling",
      "unitGroupTags": ["Temp"],
      "enabled": true
    }""" | HttpStatus.CREATED.value()

    }


    @Unroll
    def "Ascii Doc - patch from configuration"(tenantId, request, status) {
        given:
        def createresponse = mockMvcWithSecurityAndDocumentation.perform(post("/config")
                .with(bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .header("tenantId", tenantId)
                .content(request)).andReturn().response
        def path = createresponse.getHeader("Location")
        def id = path.substring(path.lastIndexOf('/') + 1);
        when:
        def from = """["c", "cel"]"""
        def response = mockMvcWithSecurityAndDocumentation.perform(patch("/config/" + id + "/from")
                .with(bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .header("tenantId", tenantId)
                .content(from))
        then:
        response.andDo(document("patch-from-config"))
        where:
        tenantId                               | request     | status
        "2412ad81-0fd1-48b6-aad9-c947793bc18d" | """{
      "name": "cel to fah",
      "expression": "(#value * (9 / 5)) + 32",
      "to": ["cel21"],
      "from": ["fah22"],
      "testData": [{"inputValue": 0,"outputValue": 32}],
      "precision": 5,
      "rounding": "ceiling",
      "unitGroupTags": ["Temp"],
      "enabled": true
    }""" | HttpStatus.CREATED.value()

    }

    @Unroll
    def "ASCII Docs - read to configuration"(tenantId, request, status) {
        given:
        def createresponse = mockMvcWithSecurityAndDocumentation.perform(post("/config")
                .with(bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .header("tenantId", tenantId.toString())
                .content(request.toString())).andReturn().response
        def path = createresponse.getHeader("Location")
        def id = path.substring(path.lastIndexOf('/') + 1);
        when:
        def response =
                mockMvcWithSecurityAndDocumentation.perform(get("/config/" + id + "/to")
                        .with(bearerToken)
                        .header("tenantId", tenantId.toString()))
        logger.info("Response Object Type : {}", response);
        then:
        response.andDo(document("read-to-config"))
        where:
        tenantId                               | request         | status
        "2412ad81-0fd1-48b6-aad9-c947793bc18d" | """{
          "name": "degc to degfah for asciidoc",
          "expression": "(#value * (9 / 5)) + 32",
          "to": ["degcelsius9"],
          "from": ["degfahrenheit10"],
          "testData": [{"inputValue": 0,"outputValue": 32}],
          "precision": 5,
          "rounding": "ceiling",
          "unitGroupTags": ["Temp"],
          "enabled": true
        }""" | HttpStatus.OK.value()
    }

    @Unroll
    def "ASCII Docs - read from configuration"(tenantId, request, status) {
        given:
        def createresponse = mockMvcWithSecurityAndDocumentation.perform(post("/config")
                .with(bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .header("tenantId", tenantId.toString())
                .content(request.toString())).andReturn().response
        def path = createresponse.getHeader("Location")
        def id = path.substring(path.lastIndexOf('/') + 1);
        when:
        def response =
                mockMvcWithSecurityAndDocumentation.perform(get("/config/" + id + "/from")
                        .with(bearerToken)
                        .header("tenantId", tenantId.toString()))
        logger.info("Response Object Type : {}", response);
        then:
        response.andDo(document("read-from-config"))
        where:
        tenantId                               | request         | status
        "2412ad81-0fd1-48b6-aad9-c947793bc18d" | """{
          "name": "degc to degfah for asciidoc",
          "expression": "(#value * (9 / 5)) + 32",
          "to": ["degcelsius29"],
          "from": ["degfahrenheit30"],
          "testData": [{"inputValue": 0,"outputValue": 32}],
          "precision": 5,
          "rounding": "ceiling",
          "unitGroupTags": ["Temp"],
          "enabled": true
        }""" | HttpStatus.OK.value()
    }

    @Unroll
    def "read measurementUnits by unitGroups"(tenantId, request, status) {
        given:
        def createresponse = mockMvcWithSecurity.perform(post("/config")
                .with(bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .header("tenantId", tenantId.toString())
                .content(request.toString())).andReturn().response
        MeasurementUnit measurementUnit = measurementUnitRepository.findByUnit("degfahrenheit100")
        when:
        def response = mockMvcWithSecurity.perform(get("/config/measurementUnits/" + measurementUnit.getId() + "/unitGroups")
                .with(bearerToken)
                .header("tenantId", tenantId.toString()))
                .andReturn().response
        logger.info("Response Object Type : {}**********", response.getContentAsString());
        then:
        response.status == status

        where:
        tenantId                               | request     | status
        "2412ad81-0fd1-48b6-aad9-c947793bc18d" | """{
      "name": "degc to degfah for asciidoc",
      "expression": "(#value * (9 / 5)) + 32",
      "to": ["degcelsius99"],
      "from": ["degfahrenheit100"],
      "testData": [{"inputValue": 0,"outputValue": 32}],
      "precision": 5,
      "rounding": "ceiling",
      "unitGroupTags": ["Temp"],
      "enabled": true
    }""" | HttpStatus.OK.value()
    }


}





