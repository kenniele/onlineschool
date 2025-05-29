package com.onlineSchool.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlineSchool.config.TestSecurityConfig;
import com.onlineSchool.model.*;
import com.onlineSchool.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SimpleApiIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebinarRepository webinarRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User testStudent;
    private User testTeacher;
    private Webinar testWebinar;
    private String baseUrl;
    private HttpClient httpClient;
    private String csrfToken;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        httpClient = HttpClient.newBuilder()
                .cookieHandler(new java.net.CookieManager())  // Автоматическое сохранение cookies
                .build();
        
        // Очищаем данные
        commentRepository.deleteAll();
        likeRepository.deleteAll();
        webinarRepository.deleteAll();
        userRepository.deleteAll();

        // Создаем тестовых пользователей
        testStudent = new User();
        testStudent.setUsername("teststudent");
        testStudent.setEmail("teststudent@example.com");
        testStudent.setPassword(passwordEncoder.encode("password"));
        testStudent.setFirstName("Test");
        testStudent.setLastName("Student");
        testStudent.setRole(Role.STUDENT);
        testStudent = userRepository.save(testStudent);

        testTeacher = new User();
        testTeacher.setUsername("testteacher");
        testTeacher.setEmail("testteacher@example.com");
        testTeacher.setPassword(passwordEncoder.encode("password"));
        testTeacher.setFirstName("Test");
        testTeacher.setLastName("Teacher");
        testTeacher.setRole(Role.TEACHER);
        testTeacher = userRepository.save(testTeacher);

        // Создаем тестовый вебинар
        testWebinar = new Webinar();
        testWebinar.setTitle("Test Webinar");
        testWebinar.setDescription("Test Description");
        testWebinar.setTeacher(testTeacher);
        testWebinar.setStartTime(LocalDateTime.now().plusDays(1));
        testWebinar.setDuration(90);
        testWebinar.setMaxParticipants(50);
        testWebinar.setStatus(WebinarStatus.SCHEDULED);
        testWebinar = webinarRepository.save(testWebinar);

        System.out.println("=== Simple Test Setup ===");
        System.out.println("Student ID: " + testStudent.getId());
        System.out.println("Webinar ID: " + testWebinar.getId());
    }

    @Test
    void testUnauthorizedApiAccess() throws Exception {
        // Тест без авторизации - должен вернуть 401 или 403
        String url = baseUrl + "/api/likes/toggle/WEBINAR/" + testWebinar.getId();
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Unauthorized response status: " + response.statusCode());
        System.out.println("Unauthorized response body: " + response.body());
        
        // Ожидаем 401 или 403
        assertThat(response.statusCode()).isIn(401, 403);
    }

    @Test
    void testCompleteWorkflowWithAuthentication() throws Exception {
        System.out.println("\n=== Testing Complete Workflow ===");

        // 1. Логинимся
        boolean loginSuccess = login("teststudent", "password");
        assertThat(loginSuccess).isTrue();
        System.out.println("✅ Login successful");

        // 2. Проверяем начальное состояние
        List<Like> initialLikes = likeRepository.findByEntityTypeAndEntityId(EntityType.WEBINAR, testWebinar.getId());
        assertThat(initialLikes).isEmpty();
        System.out.println("✅ Initial state verified - no likes");

        // 3. Ставим лайк
        boolean likeSuccess = toggleLike();
        assertThat(likeSuccess).isTrue();
        System.out.println("✅ Like added successfully");

        // 4. Проверяем лайк в базе данных
        List<Like> likesAfterAdd = likeRepository.findByEntityTypeAndEntityId(EntityType.WEBINAR, testWebinar.getId());
        assertThat(likesAfterAdd).hasSize(1);
        assertThat(likesAfterAdd.get(0).getUser().getId()).isEqualTo(testStudent.getId());
        System.out.println("✅ Like verified in database");

        // 5. Добавляем комментарий
        boolean commentSuccess = addComment("This is a test comment from integration test!");
        assertThat(commentSuccess).isTrue();
        System.out.println("✅ Comment added successfully");

        // 6. Проверяем комментарий в базе данных
        List<Comment> comments = commentRepository.findByEntityTypeAndEntityId(EntityType.WEBINAR, testWebinar.getId());
        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).getContent()).isEqualTo("This is a test comment from integration test!");
        assertThat(comments.get(0).getUser().getId()).isEqualTo(testStudent.getId());
        System.out.println("✅ Comment verified in database");

        // 7. Убираем лайк
        boolean unlikeSuccess = toggleLike();
        assertThat(unlikeSuccess).isTrue();
        System.out.println("✅ Like removed successfully");

        // 8. Проверяем, что лайк удален
        List<Like> likesAfterRemove = likeRepository.findByEntityTypeAndEntityId(EntityType.WEBINAR, testWebinar.getId());
        assertThat(likesAfterRemove).isEmpty();
        System.out.println("✅ Like removal verified in database");

        System.out.println("🎉 Complete workflow test passed!");
    }

    @Test
    void testGetPublicEndpoints() throws Exception {
        // Тест публичных эндпоинтов, которые должны работать без авторизации
        
        // 1. Получение комментариев для вебинара
        String commentsUrl = baseUrl + "/api/comments/webinar/" + testWebinar.getId();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(commentsUrl))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Get comments response status: " + response.statusCode());
        assertThat(response.statusCode()).isEqualTo(200);
        
        JsonNode commentsArray = objectMapper.readTree(response.body());
        assertThat(commentsArray.isArray()).isTrue();
        System.out.println("✅ Public comments endpoint works");
    }

    private boolean login(String username, String password) throws Exception {
        // Сначала получаем CSRF токен через GET запрос на страницу логина
        String loginPageUrl = baseUrl + "/login";
        
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(loginPageUrl))
                .GET()
                .build();

        HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
        
        if (getResponse.statusCode() != 200) {
            System.out.println("Failed to get login page: " + getResponse.statusCode());
            return false;
        }
        
        // Извлекаем CSRF токен из HTML
        csrfToken = extractCsrfToken(getResponse.body());
        if (csrfToken == null) {
            System.out.println("CSRF token not found in login page");
            return false;
        }
        
        System.out.println("CSRF token extracted: " + csrfToken.substring(0, Math.min(10, csrfToken.length())) + "...");
        
        // Выполняем логин с CSRF токеном
        String formData = "username=" + username + "&password=" + password + "&_csrf=" + csrfToken;
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(loginPageUrl))
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Login response status: " + response.statusCode());
        
        // Если это redirect, значит логин успешен
        if (response.statusCode() == 302 || response.statusCode() == 303) {
            String location = response.headers().firstValue("Location").orElse(null);
            if (location != null) {
                System.out.println("Following redirect to: " + location);
                
                // Проверяем cookies после логина
                java.net.CookieStore cookieStore = ((java.net.CookieManager) httpClient.cookieHandler().get()).getCookieStore();
                System.out.println("Cookies after login:");
                cookieStore.getCookies().forEach(cookie -> 
                    System.out.println("  " + cookie.getName() + "=" + cookie.getValue()));
                
                // Если есть JSESSIONID cookie, значит логин успешен
                boolean hasSessionCookie = cookieStore.getCookies().stream()
                    .anyMatch(cookie -> "JSESSIONID".equals(cookie.getName()));
                
                if (hasSessionCookie) {
                    System.out.println("✅ Login successful - session cookie found");
                    return true;
                }
            }
        }
        
        // Успешный логин обычно возвращает redirect (302/303) или 200
        boolean success = response.statusCode() >= 200 && response.statusCode() < 400;
        System.out.println("Login result: " + success);
        return success;
    }
    
    private String extractCsrfToken(String html) {
        // Ищем CSRF токен в HTML через простой regex
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("name=\"_csrf\".*?value=\"([^\"]+)\"");
        java.util.regex.Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        // Альтернативный поиск
        pattern = java.util.regex.Pattern.compile("_csrf.*?value=\"([^\"]+)\"");
        matcher = pattern.matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return null;
    }

    private boolean toggleLike() throws Exception {
        String url = baseUrl + "/api/likes/toggle/WEBINAR/" + testWebinar.getId();
        
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .header("Content-Type", "application/json");
        
        // Добавляем CSRF токен если он есть
        if (csrfToken != null) {
            requestBuilder.header("X-CSRF-TOKEN", csrfToken);
        }
        
        HttpRequest request = requestBuilder.build();

        // Проверяем cookies перед запросом
        java.net.CookieStore cookieStore = ((java.net.CookieManager) httpClient.cookieHandler().get()).getCookieStore();
        System.out.println("Cookies before API request:");
        cookieStore.getCookies().forEach(cookie -> 
            System.out.println("  " + cookie.getName() + "=" + cookie.getValue()));

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Toggle like response status: " + response.statusCode());
        System.out.println("Toggle like response body: " + response.body());
        
        if (response.statusCode() == 200) {
            JsonNode responseJson = objectMapper.readTree(response.body());
            return responseJson.has("success") && responseJson.get("success").asBoolean();
        }
        
        return false;
    }

    private boolean addComment(String content) throws Exception {
        String url = baseUrl + "/api/comments";
        
        Map<String, Object> commentData = new HashMap<>();
        commentData.put("content", content);
        commentData.put("entityType", "WEBINAR");
        commentData.put("entityId", testWebinar.getId());
        
        String jsonBody = objectMapper.writeValueAsString(commentData);
        
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json");
        
        // Добавляем CSRF токен если он есть
        if (csrfToken != null) {
            requestBuilder.header("X-CSRF-TOKEN", csrfToken);
        }
        
        HttpRequest request = requestBuilder.build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Add comment response status: " + response.statusCode());
        System.out.println("Add comment response body: " + response.body());
        
        if (response.statusCode() == 200) {
            JsonNode responseJson = objectMapper.readTree(response.body());
            return responseJson.has("success") && responseJson.get("success").asBoolean();
        }
        
        return false;
    }
} 