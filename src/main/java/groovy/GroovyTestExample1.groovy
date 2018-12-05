
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.junit.Rule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.restdocs.RestDocumentation
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Title

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup
/**
 * Created by 212393921 on 6/16/16.
 */
@SpringApplicationConfiguration(classes = UoMConfigurationServiceApplication.class)
@WebIntegrationTest
@Title("Verifying UOM Controller")
class ConversionControllerTest extends Specification {
    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    private OAuthHelper helper;


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
    @Shared
    MockMvc mockMvcBuilder

    def setup() {
        mockMvc = webAppContextSetup(webApplicationContext).build()
        mockMvcBuilder = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
        mockMvcWithSecurity = webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        mockMvcWithSecurityAndDocumentation = webAppContextSetup(webApplicationContext).apply(springSecurity()).apply(documentationConfiguration(this.restDocumentation)).build()
        mockMvcWithDocumentation = webAppContextSetup(webApplicationContext).apply(documentationConfiguration(this.restDocumentation)).build()
        bearerToken = helper.bearerToken("acme")
    }

    def "test conversion service - asciidoc"() {
        given: "Web application context is enabled"
        def configuration = new configuration()
        configuration.name = "Temperature to Celsius - 1"
        configuration.expression = "(#value * (9 / 5)) + 32"
        configuration.to = ["xyz1"]
        configuration.from = ["abc1"]
        def jsonSlurper = new JsonSlurper()
        def result = jsonSlurper.parseText("""[{"inputValue": 0,"outputValue": 32}]""")
        List<TestData> td = new ArrayList<>()
        result.eachWithIndex {
            def entry, int i -> td.add(new TestData(entry.inputValue, entry.outputValue))
        }
        configuration.testData = td.toArray()
        configuration.precision = 5
        configuration.rounding = "ceiling"
        configuration.unitGroupTags = ["Temp"]
        configuration.enabled = true
        when: "I insert a new expression into expression library"

        mockMvcWithSecurityAndDocumentation.perform(post("/config")
                .with(bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .header("tenantId", uuid)
                .content(JsonOutput.toJson(configuration))).andReturn().response

        and: "I use the expression created for conversion"
        ObjectGraphBuilder builder = new ObjectGraphBuilder()
        builder.classNameResolver = "com.entities";
        ConversionRequest request = builder.ConversionRequest(to: "xyz1", from: "abc1", precision: null, rounding: null, values: [5, 10] as Double[])
        def requests = [request]
        def response = mockMvcWithDocumentation.perform(post("/convert")
                .contentType(MediaType.APPLICATION_JSON)
                .header("tenantId", uuid)
                .content(JsonOutput.toJson(requests)))

        then: "expect the response to be 200"
        response.andDo(document("postconvert", requestFields(
                fieldWithPath("[0].to").type(JsonFieldType.STRING).description("To"),
                fieldWithPath("[0].from").type(JsonFieldType.STRING).description("From"),
                fieldWithPath("[0].precision").type(JsonFieldType.NUMBER).description("Precision"),
                fieldWithPath("[0].values").type(JsonFieldType.ARRAY).description("Values"),
                fieldWithPath("[0].rounding").type(JsonFieldType.STRING).description("Rounding"))))
    }

    def "test conversion service - happy path"() {
        given: "Web application context is enabled"
        def configuration = new configuration()
        configuration.name = "Temperature to Celsius - 1"
        configuration.to = ["xyz"]
        configuration.from = ["abc"]
        def jsonSlurper = new JsonSlurper()
        def result = jsonSlurper.parseText("""[{"inputValue": 0,"outputValue": 32}]""")
        List<TestData> td = new ArrayList<>()
        result.eachWithIndex {
            def entry, int i -> td.add(new TestData(entry.inputValue, entry.outputValue))
        }
        configuration.testData = td.toArray()
        configuration.precision = 5
        configuration.rounding = "ceiling"
        configuration.unitGroupTags = ["Temp"]
        configuration.enabled = true
        when: "I insert a new expression into expression library"
        mockMvcWithSecurity.perform(post("/config")
                .with(bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .header("tenantId", uuid)
                .content(JsonOutput.toJson(configuration))).andReturn().response

        and: "I use the expression created for conversion"
        ObjectGraphBuilder builder = new ObjectGraphBuilder()
        builder.classNameResolver = "com.entities";
        ConversionRequest request = builder.ConversionRequest(to: "xyz", from: "abc", precision: null, rounding: null, values: [5, 10] as Double[])
        def requests = [request]
        def response = mockMvc.perform(post("/convert")
                .contentType(MediaType.APPLICATION_JSON)
                .header("tenantId", uuid)
                .content(JsonOutput.toJson(requests))).andReturn().response

        then: "expect the response to be 200"
        response.status == HttpStatus.OK.value
    }

    def "test conversion service - with invalid to and from"() {
        given: "Web application context is enabled"
        def configuration = new configuration()
        configuration.name = "Temperature to Celsius - 1"
        configuration.expression = "(#value * (9)) + 32"
        configuration.to = ["xyz"]
        configuration.from = ["abc"]
        def jsonSlurper = new JsonSlurper()
        def result = jsonSlurper.parseText("""[{"inputValue": 0,"outputValue": 32}]""")
        List<TestData> td = new ArrayList<>()
        result.eachWithIndex {
            def entry, int i -> td.add(new TestData(entry.inputValue, entry.outputValue))
        }
        configuration.testData = td.toArray()
        configuration.precision = 5
        configuration.rounding = "ceiling"
        configuration.unitGroupTags = ["Temp"]
        configuration.enabled = true
        when: "I insert a new expression into expression library"
        mockMvcWithSecurity.perform(post("/config")
                .with(bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .header("tenantId", uuid)
                .content(JsonOutput.toJson(configuration))).andReturn().response

        and: "I use the expression created for conversion"
        ObjectGraphBuilder builder = new ObjectGraphBuilder()
        builder.classNameResolver = "com.entities";
        ConversionRequest request = builder.ConversionRequest()
        request.setTo("")
        request.setFrom("")
        request.setPrecision(null)
        request.setRounding(null)
        def response = mockMvc.perform(post("/convert")
                .contentType(MediaType.APPLICATION_JSON)
                .header("tenantId", uuid)
                .content(request.toString())).andReturn().response

        then: "expect the response to be 400"
        response.status == HttpStatus.BAD_REQUEST.value
    }

//TODO
    def "test conversion service - with invalid expression"() {
        given: "Web application context is enabled"
        def configuration = new configuration()
        configuration.name = "Pascal to KiloPascal"
        configuration.expression = "(#value /1000)"
        configuration.to = ["kPa"]
        configuration.from = ["pa"]
        def jsonSlurper = new JsonSlurper()
        def result = jsonSlurper.parseText("""[{"inputValue": 1000,"outputValue": 1}]""")
        List<TestData> td = new ArrayList<>()
        result.eachWithIndex {
            def entry, int i -> td.add(new TestData(entry.inputValue, entry.outputValue))
        }
        configuration.testData = td.toArray()
        configuration.precision = 0
        configuration.rounding = "ceiling"
        configuration.unitGroupTags = ["Pressure"]
        configuration.enabled = true
        when: "I insert a new expression into expression library"
        mockMvcWithSecurity.perform(post("/config")
                .with(bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .header("tenantId", uuid)
                .content(JsonOutput.toJson(configuration))).andReturn().response

        and: "I use the expression created for conversion"
        ObjectGraphBuilder builder = new ObjectGraphBuilder()
        builder.classNameResolver = "com.entities";
        ConversionRequest request = builder.ConversionRequest()
        request.setTo("kPa")
        request.setFrom("p")
        request.setValues([5, 10] as Double[])
        request.setPrecision(null)
        request.setRounding(null)

        def response = mockMvc.perform(post("/convert")
                .contentType(MediaType.APPLICATION_JSON)
                .header("tenantId", uuid)
                .content(request.toString())).andReturn().response
        // .andExpect(contentType(MediaType.APPLICATION_JSON))
        //.andExpect(jsonPath("converted").value(false))

        then: "expect the response to be 400"
        response.status == HttpStatus.BAD_REQUEST.value
    }


    def "test conversion service - with cvs input"() {
        given: "Web application context is enabled"
        def configuration = new configuration()
        configuration.name = "Pascal to KiloPascal"
        configuration.expression = "(#value /1000)"
        configuration.to = ["km"]
        configuration.from = ["m"]
        def jsonSlurper = new JsonSlurper()
        def result = jsonSlurper.parseText("""[{"inputValue": 1000,"outputValue": 1}]""")
        List<TestData> td = new ArrayList<>()
        result.eachWithIndex {
            def entry, int i -> td.add(new TestData(entry.inputValue, entry.outputValue))
        }
        configuration.testData = td.toArray()
        configuration.precision = 0
        configuration.rounding = "ceiling"
        configuration.unitGroupTags = ["distance"]
        configuration.enabled = true
        when: "I insert a new expression into expression library"
        mockMvcWithSecurity.perform(post("/config")
                .with(bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .header("tenantId", uuid)
                .content(JsonOutput.toJson(configuration))).andReturn().response

        and: "I use the expression created for conversion"
        ObjectGraphBuilder builder = new ObjectGraphBuilder()
        builder.classNameResolver = "com.entities";
        ConversionRequest request = builder.ConversionRequest()
        request.setTo("km")
        request.setFrom("m")
        request.setValues([5, 10] as Double[])
        request.setPrecision(null)
        request.setRounding(null)

        File file = new File('src/test/testResources/batchConversionTestData.csv')
        FileInputStream fis = new FileInputStream(file)
        MockMultipartFile multipartFile = new MockMultipartFile("file","batchConversionTestData.csv","text/csv", fis)

        def response =mockMvcBuilder.perform(MockMvcRequestBuilders.fileUpload("/convert/csv")
                .file(multipartFile).accept("text/csv")
                .header("tenantId", uuid))
                .andReturn().response
        fis.close();
        then: "expect the response to be 201"
        response.status == HttpStatus.CREATED.value
        response.getContentType()=="text/csv"
    }


    def "test conversion service - with json output"() {
        given: "Web application context is enabled"
        def configuration = new configuration()
        configuration.name = "Pascal to KiloPascal"
        configuration.expression = "(#value /1000)"
        configuration.to = ["l"]
        configuration.from = ["ml"]
        def jsonSlurper = new JsonSlurper()
        def result = jsonSlurper.parseText("""[{"inputValue": 1000,"outputValue": 1}]""")
        List<TestData> td = new ArrayList<>()
        result.eachWithIndex {
            def entry, int i -> td.add(new TestData(entry.inputValue, entry.outputValue))
        }
        configuration.testData = td.toArray()
        configuration.precision = 0
        configuration.rounding = "ceiling"
        configuration.unitGroupTags = ["distance"]
        configuration.enabled = true
        when: "I insert a new expression into expression library"
        mockMvcWithSecurity.perform(post("/config")
                .with(bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .header("tenantId", uuid)
                .content(JsonOutput.toJson(configuration))).andReturn().response

        and: "I use the expression created for conversion"
        ObjectGraphBuilder builder = new ObjectGraphBuilder()
        builder.classNameResolver = "com.entities";
        ConversionRequest request = builder.ConversionRequest()
        request.setTo("km")
        request.setFrom("m")
        request.setValues([5, 10] as Double[])
        request.setPrecision(null)
        request.setRounding(null)

        File file = new File('src/test/testResources/batchConversionTestData.csv')
        FileInputStream fis = new FileInputStream(file)
        MockMultipartFile multipartFile = new MockMultipartFile("file","batchConversionTestData.csv","application/json", fis)

        def response =mockMvcBuilder.perform(MockMvcRequestBuilders.fileUpload("/convert/csv")
                .file(multipartFile).accept(MediaType.APPLICATION_JSON.toString())
                .header("tenantId", uuid))
                .andReturn().response
        fis.close();
        then: "expect the response to be 201"
        response.status == HttpStatus.OK.value
        response.getContentType()==MediaType.APPLICATION_JSON.toString()
    }

    def "test conversion service - with cvs input ASCII doc"() {
        given: "Web application context is enabled"
        def configuration = new configuration()
        configuration.name = "km to m"
        configuration.expression = "(#value /1000)"
        configuration.to = ["km"]
        configuration.from = ["m"]
        def jsonSlurper = new JsonSlurper()
        def result = jsonSlurper.parseText("""[{"inputValue": 1,"outputValue": 1000}]""")
        List<TestData> td = new ArrayList<>()
        result.eachWithIndex {
            def entry, int i -> td.add(new TestData(entry.inputValue, entry.outputValue))
        }
        configuration.testData = td.toArray()
        configuration.precision = 0
        configuration.rounding = "ceiling"
        configuration.unitGroupTags = ["distance"]
        configuration.enabled = true
        when: "I insert a new expression into expression library"
        def res = mockMvcWithSecurityAndDocumentation.perform(post("/config")
                .with(bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .header("tenantId", uuid)
                .content(JsonOutput.toJson(configuration))).andReturn().response

        and: "I use the expression created for conversion"
        ObjectGraphBuilder builder = new ObjectGraphBuilder()
        builder.classNameResolver = "com.entities";
        ConversionRequest request = builder.ConversionRequest()
        request.setTo("km")
        request.setFrom("m")
        request.setValues([5, 10] as Double[])
        request.setPrecision(null)
        request.setRounding(null)

        File file = new File('src/test/testResources/batchConversionTestData.csv')
        FileInputStream fis = new FileInputStream(file)
        MockMultipartFile multipartFile = new MockMultipartFile("file","batchConversionTestData.csv","text/csv", fis)

        def response =mockMvcWithDocumentation.perform(MockMvcRequestBuilders.fileUpload("/convert/csv")
                .file(multipartFile).accept("text/csv")
                .header("tenantId", uuid))
        fis.close();
        then: "expect the response to be 201"
        response.andDo(document("convert-using-csv"))
    }
}
