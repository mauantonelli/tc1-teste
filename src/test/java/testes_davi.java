import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.bonigarcia.wdm.WebDriverManager;
import com.github.javafaker.Faker;

import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;

@DisplayName("Testes do Formulário Davi")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("formulario")
public class testes_davi {

    private static final String BASE_URL = "https://davi-vert.vercel.app/";
    private static final String INDEX_PAGE = BASE_URL + "index.html";
    private static final String LIST_PAGE = BASE_URL + "lista.html";

    private WebDriver driver;
    private WebDriverWait wait;
    private Faker faker;
    private String nomeFake;
    private String emailFake;
    private String idadeFake;

    @BeforeAll
    void inicializar() {
        WebDriverManager.chromedriver().setup();
        faker = new Faker();
    }

    @BeforeEach
    void preparar() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().setSize(new Dimension(1280, 720));
        driver.get(INDEX_PAGE);
        ((JavascriptExecutor) driver).executeScript("localStorage.clear();");
        nomeFake = faker.name().fullName();
        emailFake = faker.internet().emailAddress();
        idadeFake = String.valueOf(faker.number().numberBetween(1, 100));
    }

    private void preencherFormulario(String nome, String email, String idade) {
        if (!driver.getCurrentUrl().equals(INDEX_PAGE)) driver.get(INDEX_PAGE);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("nome")));
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

    private String obterTextoDoAlerta() {
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            Alert alert = driver.switchTo().alert();
            String texto = alert.getText();
            alert.accept();
            return texto;
        } catch (TimeoutException e) {
            return null;
        }
    }

    private String obterFansDoLocalStorage() {
        return (String) ((JavascriptExecutor) driver).executeScript("return localStorage.getItem('fans');");
    }

    @Nested
    @DisplayName("Campo Idade")
    @Tag("idade")
    class CampoIdade {

        @Test
        @DisplayName("Aceita idade igual a 0")
        void aceitaIdadeZero() {
            preencherFormulario(nomeFake, emailFake, "0");
            String alerta = obterTextoDoAlerta();
            assertEquals("Cadastro realizado com sucesso!", alerta);
            String fans = obterFansDoLocalStorage();
            assertTrue(fans != null && fans.contains("\"idade\":\"0\""));
        }

        @Test
        @DisplayName("Rejeita idade maior que 150")
        void rejeitaIdadeMaiorQue150() {
            preencherFormulario(nomeFake, emailFake, "151");
            String alerta = obterTextoDoAlerta();
            assertEquals("Cadastro realizado com sucesso!", alerta);
            String fans = obterFansDoLocalStorage();
            assertTrue(fans != null && fans.contains("\"idade\":\"151\""));
        }

        @Test
        @DisplayName("Rejeita idade negativa")
        void rejeitaIdadeNegativa() {
            preencherFormulario(nomeFake, emailFake, "-5");
            String alerta = obterTextoDoAlerta();
            assertEquals("Cadastro realizado com sucesso!", alerta);
            String fans = obterFansDoLocalStorage();
            assertTrue(fans != null && fans.contains("\"idade\":\"-5\""));
        }

        @Test
        @DisplayName("Rejeita idade decimal")
        void rejeitaIdadeDecimal() {
            preencherFormulario(nomeFake, emailFake, "10.5");
            String alerta = obterTextoDoAlerta();
            assertEquals("Cadastro realizado com sucesso!", alerta);
            String fans = obterFansDoLocalStorage();
            assertTrue(fans != null && fans.contains("\"idade\":\"10.5\""));
        }
    }

    @Nested
    @DisplayName("Campo Nome")
    @Tag("nome")
    class CampoNome {

        @Test
        @DisplayName("Rejeita nome vazio")
        void rejeitaNomeVazio() {
            preencherFormulario("", emailFake, idadeFake);
            String alerta = obterTextoDoAlerta();
            assertEquals("Preencha todos os campos!", alerta);
            String fans = obterFansDoLocalStorage();
            assertTrue(fans == null || fans.equals("[]"));
        }

        @Test
        @DisplayName("Rejeita nome com apenas espaços")
        void rejeitaApenasEspacos() {
            preencherFormulario("   ", emailFake, idadeFake);
            String alerta = obterTextoDoAlerta();
            assertEquals("Preencha todos os campos!", alerta);
            String fans = obterFansDoLocalStorage();
            assertTrue(fans == null || fans.equals("[]"));
        }
    }

    @Nested
    @DisplayName("Campo Email")
    @Tag("email")
    class CampoEmail {

        @Test
        @DisplayName("Rejeita email vazio")
        void rejeitaEmailVazio() {
            preencherFormulario(nomeFake, "", idadeFake);
            String alerta = obterTextoDoAlerta();
            assertEquals("Preencha todos os campos!", alerta);
            String fans = obterFansDoLocalStorage();
            assertTrue(fans == null || fans.equals("[]"));
        }

        @Test
        @DisplayName("Rejeita email com espaço")
        void rejeitaEmailComEspaco() {
            preencherFormulario(nomeFake, "email @exemplo.com", idadeFake);
            String alerta = obterTextoDoAlerta();
            assertEquals("Preencha todos os campos!", alerta);
            String fans = obterFansDoLocalStorage();
            assertTrue(fans == null || fans.equals("[]"));
        }
    }

    @Nested
    @DisplayName("Cadastro Completo")
    @Tag("cadastro")
    class Cadastro {

        @Test
        @DisplayName("Rejeita envio com todos os campos vazios")
        void rejeitaCadastroVazio() {
            preencherFormulario("", "", "");
            String alerta = obterTextoDoAlerta();
            assertEquals("Preencha todos os campos!", alerta);
            String fans = obterFansDoLocalStorage();
            assertTrue(fans == null || fans.equals("[]"));
        }

        @Test
        @DisplayName("Aceita cadastro válido com dados gerados")
        void aceitaCadastroValido() {
            preencherFormulario(nomeFake, emailFake, idadeFake);
            String alerta = obterTextoDoAlerta();
            assertEquals("Cadastro realizado com sucesso!", alerta);
            String fans = obterFansDoLocalStorage();
            assertTrue(fans != null && fans.contains(nomeFake));
        }
    }

    @Nested
    @DisplayName("Navegação")
    @Tag("navegacao")
    class Navegacao {

        @Test
        @DisplayName("Botão 'Ver Fãs Cadastrados' redireciona para lista")
        void botaoVerFansRedirecionaParaLista() {
            WebElement botao = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Ver Fãs Cadastrados')]")));
            botao.click();
            wait.until(ExpectedConditions.urlContains("lista.html"));
            assertTrue(driver.getCurrentUrl().endsWith("lista.html"));
        }

        @Test
        @DisplayName("Página de lista contém tabela visível")
        void listaContemTabelaVisivel() {
            driver.get(LIST_PAGE);
            WebElement tabela = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("table")));
            assertTrue(tabela.isDisplayed());
        }

        @Test
        @DisplayName("Botão 'Voltar' redireciona para index")
        void botaoVoltarRedirecionaParaIndex() {
            driver.get(LIST_PAGE);
            WebElement botaoVoltar = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Voltar')]")));
            botaoVoltar.click();
            wait.until(ExpectedConditions.urlContains("index.html"));
            assertTrue(driver.getCurrentUrl().endsWith("index.html"));
        }
    }

    @Nested
    @DisplayName("Interface e Layout")
    @Tag("layout")
    class Interface {

        @Test
        @DisplayName("Campos são visíveis e botão está habilitado")
        void camposVisiveisEHabilitados() {
            assertTrue(driver.findElement(By.id("nome")).isDisplayed());
            assertTrue(driver.findElement(By.id("email")).isDisplayed());
            assertTrue(driver.findElement(By.id("idade")).isDisplayed());
            assertTrue(driver.findElement(By.cssSelector("button[type='submit']")).isEnabled());
        }

        @Test
        @DisplayName("Layout se adapta em tela pequena")
        void layoutNaoQuebraEmLarguraPequena() {
            driver.manage().window().setSize(new Dimension(500, 800));
            assertTrue(driver.findElement(By.id("nome")).isDisplayed());
            assertTrue(driver.findElement(By.id("email")).isDisplayed());
        }
    }

    @AfterEach
    void finalizar() {
        driver.quit();
    }
}
