import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.bonigarcia.wdm.WebDriverManager;
import com.github.javafaker.Faker;

import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;

public class testes_davi {

    private WebDriver driver;
    private WebDriverWait wait;
    private Faker faker = new Faker();

    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        driver.get("https://davi-vert.vercel.app/index.html");
        ((JavascriptExecutor) driver).executeScript("localStorage.clear();");
    }

    private void preencherEEnviar(String nome, String email, String idade) {
        driver.get("https://davi-vert.vercel.app/index.html");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("nome")));
        if (nome != null) driver.findElement(By.id("nome")).sendKeys(nome);
        if (email != null) driver.findElement(By.id("email")).sendKeys(email);
        if (idade != null) driver.findElement(By.id("idade")).sendKeys(idade);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
    }

    private void aceitarAlert() {
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
        } catch (TimeoutException ignored) {
        }
    }

    private String getLocalStorageFans() {
        return (String) ((JavascriptExecutor) driver).executeScript("return localStorage.getItem('fans');");
    }

    @Test
    void testIdadeZeroEhPermitida() {
        preencherEEnviar("Teste", "teste@email.com", "0");
        aceitarAlert();
        String fans = getLocalStorageFans();
        assertTrue(fans != null && fans.contains("\"idade\":\"0\""));
    }

    @Test
    void testIdadeMaiorQue150NaoEhPermitida() {
        preencherEEnviar("Velhíssimo", "velho@email.com", "151");
        aceitarAlert();
        String fans = getLocalStorageFans();
        assertFalse(fans != null && fans.contains("\"idade\":\"151\""));
    }

    @Test
    void testEmailVazioNaoEhPermitido() {
        preencherEEnviar("Sem Email", null, "30");
        aceitarAlert();
        String fans = getLocalStorageFans();
        assertFalse(fans != null && fans.contains("Sem Email"));
    }

    @Test
    void testNomeVazioNaoEhPermitido() {
        preencherEEnviar(null, "teste@email.com", "25");
        aceitarAlert();
        String fans = getLocalStorageFans();
        assertFalse(fans != null && fans.contains("teste@email.com"));
    }

    @Test
    void testEmailInvalidoNaoEhPermitido() {
        preencherEEnviar("Email Ruim", "emailinvalido", "30");
        aceitarAlert();
        String fans = getLocalStorageFans();
        assertFalse(fans != null && fans.contains("Email Ruim"));
    }

    @Test
    void testIdadeComLetrasNaoEhPermitida() {
        preencherEEnviar("Idade Letra", "letras@email.com", "abc");
        aceitarAlert();
        String fans = getLocalStorageFans();
        assertFalse(fans != null && fans.contains("letras@email.com"));
    }

    @Test
    void testIdadeDecimalNaoEhPermitida() {
        preencherEEnviar("Decimal", "decimal@exemplo.com", "25.5");
        aceitarAlert();
        String fans = getLocalStorageFans();
        assertFalse(fans != null && fans.contains("\"nome\":\"Decimal\""));
    }

    @Test
    void testEmailComEspacoNaoEhValido() {
        preencherEEnviar("EmailEspaco", "email @exemplo.com", "30");
        aceitarAlert();
        String fans = getLocalStorageFans();
        assertFalse(fans != null && fans.contains("\"nome\":\"EmailEspaco\""));
    }

    @Test
    void testCadastroSemPreencherCampos() {
        preencherEEnviar("", "", "");
        aceitarAlert();
        String fans = getLocalStorageFans();
        assertFalse(fans != null && fans.length() > 2);
    }

    @Test
    void testBotaoVerFansRedirecionaParaLista() {
        WebElement botao = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Ver Fãs Cadastrados')]")));
        botao.click();
        wait.until(ExpectedConditions.urlContains("lista.html"));
        assertTrue(driver.getCurrentUrl().endsWith("lista.html"));
    }

    @Test
    void testCamposComEspacosApenasNaoSaoPermitidos() {
        preencherEEnviar("   ", "   ", "   ");
        aceitarAlert();
        String fans = getLocalStorageFans();
        assertTrue(fans == null || fans.equals("[]"));
    }

    @Test
    void testIdadeNegativaNaoEhPermitida() {
        preencherEEnviar("Nome Negativo", "negativo@email.com", "-1");
        aceitarAlert();
        String fans = getLocalStorageFans();
        assertTrue(fans == null || !fans.contains("Nome Negativo"));
    }

    @Test
    void testNomeComoNumeroNaoEhPermitido() {
        preencherEEnviar("12345", "numero@email.com", "25");
        aceitarAlert();
        String fans = getLocalStorageFans();
        assertTrue(fans == null || !fans.contains("12345"));
    }

    @Test
    void testCadastroComDadosFake() {
        String nomeFake = faker.name().fullName();
        String emailFake = faker.internet().emailAddress();
        String idadeFake = String.valueOf(faker.number().numberBetween(18, 100));
        preencherEEnviar(nomeFake, emailFake, idadeFake);
        aceitarAlert();
        String fans = getLocalStorageFans();
        assertTrue(fans != null && fans.contains(nomeFake));
    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }

}
