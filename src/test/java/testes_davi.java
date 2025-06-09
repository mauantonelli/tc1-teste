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

    @Nested
    @DisplayName("Validação do campo Idade")
    class ValidacaoIdade {

        @Test
        @DisplayName("Permite idade zero")
        void testIdadeZeroEhPermitida() {
            preencherEEnviar(faker.name().firstName(), faker.internet().emailAddress(), "0");
            aceitarAlert();
            String fans = getLocalStorageFans();
            assertTrue(fans != null && fans.contains("\"idade\":\"0\""));
        }

        @Test
        @DisplayName("Não permite idade maior que 150")
        void testIdadeMaiorQue150NaoEhPermitida() {
            preencherEEnviar(faker.name().firstName(), faker.internet().emailAddress(), "151");
            aceitarAlert();
            String fans = getLocalStorageFans();
            assertFalse(fans != null && fans.contains("\"idade\":\"151\""));
        }

        @Test
        @DisplayName("Não permite idade negativa")
        void testIdadeNegativaNaoEhPermitida() {
            preencherEEnviar(faker.name().fullName(), faker.internet().emailAddress(), "-1");
            aceitarAlert();
            String fans = getLocalStorageFans();
            assertTrue(fans == null || !fans.contains("\"nome\":\"" + faker.name().fullName() + "\""));
        }

        @Test
        @DisplayName("Não permite idade com letras")
        void testIdadeComLetrasNaoEhPermitida() {
            preencherEEnviar(faker.name().fullName(), faker.internet().emailAddress(), "abc");
            aceitarAlert();
            String fans = getLocalStorageFans();
            assertFalse(fans != null && fans.contains(faker.internet().emailAddress()));
        }

        @Test
        @DisplayName("Não permite idade decimal")
        void testIdadeDecimalNaoEhPermitida() {
            preencherEEnviar(faker.name().fullName(), faker.internet().emailAddress(), "25.5");
            aceitarAlert();
            String fans = getLocalStorageFans();
            assertFalse(fans != null && fans.contains("\"nome\":\"" + faker.name().fullName() + "\""));
        }
    }

    @Nested
    @DisplayName("Validação do campo Nome")
    class ValidacaoNome {

        @Test
        @DisplayName("Não permite nome vazio")
        void testNomeVazioNaoEhPermitido() {
            preencherEEnviar(null, faker.internet().emailAddress(), "25");
            aceitarAlert();
            String fans = getLocalStorageFans();
            assertFalse(fans != null && fans.contains(faker.internet().emailAddress()));
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
            preencherEEnviar("12345", faker.internet().emailAddress(), "25");
            aceitarAlert();
            String fans = getLocalStorageFans();
            assertTrue(fans == null || !fans.contains("12345"));
        }
    }

    @Nested
    @DisplayName("Validação do campo Email")
    class ValidacaoEmail {

        @Test
        @DisplayName("Não permite email vazio")
        void testEmailVazioNaoEhPermitido() {
            preencherEEnviar(faker.name().fullName(), null, "30");
            aceitarAlert();
            String fans = getLocalStorageFans();
            assertFalse(fans != null && fans.contains("\"nome\":\"" + faker.name().fullName() + "\""));
        }

        @Test
        @DisplayName("Não permite email inválido")
        void testEmailInvalidoNaoEhPermitido() {
            preencherEEnviar(faker.name().fullName(), "emailinvalido", "30");
            aceitarAlert();
            String fans = getLocalStorageFans();
            assertFalse(fans != null && fans.contains(faker.name().fullName()));
        }

        @Test
        @DisplayName("Não permite email com espaço")
        void testEmailComEspacoNaoEhValido() {
            preencherEEnviar(faker.name().firstName(), "email @exemplo.com", "30");
            aceitarAlert();
            String fans = getLocalStorageFans();
            assertFalse(fans != null && fans.contains("\"nome\":\"" + faker.name().firstName() + "\""));
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
            driver.get("https://davi-vert.vercel.app/lista.html");
            WebElement tabela = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table")));
            assertTrue(tabela.isDisplayed());
        }

        @Test
        @DisplayName("Botão 'Voltar' retorna para index")
        void testBotaoVoltarRedirecionaParaIndex() {
            driver.get("https://davi-vert.vercel.app/lista.html");
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
