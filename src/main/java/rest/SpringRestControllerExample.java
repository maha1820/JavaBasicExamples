
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.HateoasPageableHandlerMethodArgumentResolver;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;


@RestController
@RequestMapping(path = "/config")
@EnableResourceServer
public class ConfigurationController {

    private static final Logger log = LoggerFactory.getLogger(UoMConfigurationController.class);

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private UoMConfigService uomConfigService;

    @Value("${standard.tenant}")
    private String standardTenantId;

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("#oauth2.hasScope('uaa.resource')")
    public PagedResources<Resource<Configuration>> configurations(@RequestHeader(name = "tenantId") String tenantId,
                                                                     @RequestParam(name = "page", defaultValue = "0") int page,
                                                                     @RequestParam(name = "size", defaultValue = "25") int pageSize,
                                                                     Principal currentUser) {
        log.debug("Current User: {}", currentUser.getName());
        Page<Expression> pageExpressions = uomConfigService.expressions(tenantId, page, pageSize);
        List<Object> uomConfigurations = pageExpressions.getContent().stream().map(e -> transform(e)).collect(Collectors.toList());
        PagedResourcesAssembler<Configuration> pagedResourcesAssembler = new PagedResourcesAssembler<>(new HateoasPageableHandlerMethodArgumentResolver(), ServletUriComponentsBuilder.fromCurrentRequest().build());
        Page pageRequest = new PageImpl(uomConfigurations, new PageRequest(pageExpressions.getNumber(), pageExpressions.getSize()), pageExpressions.getTotalElements());
        PagedResources<Resource<Configuration>> resources = pagedResourcesAssembler.toResource(pageRequest);
        return resources;
    }

