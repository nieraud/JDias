package com.sgkhmjaes.jdias.web.rest;

import com.sgkhmjaes.jdias.JDiasApp;
import com.sgkhmjaes.jdias.domain.StatusMessage;
import com.sgkhmjaes.jdias.domain.User;
import com.sgkhmjaes.jdias.repository.PersonRepository;
import com.sgkhmjaes.jdias.repository.StatusMessageRepository;
import com.sgkhmjaes.jdias.repository.UserRepository;
import com.sgkhmjaes.jdias.service.PostService;
import com.sgkhmjaes.jdias.service.TagService;
import com.sgkhmjaes.jdias.repository.search.StatusMessageSearchRepository;
import com.sgkhmjaes.jdias.security.SecurityUtils;
import com.sgkhmjaes.jdias.service.UserService;
import com.sgkhmjaes.jdias.service.dto.StatusMessageDTO;
import com.sgkhmjaes.jdias.service.impl.StatusMessageDTOServiceImpl;
import com.sgkhmjaes.jdias.web.rest.errors.ExceptionTranslator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import org.junit.After;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the StatusMessageResource REST controller.
 *
 * @see StatusMessageResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = JDiasApp.class)
public class StatusMessageResourceIntTest {

    private static Long userID;

    private static final String DEFAULT_TEXT = "AAAAAAAAAA";
    private static final String UPDATED_TEXT = "BBBBBBBBBB";

    @Autowired
    private StatusMessageRepository statusMessageRepository;

    @Autowired
    private PostService postService;

    @Autowired
    private StatusMessageDTOServiceImpl statusMessageDTOService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private  TagService tagService;

    @Autowired
    private StatusMessageSearchRepository statusMessageSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restStatusMessageMockMvc;

    private StatusMessage statusMessage;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        StatusMessageResource statusMessageResource = new StatusMessageResource(postService, statusMessageDTOService, tagService
        );
        this.restStatusMessageMockMvc = MockMvcBuilders.standaloneSetup(statusMessageResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static StatusMessage createEntity(EntityManager em) {
        StatusMessage statusMessage = new StatusMessage()
            .text(DEFAULT_TEXT);
        return statusMessage;
    }

    @Before
    public void initTest() {

        User user = userService.createUser("johndoe", "johndoe", "John", "Doe", "john.doe@localhost", "http://placehold.it/50x50", "en-US");
        user.setActivated(true);
        userRepository.saveAndFlush(user);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken("johndoe", "johndoe"));
        SecurityContextHolder.setContext(securityContext);
        userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get().getId();

        userID = user.getId();

        statusMessage = createEntity(em);
    }

    @After
    public void deleteCreatedAccount(){
        statusMessageRepository.deleteAll();
        statusMessageSearchRepository.deleteAll();
        userService.deleteUser("johndoe");

    }

    @Test
    @Transactional
    public void createStatusMessage() throws Exception {
        int databaseSizeBeforeCreate = statusMessageRepository.findAll().size();
        StatusMessageDTO smdto= new StatusMessageDTO();
        smdto.setStatusMessage(statusMessage);

        // Create the StatusMessage
        restStatusMessageMockMvc.perform(post("/api/status-messages")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(smdto)))
            .andExpect(status().isCreated());

        // Validate the StatusMessage in the database
        List<StatusMessage> statusMessageList = statusMessageRepository.findAll();
        assertThat(statusMessageList).hasSize(databaseSizeBeforeCreate + 1);
        StatusMessage testStatusMessage = statusMessageList.get(statusMessageList.size() - 1);
        assertThat(testStatusMessage.getText()).isEqualTo(DEFAULT_TEXT);

