package com.exxeta.serverless.controller;

import java.util.ArrayList;
import java.util.List;

import com.exxeta.serverless.repository.PersonRepository;
import com.exxeta.serverless.repository.model.Person;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@WebMvcTest(PersonController.class)
@AutoConfigureMockMvc
public class PersonControllerTests {
	@Autowired
	protected MockMvc mockMvc;

	@MockBean
	private PersonRepository personRepository;

	@Test
	public void should_return_all_persons_in_db() throws Exception {
		List<Person> persons = new ArrayList<>();
		persons.add(new Person("id1", "Max"));
		persons.add(new Person("id2", "Erika"));
		when(personRepository.findAll()).thenReturn(persons);

		this
				.mockMvc
				.perform(MockMvcRequestBuilders.get("/api/person"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().json("[{\"id\":\"id1\",\"name\":\"Max\"},{\"id\":\"id2\",\"name\":\"Erika\"}]", true));

		verify(personRepository, times(1)).findAll();
	}

	@Test
	public void should_save_single_person() throws Exception {
		Person expectedPerson = new Person("id1", "Max");
		when(personRepository.save(expectedPerson)).thenReturn(expectedPerson);

		this
				.mockMvc
				.perform(MockMvcRequestBuilders.post("/api/person").content("{\"id\":\"id1\",\"name\":\"Max\"}").contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().json("{\"id\":\"id1\", \"name\":\"Max\"}", true));

		verify(personRepository, times(1)).save(expectedPerson);
	}
}