    /**
     * Get all Standard Configurations
     * @param tenantId
     * @param page
     * @param pageSize
     * @param currentUser
     * @return
     */
    @RequestMapping(path = "/standard", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("#oauth2.hasScope('uaa.resource')")
    public PagedResources<Resource<Configuration>> standardConfigurations(@RequestHeader(name = "tenantId") String tenantId,
                                                                     @RequestParam(name = "page", defaultValue = "0") int page,
                                                                     @RequestParam(name = "size", defaultValue = "25") int pageSize,
                                                                     Principal currentUser) {
        tenantId = standardTenantId;
        log.debug("Current User: {}", currentUser.getName());
        Page<Expression> pageExpressions = uomConfigService.expressions(tenantId, page, pageSize);
        List<Object> uomConfigurations = pageExpressions.getContent().stream().map(e -> transform(e)).collect(Collectors.toList());
        PagedResourcesAssembler<Configuration> pagedResourcesAssembler = new PagedResourcesAssembler<>(new HateoasPageableHandlerMethodArgumentResolver(), ServletUriComponentsBuilder.fromCurrentRequest().build());
        Page pageRequest = new PageImpl(uomConfigurations, new PageRequest(pageExpressions.getNumber(), pageExpressions.getSize()), pageExpressions.getTotalElements());
        PagedResources<Resource<Configuration>> resources = pagedResourcesAssembler.toResource(pageRequest);
        return resources;
    }

    /**
     * This method will return Entity based on id
     *
     * @param id
     */
    @RequestMapping(path = "/{id}", method = RequestMethod.GET)
    @PreAuthorize("#oauth2.hasScope('uaa.resource')")
    ResponseEntity<Configuration> configurations(@RequestHeader("tenantId") String tenantId, @PathVariable("id") long id) {
        Expression expression = configService.expression(tenantId, (int) id);
        Configuration Configuration = transform(expression);
        if (Configuration == null) {
            return new ResponseEntity<Configuration>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(Configuration);
    }


    /**
     * This method takes inputs from client,checks for existing configuration and
     * creates new configuration(if configuration does not exist).
     *
     * @param Configuration
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("#oauth2.hasScope('uaa.write')")
    public ResponseEntity<?> createConfiguration(@RequestHeader(name = "tenantId", required = true) String tenantId,
                                                    @RequestBody @Valid Configuration Configuration,
                                                    @RequestParam(name = "lang", defaultValue = "en") String language,
                                                    HttpServletRequest request) {

        Locale lang = StringUtils.parseLocaleString(language);
        //check for the existing configuration
        Configuration.setTenantId(tenantId);
        log.info("Creating new  Configuration \n");
        if (!Configuration.getExpression().contains("#value")) {
            throw new ValidationException(HttpStatus.BAD_REQUEST, lang, ".invalid.expression", Configuration.getExpression());
        }
        //check for same from and to fields
        if (compareToAndFromFields(Configuration.getFrom(), Configuration.getTo()) == true) {
            throw new ConfigurationValidationException(HttpStatus.BAD_REQUEST, lang, ".invalid.tofrom.fields");
        }


        if (StringUtils.hasText(Configuration.getRounding()) &&
                !(Configuration.getRounding().equalsIgnoreCase(UoMConfigUnits.CEILING.name()) ||
                        Configuration.getRounding().equalsIgnoreCase(UoMConfigUnits.FLOOR.name()))) {
            throw new ValidationException(HttpStatus.BAD_REQUEST, lang, ".invalid.rounding", Configuration.getRounding());
        }

        if (uomConfigService.doesConfigurationExist(Configuration)) {
            log.debug(messageSource.getMessage(".configuration.exist", null, lang) + Configuration);
            throw new ValidationException(HttpStatus.CONFLICT, lang, ".configuration.exist");
        }

        //validate the json testdata
        TestData[] testdata = Configuration.getTestData();
        if (testdata.length > 0) {
            for (TestData td : testdata) {
                if (td.getInputValue() != null) {
                    if (td.getOutputValue() == null) {
                        throw new ValidationException(HttpStatus.BAD_REQUEST, lang, ".testdata.outputvalue");
                    }
                } else {
                    throw new ValidationException(HttpStatus.BAD_REQUEST, lang, ".testdata.inputvalue");
                }
            }
        } else {
            throw new ValidationException(HttpStatus.BAD_REQUEST, lang, ".testdata.emtpy");
        }

        for (TestData testData : Configuration.getTestData()) {
            //check if output value has correct precision
           /* String[] result = testData.getOutputValue().toString().split("\\.");
            if((int)Configuration.getPrecision()!=0 && result[1].length()!=(int)Configuration.getPrecision()){
                throw new ConfigurationValidationException(HttpStatus.BAD_REQUEST, lang, "Incorrect combination of precision, output value and input value");
            }*/
            ExpressionParser expressionParser = new SpelExpressionParser();
            StandardEvaluationContext standardEvaluationContext = new StandardEvaluationContext();
            standardEvaluationContext.setVariable("value", new BigDecimal(testData.getInputValue()));
            Double actual = expressionParser.parseExpression(Configuration.getExpression()).getValue(standardEvaluationContext, Double.class);

            BigDecimal actualValue = BigDecimal.valueOf(actual);
            BigDecimal expectedValue = BigDecimal.valueOf(testData.getOutputValue());
            log.debug("******actualValue**" + actualValue);
            log.debug("******expectedValue**" + expectedValue);

            if (Configuration.getPrecision() != 0 && StringUtils.hasText(Configuration.getRounding())) {
                if (Configuration.getRounding().equalsIgnoreCase(UoMConfigUnits.FLOOR.name())) {
                    actualValue = actualValue.setScale((int) Configuration.getPrecision(), RoundingMode.FLOOR);
                    expectedValue = expectedValue.setScale((int) Configuration.getPrecision(), RoundingMode.FLOOR);
                } else {
                    actualValue = actualValue.setScale((int) Configuration.getPrecision(), RoundingMode.CEILING);
                    expectedValue = expectedValue.setScale((int) Configuration.getPrecision(), RoundingMode.CEILING);
                }
            } else {
                actualValue = actualValue.setScale(10, RoundingMode.CEILING);
                expectedValue = expectedValue.setScale(10, RoundingMode.CEILING);

            }
            if (expectedValue.compareTo(actualValue) != 0) {
                throw new ValidationException(HttpStatus.BAD_REQUEST, lang, ".expression.eval.failed",
                        new Object[]{testData.getInputValue(), testData.getOutputValue().toString(), actualValue.toString()});
            } else if(((testData.getOutputValue()).compareTo(expectedValue.doubleValue())!=0)){
                throw new ValidationException(HttpStatus.BAD_REQUEST, lang, ".expression.eval.failed",
                        new Object[]{testData.getInputValue(), testData.getOutputValue().toString(), actualValue.toString()});
            } else {
                testData.setOutputValue(expectedValue.doubleValue());
            }
        }
        Integer id = uomConfigService.createConfiguration(Configuration);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("tenantId", Configuration.getTenantId());
        if (id != 0) {
            responseHeaders.setLocation(ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri());
        }
        return new ResponseEntity<String>(messageSource.getMessage(".created.success", null, lang), responseHeaders, HttpStatus.CREATED);
    }

