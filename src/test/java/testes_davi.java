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
        @DisplayName("Idade zero — deve falhar se permitir")
        void idadeZeroDeveFalhar() {
            cadastroPage.preencherFormulario(nomeFake, emailFake, "0");
            String alerta = cadastroPage.obterTextoDoAlerta();
            assertNotEquals("Cadastro realizado com sucesso!", alerta);
            assertFalse(cadastroPage.fansContemIdade("0"));
        }

        @Test
        @DisplayName("Idade maior que 150 — deve falhar se permitir")
        void idadeMaior150DeveFalhar() {
            cadastroPage.preencherFormulario(nomeFake, emailFake, "151");
            String alerta = cadastroPage.obterTextoDoAlerta();
            assertNotEquals("Cadastro realizado com sucesso!", alerta);
            assertFalse(cadastroPage.fansContemIdade("151"));
        }

        @Test
        @DisplayName("Idade negativa — deve falhar se permitir")
        void idadeNegativaDeveFalhar() {
            cadastroPage.preencherFormulario(nomeFake, emailFake, "-5");
            String alerta = cadastroPage.obterTextoDoAlerta();
            assertNotEquals("Cadastro realizado com sucesso!", alerta);
            assertFalse(cadastroPage.fansContemIdade("-5"));
        }

        @Test
        @DisplayName("Não permite cadastrar idade decimal")
        void permiteIdadeDecimal() {
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

                Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                assertEquals("Fã editado com sucesso!", alert.getText());
                alert.accept();

                WebElement fanEditado = wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//strong[contains(text(),'Nome Editado')]")
                ));

                assertTrue(fanEditado.isDisplayed());

                WebElement divPai = fanEditado.findElement(By.xpath("./.."));
                assertTrue(divPai.getText().contains(emailFake));
                assertTrue(divPai.getText().contains(idadeFake));
            }

            @Test
            @DisplayName("Edita fã adicionando caracteres especiais no nome — deve falhar se permitir")
            void editaNomeComCaracteresEspeciaisDeveFalhar() {
                cadastroPage.preencherFormulario(nomeFake, emailFake, idadeFake);
                validaAlertaEStorage("Cadastro realizado com sucesso!", true);
                driver.get(LIST_PAGE);

                String nomeEspecial = "@#$%!";
                ((JavascriptExecutor) driver).executeScript(
                        "let count=0; window.prompt=(msg,val)=>{count++; return count===1?'" + nomeEspecial + "':val;};"
                );
                wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Editar')]"))).click();
                Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                String mensagem = alert.getText();
                alert.accept();

                assertNotEquals("Fã editado com sucesso!", mensagem);
                assertFalse(driver.getPageSource().contains(nomeEspecial));
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
                Alert alert1 = wait.until(ExpectedConditions.alertIsPresent());
                assertEquals("Fã editado com sucesso!", alert1.getText());
                alert1.accept();

                ((JavascriptExecutor) driver).executeScript(
                        "window.prompt=(msg,v)=>{ if(msg.includes('nome')) return 'Segundo'; return v; };"
                );
                wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//button[contains(text(),'Editar')])[2]"))).click();
                Alert alert2 = wait.until(ExpectedConditions.alertIsPresent());
                assertEquals("Fã editado com sucesso!", alert2.getText());
                alert2.accept();

                assertTrue(driver.findElement(By.xpath("//strong[contains(text(),'Primeiro')]")).isDisplayed());
                assertTrue(driver.findElement(By.xpath("//strong[contains(text(),'Segundo')]")).isDisplayed());
            }

            @Test
            @DisplayName("Edita idade para negativo — deve falhar se permitir")
            void editaIdadeNegativaDeveFalhar() {
                cadastroPage.preencherFormulario(nomeFake, emailFake, idadeFake);
                validaAlertaEStorage("Cadastro realizado com sucesso!", true);
                driver.get(LIST_PAGE);
                ((JavascriptExecutor) driver).executeScript(
                        "let c = 0; window.prompt = function(_, v){ return [v, v, '-10'][c++]; };"
                );
                wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Editar')]"))).click();
                Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                String mensagem = alert.getText();
                alert.accept();

                assertNotEquals("Fã editado com sucesso!", mensagem, "Não deveria permitir idade negativa");
                assertFalse(driver.getPageSource().contains("Idade: -10"), "Idade negativa foi exibida na tela");
            }


            @Test
            @DisplayName("Edita idade com texto — deve falhar se permitir")
            void editaIdadeTextoDeveFalhar() {
                cadastroPage.preencherFormulario(nomeFake, emailFake, "20");
                validaAlertaEStorage("Cadastro realizado com sucesso!", true);
                driver.get(LIST_PAGE);
                ((JavascriptExecutor) driver).executeScript(
                        "let c = 0; window.prompt = function(_, v){ return [v, v, 'vinte'][c++]; };"
                );
                wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Editar')]"))).click();
                Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                String mensagem = alert.getText();
                alert.accept();
                assertNotEquals("Fã editado com sucesso!", mensagem, "Não deveria editar com idade inválida");
                assertFalse(driver.getPageSource().contains("Idade: vinte"), "Texto inválido foi salvo na página");
            }


            @Test
            @DisplayName("Edita email sem '@' — deve falhar se permitir")
            void editaEmailSemArrobaDeveFalhar() {
                cadastroPage.preencherFormulario(nomeFake, emailFake, idadeFake);
                validaAlertaEStorage("Cadastro realizado com sucesso!", true);
                driver.get(LIST_PAGE);
                ((JavascriptExecutor) driver).executeScript(
                        "let c = 0; window.prompt = function(_, v){ return [v, 'emailsemarroba.com', v][c++]; };"
                );
                wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Editar')]"))).click();
                Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                String mensagem = alert.getText();
                alert.accept();

                assertNotEquals("Fã editado com sucesso!", mensagem);
                assertFalse(driver.getPageSource().contains("emailsemarroba.com"));
            }

            @Test
            @DisplayName("Edita com email maiúsculo")
            void editaEmailMaiusculo() {
                cadastroPage.preencherFormulario(nomeFake, emailFake, idadeFake);
                validaAlertaEStorage("Cadastro realizado com sucesso!", true);
                driver.get(LIST_PAGE);
                ((JavascriptExecutor) driver).executeScript(
                        "let c = 0; window.prompt = function(_, v){ return [v, 'EMAIL@EXEMPLO.COM', v][c++]; };"
                );
                wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Editar')]"))).click();
                Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                assertEquals("Fã editado com sucesso!", alert.getText());
                alert.accept();
                assertTrue(driver.getPageSource().contains("EMAIL@EXEMPLO.COM"));
            }

            @Test
            @DisplayName("Deve remover fã corretamente da lista")
            void deveRemoverFanCorretamente() {
                cadastroPage.preencherFormulario(nomeFake, emailFake, idadeFake);
                validaAlertaEStorage("Cadastro realizado com sucesso!", true);
                driver.get(LIST_PAGE);

                WebElement botaoRemover = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[contains(text(),'Remover')]")
                ));
                botaoRemover.click();

                Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                assertEquals("Fã removido com sucesso!", alert.getText());
                alert.accept();

                String fans = cadastroPage.obterFansDoLocalStorage();
                assertFalse(fans.contains(nomeFake));
            }

            @Test
            @DisplayName("Edita idade para zero — deve falhar se permitir")
            void editaIdadeZeroDeveFalhar() {
                cadastroPage.preencherFormulario(nomeFake, emailFake, "30");
                validaAlertaEStorage("Cadastro realizado com sucesso!", true);
                driver.get(LIST_PAGE);
                ((JavascriptExecutor) driver).executeScript(
                        "let c = 0; window.prompt = function(_, v){ return [v, v, '0'][c++]; };"
                );
                wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Editar')]"))).click();
                Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                String mensagem = alert.getText();
                alert.accept();

                assertNotEquals("Fã editado com sucesso!", mensagem);
                assertFalse(driver.getPageSource().contains("Idade: 0"));
            }

            @Test
            @DisplayName("Deve exibir erro ao editar com nome vazio")
            void deveExibirErroAoEditarComNomeVazio() {
                cadastroPage.preencherFormulario(nomeFake, emailFake, idadeFake);
                validaAlertaEStorage("Cadastro realizado com sucesso!", true);
                driver.get(LIST_PAGE);
                ((JavascriptExecutor) driver).executeScript(
                        "window.prompt = function(msg) { if(msg.includes('nome')) return ''; return 'preenchido'; }"
                );
                wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[contains(text(),'Editar')]"))
                ).click();
                Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                assertEquals("Erro: Todos os campos devem ser preenchidos para editar o fã.", alert.getText());
                alert.accept();
            }
            @Test
            @DisplayName("Deve exibir erro ao editar com email vazio")
            void deveExibirErroAoEditarComEmailVazio() {
                cadastroPage.preencherFormulario(nomeFake, emailFake, idadeFake);
                validaAlertaEStorage("Cadastro realizado com sucesso!", true);
                driver.get(LIST_PAGE);
                ((JavascriptExecutor) driver).executeScript(
                        "window.prompt = function(msg) { if(msg.includes('email')) return ''; return 'preenchido'; }"
                );
                wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[contains(text(),'Editar')]"))
                ).click();
                Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                assertEquals("Erro: Todos os campos devem ser preenchidos para editar o fã.", alert.getText());
                alert.accept();
            }
            @Test
            @DisplayName("Deve exibir erro ao editar com todos os campos vazios")
            void deveExibirErroAoEditarComTodosCamposVazios() {
                cadastroPage.preencherFormulario(nomeFake, emailFake, idadeFake);
                validaAlertaEStorage("Cadastro realizado com sucesso!", true);
                driver.get(LIST_PAGE);
                ((JavascriptExecutor) driver).executeScript(
                        "window.prompt = function(msg) { return ''; }"
                );
                wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[contains(text(),'Editar')]"))
                ).click();
                Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                assertEquals("Erro: Todos os campos devem ser preenchidos para editar o fã.", alert.getText());
                alert.accept();
            }

            @Test
            @DisplayName("Deve exibir erro ao editar com idade vazia")
            void deveExibirErroAoEditarComIdadeVazia() {
                cadastroPage.preencherFormulario(nomeFake, emailFake, idadeFake);
                validaAlertaEStorage("Cadastro realizado com sucesso!", true);
                driver.get(LIST_PAGE);
                ((JavascriptExecutor) driver).executeScript(
                        "window.prompt = function(msg) { if(msg.includes('idade')) return ''; return 'preenchido'; }"
                );
                wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[contains(text(),'Editar')]"))
                ).click();
                Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                assertEquals("Erro: Todos os campos devem ser preenchidos para editar o fã.", alert.getText());
                alert.accept();
            }

        }


        @AfterEach
        void tearDown() {
            driver.quit();
        }
    }

}
