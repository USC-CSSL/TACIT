package edu.usc.cssl.tacit.webview.ui;


/**
 * Marks a workbench part implementation as being a secondary part.
 * A secondary part is one that exists only to support the browser,
 * and should not be considered when determining whether to close
 * a window whose last browser has been closed.
 */
public interface ISecondaryPart {
    // marker interface only; no behaviour
}