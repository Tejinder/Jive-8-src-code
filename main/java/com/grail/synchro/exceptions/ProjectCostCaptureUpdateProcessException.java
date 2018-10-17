package com.grail.synchro.exceptions;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 7/7/14
 * Time: 12:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProjectCostCaptureUpdateProcessException extends Exception {
    public ProjectCostCaptureUpdateProcessException() {
    }

    public ProjectCostCaptureUpdateProcessException(String message) {
        super(message);
    }

    public ProjectCostCaptureUpdateProcessException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProjectCostCaptureUpdateProcessException(Throwable cause) {
        super(cause);
    }
}
