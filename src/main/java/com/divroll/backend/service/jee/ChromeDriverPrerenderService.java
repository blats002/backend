/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright (C) 2019  Kerby Martino
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses for this work are available. These replace the above
 * AGPL 3.0 and offer limited warranties, support, maintenance, and commercial
 * deployments.
 *
 * For more information, please email: support@divroll.com
 *
 */
package com.divroll.backend.service.jee;

import com.divroll.backend.service.PrerenderService;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ChromeDriverPrerenderService implements PrerenderService {

    final static Logger LOG = LoggerFactory.getLogger(ChromeDriverPrerenderService.class);
    final static Long DEFAULT_TIMEOUT = 10L;
    final static String DEFAULT_WINDOW_TIMEOUT = "10000";
    final static String HASH_BANG = "#!";

    String prerenderUrl;
    String escapedFragment;
    Long timeout = null;
    String windowTimeout = null;

    @Inject
    @Named("prerenderTimeout")
    protected String prerenderTimeout;

    @Inject
    @Named("prerenderWindowTimeout")
    protected String prerenderWindowTimeout;

    @Inject
    @Named("prerenderChromeDriverPath")
    protected String prerenderChromeDriverPath;

    @Override
    public String prerender(String url, String escapeFragment) {

        this.prerenderUrl = url;
        this.escapedFragment = escapeFragment;

        String prerenderTimeoutEnv = System.getenv("PRERENDER_TIMEOUT");
        String prerenderWindowTimeoutEnv = System.getenv("PRERENDER_WINDOW_TIMEOUT");
        String prerenderChromeDriverPathEnv = System.getenv("PRERENDER_CHROME_DRIVER_PATH");

        if(prerenderTimeoutEnv != null && !prerenderTimeoutEnv.isEmpty()) {
            prerenderTimeout = prerenderTimeoutEnv;
        }

        if(prerenderWindowTimeoutEnv != null && !prerenderWindowTimeoutEnv.isEmpty()) {
            prerenderWindowTimeout = prerenderWindowTimeoutEnv;
        }

        if(prerenderChromeDriverPathEnv != null && !prerenderChromeDriverPathEnv.isEmpty()) {
            prerenderChromeDriverPath = prerenderChromeDriverPathEnv;
        }

        if(escapedFragment != null) {
            prerenderUrl = prerenderUrl + HASH_BANG + escapedFragment;
        }

        timeout = Long.valueOf(prerenderTimeout);
        windowTimeout = prerenderWindowTimeout;

        LOG.info("Prerender request path: " + prerenderUrl);
        LOG.info("Prerender timeout (seconds): " + timeout);
        LOG.info("Prerender window timeout (ms): " + windowTimeout);
        if(prerenderUrl == null) {
            return null;
        }
        try {
            long startTime = System.currentTimeMillis();
            System.setProperty("webdriver.chrome.driver", prerenderChromeDriverPath);
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200","--ignore-certificate-errors");
            //options.addArguments("--window-size=1920,1200","--ignore-certificate-errors");

            WebDriver driver = new ChromeDriver(options);
            driver.get(prerenderUrl);

            boolean isPrerender = ((JavascriptExecutor) driver)
                    .executeScript("return (window.prerenderReady !== 'undefined' && window.prerenderReady === false)").equals(true);

            LOG.info("isPrerender = " + isPrerender);
            if(isPrerender) {
                ExpectedCondition<Boolean> expectation = new
                        ExpectedCondition<Boolean>() {
                            public Boolean apply(WebDriver driver) {
                                return ((JavascriptExecutor) driver).executeScript("return window.prerenderReady").equals(true);
                            }
                        };
                new WebDriverWait(driver, timeout)
                        .until(expectation);
            } else {
                new WebDriverWait(driver, timeout);
            }

            String pageSource = driver.getPageSource();

//            File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
//            FileUtils.copyFile(scrFile, new File("/home/temp/screenshot.png"));

            driver.quit();
            long endTime = System.currentTimeMillis();
            LOG.info("Driver took " + (endTime - startTime) + " milliseconds");
            return injectMetaTag(pageSource);
        } catch (Exception e) {
            LOG.error("Error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private String injectMetaTag(String html) {
        Document doc = Jsoup.parse(html);
        doc.head().append("<meta property=\"_escaped_fragment_\" content=\"" + escapedFragment + "\">");
        return doc.html();
    }

}
