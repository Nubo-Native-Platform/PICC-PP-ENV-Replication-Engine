/**
 * ArgoApiExceptionMessage.java
 *
 * @author AC
 * @date 06-May-2025
 */
package com.nnp.envrep.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * ArgoApiExceptionMessage.java
 *
 * @author AC
 * @date 06-May-2025
 */
@Getter
@Setter
public class ArgoApiExceptionMessage {
    private int code;
    private String error;
    private String message;
}
