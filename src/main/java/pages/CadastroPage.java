package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CadastroPage {

    private WebDriver driver;
    private WebDriverWait wait;

    public CadastroPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void preencherFormulario(String nome, String email, String idade) {
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

    public String obterTextoDoAlerta() {
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            var alert = driver.switchTo().alert();
            var texto = alert.getText();
            alert.accept();
            return texto;
        } catch (TimeoutException e) {
            return null;
        }
    }

}
