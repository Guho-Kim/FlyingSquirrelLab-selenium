import java.io.*;
import java.time.Duration;
import java.util.Set;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class Main {
    public static void main(String[] args) {
        System.setProperty("webdriver.chrome.driver", "./chromedriver-win64/chromedriver.exe");
        String url = "https://everytime.kr/418760";

        WebDriver driver = new ChromeDriver();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt", true))) {

            try {
                driver.get("https://everytime.kr");

                // 쿠키가 있는 경우 동아리 페이지로 이동
                File cookieFile = new File("cookies.data");
                loadCookies(driver, cookieFile);
                driver.get("https://everytime.kr/418760");
                Thread.sleep(1000);

                // 쿠키가 없는 경우 로그인 페이지로 이동
                if (!driver.getCurrentUrl().equals("https://everytime.kr/418760")) {
                    // 로그인 페이지인 경우 로그인 처리
                    System.out.println("On the login page. Logging in...");
    
                    // 로그인 폼 자동화
                    WebElement idField = driver.findElement(By.name("id")); // 아이디 입력 필드 찾기
                    WebElement passwordField = driver.findElement(By.name("password")); // 비밀번호 입력 필드 찾기
                    idField.sendKeys("weeeeek2"); // 사용자 아이디 입력
                    passwordField.sendKeys("every3158!"); // 사용자 비밀번호 입력
    
                    // 로그인 폼 제출
                    WebElement submitButton = driver.findElement(By.cssSelector("input[type='submit']")); // 제출 버튼 찾기
                    submitButton.click(); // 제출 버튼 클릭
    
                    // 로그인 완료 후 쿠키 저장
                    Thread.sleep(1000);
                    driver.get("https://everytime.kr/418760");
                    saveCookies(driver, cookieFile);
                }

                System.out.println("Successfully opened the webpage.");
                Thread.sleep(1000);
                List<WebElement> articleLinks = driver.findElements(By.cssSelector("a.article"));

                // int cnt=10;
                for (WebElement articleLink : articleLinks) {
                    // if(cnt--==0) break;
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

                        String title = paragraphTitle.isEmpty() ? "No title found"
                                : paragraphTitle.get(0).getText().replaceAll("[^\\p{L}\\p{N}\\p{P}\\s]", "");

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

    // 쿠키 저장 메서드
    private static void saveCookies(WebDriver driver, File file) throws IOException {
        FileWriter fileWriter = new FileWriter(file);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        Set<Cookie> cookies = driver.manage().getCookies();
        for (Cookie cookie : cookies) {
            bufferedWriter.write(cookie.getName() + ";" + cookie.getValue() + ";" + cookie.getDomain() + ";"
                    + cookie.getPath() + ";" + cookie.getExpiry() + ";" + cookie.isSecure());
            bufferedWriter.newLine();
        }
        bufferedWriter.close();
    }

    // 쿠키 로드 메서드
    private static void loadCookies(WebDriver driver, File file) throws IOException {
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            String[] cookieDetails = line.split(";");
            Cookie cookie = new Cookie.Builder(cookieDetails[0], cookieDetails[1])
                    .domain(cookieDetails[2])
                    .path(cookieDetails[3])
                    .expiresOn(null) // 만료 날짜를 처리할 필요가 있음
                    .isSecure(Boolean.parseBoolean(cookieDetails[5]))
                    .build();
            driver.manage().addCookie(cookie);
        }
        bufferedReader.close();
    }
}
