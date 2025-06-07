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

        WebElement emailInput = driver.findElement(By.id("email"));
        emailInput.sendKeys("teste@email.com");

        WebElement idadeInput = driver.findElement(By.id("idade"));
        idadeInput.sendKeys("0");

        WebElement enviarBtn = driver.findElement(By.cssSelector("button[type='submit']"));
        enviarBtn.click();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String localStorageFans = (String) ((JavascriptExecutor) driver).executeScript("return localStorage.getItem('fans');");
        assertTrue(localStorageFans != null && (localStorageFans.contains("\"idade\":\"0\"") || localStorageFans.contains("\"idade\":0")));
    }

    @Test
    void testIdadeMaiorQue150NaoEhPermitida() {
        driver.get("https://davi-vert.vercel.app/index.html");

        WebElement nomeInput = driver.findElement(By.id("nome"));
        nomeInput.sendKeys("Velhíssimo");

        WebElement emailInput = driver.findElement(By.id("email"));
        emailInput.sendKeys("velho@email.com");

        WebElement idadeInput = driver.findElement(By.id("idade"));
        idadeInput.sendKeys("151");

        WebElement enviarBtn = driver.findElement(By.cssSelector("button[type='submit']"));
        enviarBtn.click();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String localStorageFans = (String) ((JavascriptExecutor) driver).executeScript("return localStorage.getItem('fans');");
        assertFalse(localStorageFans != null && (localStorageFans.contains("\"idade\":\"151\"") || localStorageFans.contains("\"idade\":151")));
    }

    @Test
    void testEmailVazioNaoEhPermitido() {
        driver.get("https://davi-vert.vercel.app/index.html");

        WebElement nomeInput = driver.findElement(By.id("nome"));
        nomeInput.sendKeys("Sem Email");

        WebElement idadeInput = driver.findElement(By.id("idade"));
        idadeInput.sendKeys("30");

        WebElement enviarBtn = driver.findElement(By.cssSelector("button[type='submit']"));
        enviarBtn.click();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String localStorageFans = (String) ((JavascriptExecutor) driver).executeScript("return localStorage.getItem('fans');");
        assertFalse(localStorageFans != null && localStorageFans.contains("Sem Email"));
    }

    @Test
    void testNomeVazioNaoEhPermitido() {
        driver.get("https://davi-vert.vercel.app/index.html");

        WebElement emailInput = driver.findElement(By.id("email"));
        emailInput.sendKeys("teste@email.com");

        WebElement idadeInput = driver.findElement(By.id("idade"));
        idadeInput.sendKeys("25");

        WebElement enviarBtn = driver.findElement(By.cssSelector("button[type='submit']"));
        enviarBtn.click();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String localStorageFans = (String) ((JavascriptExecutor) driver).executeScript("return localStorage.getItem('fans');");
        assertFalse(localStorageFans != null && localStorageFans.contains("teste@email.com"));
    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }
}
