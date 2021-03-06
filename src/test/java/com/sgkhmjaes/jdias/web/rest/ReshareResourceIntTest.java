package com.sgkhmjaes.jdias.web.rest;

import com.sgkhmjaes.jdias.JDiasApp;
import com.sgkhmjaes.jdias.domain.Reshare;
import com.sgkhmjaes.jdias.domain.StatusMessage;
import com.sgkhmjaes.jdias.domain.User;
import com.sgkhmjaes.jdias.repository.PersonRepository;
import com.sgkhmjaes.jdias.repository.PostRepository;
import com.sgkhmjaes.jdias.repository.ReshareRepository;
import com.sgkhmjaes.jdias.repository.UserRepository;
import com.sgkhmjaes.jdias.repository.search.PostSearchRepository;
import com.sgkhmjaes.jdias.repository.search.ReshareSearchRepository;
import com.sgkhmjaes.jdias.repository.search.StatusMessageSearchRepository;
import com.sgkhmjaes.jdias.security.SecurityUtils;
import com.sgkhmjaes.jdias.service.PostService;
import com.sgkhmjaes.jdias.service.UserService;
import com.sgkhmjaes.jdias.service.dto.PostDTO;
import com.sgkhmjaes.jdias.service.dto.StatusMessageDTO;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the ReshareResource REST controller.
 *
 * @see ReshareResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = JDiasApp.class)
public class ReshareResourceIntTest {

    private static final String DEFAULT_ROOT_AUTHOR = "AAAAAAAAAA";
    private static final String UPDATED_ROOT_AUTHOR = "BBBBBBBBBB";

    private static final String DEFAULT_ROOT_GUID = "AAAAAAAAAA";
    private static final String UPDATED_ROOT_GUID = "BBBBBBBBBB";
    
    private static final String DEFAULT_TEXT = "TEST STATUS MESSAGES TEXT";
    
    private static StatusMessage statusMessage;

    @Autowired
    private ReshareRepository reshareRepository;

    @Autowired
    private PostService postService;
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private PostSearchRepository postSearchRepository;

    @Autowired
    private ReshareSearchRepository reshareSearchRepository;
    
    @Autowired
    private StatusMessageSearchRepository statusMessageSearchRepository;
    
    @Autowired
    private StatusMessageSearchRepository statusMessageRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private EntityManager em;

    private MockMvc restReshareMockMvc;

    private Reshare reshare;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ReshareResource reshareResource = new ReshareResource(postService);
        this.restReshareMockMvc = MockMvcBuilders.standaloneSetup(reshareResource)
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
    public static Reshare createEntity(EntityManager em) {
        Reshare reshare = new Reshare()
            .rootAuthor(DEFAULT_ROOT_AUTHOR)
            .rootGuid(DEFAULT_ROOT_GUID);
        return reshare;
    }

    @Before
    public void initTest() throws Exception {
        
        User user = userService.createUser("johndoe", "johndoe", "John", "Doe", "john.doe@localhost", "http://placehold.it/50x50", "en-US");
        user.setActivated(true);
        userRepository.saveAndFlush(user);
        
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken("johndoe", "johndoe"));
        SecurityContextHolder.setContext(securityContext);
        userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get().getId();
        
        StatusMessage sm = new StatusMessage();
        sm.setText(DEFAULT_TEXT);
        StatusMessageDTO smdto= new StatusMessageDTO();
        smdto.setStatusMessage(sm);
        
        statusMessage = postService.save(smdto);
        
