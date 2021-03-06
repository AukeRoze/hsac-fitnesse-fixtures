package nl.hsac.fitnesse.fixture.util.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

import java.util.List;

/**
 * Adds a decorator on top of a decorator such that it is applied to all frames and iframes nested
 * inside the current page (or active iframe).
 * @param <T> type of condition result.
 */
class TryAllFramesConditionDecorator<T> implements ExpectedCondition<T> {
    private static final By BY_FRAME = By.cssSelector("iframe,frame");

    private final SeleniumHelper helper;
    private final ExpectedCondition<T> decorated;

    /**
     * Creates new, working inside the aHelper's current (i)frame.
     * @param toBeDecorated condition to be applied for each (i)frame.
     */
    public TryAllFramesConditionDecorator(SeleniumHelper aHelper, ExpectedCondition<T> toBeDecorated) {
        helper = aHelper;
        decorated = toBeDecorated;
    }

    @Override
    public T apply(WebDriver webDriver) {
        T result = decorated.apply(webDriver);
        if (!isFinished(result)) {
            result = invokeInFrames(webDriver);
        }
        return result;
    }

    private T invokeInFrames(WebDriver webDriver) {
        T result = null;
        List<WebElement> frames = webDriver.findElements(BY_FRAME);
        for (WebElement frame : frames) {
            helper.switchToFrame(frame);
            try {
                result = decorated.apply(webDriver);
                if (isFinished(result)) {
                    break;
                } else {
                    result = invokeInFrames(webDriver);
                    if (isFinished(result)) {
                        break;
                    }
                }
            } finally {
                helper.switchToParentFrame();
            }
        }
        return result;
    }

    private boolean isFinished(Object result) {
        return result != null && !Boolean.FALSE.equals(result);
    }
}
