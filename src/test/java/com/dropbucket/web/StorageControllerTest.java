package com.dropbucket.web;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StorageControllerTest {

    private static final Path TEMP_DIR = Path.of(
            System.getProperty("java.io.tmpdir"),
            "dropbucket-tests-" + UUID.randomUUID()
    );

    static {
        try {
            Files.createDirectories(TEMP_DIR);
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("dropbucket.storage.object-dir", () -> TEMP_DIR.resolve("objects").toString());
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + TEMP_DIR.resolve("dropbucket-test.sqlite"));
    }

    @Test
    void uploadDownloadMetadataAndDeleteObject() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "hello.txt",
                "text/plain",
                "hello dropbucket".getBytes()
        );

        String location = mockMvc.perform(multipart("/api/storage/docs").file(file))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/storage/docs/")))
                .andReturn()
                .getResponse()
                .getHeader("Location");

        String id = location.substring(location.lastIndexOf("/") + 1);

        mockMvc.perform(get("/api/storage/docs/{id}/info", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.bucketName").value("docs"))
                .andExpect(jsonPath("$.originalFileName").value("hello.txt"))
                .andExpect(jsonPath("$.contentType").value("text/plain"))
                .andExpect(jsonPath("$.sizeBytes").value(16));

        mockMvc.perform(get("/api/storage/docs/{id}", id))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("text/plain")))
                .andExpect(content().string("hello dropbucket"));

        mockMvc.perform(delete("/api/storage/docs/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/storage/docs/{id}/info", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsInvalidBucketNames() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "hello.txt",
                "text/plain",
                "hello".getBytes()
        );

        mockMvc.perform(multipart("/api/storage/{bucketName}", "bad..bucket").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsNotFoundForMissingObject() throws Exception {
        mockMvc.perform(get("/api/storage/docs/00000000-0000-0000-0000-000000000000/info"))
                .andExpect(status().isNotFound());
    }
}
