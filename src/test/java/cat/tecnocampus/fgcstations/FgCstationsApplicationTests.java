package cat.tecnocampus.fgcstations;

import cat.tecnocampus.fgcstations.application.DTOs.DayTimeStartDTO;
import cat.tecnocampus.fgcstations.application.DTOs.FavoriteJourneyDTO;
import cat.tecnocampus.fgcstations.application.DTOs.FriendsDTO;
import cat.tecnocampus.fgcstations.application.exception.FriendAlreadyExistsException;
import cat.tecnocampus.fgcstations.application.exception.UserDoesNotExistsException;
import cat.tecnocampus.fgcstations.domain.exceptions.SameOriginDestinationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FgCstationsApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private FriendsDTO expectedFriend;

    @Test
    @Order(1)
    void contextLoads() {
    }

    @Test
    @Order(2)
    void usernameValid() throws Exception {

        expectedFriend = new FriendsDTO();
        expectedFriend.setUsername("robert"); //ok

        String friend1 = "{\"username\":\"sh\"}";

        mockMvc.perform(post("/users/friends")
                        .contentType("application/json")
                        .content(friend1))
                .andExpect(status().isBadRequest());

        String friend2 = "{\"username\":\"Robert\"}";

        mockMvc.perform(post("/users/friends")
                        .contentType("application/json")
                        .content(friend2))
                .andExpect(status().isBadRequest());

        String friend3 = "{\"username\":\"robert\"}";

        MvcResult mvcResult = mockMvc.perform(post("/users/friends")
                        .contentType("application/json")
                        .content(friend3))
                .andExpect(status().isCreated()).andReturn();

        String objectCreated = mvcResult.getResponse().getContentAsString();

        assertThat(objectCreated).isEqualToIgnoringWhitespace(objectMapper.writeValueAsString(expectedFriend));
    }

    @Test
    @Order(3)
    void journeyValid() throws Exception {

        String journey1 = "{\"origin\":\"BCN\",\"destination\":\"Madrid\"}";    //origin wrong
        String journey2 = "{\"origin\":\"Barcelona\",\"destination\":\"MAD\"}";    //destination wrong
        String journey3 = "{\"origin\":\"Barcelona\",\"destination\":\"Madrid\"}";    //ok

        FavoriteJourneyDTO expectedJourney = new FavoriteJourneyDTO();
        expectedJourney.setOrigin("Barcelona");
        expectedJourney.setOrigin("Madrid");


        mockMvc.perform(post("/users/{userName}/favoriteJourney", expectedFriend.getUsername())
                        .content(journey1))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/users/{userName}/favoriteJourney", expectedFriend.getUsername())
                        .content(journey2))
                .andExpect(status().isBadRequest());

        MvcResult mvcResult = mockMvc.perform(post("/users/{userName}/favoriteJourney", expectedFriend.getUsername())
                        .content(journey3))
                .andExpect(status().isCreated()).andReturn();

        String objectCreated = mvcResult.getResponse().getContentAsString();

        assertThat(objectCreated).isEqualToIgnoringWhitespace(objectMapper.writeValueAsString(expectedJourney));

    }

    @Test
    void DayAndTimeValid() throws Exception {

        String DayTime1 = "{\"dayOfWeek\":\"tuesday\",\"time\":\"02:14\"}"; //wrong day
        String DayTime2 = "{\"dayOfWeek\":\"Tuesday\",\"time\":\"2:14\"}";  //wrong time
        String DayTime3 = "{\"dayOfWeek\":\"Tuesday\",\"time\":\"02:14\"}"; //ok

        DayTimeStartDTO expectedDayTime = new DayTimeStartDTO();
        expectedDayTime.setDayOfWeek("Tuesday");
        expectedDayTime.setTime("02:14");

        mockMvc.perform(post("/users/{userName}/favoriteJourney", expectedFriend.getUsername())
                        .content(DayTime1))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/users/{userName}/favoriteJourney", expectedFriend.getUsername())
                        .content(DayTime2))
                .andExpect(status().isBadRequest());

        MvcResult mvcResult = mockMvc.perform(post("/users/{userName}/favoriteJourney", expectedFriend.getUsername())
                        .content(DayTime3))
                .andExpect(status().isCreated()).andReturn();

        String objectCreated = mvcResult.getResponse().getContentAsString();

        assertThat(objectCreated).isEqualToIgnoringWhitespace(objectMapper.writeValueAsString(expectedDayTime));

    }

    @Test
    void NotFoundExceptionTest() throws Exception {

        String journey = "{\"origin\":\"Barcelona\",\"destination\":\"Madrid\"}";

        mockMvc.perform(post("/users/{userName}/favoriteJourney", "unexistentUser")
                        .content(journey))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserDoesNotExistsException));

        String DayTime = "{\"dayOfWeek\":\"Tuesday\",\"time\":\"02:14\"}";

        mockMvc.perform(post("/users/{userName}/favoriteJourney", "unexistentUser")
                        .content(DayTime))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserDoesNotExistsException));
    }

    @Test
    void OriginEqualsDestinationTest() throws Exception {

        String journey = "{\"origin\":\"Barcelona\",\"destination\":\"Barcelona\"}";

        mockMvc.perform(post("/users/{userName}/favoriteJourney", expectedFriend.getUsername())
                        .content(journey))
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof SameOriginDestinationException));

        String DayTime = "{\"dayOfWeek\":\"Tuesday\",\"time\":\"02:14\"}";

        mockMvc.perform(post("/users/{userName}/favoriteJourney", "unexistentUser")
                        .content(DayTime))
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof SameOriginDestinationException));
    }

    @Test
    void FriendAlreadyExistTest() throws Exception {

        String friend = "{\"username\":\"robert\"}";

        mockMvc.perform(post("/users/friends")
                        .content(friend))
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof FriendAlreadyExistsException));


    }


}