import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.bonigarcia.wdm.WebDriverManager;
import com.github.javafaker.Faker;

import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;

@DisplayName("Testes de Validação e Navegação do Formulário Davi")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class testes_davi {

    private static final String BASE_URL = "https://davi-vert.vercel.app/";
    private static final String INDEX_PAGE = BASE_URL + "index.html";
    private static final String LIST_PAGE = BASE_URL + "lista.html";

    private WebDriver driver;
    private WebDriverWait wait;
    private Faker faker;

    @BeforeAll
    void beforeAll() {
        WebDriverManager.chromedriver().setup();
        faker = new Faker();
    }

    @BeforeEach
    void setUp() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().setSize(new Dimension(1280, 720));
        driver.get(INDEX_PAGE);
        ((JavascriptExecutor) driver).executeScript("localStorage.clear();");
    }

    private void preencherEEnviar(String nome, String email, String idade) {
        if (!driver.getCurrentUrl().equals(INDEX_PAGE)) {
            driver.get(INDEX_PAGE);
        }
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("nome")));
        WebElement inputNome = driver.findElement(By.id("nome"));
        WebElement inputEmail = driver.findElement(By.id("email"));
        WebElement inputIdade = driver.findElement(By.id("idade"));
        inputNome.clear();
        inputEmail.clear();
        inputIdade.clear();
        if (nome != null) inputNome.sendKeys(nome);
        if (email != null) inputEmail.sendKeys(email);
        if (idade != null) inputIdade.sendKeys(idade);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
    }

    private void aceitarAlert() {
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
        } catch (TimeoutException ignored) {}
    }

    private String getLocalStorageFans() {
        return (String) ((JavascriptExecutor) driver).executeScript("return localStorage.getItem('fans');");
    }

    @Nested
    @DisplayName("Validação do campo Idade")
    class ValidacaoIdade {

        @Test
        @DisplayName("Permite idade zero")
        void testIdadeZeroEhPermitida() {
            String nome = faker.name().firstName();
            String email = faker.internet().emailAddress();
            preencherEEnviar(nome, email, "0");
            aceitarAlert();
            String fans = getLocalStorageFans();
            assertTrue(fans != null && fans.contains("\"idade\":\"0\""));
        }

        @Test
        @DisplayName("Não permite idade maior que 150")
        void testIdadeMaiorQue150NaoEhPermitida() {
            String nome = faker.name().firstName();
            String email = faker.internet().emailAddress();
            preencherEEnviar(nome, email, "151");
            aceitarAlert();
            String fans = getLocalStorageFans();
            assertFalse(fans != null && fans.contains("\"idade\":\"151\""));
        }

        @Test
        @DisplayName("Não permite idade negativa")
        void testIdadeNegativaNaoEhPermitida() {
            String nome = faker.name().fullName();
            String email = faker.internet().emailAddress();
            preencherEEnviar(nome, email, "-1");
            aceitarAlert();
            String fans = getLocalStorageFans();
            assertFalse(fans != null && fans.contains("\"nome\":\"" + nome + "\""));
        }

        @Test
        @DisplayName("Não permite idade com letras")
        void testIdadeComLetrasNaoEhPermitida() {
            String nome = faker.name().fullName();
            String email = faker.internet().emailAddress();
            preencherEEnviar(nome, email, "abc");
            aceitarAlert();
            String fans = getLocalStorageFans();
            assertFalse(fans != null && fans.contains(email));
        }

        @Test
        @DisplayName("Não permite idade decimal")
        void testIdadeDecimalNaoEhPermitida() {
            String nome = faker.name().fullName();
            String email = faker.internet().emailAddress();
            preencherEEnviar(nome, email, "25.5");
            aceitarAlert();
            String fans = getLocalStorageFans();
            assertFalse(fans != null && fans.contains("\"nome\":\"" + nome + "\""));
        }
    }

    @Nested
    @DisplayName("Validação do campo Nome")
    class ValidacaoNome {

        @Test
        @DisplayName("Não permite nome vazio")
        void testNomeVazioNaoEhPermitido() {
            String email = faker.internet().emailAddress();
            preencherEEnviar("", email, "25");
            aceitarAlert();
            String fans = getLocalStorageFans();
            assertFalse(fans != null && fans.contains(email));
        }

        @Test
        @DisplayName("Não permite nome com apenas espaços")
        void testCamposComEspacosApenasNaoSaoPermitidos() {
            preencherEEnviar("   ", "   ", "   ");
            aceitarAlert();
            String fans = getLocalStorageFans();
            assertTrue(fans == null || fans.equals("[]"));
        }

        @Test
        @DisplayName("Não permite nome como número")
        void testNomeComoNumeroNaoEhPermitido() {
            String email = faker.internet().emailAddress();
            preencherEEnviar("12345", email, "25");
            aceitarAlert();
            String fans = getLocalStorageFans();
            assertFalse(fans != null && fans.contains("12345"));
        }
    }

    @Nested
    @DisplayName("Validação do campo Email")
    class ValidacaoEmail {

        @Test
        @DisplayName("Não permite email vazio")
        void testEmailVazioNaoEhPermitido() {
            String nome = faker.name().fullName();
            preencherEEnviar(nome, "", "30");
            aceitarAlert();
            String fans = getLocalStorageFans();
            assertFalse(fans != null && fans.contains("\"nome\":\"" + nome + "\""));
        }

        @Test
        @DisplayName("Não permite email inválido")
        void testEmailInvalidoNaoEhPermitido() {
            String nome = faker.name().fullName();
            preencherEEnviar(nome, "emailinvalido", "30");
            aceitarAlert();
            String fans = getLocalStorageFans();
            assertFalse(fans != null && fans.contains(nome));
        }

        @Test
        @DisplayName("Não permite email com espaço")
        void testEmailComEspacoNaoEhValido() {
            String nome = faker.name().firstName();
            preencherEEnviar(nome, "email @exemplo.com", "30");
            aceitarAlert();
            String fans = getLocalStorageFans();
            assertFalse(fans != null && fans.contains("\"nome\":\"" + nome + "\""));
        }
    }

    @Nested
    @DisplayName("Testes Gerais de Cadastro")
    class CadastroCompleto {

        @Test
        @DisplayName("Não permite cadastro sem preencher campos")
        void testCadastroSemPreencherCampos() {
            preencherEEnviar("", "", "");
            aceitarAlert();
            String fans = getLocalStorageFans();
            assertFalse(fans != null && fans.length() > 2);
        }

        @Test
        @DisplayName("Permite cadastro com dados fake válidos")
        void testCadastroComDadosFake() {
            String nomeFake = faker.name().fullName();
            String emailFake = faker.internet().emailAddress();
            String idadeFake = String.valueOf(faker.number().numberBetween(18, 100));
            preencherEEnviar(nomeFake, emailFake, idadeFake);
            aceitarAlert();
            String fans = getLocalStorageFans();
            assertTrue(fans != null && fans.contains(nomeFake));
        }
    }

    @Nested
    @DisplayName("Testes de Navegação")
    class Navegacao {

        @Test
        @DisplayName("Botão 'Ver Fãs Cadastrados' redireciona para lista")
        void testBotaoVerFansRedirecionaParaLista() {
            WebElement botao = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Ver Fãs Cadastrados')]")));
            botao.click();
            wait.until(ExpectedConditions.urlContains("lista.html"));
            assertTrue(driver.getCurrentUrl().endsWith("lista.html"));
        }

        @Test
        @DisplayName("Página de lista tem tabela visível")
        void testTabelaVisivelNaLista() {
            driver.get(LIST_PAGE);
            WebElement tabela = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table")));
            assertTrue(tabela.isDisplayed());
        }

        @Test
        @DisplayName("Botão 'Voltar' retorna para index")
        void testBotaoVoltarRedirecionaParaIndex() {
            driver.get(LIST_PAGE);
            WebElement botaoVoltar = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Voltar')]")));
            botaoVoltar.click();
            wait.until(ExpectedConditions.urlContains("index.html"));
            assertTrue(driver.getCurrentUrl().endsWith("index.html"));
        }
    }

    @Nested
    @DisplayName("Testes de Interface e Layout")
    class Interface {

        @Test
        @DisplayName("Campos estão visíveis e habilitados")
        void testCamposVisiveisEHabilitados() {
            assertTrue(driver.findElement(By.id("nome")).isDisplayed());
            assertTrue(driver.findElement(By.id("email")).isDisplayed());
            assertTrue(driver.findElement(By.id("idade")).isDisplayed());
            assertTrue(driver.findElement(By.cssSelector("button[type='submit']")).isEnabled());
        }

        @Test
        @DisplayName("Layout não quebra em 500px de largura")
        void testLayoutResponsivo() {
            driver.manage().window().setSize(new Dimension(500, 800));
            assertTrue(driver.findElement(By.id("nome")).isDisplayed());
            assertTrue(driver.findElement(By.id("email")).isDisplayed());
        }
    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }
}
