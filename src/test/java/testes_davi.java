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

    @Test
    void testEmailInvalidoNaoEhPermitido() {
        driver.get("https://davi-vert.vercel.app/index.html");

        driver.findElement(By.id("nome")).sendKeys("Email Ruim");
        driver.findElement(By.id("email")).sendKeys("emailinvalido");
        driver.findElement(By.id("idade")).sendKeys("30");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }

        String fans = (String) ((JavascriptExecutor) driver).executeScript("return localStorage.getItem('fans');");
        assertFalse(fans != null && fans.contains("Email Ruim"));
    }

    @Test
    void testIdadeComLetrasNaoEhPermitida() {
        driver.get("https://davi-vert.vercel.app/index.html");

        driver.findElement(By.id("nome")).sendKeys("Idade Letra");
        driver.findElement(By.id("email")).sendKeys("letras@email.com");
        driver.findElement(By.id("idade")).sendKeys("abc");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }

        String fans = (String) ((JavascriptExecutor) driver).executeScript("return localStorage.getItem('fans');");
        assertFalse(fans != null && fans.contains("letras@email.com"));
    }

}