        reshareSearchRepository.deleteAll();
        reshare = createEntity(em);
    }
    
    @After
    public void deleteCreatedAccount(){
        postRepository.deleteAll();
        postSearchRepository.deleteAll();
        statusMessageRepository.deleteAll();
        statusMessageSearchRepository.deleteAll();
        reshareRepository.deleteAll();
        reshareSearchRepository.deleteAll();
        userService.deleteUser("johndoe");
    }

    @Test
    @Transactional
    public void createReshare() throws Exception {
        int databaseSizeBeforeCreate = reshareRepository.findAll().size();
        
        PostDTO postDTO = new PostDTO();
        postDTO.setId(statusMessage.getId());
        
        // Create the Reshare
        restReshareMockMvc.perform(post("/api/reshares")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(postDTO)))
            .andExpect(status().isCreated());

        // Validate the Reshare in the database
        List<Reshare> reshareList = reshareRepository.findAll();
        assertThat(reshareList).hasSize(databaseSizeBeforeCreate);
        Reshare testReshare = reshareList.get(reshareList.size() - 1);
        //assertThat(testReshare.getRootAuthor()).isEqualTo(DEFAULT_ROOT_AUTHOR);
        //assertThat(testReshare.getRootGuid()).isEqualTo(DEFAULT_ROOT_GUID);

        // Validate the Reshare in Elasticsearch
        Reshare reshareEs = reshareSearchRepository.findOne(testReshare.getId());
        assertThat(testReshare.getId()).isEqualTo(reshareEs.getId());
        assertThat(testReshare.getRootGuid()).isEqualTo(reshareEs.getRootGuid());
        assertThat(testReshare.getRootAuthor()).isEqualTo(reshareEs.getRootAuthor());
    }

    @Test
    @Transactional
    public void getAllReshares() throws Exception {
        // Initialize the database
        PostDTO postDTO = new PostDTO();
        postDTO.setId(statusMessage.getId());
        Reshare saveReshare = postService.saveReshare(postDTO);
        //reshareRepository.saveAndFlush(reshare);

        // Get all the reshareList
        restReshareMockMvc.perform(get("/api/reshares?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(saveReshare.getId().intValue())))
            .andExpect(jsonPath("$.[*].rootAuthor").value(hasItem(saveReshare.getRootAuthor())))
            .andExpect(jsonPath("$.[*].rootGuid").value(hasItem(saveReshare.getRootGuid())));
    }
    
    @Test
    @Transactional
    public void getReshare() throws Exception {
        // Initialize the database
        PostDTO postDTO = new PostDTO();
        postDTO.setId(statusMessage.getId());
        Reshare saveReshare = postService.saveReshare(postDTO);
        //reshareRepository.saveAndFlush(reshare);

        // Get the reshare
        restReshareMockMvc.perform(get("/api/reshares/{id}", reshare.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(saveReshare.getId().intValue())))
            .andExpect(jsonPath("$.[*].rootAuthor").value(hasItem(saveReshare.getRootAuthor())))
            .andExpect(jsonPath("$.[*].rootGuid").value(hasItem(saveReshare.getRootGuid())));
    }

    @Test
    @Transactional
    public void getNonExistingReshare() throws Exception {
        // Get the reshare
        restReshareMockMvc.perform(get("/api/reshares/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateReshare() throws Exception {
        // Initialize the database
        
        PostDTO postDTO = new PostDTO();
        postDTO.setId(statusMessage.getId());
        Reshare saveReshare = postService.saveReshare(postDTO);

        int databaseSizeBeforeUpdate = reshareRepository.findAll().size();

        // Update the reshare
        Reshare updatedReshare = reshareRepository.findOne(saveReshare.getId());
        updatedReshare
            .rootAuthor(UPDATED_ROOT_AUTHOR)
            .rootGuid(UPDATED_ROOT_GUID);

        restReshareMockMvc.perform(put("/api/reshares")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedReshare)))
            .andExpect(status().isOk());

        // Validate the Reshare in the database
        List<Reshare> reshareList = reshareRepository.findAll();
        assertThat(reshareList).hasSize(databaseSizeBeforeUpdate);
        Reshare testReshare = reshareList.get(reshareList.size() - 1);
        assertThat(testReshare.getRootAuthor()).isEqualTo(UPDATED_ROOT_AUTHOR);
        assertThat(testReshare.getRootGuid()).isEqualTo(UPDATED_ROOT_GUID);

        // Validate the Reshare in Elasticsearch
        Reshare reshareEs = reshareSearchRepository.findOne(testReshare.getId());
        
        assertThat(testReshare.getId()).isEqualTo(reshareEs.getId());
        assertThat(testReshare.getRootGuid()).isEqualTo(reshareEs.getRootGuid());
        assertThat(testReshare.getRootAuthor()).isEqualTo(reshareEs.getRootAuthor());
        //assertThat(reshareEs).isEqualToComparingFieldByField(testReshare);
    }
    
    /*
        @Test
    @Transactional
    public void deleteReshare() throws Exception {
        // Initialize the database
        PostDTO postDTO = new PostDTO();
        postDTO.setId(statusMessage.getId());
        Reshare saveReshare = postService.saveReshare(postDTO);
        //postService.save(reshare);
        
        System.err.println(reshareRepository.findAll());

        int databaseSizeBeforeDelete = reshareRepository.findAll().size();
        
        // Get the reshare
        restReshareMockMvc.perform(delete("/api/reshares/{id}", saveReshare.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate Elasticsearch is empty
        boolean reshareExistsInEs = reshareSearchRepository.exists(saveReshare.getId());
        assertThat(reshareExistsInEs).isFalse();

        // Validate the database is empty
        List<Reshare> reshareList = reshareRepository.findAll();
        assertThat(reshareList).hasSize(databaseSizeBeforeDelete - 1);
    }
    
    @Test
    @Transactional
    public void updateNonExistingReshare() throws Exception {
        int databaseSizeBeforeUpdate = reshareRepository.findAll().size();
        
        PostDTO postDTO = new PostDTO();
        postDTO.setId(statusMessage.getId());
        Reshare saveReshare = postService.saveReshare(postDTO);

        // Create the Reshare

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restReshareMockMvc.perform(put("/api/reshares")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(postDTO)))
            .andExpect(status().isCreated());

        // Validate the Reshare in the database
        List<Reshare> reshareList = reshareRepository.findAll();
        assertThat(reshareList).hasSize(databaseSizeBeforeUpdate + 1);
    }
        
    @Test
    @Transactional
    public void createReshareWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = reshareRepository.findAll().size();
        PostDTO postDTO = new PostDTO();
        postDTO.setId(statusMessage.getId());
        Reshare saveReshare = postService.saveReshare(postDTO);

        // Create the Reshare with an existing ID
        saveReshare.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restReshareMockMvc.perform(post("/api/reshares")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(saveReshare)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<Reshare> reshareList = reshareRepository.findAll();
        assertThat(reshareList).hasSize(databaseSizeBeforeCreate);
    }
*/
    @Test
    @Transactional
    public void searchReshare() throws Exception {
        // Initialize the database
        PostDTO postDTO = new PostDTO();
        postDTO.setId(statusMessage.getId());
        Reshare saveReshare = postService.saveReshare(postDTO);
        //postService.save(reshare);

        // Search the reshare
        restReshareMockMvc.perform(get("/api/_search/reshares?query=id:" + saveReshare.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(saveReshare.getId().intValue())))
            .andExpect(jsonPath("$.[*].rootAuthor").value(hasItem(saveReshare.getRootAuthor())))
            .andExpect(jsonPath("$.[*].rootGuid").value(hasItem(saveReshare.getRootGuid())));
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Reshare.class);
        Reshare reshare1 = new Reshare();
        reshare1.setId(1L);
        Reshare reshare2 = new Reshare();
        reshare2.setId(reshare1.getId());
        assertThat(reshare1).isEqualTo(reshare2);
        reshare2.setId(2L);
        assertThat(reshare1).isNotEqualTo(reshare2);
        reshare1.setId(null);
        assertThat(reshare1).isNotEqualTo(reshare2);
    }
}