        // Validate the StatusMessage in Elasticsearch
        StatusMessage statusMessageEs = statusMessageSearchRepository.findOne(testStatusMessage.getId());
        assertThat(testStatusMessage.getId()).isEqualTo(statusMessageEs.getId());
        assertThat(testStatusMessage.getText()).isEqualTo(statusMessageEs.getText());
        assertThat(testStatusMessage.getLocation()).isEqualTo(statusMessageEs.getLocation());
        assertThat(testStatusMessage.getPhotos()).isEqualTo(statusMessageEs.getPhotos());
        assertThat(testStatusMessage.getPoll()).isEqualTo(statusMessageEs.getPoll());
        //assertThat(testStatusMessage.getPosts()).isEqualTo(statusMessageEs.getPosts());
        //assertThat(statusMessageEs).isEqualToComparingFieldByField(testStatusMessage);
    }

    @Test
    @Transactional
    public void createStatusMessageWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = statusMessageRepository.findAll().size();
        StatusMessageDTO smdto= new StatusMessageDTO();
        smdto.setStatusMessage(statusMessage);

        // Create the StatusMessage with an existing ID
        statusMessage.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restStatusMessageMockMvc.perform(post("/api/status-messages")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(smdto)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<StatusMessage> statusMessageList = statusMessageRepository.findAll();
        assertThat(statusMessageList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllStatusMessages() throws Exception {
        // Initialize the database
        StatusMessageDTO smdto= new StatusMessageDTO();
        smdto.setStatusMessage(statusMessage);

        restStatusMessageMockMvc.perform(post("/api/status-messages")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(smdto)))
            .andExpect(status().isCreated());
        //statusMessageRepository.saveAndFlush(statusMessage);

        // Get all the statusMessageList
        restStatusMessageMockMvc.perform(get("/api/status-messages?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].text").value(hasItem(DEFAULT_TEXT)));
            //.andExpect(jsonPath("$.[*].id").value(hasItem(smdto.getStatusMessage().getId().intValue())))

    }

    @Test
    @Transactional
    public void getStatusMessage() throws Exception {
        // Initialize the database
        StatusMessageDTO smdto= new StatusMessageDTO();
        smdto.setStatusMessage(statusMessage);

        restStatusMessageMockMvc.perform(post("/api/status-messages")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(smdto)))
            .andExpect(status().isCreated());
        //statusMessageRepository.saveAndFlush(statusMessage);
        ResultActions perform = restStatusMessageMockMvc.perform(get("/api/status-messages/{id}", statusMessage.getId()));

        //Get the statusMessage
        restStatusMessageMockMvc.perform(get("/api/status-messages/{id}", statusMessage.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].text").value(DEFAULT_TEXT));
            //.andExpect(jsonPath("$.id").value(smdto.getStatusMessage().getId().intValue()));

    }

    @Test
    @Transactional
    public void getNonExistingStatusMessage() throws Exception {
        // Get the statusMessage
        restStatusMessageMockMvc.perform(get("/api/status-messages/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateStatusMessage() throws Exception {
        // Initialize the database
        StatusMessageDTO smdto= new StatusMessageDTO();
        smdto.setStatusMessage(statusMessage);
        postService.save(smdto);

        int databaseSizeBeforeUpdate = statusMessageRepository.findAll().size();

        // Update the statusMessage
        StatusMessage updatedStatusMessage = statusMessageRepository.findOne(statusMessage.getId());
        updatedStatusMessage.text(UPDATED_TEXT);
        StatusMessageDTO smdtoUpdate= new StatusMessageDTO();
        smdtoUpdate.setStatusMessage(updatedStatusMessage);

        restStatusMessageMockMvc.perform(put("/api/status-messages")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(smdtoUpdate)))
            .andExpect(status().isOk());

        // Validate the StatusMessage in the database
        List<StatusMessage> statusMessageList = statusMessageRepository.findAll();
        assertThat(statusMessageList).hasSize(databaseSizeBeforeUpdate);
        StatusMessage testStatusMessage = statusMessageList.get(statusMessageList.size() - 1);
        assertThat(testStatusMessage.getText()).isEqualTo(UPDATED_TEXT);

        // Validate the StatusMessage in Elasticsearch
        StatusMessage statusMessageEs = statusMessageSearchRepository.findOne(testStatusMessage.getId());
        assertThat(statusMessageEs).isEqualToComparingFieldByField(testStatusMessage);
    }

    @Test
    @Transactional
    public void updateNonExistingStatusMessage() throws Exception {
        int databaseSizeBeforeUpdate = statusMessageRepository.findAll().size();
        StatusMessageDTO smdto= new StatusMessageDTO();
        smdto.setStatusMessage(statusMessage);

        // Create the StatusMessage

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restStatusMessageMockMvc.perform(put("/api/status-messages")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(smdto)))
            .andExpect(status().isCreated());

        // Validate the StatusMessage in the database
        List<StatusMessage> statusMessageList = statusMessageRepository.findAll();
        assertThat(statusMessageList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteStatusMessage() throws Exception {
        StatusMessageDTO smdto= new StatusMessageDTO();
        smdto.setStatusMessage(statusMessage);
        // Initialize the database
        postService.save(smdto);

        int databaseSizeBeforeDelete = statusMessageRepository.findAll().size();

        // Get the statusMessage
        restStatusMessageMockMvc.perform(delete("/api/status-messages/{id}", statusMessage.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate Elasticsearch is empty
        boolean statusMessageExistsInEs = statusMessageSearchRepository.exists(statusMessage.getId());
        assertThat(statusMessageExistsInEs).isFalse();

        // Validate the database is empty
        List<StatusMessage> statusMessageList = statusMessageRepository.findAll();
        assertThat(statusMessageList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchStatusMessage() throws Exception {
        // Initialize the database
        postService.save(statusMessage);

        // Search the statusMessage
        restStatusMessageMockMvc.perform(get("/api/_search/status-messages?query=id:" + statusMessage.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(statusMessage.getId().intValue())))
            .andExpect(jsonPath("$.[*].text").value(hasItem(DEFAULT_TEXT.toString())));
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(StatusMessage.class);
        StatusMessage statusMessage1 = new StatusMessage();
        statusMessage1.setId(1L);
        StatusMessage statusMessage2 = new StatusMessage();
        statusMessage2.setId(statusMessage1.getId());
        assertThat(statusMessage1).isEqualTo(statusMessage2);
        statusMessage2.setId(2L);
        assertThat(statusMessage1).isNotEqualTo(statusMessage2);
        statusMessage1.setId(null);
        assertThat(statusMessage1).isNotEqualTo(statusMessage2);
    }
}
