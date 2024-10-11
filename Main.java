import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class Main {
    public static void main(String[] args) {
        System.setProperty("webdriver.chrome.driver", "./chromedriver-win64/chromedriver.exe");

        WebDriver driver = new ChromeDriver();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt", true))) {
            try {
                // everytime club board url
                String url = "https://everytime.kr/418760";
                driver.get(url);
                System.out.println("Successfully opened the webpage.");
                Thread.sleep(10000);

                List<WebElement> articleLinks = driver.findElements(By.cssSelector("a.article"));

                for (WebElement articleLink : articleLinks) {
                    String linkHref = articleLink.getAttribute("href");
                    System.out.println("Found article link: " + linkHref);
                    if (!linkHref.startsWith(url)) {
                        continue;
                    }

                    try {
                        String articleUrl = articleLink.getAttribute("href");
                        driver.get(articleUrl);
                        System.out.println("Navigated to article URL: " + articleUrl);
                        Thread.sleep(5000);

                        List<WebElement> paragraphTitle = driver.findElements(By.cssSelector("h2.large"));
                        List<WebElement> paragraphBody = driver.findElements(By.cssSelector("p.large"));

                        String title = paragraphTitle.isEmpty() ? "No title found" :
                                paragraphTitle.get(0).getText().replaceAll("[^\\p{L}\\p{N}\\p{P}\\s]", "");

                        StringBuilder body = new StringBuilder();
                        for (WebElement paragraph : paragraphBody) {
                            String filteredText = paragraph.getText().replaceAll("[^\\p{L}\\p{N}\\p{P}\\s]", "");
                            body.append(filteredText).append("\n");
                        }

                        writer.write("Title: " + title + "\n");
                        writer.write("Body:\n" + body + "\n");
                        writer.write("--------------------------------------------------\n");

                        driver.navigate().back();
                        Thread.sleep(3000);

                    } catch (Exception innerEx) {
                        System.out.println("Error while processing article: " + innerEx.getMessage());
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                driver.quit();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
