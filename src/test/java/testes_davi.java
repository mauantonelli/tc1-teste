import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import io.github.bonigarcia.wdm.WebDriverManager;

import static org.junit.jupiter.api.Assertions.*;

public class testes_davi {

    private WebDriver driver;

    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
    }

    private void preencherEEnviar(String nome, String email, String idade) {
        driver.get("https://davi-vert.vercel.app/index.html");
        if (nome != null) driver.findElement(By.id("nome")).sendKeys(nome);
        if (email != null) driver.findElement(By.id("email")).sendKeys(email);
        if (idade != null) driver.findElement(By.id("idade")).sendKeys(idade);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }
    }

    private String getLocalStorageFans() {
        return (String) ((JavascriptExecutor) driver).executeScript("return localStorage.getItem('fans');");
    }

    @Test
    void testIdadeZeroEhPermitida() {
        preencherEEnviar("Teste", "teste@email.com", "0");
        String fans = getLocalStorageFans();
        assertTrue(fans != null && (fans.contains("\"idade\":\"0\"") || fans.contains("\"idade\":0")));
    }

    @Test
    void testIdadeMaiorQue150NaoEhPermitida() {
        preencherEEnviar("Velhíssimo", "velho@email.com", "151");
        String fans = getLocalStorageFans();
        assertFalse(fans != null && (fans.contains("\"idade\":\"151\"") || fans.contains("\"idade\":151")));
    }

    @Test
    void testEmailVazioNaoEhPermitido() {
        preencherEEnviar("Sem Email", null, "30");
        String fans = getLocalStorageFans();
        assertFalse(fans != null && fans.contains("Sem Email"));
    }

    @Test
    void testNomeVazioNaoEhPermitido() {
        preencherEEnviar(null, "teste@email.com", "25");
        String fans = getLocalStorageFans();
        assertFalse(fans != null && fans.contains("teste@email.com"));
    }

    @Test
    void testEmailInvalidoNaoEhPermitido() {
        preencherEEnviar("Email Ruim", "emailinvalido", "30");
        String fans = getLocalStorageFans();
        assertFalse(fans != null && fans.contains("Email Ruim"));
    }

    @Test
    void testIdadeComLetrasNaoEhPermitida() {
        preencherEEnviar("Idade Letra", "letras@email.com", "abc");
        String fans = getLocalStorageFans();
        assertFalse(fans != null && fans.contains("letras@email.com"));
    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }
}
