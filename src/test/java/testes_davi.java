import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.bonigarcia.wdm.WebDriverManager;
import com.github.javafaker.Faker;
import pages.CadastroPage;
import pages.EditarDeletarPage;

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
    private CadastroPage cadastroPage;
    private EditarDeletarPage editarDeletarPage;

    @BeforeAll
    void setup() {
        WebDriverManager.chromedriver().setup();
        faker = new Faker();
    }

    @BeforeEach
    void initDriver() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().setSize(new Dimension(1280, 720));
        driver.get(INDEX_PAGE);
        ((JavascriptExecutor) driver).executeScript("localStorage.clear();");

        nomeFake = faker.name().fullName();
        emailFake = faker.internet().emailAddress();
        idadeFake = String.valueOf(faker.number().numberBetween(1, 100));

        cadastroPage = new CadastroPage(driver, wait);
        editarDeletarPage = new EditarDeletarPage(driver, wait);
    }

    private void validaAlertaEStorage(String mensagemEsperada, boolean deveConterDados) {
        String alerta = cadastroPage.obterTextoDoAlerta();
        assertEquals(mensagemEsperada, alerta);
        String fans = cadastroPage.obterFansDoLocalStorage();
        if (deveConterDados) {
            assertNotNull(fans);
            assertFalse(fans.isEmpty());
        } else {
            assertTrue(fans == null || fans.equals("[]"));
        }
    }

    @Nested
    @DisplayName("Campo Idade")
    @Tag("idade")
    class CampoIdade {

        @Test
        @DisplayName("Aceita idade zero")
        void idadeZeroAceita() {
            cadastroPage.preencherFormulario(nomeFake, emailFake, "0");
            validaAlertaEStorage("Cadastro realizado com sucesso!", true);
            assertTrue(cadastroPage.fansContemIdade("0"));
        }

        @Test
        @DisplayName("Aceita idade maior que 150")
        void idadeMaior150Aceita() {
            cadastroPage.preencherFormulario(nomeFake, emailFake, "151");
            validaAlertaEStorage("Cadastro realizado com sucesso!", true);
            assertTrue(cadastroPage.fansContemIdade("151"));
        }

        @Test
        @DisplayName("Aceita idade negativa")
        void idadeNegativaAceita() {
            cadastroPage.preencherFormulario(nomeFake, emailFake, "-5");
            validaAlertaEStorage("Cadastro realizado com sucesso!", true);
            assertTrue(cadastroPage.fansContemIdade("-5"));
        }

        @Test
        @DisplayName("Aceita idade decimal")
        void idadeDecimalAceita() {
            cadastroPage.preencherFormulario(nomeFake, emailFake, "10.5");
            validaAlertaEStorage("Cadastro realizado com sucesso!", true);
            assertTrue(cadastroPage.fansContemIdade("10.5"));
        }
    }

    @Nested
    @DisplayName("Campo Nome")
    @Tag("nome")
    class CampoNome {

        @Test
        @DisplayName("Rejeita nome vazio")
        void nomeVazioRejeitado() {
            cadastroPage.preencherFormulario("", emailFake, idadeFake);
            validaAlertaEStorage("Preencha todos os campos!", false);
        }

        @Test
        @DisplayName("Rejeita nome com espaços")
        void nomeApenasEspacosRejeitado() {
            cadastroPage.preencherFormulario("   ", emailFake, idadeFake);
            validaAlertaEStorage("Preencha todos os campos!", false);
        }
    }

    @Nested
    @DisplayName("Campo Email")
    @Tag("email")
    class CampoEmail {

        @Test
        @DisplayName("Rejeita email vazio")
        void emailVazioRejeitado() {
            cadastroPage.preencherFormulario(nomeFake, "", idadeFake);
            validaAlertaEStorage("Preencha todos os campos!", false);
        }

        @Test
        @DisplayName("Rejeita email com espaço")
        void emailComEspacoRejeitado() {
            cadastroPage.preencherFormulario(nomeFake, "email @exemplo.com", idadeFake);
            validaAlertaEStorage("Preencha todos os campos!", false);
        }
    }

    @Nested
    @DisplayName("Cadastro Completo")
    @Tag("cadastro")
    class Cadastro {

        @Test
        @DisplayName("Rejeita cadastro com campos vazios")
        void cadastroVazioRejeitado() {
            cadastroPage.preencherFormulario("", "", "");
            validaAlertaEStorage("Preencha todos os campos!", false);
        }

        @Test
        @DisplayName("Aceita cadastro válido")
        void cadastroValidoAceito() {
            cadastroPage.preencherFormulario(nomeFake, emailFake, idadeFake);
            validaAlertaEStorage("Cadastro realizado com sucesso!", true);
            String fans = cadastroPage.obterFansDoLocalStorage();
            assertTrue(fans.contains(nomeFake));
        }
    }

    @Nested
    @DisplayName("Navegação")
    @Tag("navegacao")
    class Navegacao {

        @Test
        @DisplayName("Botão 'Ver Fãs Cadastrados' leva para lista")
        void botaoVerFansRedireciona() {
            var botao = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Ver Fãs Cadastrados')]")));
            botao.click();
            wait.until(ExpectedConditions.urlContains("lista.html"));
            assertTrue(driver.getCurrentUrl().endsWith("lista.html"));
        }

        @Test
        @DisplayName("Lista mostra tabela visível")
        void listaExibeTabela() {
            driver.get(LIST_PAGE);
            var tabela = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("table")));
            assertTrue(tabela.isDisplayed());
        }

        @Test
        @DisplayName("Botão 'Voltar' leva para index")
        void botaoVoltarRedireciona() {
            driver.get(LIST_PAGE);
            var botaoVoltar = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Voltar')]")));
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
        @DisplayName("Campos visíveis e botão habilitado")
        void camposVisiveisBotaoHabilitado() {
            assertTrue(driver.findElement(By.id("nome")).isDisplayed());
            assertTrue(driver.findElement(By.id("email")).isDisplayed());
            assertTrue(driver.findElement(By.id("idade")).isDisplayed());
            assertTrue(driver.findElement(By.cssSelector("button[type='submit']")).isEnabled());
        }

        @Test
        @DisplayName("Layout funciona em tela pequena")
        void layoutResponsivo() {
            driver.manage().window().setSize(new Dimension(500, 800));
            assertTrue(driver.findElement(By.id("nome")).isDisplayed());
            assertTrue(driver.findElement(By.id("email")).isDisplayed());
        }
    }

    @Nested
    @DisplayName("Edição")
    @Tag("edicao")
    class Edicao {


        @Test
        @DisplayName("Edita fã alterando somente o nome")
        void editaSomenteNome() {
            cadastroPage.preencherFormulario(nomeFake, emailFake, idadeFake);
            validaAlertaEStorage("Cadastro realizado com sucesso!", true);

            driver.get(LIST_PAGE);

            ((JavascriptExecutor) driver).executeScript(
                    "let count = 0;" +
                            "window.prompt = function(msg, val) {" +
                            "  count++;" +
                            "  if(count === 1) return 'Nome Editado';" +
                            "  if(count === 2) return val;" +
                            "  if(count === 3) return val;" +
                            "  return val;" +
                            "};"
            );

            WebElement botaoEditar = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(text(),'Editar')]")
            ));
            botaoEditar.click();

            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();

            WebElement fanEditado = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//strong[contains(text(),'Nome Editado')]")
            ));

            assertTrue(fanEditado.isDisplayed());

            WebElement divPai = fanEditado.findElement(By.xpath("./.."));
            assertTrue(divPai.getText().contains(emailFake));
            assertTrue(divPai.getText().contains(idadeFake));
        }

        @Test
        @DisplayName("Edita fã adicionando caracteres especiais no nome")
        void editaNomeComCaracteresEspeciais() {
            cadastroPage.preencherFormulario(nomeFake, emailFake, idadeFake);
            validaAlertaEStorage("Cadastro realizado com sucesso!", true);
            driver.get(LIST_PAGE);

            String nomeEspecial = "@#$%!";
            ((JavascriptExecutor) driver).executeScript(
                    "let count=0; window.prompt=(msg,val)=>{count++; return count===1?'" + nomeEspecial + "':val;};"
            );
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Editar')]"))).click();
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();

            WebElement editado = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//strong[contains(text(),'" + nomeEspecial + "')]")
            ));
            assertTrue(editado.isDisplayed());
        }

        @Test
        @DisplayName("Edita múltiplos fãs sequencialmente")
        void editaMultiplosFans() {
            cadastroPage.preencherFormulario(nomeFake, emailFake, idadeFake);
            validaAlertaEStorage("Cadastro realizado com sucesso!", true);
            String nome2 = faker.name().fullName();
            String email2 = faker.internet().emailAddress();
            String idade2 = "55";
            cadastroPage.preencherFormulario(nome2, email2, idade2);
            validaAlertaEStorage("Cadastro realizado com sucesso!", true);
            driver.get(LIST_PAGE);

            ((JavascriptExecutor) driver).executeScript(
                    "let c=0; window.prompt=(msg,v)=>{ c++; if(c===1)return 'Primeiro'; if(c===2)return v; if(c===3)return v; return v;};"
            );
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//button[contains(text(),'Editar')])[1]"))).click();
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();

            ((JavascriptExecutor) driver).executeScript(
                    "window.prompt=(msg,v)=>{ if(msg.includes('nome')) return 'Segundo'; return v; };"
            );
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//button[contains(text(),'Editar')])[2]"))).click();
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();

            assertTrue(driver.findElement(By.xpath("//strong[contains(text(),'Primeiro')]")).isDisplayed());
            assertTrue(driver.findElement(By.xpath("//strong[contains(text(),'Segundo')]")).isDisplayed());
        }








    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }
}