    /**
     * compare to and from fields. check if they have any one of the elements matching.
     *
     * @param arr1
     * @param arr2
     * @return
     */

    public boolean compareToAndFromFields(String[] arr1, String[] arr2) {
        Arrays.sort(arr1);
        Arrays.sort(arr2);
        for (int i = 0; i < arr1.length; i++) {
            for (int j = 0; j < arr2.length; j++) {
                if (arr1[i].contentEquals(arr2[j])) {
                    return true;
                }
            }
        }
        return false;
    }


    private Configuration transform(Expression expression) {
        Configuration Configuration = null;
        if (expression != null) {
            Configuration = new Configuration();
            Configuration.setName(expression.getName());
            Configuration.setEnabled(expression.isEnabled());
            Configuration.setPrecision(expression.getPrecision());
            Configuration.setRounding(expression.getRounding());
            String[] unitGroupTags = new String[expression.getUnitGroupTags().size()];

            Set<Object> tags = expression.getUnitGroupTags().stream().map(UnitGroupTag::getUnitGroup).collect(Collectors.toSet());
            Configuration.setUnitGroupTags(tags.toArray(unitGroupTags));

            Set<Object> fromUnits = expression.getFromMeasurementUnit().stream().map(MeasurementUnit::getUnit).collect(Collectors.toSet());
            String[] from = new String[expression.getFromMeasurementUnit().size()];
            Configuration.setFrom(fromUnits.toArray(from));
            Configuration.add(linkTo(methodOn(UoMMeasurementUnitController.class).fromMeasurementUnits(expression.getTenantId(), expression.getId(), null)).withRel("from"));


            Set<Object> toUnits = expression.getToMeasurementUnit().stream().map(MeasurementUnit::getUnit).collect(Collectors.toSet());
            String[] to = new String[expression.getToMeasurementUnit().size()];
            Configuration.setTo(toUnits.toArray(to));
            Configuration.setExpression(expression.getExpression());
            Configuration.add(linkTo(methodOn(UoMMeasurementUnitController.class).toMeasurementUnits(expression.getTenantId(), expression.getId(), null)).withRel("to"));


            Configuration.add(linkTo(methodOn(UoMConfigurationController.class).configurations(expression.getTenantId(), expression.getId())).withSelfRel());

            return Configuration;
        }
        return Configuration;
    }

    /**
     * Patch for all non array fields
     *
     * @param tenantId
     * @param id
     * @param requestBody
     * @param language
     * @return
     */
    @RequestMapping(path = "/{id}", method = RequestMethod.PATCH)
    @PreAuthorize("#oauth2.hasScope('uaa.write')")
    //@PreAuthorize("hasAnyAuthority('uaa.write')")
    public ResponseEntity<?> patchInUoMConfiguration(@RequestHeader(name = "tenantId", required = true) String tenantId,
                                                     @PathVariable("id") Integer id,
                                                     @RequestBody @NotNull String requestBody,
                                                     @RequestParam(name = "lang", defaultValue = "en") String language) {

        Locale lang = StringUtils.parseLocaleString(language);
        log.debug("TenantId in Request Headers " + tenantId);
        log.debug("RequestBody " + requestBody);

        if (!StringUtils.isEmpty(requestBody) && !requestBody.isEmpty() && !StringUtils.isEmpty(requestBody.substring(1, requestBody.length() - 1))) {
            if (uomConfigService.doesConfigurationExist(tenantId, id)) {
                boolean updated = uomConfigService.updateAllNonArrayFields(id, requestBody, lang);
                if (updated) {
                    return new ResponseEntity<String>(messageSource.getMessage("Configuration was updated Successfully", null, lang), HttpStatus.OK);
                } else {
                    return new ResponseEntity<String>(messageSource.getMessage("failed to update configuration", null, lang), HttpStatus.BAD_REQUEST);
                }
            }
            throw new ValidationException(HttpStatus.NOT_FOUND, lang, ".tofrom.doesnt.exist", requestBody);
        }
        throw new ValidationException(HttpStatus.BAD_REQUEST, lang, ".no.inputdata", requestBody);
    }
}