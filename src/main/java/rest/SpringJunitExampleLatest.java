
import org.junit.After;
import org.junit.Before;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.junit.Assert.assertEquals;GroupTagTest

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ServiceApplication.class)
@WebIntegrationTest
@ActiveProfiles("test")
public class InteractionControllerTest {


    @Autowired
    WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    ServiceInteractionService serviceInteractionService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }
    @After
    public void reset_mocks() {
        Mockito.reset(this.serviceInteractionService);
    }

    @Test
    public void testModel()throws Exception {

        List<InteractionModel> catalogData =new ArrayList<>();
        IData iData =new IData();
        iData.setId("21a87c3b-321e-4b23-873c-aa945c32a6f4");
        iData.setEntityName("2126017378");
        iData.setEntityType("User");
        iData.setValue(44);

        InteractionModel interactionModelNode = new InteractionModel();
        interactionModelNode.setData(iData);
        interactionModelNode.setGroup("Nodes");
        catalogData.add(interactionModelNode);

        IData iEdgeData =new IData();
        iEdgeData.setId("22a87c3b-321e-4b23-873c-aa945c32a6f4");
        iEdgeData.setTarget("23a87c3b-321e-4b23-873c-aa945c32a6f4");
        iEdgeData.setSource("24a87c3b-321e-4b23-873c-aa945c32a6f4");
        iEdgeData.setAction("roleAssigned");
        iEdgeData.setValue(34);

        InteractionModel interactionModelEdge = new InteractionModel();
        interactionModelEdge.setData(iEdgeData);
        interactionModelEdge.setGroup("Edges");
        catalogData.add(interactionModelEdge);
        Mockito.when(serviceInteractionService.getCatalogInteractionModel(0L,1639116859752L)).thenReturn(catalogData);
        MockHttpServletResponse response = mockMvc.perform(get("/v1/model/catalog").param("startdate","0").param("enddate","1639116859752")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }

    @Test
    public void testgetCatalogInteractionModel2()throws Exception {
        when(serviceInteractionService.getCatalogInteractionModel(0L,1539116859752L)).thenReturn(new ArrayList<InteractionModel>());
        MockHttpServletResponse response = mockMvc.perform(get("/v1/model/catalog").param("startdate","0").param("enddate","1539116859752")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatus());
    }

    @Test
    public void testgetCatalogInteractionModel3()throws Exception {
        when(serviceInteractionService.getCatalogInteractionModel(0L,1539116859752L)).thenReturn(new ArrayList<InteractionModel>());
        MockHttpServletResponse response = mockMvc.perform(get("/v1/model/catalog").param("startdate","0").param("enddate","")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
    }



    @Ignore
    @Test
    public void testgetServiceInteractionModel()throws Exception {
    //TODO: Waiting for service interaction code to merge to master
        List<InteractionModel> catalogData =new ArrayList<>();
        IData iData =new IData();
        iData.setId("21a87c3b-321e-4b23-873c-aa945c32a6f4");
        iData.setEntityName("app_service_1");
        iData.setEntityType("app_service_1");
        iData.setValue(44);

        InteractionModel interactionModelNode = new InteractionModel();
        interactionModelNode.setData(iData);
        interactionModelNode.setGroup("Nodes");
        catalogData.add(interactionModelNode);

        IData iEdgeData =new IData();
        iEdgeData.setId("22a87c3b-321e-4b23-873c-aa945c32a6f4");
        iEdgeData.setTarget("23a87c3b-321e-4b23-873c-aa945c32a6f4");
        iEdgeData.setSource("24a87c3b-321e-4b23-873c-aa945c32a6f4");
        iEdgeData.setAction("34");
        iEdgeData.setValue(34);

        InteractionModel interactionModelEdge = new InteractionModel();
        interactionModelEdge.setData(iEdgeData);
        interactionModelEdge.setGroup("Edges");
        cData.add(interactionModelEdge);


        when(serviceInteractionService.getCatalogInteractionModel(0L,1639116859752L)).thenReturn(cData);
        MockHttpServletResponse response = mockMvc.perform(get("/v1/model").param("startdate","0").param("enddate","1639116859752")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }
    @Ignore
    @Test
    public void testgetServiceInteractionModel2()throws Exception {
        //TODO: Waiting for service interaction code to merge to master
        when(serviceInteractionService.getCatalogInteractionModel(0L,1539116859752L)).thenReturn(new ArrayList<InteractionModel>());
        MockHttpServletResponse response = mockMvc.perform(get("/v1/model/").param("startdate","0").param("enddate","1539116859752")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatus());
    }

    @Ignore
    @Test
    public void testgetServiceInteractionModel3()throws Exception {
        when(serviceInteractionService.getCatalogInteractionModel(0L,1539116859752L)).thenReturn(new ArrayList<InteractionModel>());
        MockHttpServletResponse response = mockMvc.perform(get("/v1/model/").param("startdate","0").param("enddate","")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
    }
}
