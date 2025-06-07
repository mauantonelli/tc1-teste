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

    @Test
    void testIdadeZeroEhPermitida() {
        driver.get("https://davi-vert.vercel.app/index.html");

        WebElement nomeInput = driver.findElement(By.id("nome"));
        nomeInput.sendKeys("Teste");

        WebElement idadeInput = driver.findElement(By.id("idade"));
        idadeInput.sendKeys("0");

        WebElement enviarBtn = driver.findElement(By.tagName("button"));
        enviarBtn.click();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        WebElement resultado = driver.findElement(By.id("resultado"));
        String texto = resultado.getText();

        assertTrue(texto.contains("0") || texto.length() > 0);
    }

    @Test
    void testIdadeMaiorQue150NaoEhPermitida() {
        driver.get("https://davi-vert.vercel.app/index.html");

        WebElement nomeInput = driver.findElement(By.id("nome"));
        nomeInput.sendKeys("Velhíssimo");

        WebElement idadeInput = driver.findElement(By.id("idade"));
        idadeInput.sendKeys("151");

        WebElement enviarBtn = driver.findElement(By.tagName("button"));
        enviarBtn.click();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        WebElement resultado = driver.findElement(By.id("resultado"));
        String texto = resultado.getText();

        assertFalse(texto.contains("151"));
    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }
}
